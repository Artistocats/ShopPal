package com.shoppalteam.shoppal.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.shoppalteam.shoppal.R;
import com.shoppalteam.shoppal.contentprovider.ProductContentProvider;
import com.shoppalteam.shoppal.database.ProductTable;
import com.shoppalteam.shoppal.misc.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class StatisticsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //TAGs for debugging purposes
    private static final String ACTIVITY="STATISTICS";
    private static final String LOADER="LOADER";

    private CustomCursorAdapter adapter;

    ArrayList<PieEntry> entries;
    PieChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(ACTIVITY, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        createToolbar();
        entries = new ArrayList<>();
        chart = findViewById(R.id.chart);
        getLoaderManager().initLoader(0, null, this);   //Initializes the loader
        adapter = new CustomCursorAdapter(this,null,0); //Creates adapter
    }

    @Override
    public void onDestroy() {
        Log.d(ACTIVITY, "onDestroy");
        getLoaderManager().destroyLoader(0);    //Loader that created in onCreate is destroyed here
        super.onDestroy();
    }


    //----------------------------------------------LOADER----------------------------------------------

    /* Executed when all data is retrieved from database. Current productList is renewed, so persistent
       variables such as isSelected have to be copied to the new list*/
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(LOADER, "onLoadFinished");
        PieEntry tempEntry;
        float temp;
        int total=0,count=cursor.getCount();
        if(count!=0) {
            float sum=0;

            while (cursor.moveToNext()) {
                BigDecimal bP= new BigDecimal(cursor.getDouble(cursor.getColumnIndex(ProductTable.BUY_PERIOD)));
                if(bP.doubleValue()!=0)
                {
                    bP = bP.divide(Utils.HOURS_PER_DAY, 10, RoundingMode.HALF_EVEN);
                    bP = BigDecimal.valueOf(1).divide(bP, 10, RoundingMode.HALF_EVEN);

                    tempEntry=new PieEntry(bP.floatValue(),cursor.getPosition());
                    tempEntry.setLabel(cursor.getString(cursor.getColumnIndex(ProductTable.NAME)));

                    entries.add(tempEntry);


                    sum+=bP.floatValue();
                    total++;
                }
            }

            if(total>0) {
                for (int i = 0; i < total; i++) {
                    temp=entries.get(i).getY();
                    entries.get(i).setY((temp*100)/sum);
                }

                PieDataSet dataSet = new PieDataSet(entries, "Buy Frequencies Comparison");


                ArrayList<Integer> colors = new ArrayList<>();

                for (int c : ColorTemplate.COLORFUL_COLORS)
                    colors.add(c);

                for (int c : ColorTemplate.LIBERTY_COLORS)
                    colors.add(c);

                for (int c : ColorTemplate.PASTEL_COLORS)
                    colors.add(c);

                for (int c : ColorTemplate.VORDIPLOM_COLORS)
                    colors.add(c);

                for (int c : ColorTemplate.JOYFUL_COLORS)
                    colors.add(c);

                colors.add(ColorTemplate.getHoloBlue());

                dataSet.setColors(colors);

                PieData data = new PieData(dataSet);
                data.setValueFormatter(new PercentFormatter());
                data.setValueTextSize(11f);
                data.setValueTextColor(Color.BLACK);

                chart.setData(data);
                chart.setDescription(null);

                chart.getLegend().setEnabled(false);

                chart.invalidate();
                chart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
            }
        }
        adapter.swapCursor(cursor);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOADER, "onCreateLoader");
        String[] projection = {ProductTable.ID,ProductTable.NAME, ProductTable.BUY_PERIOD};
        return new CursorLoader(this, ProductContentProvider.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOADER, "onLoaderReset");
        adapter.swapCursor(null);
    }

//---------------------------------------------LOADER ENDS------------------------------------------


//---------------------------------------------ADAPTER----------------------------------------------
    private class CustomCursorAdapter extends CursorAdapter
    {

        CustomCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent){
            return null;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

        }
    }

//-------------------------------------------ADAPTER ENDS-------------------------------------------
}
