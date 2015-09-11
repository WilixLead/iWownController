package ru.wilix.device.geekbracelet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;

import ru.wilix.device.geekbracelet.App;
import ru.wilix.device.geekbracelet.BLEService;
import ru.wilix.device.geekbracelet.BroadcastConstants;

/**
 * Created by Aloyan Dmitry on 29.08.2015
 */
public class CallReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent){
        if( !App.sPref.getBoolean("cbx_notice_call", false) )
            return;

        if( BLEService.getSelf() == null || BLEService.getSelf().getDevice() == null )
            return;

        Intent in;
        switch (((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState()){
            case TelephonyManager.CALL_STATE_OFFHOOK:
            case TelephonyManager.CALL_STATE_IDLE:
                in = new Intent(BroadcastConstants.ACTION_END_CALL);
                context.sendBroadcast(in);
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                String number = intent.getStringExtra("incoming_number");

                in = new Intent(BroadcastConstants.ACTION_INCOMING_CALL);
                in.putExtra("data", getContact(context, number));
                context.sendBroadcast(in);
                break;
            default:
                in = new Intent(BroadcastConstants.ACTION_END_CALL);
                context.sendBroadcast(in);
                break;
        }
    }

    public static String getContact(Context context, String phoneNumber) {
        if ( phoneNumber == null || phoneNumber.length() <= 0 )
            return "Unknown";

        String name = "No name";
        Cursor cursor = null;
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            cursor = context.getContentResolver().query(uri, new String[]{"display_name", "type", "label"}, null, null, "display_name LIMIT 1");
            if (cursor.moveToNext())
                name = cursor.getString(cursor.getColumnIndex("display_name"));
            if (cursor != null)
                cursor.close();
        } catch (Exception e) {
            name = phoneNumber;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return name;
    }

    public static void rejectCall(Context context, int mode){
        try{
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyService = m.invoke(tm); // Get the internal ITelephony object
            c = Class.forName(telephonyService.getClass().getName()); // Get its class
            m = c.getDeclaredMethod( (mode == 0) ? "endCall" : "silenceRinger");
            m.setAccessible(true); // Make it accessible
            m.invoke(telephonyService); // invoke endCall()
        }catch( Exception e ){ e.printStackTrace(); }
    }
}
