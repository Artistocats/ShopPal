package com.shoppalteam.shoppal.activities;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.shoppalteam.shoppal.R;
import com.shoppalteam.shoppal.contentprovider.ProductContentProvider;
import com.shoppalteam.shoppal.database.ProductTable;
import com.shoppalteam.shoppal.misc.Product;
import com.shoppalteam.shoppal.misc.Utils;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings({"deprecation","unchecked"})
public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //TAGs for debugging purposes
    private static final String ACTIVITY="MAIN";
    private static final String LOADER="LOADER";
    private static final String ADAPTER="ADAPTER";
    private static final String TIMERTASK="TIMERTASK";


    private ConcurrentHashMap<Integer,Product> productList; //Contains all the Products as retrieved from database

    private CustomCursorAdapter adapter;

    MyTimerTask myTask;
    Timer myTimer;

    private TextView totalBtn;
    private ImageButton cartBtn;

    private String totalBtnText;

    private static final String currencySymbol = Currency.getInstance(Locale.getDefault()).getSymbol();

    private int blackColor,grayColor;

    private static String deleteTitle, deleteText, yes,no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(ACTIVITY, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createToolbar();
        try {getSupportActionBar().setDisplayHomeAsUpEnabled(false);}catch (Exception e){}

        blackColor=getResources().getColor(R.color.black);
        grayColor=getResources().getColor(R.color.gray);

        /* Creates the list of Products or loads it if activity was recreated, so variables such
           as isSelected etc. will remain intact*/
        if((savedInstanceState!=null)&&(savedInstanceState.getSerializable("productList") != null))
                productList = new ConcurrentHashMap<>((Map<Integer, Product>) savedInstanceState.getSerializable("productList"));
        else
            productList = new ConcurrentHashMap<>();

        totalBtn = findViewById(R.id.total);
        cartBtn = findViewById(R.id.cartButton);
        totalBtnText=getResources().getString(R.string.totalField);

        //---ListView & Adapter initialization---
        ListView lv = findViewById(R.id.list);

        /* When an item from the list is clicked, application transits to the "EditProductActivity"
           , carrying the Uri of the corresponding product*/
        lv.setOnItemClickListener((adapter, v, position, id) -> {
            Intent i = new Intent(MainActivity.this, EditProductActivity.class);
            Uri pUri = Uri.parse(ProductContentProvider.CONTENT_URI + "/" + id);
            i.putExtra(ProductContentProvider.CONTENT_ITEM_TYPE, pUri);

            startActivity(i);
        });

        lv.setOnItemLongClickListener((parent, view, position, id) -> {
            Uri pUri = Uri.parse(ProductContentProvider.CONTENT_URI + "/" + id);
            AlertDialog diaBox = ConfirmDelete(pUri);
            diaBox.show();

            return true;
        });

        getLoaderManager().initLoader(0, null, this);   //Initializes the loader
        adapter = new CustomCursorAdapter(this,null,0); //Creates adapter
        lv.setAdapter(adapter);                         //Connects the adapter with our ListView

        deleteTitle=getResources().getString(R.string.delete_title);
        deleteText=getResources().getString(R.string.delete_text);
        yes        =getResources().getString(R.string.yes);
        no=getResources().getString(R.string.no);
    }

    @Override
    public void onResume(){
        Log.d(ACTIVITY, "onResume");
        super.onResume();
        myTask = new MyTimerTask();
        myTimer = new Timer();
        myTimer.schedule(myTask, 0, 2000); //interval to update product colors (ms)
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(ACTIVITY, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(ACTIVITY, "onPause");
        myTask.cancel();
        myTimer.cancel();
        super.onPause();
    }


    /* Saves the list of Products  so variables such
       as isSelected etc. will remain intact upon recreation of Activity*/
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(ACTIVITY, "onSaveInstanceState");
        savedInstanceState.putSerializable("productList", productList);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onDestroy() {
        Log.d(ACTIVITY, "onDestroy");
        getLoaderManager().destroyLoader(0);    //Loader that created in onCreate is destroyed here
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks
        int id = item.getItemId();

        if(id == R.id.action_new) {   //Starts "EditProductActivity" for a new product
            Intent myIntent = new Intent(this, EditProductActivity.class);
            startActivity(myIntent);
        }
        else if (id == R.id.action_statistics) {    //Starts "StatisticsActivity"
            Intent myIntent = new Intent(this, StatisticsActivity.class);
            startActivity(myIntent);
        }
        else if (id == R.id.action_settings) {  //Starts "SettingsActivity"
            Intent myIntent = new Intent(this, SettingsActivity.class);
            startActivity(myIntent);
        }
        else if(id == R.id.action_about) {  //Starts "AboutActivity"
            Intent myIntent = new Intent(this, AboutActivity.class);
            startActivity(myIntent);
        }

        return super.onOptionsItemSelected(item);
    }


//----------------------------------------------LOADER----------------------------------------------

    /* Executed when all data is retrieved from database. Current productList is renewed, so persistent
       variables such as isSelected have to be copied to the new list*/
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Thread.currentThread().setName("Loader");
        Log.d(LOADER, "onLoadFinished");

        //Sets all products of the current list to be deleted. Every new product that is added
        //afterwards will automatically unset this variable upon creation
        Set<Integer> keys = productList.keySet();
        for(Integer key: keys)
            productList.get(key).setToBeDeleted(true);

        while (data.moveToNext()) {
            int id = data.getInt(data.getColumnIndex(ProductTable.ID));
            String name = data.getString(data.getColumnIndex(ProductTable.NAME));
            double price = data.getDouble(data.getColumnIndex(ProductTable.PRICE));
            int quantity = data.getInt(data.getColumnIndex(ProductTable.QUANTITY));
            String lastBuy = data.getString(data.getColumnIndex(ProductTable.LAST_BUY));
            double buyPeriod = data.getDouble(data.getColumnIndex(ProductTable.BUY_PERIOD));
            int timesBought=data.getInt(data.getColumnIndex(ProductTable.TIMES_BOUGHT));
            Product previous = productList.put(id, (new Product(id, name, price, quantity,lastBuy,buyPeriod,timesBought)));
            if (previous != null)
                productList.get(id).setSelected(previous.isSelected());
        }

        //Delete products that no longer exist in database, else calculate depletionDateTimeMin
        Iterator<ConcurrentHashMap.Entry<Integer,Product>> it = productList.entrySet().iterator();
        DateTime depletionDateTimeMin = null;
        while (it.hasNext()) {
            ConcurrentHashMap.Entry<Integer,Product> entry = it.next();
            if(entry.getValue().isToBeDeleted())
                it.remove();
            else
            {
                DateTime tempDateTime = entry.getValue().getDepletionDate();

                if (tempDateTime !=null)
                {
                    if (depletionDateTimeMin==null)
                        depletionDateTimeMin= tempDateTime;
                    else if (tempDateTime.isBefore(depletionDateTimeMin))
                        depletionDateTimeMin= tempDateTime;
                }


            }
        }

        Log.d("NOTIFICATIONS", "Next Depletion: " + Utils.dateTimeToString(depletionDateTimeMin));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("depletionDateTime",Utils.dateTimeToString(depletionDateTimeMin));
        editor.apply();

        Log.d(LOADER, "productList updated");

        updateUI();
        adapter.swapCursor(data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOADER, "onCreateLoader");
        String[] projection = { ProductTable.ID, ProductTable.NAME, ProductTable.PRICE, ProductTable.QUANTITY,
                                ProductTable.LAST_BUY, ProductTable.BUY_PERIOD, ProductTable.TIMES_BOUGHT};

        return new CursorLoader(this, ProductContentProvider.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOADER, "onLoaderReset");
        adapter.swapCursor(null);
    }

//---------------------------------------------LOADER ENDS------------------------------------------


//---------------------------------------------ADAPTER----------------------------------------------
    private class CustomCursorAdapter extends CursorAdapter {
        ProductViewHolder viewHolder;
        private LayoutInflater mInflater;

        CustomCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            Log.d(ADAPTER, "New View (position " + cursor.getPosition() + ")");

            View convertView=mInflater.inflate(R.layout.product_row, parent, false);

            viewHolder = new ProductViewHolder();

            viewHolder.productRow = convertView;
            viewHolder.cb = convertView.findViewById(R.id.checkBox);
            viewHolder.nameTV = convertView.findViewById(R.id.label);
            viewHolder.priceTV = convertView.findViewById(R.id.label2);

            convertView.setTag(viewHolder);

            return convertView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            viewHolder = (ProductViewHolder) view.getTag();
            viewHolder.productId=cursor.getInt(cursor.getColumnIndex(ProductTable.ID));

            Log.d(ADAPTER, "Binding productView with ID " + viewHolder.productId + " at position "
                    + cursor.getPosition());

            Product p=productList.get(viewHolder.productId);

            if(p!=null)
            {
                if(p.getQuantity()==1)
                    viewHolder.nameTV.setText(p.getName());
                else
                    viewHolder.nameTV.setText(p.getName() + " (x" + p.getQuantity() + ")");

                viewHolder.priceTV.setText(p.getSubtotal().toPlainString() + currencySymbol);

                viewHolder.cb.setChecked(p.isSelected());

                switch (p.getColor())
                {
                    case 0:
                        viewHolder.productRow.setBackgroundResource(R.drawable.product_selector_0);
                        break;
                    case 1:
                        viewHolder.productRow.setBackgroundResource(R.drawable.product_selector_1);
                        break;
                    case 2:
                        viewHolder.productRow.setBackgroundResource(R.drawable.product_selector_2);
                        break;
                    case 3:
                        viewHolder.productRow.setBackgroundResource(R.drawable.product_selector_3);
                        break;
                    case 4:
                        viewHolder.productRow.setBackgroundResource(R.drawable.product_selector_4);
                        break;
                }
            }
            else
                Log.w(ADAPTER,"Null product in bindView!");
        }
    }

    /* The ViewHolder pattern is used to boost performance. Basically it bypasses the repeated use
       of findViewById(). For more information:
        http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    */
    static class ProductViewHolder {
        View productRow;
        CheckBox cb;
        TextView nameTV; //Name (xQuantity)
        TextView priceTV;

        int productId;
    }

//-------------------------------------------ADAPTER ENDS-------------------------------------------

    public void check (View view) {
        CheckBox cb = (CheckBox) view;
        View parentView=(View)cb.getParent();

        ProductViewHolder viewHolder = (ProductViewHolder) parentView.getTag();
        productList.get(viewHolder.productId).setSelected(cb.isChecked());


        updateUI();
    }

    public void buy(View view) {
        ContentValues values = new ContentValues();

        String now;
        Product p;
        Set<Integer> keys = productList.keySet();
        for (Integer key : keys) {
            p = productList.get(key);
            if (p.isSelected()) {
                now = Utils.getNow();

                if (p.getTimesBought() > 0)
                {
                    Duration duration = Utils.getDuration(p.getLastBuy(),now);

                    BigDecimal durationBD = BigDecimal.valueOf(duration.getStandardSeconds()).divide(Utils.SECONDS_PER_HOUR, Utils.DIGITS_PRECISION, RoundingMode.HALF_EVEN);
                    durationBD = durationBD.divide(new BigDecimal(p.getQuantity()),Utils.DIGITS_PRECISION, RoundingMode.HALF_EVEN);

                    if (p.getTimesBought() == 1)
                        values.put(ProductTable.BUY_PERIOD, durationBD.toPlainString());
                    else {
                        BigDecimal bd2 = durationBD.divide(new BigDecimal(p.getTimesBought()), Utils.DIGITS_PRECISION,RoundingMode.HALF_EVEN);
                        BigDecimal bd1 = new BigDecimal(p.getBuyPeriod());
                        bd1 = bd1.multiply(new BigDecimal(p.getTimesBought()-1));
                        bd1 = bd1.divide(new BigDecimal(p.getTimesBought()),Utils.DIGITS_PRECISION, RoundingMode.HALF_EVEN);
                        BigDecimal total = bd1.add(bd2).setScale(Utils.DIGITS_PRECISION/2,RoundingMode.HALF_EVEN);

                        values.put(ProductTable.BUY_PERIOD, total.toPlainString());
                    }

                }
                values.put(ProductTable.LAST_BUY, now);
                values.put(ProductTable.TIMES_BOUGHT, p.getTimesBought() + 1);

                p.setSelected(false);
                getContentResolver().update(Uri.parse(ProductContentProvider.CONTENT_URI + "/" + p.getId()), values, null, null);
            }

        }
    }

    private void updateUI() {
        //----Update Cart & Total
        Set<Integer> keys = productList.keySet();
        boolean cartEnable=false;

        BigDecimal total=new BigDecimal(0);

        for(Integer key: keys) {
            if(productList.get(key).isSelected()) {
                total=total.add(productList.get(key).getSubtotal());
                cartEnable=true;
            }

        }

        total=total.setScale(2, RoundingMode.CEILING);
        if(cartEnable)
            totalBtn.setTextColor(blackColor);
        else
            totalBtn.setTextColor(grayColor);

        totalBtn.setText(totalBtnText + " " + total.toPlainString() + currencySymbol);

        Log.d(ACTIVITY, "UI updated");
        cartBtn.setEnabled(cartEnable);
    }


    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                boolean flag = false;
                Set<Integer> keys = productList.keySet();

                //Generate colors and ask UI to update if needed
                for (Integer key : keys) {
                    if (productList.get(key).generateColor())
                        flag = true;
                }

                //At least one color changed, so update UI
                if (flag)
                    runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                Log.w(TIMERTASK, "Exception at TimerTask", e);
            }
        }
    }

    private AlertDialog ConfirmDelete(Uri productUri) {
        final Uri pUri = productUri;
        return(new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(deleteTitle)
                .setMessage(deleteText)

                .setPositiveButton(yes, (dialog, whichButton) -> {
                    getContentResolver().delete(pUri, null, null);
                    dialog.dismiss();
                    adapter.notifyDataSetChanged();
                })

                .setNegativeButton(no, (dialog, which) -> dialog.dismiss())
                .create());
    }
}
