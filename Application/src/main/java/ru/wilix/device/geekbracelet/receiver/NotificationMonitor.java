package ru.wilix.device.geekbracelet.receiver;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.util.Log;

import java.io.Serializable;

import ru.wilix.device.geekbracelet.BroadcastConstants;
import ru.wilix.device.geekbracelet.i5.Constants;
import ru.wilix.device.geekbracelet.model.AppNotification;

/**
 * Created by Aloyan Dmitry on 30.08.2015
 */
public class NotificationMonitor extends NotificationListenerService {
    public static StatusBarNotification lastSbn;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i("NOTIFICATION", "From package: " + sbn.getPackageName());
        lastSbn = sbn;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int canNotice = AppNotification.canNotice(lastSbn.getPackageName());
                    if(canNotice > 0){
                        Intent intent = new Intent(BroadcastConstants.ACTION_NEW_NOTIFICATION_RECEIVED);
                        intent.putExtra("data", Notif.fromSbn(lastSbn, canNotice));
                        sendBroadcast(intent);
                        return;
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
        public String fromName = "";
        public String msgText = "";
        public Integer noticeType = 0;

        public static Notif fromSbn(StatusBarNotification sbn, int notice_type){
            Notif nf = new Notif();
            //String ticker = "";

            if( sbn == null || sbn.getPackageName() == null )
                return nf;

            Log.i("Package", sbn.getPackageName());

            if( sbn.getNotification() == null )
                return nf;

//            if( sbn.getNotification().tickerText != null ) {
//                ticker = sbn.getNotification().tickerText.toString();
//            }
            if( sbn.getNotification().extras != null ) {
                Bundle extras = sbn.getNotification().extras;
                if( extras.containsKey(Notification.EXTRA_TITLE) )
                    nf.fromName = extras.get(Notification.EXTRA_TITLE).toString();
                if( extras.containsKey("android.text") )
                    nf.msgText = extras.get("android.text").toString();
            }

//            Log.i("Ticker", ticker);
            if( nf.fromName != null )
                Log.i("Title", nf.fromName);
//            if( nf.msgText != null )
//                Log.i("Text", nf.msgText);
            nf.noticeType = notice_type;

            return nf;
        }

        public Integer getDeviceNoticeType(){
            switch (this.noticeType){
                case 1:return Constants.ALERT_TYPE_MESSAGE;
                case 2:return Constants.ALERT_TYPE_CLOUD;
                case 3:return Constants.ALERT_TYPE_ERROR;
                default:return Constants.ALERT_TYPE_MESSAGE;
            }
        }

        public void setAppName(String value){
            this.appName = value;
        }
        public String getAppName(){
            return this.appName;
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
        public void setNoticeType(Integer type){
            this.noticeType = type;
        }
        public Integer getNoticeType(){
            return this.noticeType;
        }
    }
}