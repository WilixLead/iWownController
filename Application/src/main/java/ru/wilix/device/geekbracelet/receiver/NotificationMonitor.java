package ru.wilix.device.geekbracelet.receiver;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

import ru.wilix.device.geekbracelet.BroadcastConstants;

/**
 * Created by Aloyan Dmitry on 30.08.2015
 */
public class NotificationMonitor extends NotificationListenerService {
    public static ArrayList<String> availablePackages = new ArrayList<>();
    public static StatusBarNotification lastSbn;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i("NOTIFICATION", "From package: " + sbn.getPackageName());
        lastSbn = sbn;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String pkg : availablePackages) {
                        if (lastSbn.getPackageName().indexOf(pkg) != -1) {
                            Intent intent = new Intent(BroadcastConstants.ACTION_NEW_NOTIFICATION_RECEIVED);
                            intent.putExtra("data", Notif.fromSbn(lastSbn));
                            sendBroadcast(intent);
                            return;
                        }
                    }
                }catch (Exception e){ e.printStackTrace(); }
                Log.i("NOTIFICATION", "Package: " + lastSbn.getPackageName() + " skipped");
            }
        }).start();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //Log.i("NOTIF!", "On remove");
    }

    public IBinder onBind(Intent intent){
        return super.onBind(intent);
    }

    public static class Notif implements Serializable{
        public String appName = "";
        public String shortName = "";
        public String fromName = "";
        public String msgText = "";

        public static Notif fromSbn(StatusBarNotification sbn){
            Notif nf = new Notif();
            //String ticker = "";

            if( sbn == null || sbn.getPackageName() == null )
                return nf;

            Log.i("Package", sbn.getPackageName());
            nf.fillAppName(sbn.getPackageName());

            if( sbn.getNotification() == null )
                return nf;

//            if( sbn.getNotification().tickerText != null ) {
//                ticker = sbn.getNotification().tickerText.toString();
//            }
            if( sbn.getNotification().extras != null ) {
                Bundle extras = sbn.getNotification().extras;
                if( extras.containsKey("android.title") )
                    nf.fromName = extras.getString(Notification.EXTRA_TITLE);
//                if( extras.containsKey("android.text") )
//                    nf.msgText = extras.getString("android.text").toString();
            }

//            Log.i("Ticker", ticker);
            if( nf.fromName != null )
                Log.i("Title", nf.fromName);
//            if( nf.msgText != null )
//                Log.i("Text", nf.msgText);

            return nf;
        }

        public void fillAppName(String packageName){
            if( packageName == null || packageName.length() <= 0 )
                return ;

            switch (packageName){
                case "com.vkontakte.android":
                    this.appName = "VK";
                    this.shortName = "VK";
                    break;
                case "org.telegram.messenger":
                    this.appName = "Telegram";
                    this.shortName = "Tm";
                    break;
                case "com.google.android.talk":
                    this.appName = "Hangouts";
                    this.shortName = "Hs";
                    break;
                case "com.whatsapp":
                    this.appName = "Whatsapp";
                    this.shortName = "Wp";
                    break;
                case "com.google.android.gm":
                    this.appName = "Gmail";
                    this.shortName = "Gm";
                    break;
                case "com.viber.voip":
                    this.appName = "Viber";
                    this.shortName = "Vb";
                default:
                    this.appName = packageName.substring(packageName.lastIndexOf("."));
                    this.shortName = packageName.substring(0, 2);
                    break;
            }
        }

        public void setAppName(String value){
            this.appName = value;
        }
        public String getAppName(){
            return this.appName;
        }
        public void setShortName(String value){
            this.shortName = value;
        }
        public String getShortName(){
            return this.shortName;
        }
        public void setFromName(String value){
            this.fromName = value;
        }
        public String getFromName(){
            return this.fromName;
        }
        public void setMsgText(String value){
            this.msgText = value;
        }
        public String getMsgText(){
            return this.msgText;
        }
    }
}