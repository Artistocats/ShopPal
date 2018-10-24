package com.shoppalteam.shoppal.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.shoppalteam.shoppal.R;
import com.shoppalteam.shoppal.contentprovider.ProductContentProvider;
import com.shoppalteam.shoppal.database.ProductTable;
import com.shoppalteam.shoppal.misc.CurrencyFilter;

import java.math.BigDecimal;
import java.math.RoundingMode;


@SuppressWarnings("deprecation")
public class EditProductActivity extends BaseActivity {
    private Uri productUri;

    private EditText name, price,quantity,notes;
    private Bundle extras;

    private static String deleteTitle, deleteText, yes,no, pleaseEnterName;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_edit_product);
        createToolbar();

        deleteTitle=getResources().getString(R.string.delete_title);
        deleteText=getResources().getString(R.string.delete_text);
        yes        =getResources().getString(R.string.yes);
        no=getResources().getString(R.string.no);
        pleaseEnterName = getResources().getString(R.string.please_enter_name);

        name = findViewById(R.id.name);
        price = findViewById(R.id.price);
        price.setFilters(new InputFilter[]{new CurrencyFilter(10, 2)});
        quantity = findViewById(R.id.quantity);
        Button plus = findViewById(R.id.plus);
        Button minus = findViewById(R.id.minus);
        notes = findViewById(R.id.notes);

        plus.setOnClickListener(v -> {
            if(quantity.getText().length()>0) {
                int quant = Integer.parseInt(quantity.getText().toString());
                if(quant<9999)
                {
                    quantity.setText(String.valueOf(quant+1));
                    quantity.setSelection(quantity.getText().length());
                }

            }
            else {
                quantity.setText("1");
                quantity.setSelection(1);
            }
        });

        minus.setOnClickListener(v -> {
            if(quantity.getText().length()>0) {
                if (Integer.parseInt(quantity.getText().toString()) > 1) {
                    int quant;
                    quant = Integer.parseInt(quantity.getText().toString());
                    if (quant > 1) {
                        quantity.setText(String.valueOf(quant-1));
                        quantity.setSelection(quantity.getText().length());
                    }
                }
            }
            else {
                quantity.setText("1");
                quantity.setSelection(1);
            }
        });

        extras = getIntent().getExtras();

        // check from the saved Instance
        productUri = (bundle == null) ? null : (Uri) bundle.getParcelable(ProductContentProvider.CONTENT_ITEM_TYPE);


        // Or passed from the other activity
        if (extras != null)
        {
            productUri = extras.getParcelable(ProductContentProvider.CONTENT_ITEM_TYPE);
            fillData(productUri);
        }
        else
            name.requestFocus();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (extras == null)
            getMenuInflater().inflate(R.menu.menu_add_product, menu);
        else
            getMenuInflater().inflate(R.menu.menu_edit_product, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id==android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void fillData(Uri uri) {
        String[] projection = {ProductTable.ID,ProductTable.NAME,
                ProductTable.PRICE, ProductTable.QUANTITY,ProductTable.NOTES};
        Cursor cursor = getContentResolver().query(uri, projection, null, null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
            name.setText(cursor.getString(cursor.getColumnIndexOrThrow(ProductTable.NAME)));
            price.setText(BigDecimal.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(ProductTable.PRICE))).toPlainString());
            quantity.setText((cursor.getString(cursor.getColumnIndexOrThrow(ProductTable.QUANTITY))));
            notes.setText(cursor.getString(cursor.getColumnIndexOrThrow(ProductTable.NOTES)));
            cursor.close();
        }

    }

    private void saveState() {
        String productName = name.getText().toString();
        if (productName.isEmpty()) {
            Toast.makeText(EditProductActivity.this, pleaseEnterName,Toast.LENGTH_SHORT).show();
            return;
        }

        double productPrice=0;
        String pPrice = price.getText().toString();
        if(!pPrice.isEmpty()&&!pPrice.equals(".")) {
            BigDecimal productPriceBD=BigDecimal.valueOf(Double.parseDouble(pPrice));
            productPriceBD=productPriceBD.setScale(2, RoundingMode.CEILING);
            productPrice = productPriceBD.doubleValue();
        }

        int productQuantity = 1;
        String pQuantity = quantity.getText().toString();
        if(!pQuantity.isEmpty()&&!pQuantity.equals("0"))
            productQuantity=Integer.parseInt(pQuantity);

        String productNotes = notes.getText().toString();

        ContentValues values = new ContentValues();
        values.put(ProductTable.NAME, productName);
        values.put(ProductTable.PRICE, productPrice);
        values.put(ProductTable.QUANTITY, productQuantity);
        values.put(ProductTable.NOTES, productNotes);

        if (productUri == null)
        {
            values.put(ProductTable.LAST_BUY,0);
            values.put(ProductTable.BUY_PERIOD,0);
            values.put(ProductTable.TIMES_BOUGHT,0);
            productUri = getContentResolver().insert(ProductContentProvider.CONTENT_URI, values);
        }
        else
            getContentResolver().update(productUri, values, null, null);
        finish();
    }

    public void saveState(View v) {
        saveState();
    }

    public void deleteProduct(MenuItem item) {
        AlertDialog diaBox = ConfirmDelete();
        diaBox.show();
    }

    private AlertDialog ConfirmDelete() {
        return(new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(deleteTitle)
                .setMessage(deleteText)

                .setPositiveButton(yes, (dialog, whichButton) -> {
                    getContentResolver().delete(productUri, null, null);
                    dialog.dismiss();
                    finish();
                })

                .setNegativeButton(no, (dialog, which) -> dialog.dismiss())
                .create());
    }
}
