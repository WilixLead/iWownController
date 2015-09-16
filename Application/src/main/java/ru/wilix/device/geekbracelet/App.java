package ru.wilix.device.geekbracelet;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                    new Intent(this, MainActivity.class), 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentText("WiliX Controller")
                    .setContentTitle("WiliX Controller")
                    .setTicker("WiliX Controller")
                    .setPriority(Notification.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setContentIntent(pi)
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
        if( sp.getBoolean("fit_connected", false) )
            GoogleFitConnector.connect(App.mContext);
    }
}
