package ru.wilix.device.geekbracelet;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.HashMap;

public class DeviceSettingsActivity extends PreferenceActivity {
    ProgressDialog dialog;
    IntentFilter inFilter;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if( BLEService.getSelf() == null || BLEService.getSelf().getDevice() == null ){
            Toast.makeText(this, R.string.device_settings_device_not_connected, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        inFilter = new IntentFilter();
        inFilter.addAction(BroadcastConstants.ACTION_DEVICE_CONF_DATA);
        inFilter.addAction(BroadcastConstants.ACTION_BLE_DATA);
        inFilter.addAction(BroadcastConstants.ACTION_USER_BODY_DATA);

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.device_settings_wait_device_alert));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BLEService.getSelf().getDevice().askBle();
                    Thread.sleep(500);
                    BLEService.getSelf().getDevice().askConfig();
                    Thread.sleep(500);
                    BLEService.getSelf().getDevice().askUserParams();
                }catch (Exception e){}
            }
        }).start();
        dialog.show();
    }

    private int packetIterator = 0;
    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            packetIterator++;
            SharedPreferences.Editor ed = App.sPref.edit();
            final String action = intent.getAction();
            switch (action){
                case BroadcastConstants.ACTION_BLE_DATA:
                    ed.putBoolean("dev_conf_ble", intent.getIntExtra("data", 0) > 0);
                    break;
                case BroadcastConstants.ACTION_USER_BODY_DATA:
                    HashMap<String, Integer> userData = (HashMap<String, Integer>)intent.getSerializableExtra("data");

                    ed.putInt("dev_conf_goal", userData.get("goal_high"));
                    ed.putInt("dev_conf_weight", userData.get("weight"));
                    ed.putInt("dev_conf_height", userData.get("height"));
                    ed.putInt("dev_conf_age", userData.get("age"));
                    ed.putInt("dev_conf_gender", userData.get("gender"));
                    break;
                case BroadcastConstants.ACTION_DEVICE_CONF_DATA:
                    HashMap<String, Integer> confData = (HashMap<String, Integer>)intent.getSerializableExtra("data");

                    ed.putBoolean("dev_conf_light", confData.get("light") > 0);
                    ed.putBoolean("dev_conf_gesture", confData.get("gesture") > 0);
                    ed.putBoolean("dev_conf_englishunits", confData.get("englishUnits") > 0);
                    ed.putBoolean("dev_conf_use24hours", confData.get("use24hour") > 0);
                    ed.putBoolean("dev_conf_autosleep", confData.get("autoSleep") > 0);
                    break;
            }
            ed.apply();
            if( packetIterator >= 3 ){
                if( dialog != null && dialog.isShowing() )
                    dialog.dismiss();
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(resultReceiver, inFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(resultReceiver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}
