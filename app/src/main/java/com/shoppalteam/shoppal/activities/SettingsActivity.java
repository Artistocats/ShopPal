package com.shoppalteam.shoppal.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.TwoStatePreference;
import android.support.v7.app.AlertDialog;

import com.shoppalteam.shoppal.R;
import com.shoppalteam.shoppal.contentprovider.ProductContentProvider;
import com.shoppalteam.shoppal.database.ProductTable;
import com.shoppalteam.shoppal.misc.Utils;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.math.BigDecimal;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">SettingsActivity
 * API Guide</a> for more information on developing a SettingsActivity UI.
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends BaseActivity {

    private static String factoryResetTitle,factoryResetText,sltResetTitle,sltResetText,yes,no;
    private static final String DEFAULT_FREQ = "86400000";   //Equivalent to 1 day

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        factoryResetTitle=getResources().getString(R.string.factory_reset);
        factoryResetText=getResources().getString(R.string.factory_reset__text);
        sltResetTitle=getResources().getString(R.string.slt_reset);
        sltResetText=getResources().getString(R.string.slt_reset__text);
        yes=getResources().getString(R.string.yes);
        no=getResources().getString(R.string.no);

        setContentView(R.layout.activity_settings);
        createToolbar();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_empty);

            //Notification Preferences

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            SharedPreferences.Editor editor;

            PreferenceCategory header = new PreferenceCategory(this.getActivity());
            header.setTitle(R.string.pref_header_notifications);
            getPreferenceScreen().addPreference(header);
            addPreferencesFromResource(R.xml.pref_notifications);

            //Notifications Enable
            TwoStatePreference notiPref = (TwoStatePreference) findPreference("notifications_enable");

            if (sharedPreferences.contains("notifications_enable"))
                notiPref.setChecked(sharedPreferences.getBoolean("notifications_enable",false));
            else {    //first time running
                editor = sharedPreferences.edit();
                editor.putBoolean("notifications_enable", false);
                editor.apply();
                notiPref.setChecked(sharedPreferences.getBoolean("notifications_enable",false));
            }

            notiPref.setOnPreferenceClickListener(preference -> {
                TwoStatePreference tsp = (TwoStatePreference) preference;
                SharedPreferences sharedPreferences13 = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getActivity());
                SharedPreferences.Editor editor13 = sharedPreferences13.edit();
                editor13.putBoolean("notifications_enable", tsp.isChecked());
                editor13.apply();

                return true;
            });

            //Notifications Frequency
            ListPreference freq = (ListPreference) findPreference("notification_frequency");

            if (sharedPreferences.contains("notification_frequency"))
                freq.setValue(sharedPreferences.getString("notification_frequency", DEFAULT_FREQ));
            else {   //first time running
                editor = sharedPreferences.edit();
                editor.putString("notification_frequency", DEFAULT_FREQ);
                editor.apply();
                freq.setValue(sharedPreferences.getString("notification_frequency", DEFAULT_FREQ));
            }

            freq.setOnPreferenceClickListener(preference -> {
                ListPreference lsp = (ListPreference) preference;
                SharedPreferences sharedPreferences12 = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getActivity());
                SharedPreferences.Editor editor12 = sharedPreferences12.edit();

                editor12.putString("notification_frequency", lsp.getValue());
                editor12.apply();

                return true;
            });

            //Notification Sounds Enable
            TwoStatePreference soundsPref = (TwoStatePreference) findPreference("notifications_sounds");

            if (sharedPreferences.contains("notifications_sounds"))
                soundsPref.setChecked(sharedPreferences.getBoolean("notifications_sounds",false));
            else {    //first time running
                editor = sharedPreferences.edit();
                editor.putBoolean("notifications_sounds", false);
                editor.apply();
                soundsPref.setChecked(sharedPreferences.getBoolean("notifications_sounds",false));
            }

            soundsPref.setOnPreferenceClickListener(preference -> {
                TwoStatePreference tsp = (TwoStatePreference) preference;
                SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getActivity());
                SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                editor1.putBoolean("notifications_sounds", tsp.isChecked());
                editor1.apply();

                return true;
            });

            //Data Reset Preferences
            header = new PreferenceCategory(this.getActivity());
            header.setTitle(R.string.pref_header_data);
            getPreferenceScreen().addPreference(header);
            addPreferencesFromResource(R.xml.pref_data);

            Preference myPref1 = findPreference("sltReset");
            myPref1.setOnPreferenceClickListener(preference -> {
                AlertDialog diaBox = ConfirmSLTReset();
                diaBox.show();
                return true;
            });

            Preference myPref2 = findPreference("fReset");
            myPref2.setOnPreferenceClickListener(preference -> {
                AlertDialog diaBox = ConfirmFactoryReset();
                diaBox.show();
                return true;
            });
        }

        private AlertDialog ConfirmFactoryReset() {
            Activity act = getActivity();
            return (new AlertDialog.Builder(act, R.style.AlertDialogStyle)
                    .setTitle(factoryResetTitle)
                    .setMessage(factoryResetText)
                    .setPositiveButton(yes, (dialog, whichButton) -> {
                        Uri pUri = Uri.parse(ProductContentProvider.CONTENT_URI + "/");
                        getActivity().getContentResolver().delete(pUri, null, null);
                        dialog.dismiss();
                        getActivity().finish();
                    })
                    .setNegativeButton(no, (dialog, which) -> dialog.dismiss())
                    .create());
        }

        private AlertDialog ConfirmSLTReset() {
            Activity act = getActivity();
            return (new AlertDialog.Builder(act, R.style.AlertDialogStyle)
                    .setTitle(sltResetTitle)
                    .setMessage(sltResetText)
                    .setPositiveButton(yes, (dialog, whichButton) -> {

                        Uri pUri = Uri.parse(ProductContentProvider.CONTENT_URI + "/");
                        String[] projection = {ProductTable.ID, ProductTable.BUY_PERIOD, ProductTable.TIMES_BOUGHT};
                        Cursor cursor = getActivity().getContentResolver().query(pUri, projection, null, null, null);
                        ContentValues values = new ContentValues();

                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            int id = cursor.getInt(cursor.getColumnIndex(ProductTable.ID));
                            int timesBought = cursor.getInt(cursor.getColumnIndex(ProductTable.TIMES_BOUGHT));
                            if (timesBought > 1) {
                                double buyPeriod = cursor.getDouble(cursor.getColumnIndex(ProductTable.BUY_PERIOD));

                                BigDecimal bd = new BigDecimal(buyPeriod);
                                BigDecimal durationBD = bd.multiply(Utils.SECONDS_PER_HOUR);
                                Duration duration = Duration.standardSeconds(durationBD.longValue());

                                DateTime dt = Utils.getDateTimeNow();
                                dt.minus(duration);

                                values.put(ProductTable.LAST_BUY, dt.toString(Utils.formatter));
                                getActivity().getContentResolver().update(Uri.parse(pUri.toString() + id), values, null, null);
                            }
                            else if (timesBought==1) {
                                values.put(ProductTable.LAST_BUY, Utils.getNow());
                                getActivity().getContentResolver().update(Uri.parse(pUri.toString() + id), values, null, null);
                            }

                            cursor.moveToNext();
                        }
                        cursor.close();

                        dialog.dismiss();
                        getActivity().finish(); }
                    )
                    .setNegativeButton(no, (dialog, which) -> dialog.dismiss())
                    .create()
            );
        }
    }
}


