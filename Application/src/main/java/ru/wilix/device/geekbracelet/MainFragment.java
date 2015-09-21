package ru.wilix.device.geekbracelet;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import ru.wilix.device.geekbracelet.model.DeviceInfo;

/**
 * Created by Aloyan Dmitry on 16.09.2015
 */
public class MainFragment extends Fragment {
    private Button connectBtn;
    private View container;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inView = inflater.inflate(R.layout.main_fragment, null);
        this.container = inView;
        initViews();
        return inView;
    }

    private void initViews(){
        connectBtn = (Button)container.findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DeviceScanActivity.class);
                startActivity(intent);
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor ed = App.sPref.edit();

                ed.putBoolean("cbx_notice_call",
                        ((CheckBox)container.findViewById(R.id.cbx_notice_call)).isChecked());
                ed.putBoolean("cbx_action_locator_on_long",
                        ((CheckBox)container.findViewById(R.id.cbx_action_locator_on_long)).isChecked());
                ed.putBoolean("cbx_action_mute_onclick",
                        ((CheckBox)container.findViewById(R.id.cbx_action_mute_onclick)).isChecked());
                ed.putBoolean("cbx_action_reject_on_long",
                        ((CheckBox)container.findViewById(R.id.cbx_action_reject_on_long)).isChecked());
                ed.putBoolean("cbx_notice_deskclock",
                        ((CheckBox)container.findViewById(R.id.cbx_notice_deskclock)).isChecked());
                ed.apply();
                App.loadProperties();
            }
        };
        ((CheckBox)container.findViewById(R.id.cbx_notice_call)).setOnClickListener(listener);
        ((CheckBox)container.findViewById(R.id.cbx_action_locator_on_long)).setOnClickListener(listener);
        ((CheckBox)container.findViewById(R.id.cbx_action_mute_onclick)).setOnClickListener(listener);
        ((CheckBox)container.findViewById(R.id.cbx_action_reject_on_long)).setOnClickListener(listener);
        ((CheckBox)container.findViewById(R.id.cbx_notice_deskclock)).setOnClickListener(listener);

        ((Button)container.findViewById(R.id.settingsBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getActivity(), DeviceSettingsActivity.class);
                startActivity(in);
            }
        });

        ((Button)container.findViewById(R.id.connectToFitBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleFitConnector.connect(getActivity());
            }
        });

        ((Button)container.findViewById(R.id.show_applist_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new AppListFragment())
                        .addToBackStack("applist")
                        .commit();
            }
        });

        if( !checkNotificationListenEnabled() ) {
            askNotificationAccess();
        }
    }

    private void loadProperties(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = App.sPref;
                ((CheckBox) container.findViewById(R.id.cbx_notice_call)).setChecked(sp.getBoolean("cbx_notice_call", false));
                ((CheckBox) container.findViewById(R.id.cbx_action_locator_on_long)).setChecked(sp.getBoolean("cbx_action_locator_on_long", false));
                ((CheckBox) container.findViewById(R.id.cbx_action_mute_onclick)).setChecked(sp.getBoolean("cbx_action_mute_onclick", false));
                ((CheckBox) container.findViewById(R.id.cbx_action_reject_on_long)).setChecked(sp.getBoolean("cbx_action_reject_on_long", false));
                ((CheckBox) container.findViewById(R.id.cbx_notice_deskclock)).setChecked(sp.getBoolean("cbx_notice_deskclock", false));
            }
        });
    }

    private boolean checkNotificationListenEnabled(){
        ContentResolver contentResolver = getActivity().getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getActivity().getPackageName();

        // check to see if the enabledNotificationListeners String contains our package name
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName))
            // in this situation we know that the user has not granted the app the Notification access permission
            return false;
        else
            return true;
    }

    private void askNotificationAccess() {
        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle("Внимание")
                .setMessage("Для отображения уведомлений на браслете необходимо разрешить доступ к уведомлениям. Открыть настройки?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Отмена", null)
                .create();
        alert.show();
    }

    private void requestDeviceInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BLEService.getSelf().getDevice().askFmVersionInfo();
                    Thread.sleep(500);
                    BLEService.getSelf().getDevice().askPower();
                    Thread.sleep(500);
                    BLEService.getSelf().getDevice().askDate();
                }catch (Exception e){}
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProperties();

        getActivity().registerReceiver(mGattUpdateReceiver, gattIntentFilter());

        if( BLEService.getSelf() != null &&
                BLEService.getSelf().getmBluetoothGatt() != null &&
                BLEService.getSelf().getmBluetoothGatt().getDevice() != null ) {
            ((Button)container.findViewById(R.id.connectBtn)).setText(BLEService.getSelf().getmBluetoothGatt().getDevice().getName());
            requestDeviceInfo();
        }else{
            ((Button)container.findViewById(R.id.connectBtn)).setText(getResources().getString(R.string.device_not_connected));
            if( BLEService.getSelf() != null )
                BLEService.getSelf().connect(App.sPref.getString("DEVICE_ADDR", ""), true);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        try {
            getActivity().unregisterReceiver(mGattUpdateReceiver);
        }catch (Exception e){}
    }

    public void onDestroy(){
        super.onDestroy();
        //getActivity().finish();
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final Intent in = intent;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String action = in.getAction();
                    switch (action) {
                        case BroadcastConstants.ACTION_GATT_CONNECTED:
                            ((Button) container.findViewById(R.id.connectBtn))
                                    .setText(BLEService.getSelf().getmBluetoothGatt().getDevice().getName());
                            break;
                        case BroadcastConstants.ACTION_GATT_DISCONNECTED:
                            ((Button) container.findViewById(R.id.connectBtn)).setText("Не подключено");
                            break;
                        case BroadcastConstants.ACTION_GATT_SERVICES_DISCOVERED:
                            requestDeviceInfo();
                            break;
                        case BroadcastConstants.ACTION_DEVICE_INFO:
                            final DeviceInfo info = (DeviceInfo) in.getSerializableExtra("data");
                            ((TextView) container.findViewById(R.id.label_device_model)).setText(info.getModel());
                            ((TextView) container.findViewById(R.id.label_device_sw)).setText(info.getSwversion());
                            break;
                        case BroadcastConstants.ACTION_DEVICE_POWER:
                            ((TextView) container.findViewById(R.id.label_device_power)).setText(in.getIntExtra("data", 0) + "%");
                            break;
                        case BroadcastConstants.ACTION_DATE_DATA:
                            long timestamp = in.getLongExtra("data", 0);
                            if (timestamp <= 0)
                                ((TextView) container.findViewById(R.id.label_device_time)).setText("Неверное время!");
                            else {
                                CharSequence dt = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", new Date(timestamp));
                                ((TextView) container.findViewById(R.id.label_device_time)).setText(dt);
                            }
                            BLEService.getSelf().getDevice().setDate();
                            break;
                        case BroadcastConstants.ACTION_CONNECT_TO_GFIT:
                            if (App.sPref.getBoolean("fit_connected", false) == false) {
                                Toast.makeText(getActivity(), getResources().getString(R.string.google_fit_not_connected),
                                        Toast.LENGTH_SHORT).show();
                                ((Button) container.findViewById(R.id.connectToFitBtn))
                                        .setText(getResources().getString(R.string.connect_to_fit));
                                return;
                            }

                            Toast.makeText(getActivity(), getResources().getString(R.string.google_fit_connected),
                                    Toast.LENGTH_SHORT).show();
                            ((Button) container.findViewById(R.id.connectToFitBtn))
                                    .setText(getResources().getString(R.string.reconnect_to_fit));
                            if (BLEService.getSelf() == null || BLEService.getSelf().getDevice() == null)
                                return;

//                            BLEService.getSelf().getDevice().askDailyData();
                            BLEService.getSelf().getDevice().subscribeForSportUpdates();
                            break;
                    }
                }
            });
        }
    };

    public static IntentFilter gattIntentFilter(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConstants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BroadcastConstants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BroadcastConstants.ACTION_GATT_SERVICES_DISCOVERED);

        intentFilter.addAction(BroadcastConstants.ACTION_DEVICE_INFO);
        intentFilter.addAction(BroadcastConstants.ACTION_DEVICE_POWER);
        intentFilter.addAction(BroadcastConstants.ACTION_DATE_DATA);
        intentFilter.addAction(BroadcastConstants.ACTION_CONNECT_TO_GFIT);

        return intentFilter;
    }
}
