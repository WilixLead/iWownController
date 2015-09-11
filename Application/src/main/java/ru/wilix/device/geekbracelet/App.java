package ru.wilix.device.geekbracelet;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import ru.wilix.device.geekbracelet.receiver.NotificationMonitor;

/**
 * Created by Aloyan Dmitry on 29.08.2015
 */
@ReportsCrashes(formUri = "http://acra-server.wilix.ru/logs/iWownController")
public class App extends Application {
    public static Context mContext;
    public static SharedPreferences sPref;

    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
        App.mContext = getApplicationContext();
        App.sPref = PreferenceManager.getDefaultSharedPreferences(App.mContext);

        if (BLEService.isBluetoothAvailable()) {
            // Create service
            Intent gattServiceIntent = new Intent(this, BLEService.class);
            startService(gattServiceIntent);

            Notification notification = new NotificationCompat.Builder(this)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentText("iWown Controller")
                    .setContentTitle("iWown Controller")
                    .setTicker("iWown Controller")
                    .setAutoCancel(false)
                    .build();
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_INSISTENT;// Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT |
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(0, notification);
            loadProperties();
        } else {
            Toast.makeText(App.mContext, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public static void loadProperties(){
        SharedPreferences sp = App.sPref;

        NotificationMonitor.availablePackages.clear();
        if( sp.getBoolean("cbx_notice_vk", false) )
            NotificationMonitor.availablePackages.add("com.vkontakte.android");
        if( sp.getBoolean("cbx_notice_facebook", false) )
            NotificationMonitor.availablePackages.add("facebook");
        if( sp.getBoolean("cbx_notice_twitter", false) )
            NotificationMonitor.availablePackages.add("twitter");
        if( sp.getBoolean("cbx_notice_whatsapp", false) )
            NotificationMonitor.availablePackages.add("com.whatsapp");
        if( sp.getBoolean("cbx_notice_viber", false) )
            NotificationMonitor.availablePackages.add("com.viber.voip");
        if( sp.getBoolean("cbx_notice_telegram", false) )
            NotificationMonitor.availablePackages.add("org.telegram.messenger");
        if( sp.getBoolean("cbx_notice_skype", false) )
            NotificationMonitor.availablePackages.add("skype");
        if( sp.getBoolean("cbx_notice_hangouts", false) )
            NotificationMonitor.availablePackages.add("com.google.android.talk");
        if( sp.getBoolean("cbx_notice_sms", false) )
            NotificationMonitor.availablePackages.add("sms");
        if( sp.getBoolean("cbx_notice_call", false) )
            NotificationMonitor.availablePackages.add("com.vkontakte.android");
        if( sp.getBoolean("cbx_notice_gmail", false) ) {
            NotificationMonitor.availablePackages.add("com.google.android.gm");
            NotificationMonitor.availablePackages.add("mail");
        }
    }
}
