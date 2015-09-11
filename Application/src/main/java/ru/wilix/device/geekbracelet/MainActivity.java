package ru.wilix.device.geekbracelet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Date;

import ru.wilix.device.geekbracelet.model.DeviceInfo;

public class MainActivity extends Activity {
    private Button connectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBtn = (Button)findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DeviceScanActivity.class);
                startActivity(intent);
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor ed = App.sPref.edit();

                ed.putBoolean("cbx_action_locator_on_long",
                        ((CheckBox)findViewById(R.id.cbx_action_locator_on_long)).isChecked());
                ed.putBoolean("cbx_action_mute_onclick",
                            ((CheckBox)findViewById(R.id.cbx_action_mute_onclick)).isChecked());
                ed.putBoolean("cbx_action_reject_on_long",
                        ((CheckBox)findViewById(R.id.cbx_action_reject_on_long)).isChecked());

                ed.putBoolean("cbx_notice_deskclock",
                        ((CheckBox)findViewById(R.id.cbx_notice_deskclock)).isChecked());

                ed.putBoolean("cbx_notice_vk",
                        ((CheckBox)findViewById(R.id.cbx_notice_vk)).isChecked());

                ed.putBoolean("cbx_notice_facebook",
                        ((CheckBox)findViewById(R.id.cbx_notice_facebook)).isChecked());

                ed.putBoolean("cbx_notice_twitter",
                        ((CheckBox)findViewById(R.id.cbx_notice_twitter)).isChecked());

                ed.putBoolean("cbx_notice_whatsapp",
                        ((CheckBox)findViewById(R.id.cbx_notice_whatsapp)).isChecked());

                ed.putBoolean("cbx_notice_viber",
                        ((CheckBox)findViewById(R.id.cbx_notice_viber)).isChecked());

                ed.putBoolean("cbx_notice_telegram",
                        ((CheckBox)findViewById(R.id.cbx_notice_telegram)).isChecked());

                ed.putBoolean("cbx_notice_skype",
                        ((CheckBox)findViewById(R.id.cbx_notice_skype)).isChecked());

                ed.putBoolean("cbx_notice_hangouts",
                        ((CheckBox)findViewById(R.id.cbx_notice_hangouts)).isChecked());

                ed.putBoolean("cbx_notice_sms",
                        ((CheckBox)findViewById(R.id.cbx_notice_sms)).isChecked());

                ed.putBoolean("cbx_notice_call",
                        ((CheckBox)findViewById(R.id.cbx_notice_call)).isChecked());

                ed.putBoolean("cbx_notice_gmail",
                        ((CheckBox)findViewById(R.id.cbx_notice_gmail)).isChecked());

                ed.apply();
                App.loadProperties();
            }
        };
        ((CheckBox)findViewById(R.id.cbx_action_locator_on_long)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_action_mute_onclick)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_action_reject_on_long)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_deskclock)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_vk)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_facebook)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_twitter)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_whatsapp)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_viber)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_telegram)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_skype)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_hangouts)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_sms)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_call)).setOnClickListener(listener);
        ((CheckBox)findViewById(R.id.cbx_notice_gmail)).setOnClickListener(listener);

        ((TextView)findViewById(R.id.label_device_model)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getBaseContext(), DeviceSettingsActivity.class);
                startActivity(in);
            }
        });

        if( !checkNotificationListenEnabled() ) {
            askNotificationAccess();
        }
    }

    private void loadProperties(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = App.sPref;
                ((CheckBox)findViewById(R.id.cbx_action_locator_on_long)).setChecked(sp.getBoolean("cbx_action_locator_on_long", false));
                ((CheckBox)findViewById(R.id.cbx_action_mute_onclick)).setChecked(sp.getBoolean("cbx_action_mute_onclick", false));
                ((CheckBox)findViewById(R.id.cbx_action_reject_on_long)).setChecked(sp.getBoolean("cbx_action_reject_on_long", false));
                ((CheckBox)findViewById(R.id.cbx_notice_deskclock)).setChecked(sp.getBoolean("cbx_notice_deskclock", false));
                ((CheckBox)findViewById(R.id.cbx_notice_vk)).setChecked(sp.getBoolean("cbx_notice_vk", false));
                ((CheckBox)findViewById(R.id.cbx_notice_facebook)).setChecked(sp.getBoolean("cbx_notice_facebook", false));
                ((CheckBox)findViewById(R.id.cbx_notice_twitter)).setChecked(sp.getBoolean("cbx_notice_twitter", false));
                ((CheckBox)findViewById(R.id.cbx_notice_whatsapp)).setChecked(sp.getBoolean("cbx_notice_whatsapp", false));
                ((CheckBox)findViewById(R.id.cbx_notice_viber)).setChecked(sp.getBoolean("cbx_notice_viber", false));
                ((CheckBox)findViewById(R.id.cbx_notice_telegram)).setChecked(sp.getBoolean("cbx_notice_telegram", false));
                ((CheckBox)findViewById(R.id.cbx_notice_skype)).setChecked(sp.getBoolean("cbx_notice_skype", false));
                ((CheckBox)findViewById(R.id.cbx_notice_hangouts)).setChecked(sp.getBoolean("cbx_notice_hangouts", false));
                ((CheckBox)findViewById(R.id.cbx_notice_sms)).setChecked(sp.getBoolean("cbx_notice_sms", false));
                ((CheckBox)findViewById(R.id.cbx_notice_call)).setChecked(sp.getBoolean("cbx_notice_call", false));
                ((CheckBox)findViewById(R.id.cbx_notice_gmail)).setChecked(sp.getBoolean("cbx_notice_gmail", false));
            }
        });
    }

    private boolean checkNotificationListenEnabled(){
        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();

        // check to see if the enabledNotificationListeners String contains our package name
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName))
            // in this situation we know that the user has not granted the app the Notification access permission
            return false;
        else
            return true;
    }

    private void askNotificationAccess() {
        AlertDialog alert = new AlertDialog.Builder(this)
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
    protected void onResume() {
        super.onResume();
        loadProperties();

        registerReceiver(mGattUpdateReceiver, gattIntentFilter());

        if( BLEService.getSelf() != null &&
                BLEService.getSelf().getmBluetoothGatt() != null &&
                BLEService.getSelf().getmBluetoothGatt().getDevice() != null ) {
            ((Button) findViewById(R.id.connectBtn)).setText(BLEService.getSelf().getmBluetoothGatt().getDevice().getName());
            requestDeviceInfo();
        }else{
            ((Button)findViewById(R.id.connectBtn)).setText("Не подключено");
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try {
            unregisterReceiver(mGattUpdateReceiver);
        }catch (Exception e){}
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Intent in = intent;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String action = in.getAction();
                    switch (action) {
                        case BLEService.ACTION_GATT_CONNECTED:
                            ((Button) findViewById(R.id.connectBtn))
                                    .setText(BLEService.getSelf().getmBluetoothGatt().getDevice().getName());
                            break;
                        case BLEService.ACTION_GATT_DISCONNECTED:
                            ((Button) findViewById(R.id.connectBtn)).setText("Не подключено");
                            break;
                        case BLEService.ACTION_GATT_SERVICES_DISCOVERED:
                            requestDeviceInfo();
                            break;
                        case BroadcastConstants.ACTION_DEVICE_INFO:
                            final DeviceInfo info = (DeviceInfo) in.getSerializableExtra("data");
                            ((TextView) findViewById(R.id.label_device_model)).setText(info.getModel());
                            ((TextView) findViewById(R.id.label_device_sw)).setText(info.getSwversion());
                            break;
                        case BroadcastConstants.ACTION_DEVICE_POWER:
                            ((TextView) findViewById(R.id.label_device_power)).setText(in.getIntExtra("data", 0) + "%");
                            break;
                        case BroadcastConstants.ACTION_DATE_DATA:
                            long timestamp = in.getLongExtra("data", 0);
                            if (timestamp <= 0)
                                ((TextView) findViewById(R.id.label_device_time)).setText("Неверное время!");
                            else {
                                CharSequence dt = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", new Date(timestamp));
                                ((TextView) findViewById(R.id.label_device_time)).setText(dt);
                            }
                            BLEService.getSelf().getDevice().setDate();
                            break;
                    }
                }
            });
        }
    };

    public static IntentFilter gattIntentFilter(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);

        intentFilter.addAction(BroadcastConstants.ACTION_DEVICE_INFO);
        intentFilter.addAction(BroadcastConstants.ACTION_DEVICE_POWER);
        intentFilter.addAction(BroadcastConstants.ACTION_DATE_DATA);

        return intentFilter;
    }
}
