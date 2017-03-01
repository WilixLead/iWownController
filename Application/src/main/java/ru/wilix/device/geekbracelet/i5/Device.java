package ru.wilix.device.geekbracelet.i5;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.UUID;

import ru.wilix.device.geekbracelet.App;
import ru.wilix.device.geekbracelet.BLEService;
import ru.wilix.device.geekbracelet.BroadcastConstants;
import ru.wilix.device.geekbracelet.model.DeviceClockAlarm;
import ru.wilix.device.geekbracelet.model.DeviceInfo;
import ru.wilix.device.geekbracelet.model.Sport;
import ru.wilix.device.geekbracelet.receiver.NotificationMonitor;

/**
 * Created by Dmitry on 29.08.2015.
 * My swversion: 1.1.0.9 I5
 * DEVICE_POWER﹕ bleAddr: 4d2b2a84bec4 displayWidthFont: 0 model: I5 oadmode: 0 swversion: 1.1.0.9
 */
public class Device {
    private static final String TAG = "Device_iWown_i5";
    public Communication comm;
    public DeviceInfo deviceInfo;

    public Device(BLEService bleService){
        this.comm = new Communication(bleService, this);
    }

    /**
     * Return Firmware version
     */
    public void askFmVersionInfo(){
        writePacket(Utils.getDataByte(true, Utils.form_Header(0, 0), null));
    }

    /**
     * Return battery power
     */
    public void askPower(){
        writePacket(Utils.getDataByte(true, Utils.form_Header(0, 1), null));
    }

    /**
     * Return device configuration
     */
    public void askConfig(){
        writePacket(Utils.getDataByte(true, Utils.form_Header(1, 9), null));
    }

    /**
     * Return User Body parameters
     */
    public void askUserParams(){
        writePacket(Utils.getDataByte(true, Utils.form_Header(2, 1), null));
    }

    /**
     * Return BLE state
     * TODO need to understand what is this
     */
    public void askBle(){
        writePacket(Utils.getDataByte(true, Utils.form_Header(1, 3), null));
    }

    // TODO ask Alarams. Need to test. If send this command, device return all alrms or need
    // specified alarm ID in ask
//    public void askAlarm(){
//        writePacket(Utils.getDataByte(true, Utils.form_Header(1, 3), null));
//    }

    /**
     * Return Daily Sport entries. This entries automatically clear in device on ask
     */
    public void askDailyData(){
        writePacket(Utils.getDataByte(true, Utils.form_Header(2, 7), null));
    }

    /**
     * Return device data and time
     */
    public void askDate(){
        writePacket(Utils.getDataByte(true, Utils.form_Header(1, 1), null));
    }

    /**
     * Return data of local sport. May be it sleep data...
     * TODO Check what is this data means
     */
    public void askLocalSport(){
        writePacket(Utils.getDataByte(true, Utils.form_Header(2, 5), null));
    }

    /**
     * After subscribe, device will send Sport object on each activity
     * and once in minute if activity don't register
     */
    public void subscribeForSportUpdates(){
        writePacket(Utils.getDataByte(true, Utils.form_Header(2, 3), null));
    }

    /**
     * Set date and time to device internal clock
     */
    public void setDate() {
        GregorianCalendar date = new GregorianCalendar();
        ArrayList<Byte> data = new ArrayList<>();
        data.add( ((byte)(date.get(Calendar.YEAR) - 2000)) );
        if(App.sPref.getString("device_model", "i5").contains("+"))
            data.add( ((byte)(date.get(Calendar.MONTH))) );
        else
            data.add( ((byte)(date.get(Calendar.MONTH) - 1)) );
        data.add( ((byte)(date.get(Calendar.DAY_OF_MONTH) - 1)) );
        data.add( ((byte)date.get(Calendar.HOUR_OF_DAY)) );
        data.add( ((byte)date.get(Calendar.MINUTE)) );
        data.add( ((byte)date.get(Calendar.SECOND)) );
        writePacket(Utils.getDataByte(true, Utils.form_Header(1, 0), data));
    }

    /**
     * Sel alarm to device internal memory.
     * @param alarm - alarm to write
     * @param sectionId - Available 7 alarms. IDS: 0,1,2,3,4,5,6
     */
    public void setClockAlarm(DeviceClockAlarm alarm, int sectionId){
        ArrayList<Byte> data = new ArrayList<>();
        data.add((byte) sectionId);
        data.add((byte) 0);
        data.add((byte) (alarm.isOpen ? alarm.week : 0));
        data.add((byte) alarm.hour);
        data.add((byte) alarm.minute);
        writePacket(Utils.getDataByte(true, Utils.form_Header(1, 4), data));
    }

    /**
     * Unknown command. Call on send configuration params to device
     */
    public void setBle(boolean enabled){
        ArrayList<Byte> data = new ArrayList<>();
        data.add((byte)0);
        data.add((byte) (enabled ? 1 : 0));
        writePacket(Utils.getDataByte(true, Utils.form_Header(1, 2), data));
    }

    /**
     * Set selfie mode. Rise selfie event on one click to button
     * @param enable
     */
    public void setSelfieMode(boolean enable){
        ArrayList<Byte> data = new ArrayList<>();
        data.add(enable ? (byte) 1 : (byte) 0);
        writePacket(Utils.getDataByte(true, Utils.form_Header(4, 0), data));
    }

    /**
     * Send user body and goal params. This params important for calculating steps and distantion
     * FIXME NOT TESTED
     *
     * @param height - Body height. Like 180
     * @param weight - Body weight. Like 79
     * @param gender false = male, true = female
     * @param age - age in years. Like 26 years
     * @param goal - in steps. For example 10000 steps per day
     */
    public void setUserParams(int height, int weight, boolean gender, int age, int goal){
        ArrayList<Byte> datas = new ArrayList<>();
        datas.add((byte)height);
        datas.add((byte)weight);
        datas.add((byte)( !gender ? 0 : 1));
        datas.add((byte)age);
        int goal_low = goal % AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY;
        int goal_high = (goal - goal_low) / AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY;
        datas.add((byte) goal_low);
        datas.add((byte) goal_high);

        byte[] data = Utils.getDataByte(true, Utils.form_Header(2, 0), datas);
        writePacket(data);
    }

    /**
     * Send config to device
     * FIXME NOT TESTED
     *
     * @param light - Enable blue light blinking
     * @param gesture - Enable gesture. Turn display on if you try to look at the device
     * @param englishUnits - Use English Units (Miles, Foots and etc.)
     * @param use24hour - Use 24 hour time format, if false used 12 hour time
     * @param autoSleep - Enable auto sleep mode when you go to sleep
     */
    public void setConfig(boolean light, boolean gesture, boolean englishUnits,
                             boolean use24hour, boolean autoSleep) {
        ArrayList<Byte> datas = new ArrayList<>();

        datas.add((byte) (light ? 1 : 0));
        datas.add((byte) (gesture ? 1 : 0));
        datas.add((byte) (englishUnits ? 1 : 0));
        datas.add((byte) (use24hour ? 1 : 0));
        datas.add((byte) (autoSleep ? 1 : 0));

        if(Communication.apiVersion == 2)
        {
            datas.add((byte) 1);
            datas.add((byte) 8); // Light Start Time (Whatever that means); Default
            datas.add((byte) 20); // Light End Time (Whatever that means); Default

            datas.add((byte) 0); // Inverse Colors
            datas.add((byte) 0); // Is English
            datas.add((byte) 0); // Disconnect Tip
        }
        else
        {
            datas.add((byte) 0);
            datas.add((byte) 0);
        }

        byte[] data = Utils.getDataByte(true, Utils.form_Header(1, 8), datas);
        writePacket(data);
    }

    /**
     * Show message alert in display
     * @param msg - message to show
     */
    public void sendMessage(String msg){
        sendAlert(msg, Constants.ALERT_TYPE_MESSAGE);
    }

    /**
     * Show call message and vibrate while not receive sendCallEnd command
     * @param msg - mesage to show
     */
    public void sendCall(String msg){
        sendAlert(msg, Constants.ALERT_TYPE_CALL);
    }

    /**
     * End vibration and call event started after sendCall command
     */
    public void sendCallEnd() {
        ArrayList<Byte> datas = new ArrayList<>();
        datas.add((byte) 0);
        writePacket(Utils.getDataByte(true, Utils.form_Header(4, 1), datas));
    }

    /**
     * Send alert to device. Device shows your message and icon (call for type 1, msg for type 2)
     * @param msg - Text message
     * @param type - Type of alert. 1 - Call type, 2 - Message type
     */
    public void sendAlert(String msg, int type){
        if( msg == null )
            return ;
        ArrayList<Byte> datas = new ArrayList<>();
        if(Communication.apiVersion == 2)
        {
            datas.add((byte) type);
            datas.add((byte) -1);

            byte[] buffer = new byte[0];

            try {
                if(NotificationMonitor.settingsKeepForeign) {
                    buffer = msg.getBytes("utf-8");
                } else {
                    buffer = msg.replaceAll("[^\u0020-\u0079]", "#").getBytes("utf-8");
                }
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            for(byte b : buffer)
                datas.add(b);
        }
        else
        {
            datas.add((byte) type);
            int i = 0;
            if(msg.length() < 6)
            {
                while(msg.length() < 6)
                    msg += " ";
            }
            else if(msg.length() >= 6)
            {
                msg = msg.substring(0, 6);
            }

            while(i < msg.length())
            {
                //            if (msg.charAt(i) < '@' || (msg.charAt(i) < '\u0080' && msg.charAt(i) > '`')) {
                //                char e = msg.charAt(i);
                //                datas.add(Byte.valueOf((byte) 0));
                //                for (byte valueOf : PebbleBitmap.fromString(String.valueOf(e), 8, 1).data) {
                //                    datas.add(Byte.valueOf(valueOf));
                //                }
                //            } else {
                char c = msg.charAt(i);
                datas.add((byte) 1);
                for(byte valueOf2 : PebbleBitmap.fromString(String.valueOf(c), 16, 1).data)
                {
                    datas.add(valueOf2);
                }
                //            }
                i++;
            }
        }
        byte[] data = Utils.getDataByte(true, Utils.form_Header(3, 1), datas);
        ArrayList<Communication.WriteDataTask> tasks = new ArrayList<>();
        for (int i = 0; i < data.length; i += 20) {
            byte[] writeData;
            if (i + 20 > data.length) {
                writeData = Arrays.copyOfRange(data, i, data.length);
            } else {
                writeData = Arrays.copyOfRange(data, i, i + 20);
            }
            tasks.add(new Communication.WriteDataTask(UUID.fromString(Constants.BAND_CHARACTERISTIC_NEW_WRITE), writeData));
        }
        comm.WriteDataPacket(tasks);
    }

    private byte[] receiveBuffer;
    private int receiveBufferLength = 0;
    private boolean isDataOver = true;

    public void parserAPIv1(BluetoothGattCharacteristic chr){
        String uuid = chr.getUuid().toString();
        Log.i(TAG, "Parse APIv1 data. UUID:" + uuid);
        if (Constants.BAND_CHARACTERISTIC_NEW_NOTIFY.equals(uuid) || Constants.BAND_CHARACTERISTIC_NEW_INDICATE.equals(uuid)) {
            byte[] data = chr.getValue();
            if (data != null && data.length != 0) {
                if (this.isDataOver) {
                    if (data[0] == 34 || (Communication.apiVersion == 2 && data[0] == 35)) {
                        this.receiveBufferLength = data[3];
                        //Log.i(TAG, "Received length --->" + this.receiveBufferLength);
                        //Log.i(TAG, "Received data --->" + Utils.bytesToString(data));
                    } else {
                        return;
                    }
                }
                this.receiveBuffer = Utils.concat(this.receiveBuffer, data);
                if (this.receiveBuffer.length - 4 >= this.receiveBufferLength) {
                    this.isDataOver = true;
                    //Log.i(TAG, "Received length--->" + (this.receiveBuffer.length - 4));
                    //Log.i(TAG, "Received data--->" + Utils.bytesToString(this.receiveBuffer));
                    // Data ready for parse
                    if (this.receiveBuffer.length >= 3) {
                        Intent intent;
                        switch (this.receiveBuffer[2]){
                            case Constants.APIv1_DATA_DEVICE_INFO:
                                DeviceInfo info = DeviceInfo.fromData(this.receiveBuffer);
                                deviceInfo = info;

                                SharedPreferences.Editor ed = App.sPref.edit();
                                ed.putString("device_model", info.getModel());
                                ed.putString("device_sw", info.getSwversion());
                                ed.apply();

                                Log.d(TAG, "DEVICE_INFO: " + info.toString());
                                intent = new Intent(BroadcastConstants.ACTION_DEVICE_INFO);
                                intent.putExtra("data", DeviceInfo.fromData(this.receiveBuffer));
                                App.mContext.sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_DEVICE_POWER:
                                int power = Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5));
                                Log.d(TAG, "DEVICE_POWER: " + power + "%");
                                intent = new Intent(BroadcastConstants.ACTION_DEVICE_POWER);
                                intent.putExtra("data", power);
                                App.mContext.sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_DEVICE_BLE:
                                int ble = Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5));
                                Log.d(TAG, "DEVICE_BLE: " + (ble > 0 ? "enabled" : "disabled"));
                                intent = new Intent(BroadcastConstants.ACTION_BLE_DATA);
                                intent.putExtra("data", ble);
                                App.mContext.sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_USER_PARAMS:
                                HashMap<String, Integer> userData = new HashMap<>();
                                userData.put("height", Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5)));
                                userData.put("weight", Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 5, 6)));
                                userData.put("gender", Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 6, 7)));
                                userData.put("age", Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 7, 8)));
                                userData.put("goal_low", Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 8, 9)));
                                userData.put("goal_high", Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 9, 10)));
                                Log.d(TAG, "DEVICE_USER_PARAMS: " +
                                        " Height: " + userData.get("height") +
                                        " Weight: " + userData.get("weight") +
                                        " Gender: " + (userData.get("gender") > 0 ? "female" : "male") +
                                        " Age: " + userData.get("age") +
                                        " Goal: " + (userData.get("goal_high") + "." + userData.get("goal_low")));
                                intent = new Intent(BroadcastConstants.ACTION_USER_BODY_DATA);
                                intent.putExtra("data", userData);
                                App.mContext.sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_DEVICE_CONFIG:
                                HashMap<String, Integer> configData = new HashMap<>();
                                configData.put("light", Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5)));
                                configData.put("gesture", Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 5, 6)));
                                configData.put("englishUnits", Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 6, 7)));
                                configData.put("use24hour", Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 7, 8)));
                                configData.put("autoSleep", Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 8, 9)));

                                Log.d(TAG, "DEVICE_DEVICE_CONFIG: " +
                                        " light: " + configData.get("light") +
                                        " gesture: " + configData.get("gesture") +
                                        " englishUnits: " + configData.get("englishUnits") +
                                        " use24hour: " + configData.get("use24hour") +
                                        " autoSleep: " + configData.get("autoSleep"));
                                intent = new Intent(BroadcastConstants.ACTION_DEVICE_CONF_DATA);
                                intent.putExtra("data", configData);
                                App.mContext.sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_DEVICE_DATE:
                                int year = Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5)) + 2000;
                                int month;
                                if(App.sPref.getString("device_model", "i5").contains("+"))
                                    month = Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 5, 6));
                                else
                                    month = Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 5, 6)) + 1;
                                int day = Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 6, 7)) + 1;
                                int hour = Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 7, 8));
                                int minute = Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 8, 9));
                                int second = Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 9, 10));
                                long timestamp = new GregorianCalendar(year, month, day, hour, minute, second).getTimeInMillis();
                                Log.d(TAG, "DEVICE_DATE: " + new Date(timestamp).toString());
                                intent = new Intent(BroadcastConstants.ACTION_DATE_DATA);
                                intent.putExtra("data", timestamp);
                                App.mContext.sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_SUBSCRIBE_FOR_SPORT:
                                Sport dailySport = Sport.fromBytes(this.receiveBuffer, Sport.TYPE_DAILY_A);
                                Log.d(TAG, "DAILY_SPORT: " + dailySport.toString());
                                intent = new Intent(BroadcastConstants.ACTION_SPORT_DATA);
                                intent.putExtra("data", dailySport);
                                App.mContext.sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_LOCAL_SPORT:
                                Sport localSport = Sport.fromBytes(this.receiveBuffer, Sport.TYPE_LOCAL_SPORT);
                                Log.d(TAG, "LOCAL_SPORT: " + localSport.toString());
                                intent = new Intent(BroadcastConstants.ACTION_SPORT_DATA);
                                intent.putExtra("data", localSport);
                                App.mContext.sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_DAILY_SPORT2:
                                Sport dailySport2 = Sport.fromBytes(this.receiveBuffer, Sport.TYPE_DAILY_B);
                                Log.d(TAG, "DAILY_SPORT2: " + dailySport2.toString());
                                intent = new Intent(BroadcastConstants.ACTION_SPORT_DATA);
                                intent.putExtra("data", dailySport2);
                                App.mContext.sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_SELFIE:
                                int code = Utils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5));
                                Log.d(TAG, "SELFIE_DATA: " + Integer.toString(code));
                                intent = new Intent( (code == 1) ? BroadcastConstants.ACTION_SELFIE : BroadcastConstants.ACTION_PLAYPAUSE );
                                intent.putExtra("data", code);
                                App.mContext.sendBroadcast(intent);
                            default:
                                String buff = "";
                                Log.i(TAG, "Receiver unknown APIv1 command");
                                for (Byte b : this.receiveBuffer )
                                    buff += b + " ";
                                Log.i(TAG, "Buffer: " + buff);
                        }
                    }
                    // Clear buffer for new data
                    this.receiveBuffer = new byte[0];
                    return;
                }
                this.isDataOver = false;
            }
        }
    }

    public void parserAPIv0(BluetoothGattCharacteristic chr){
        String uuid = chr.getUuid().toString();
        Log.i(TAG, "Parse APIv0 data. UUID:" + uuid);
        Intent intent;
        Sport sp;
        switch (uuid){
            case Constants.BAND_CHARACTERISTIC_SPORT:
                sp = Sport.fromCharacteristic(chr, false);
                intent = new Intent(BroadcastConstants.ACTION_SPORT_DATA);
                intent.putExtra("data", sp);
                App.mContext.sendBroadcast(intent);
                break;
            case Constants.BAND_CHARACTERISTIC_DAILY:
                sp = Sport.fromCharacteristic(chr, true);
                intent = new Intent(BroadcastConstants.ACTION_DAILY_DATA);
                intent.putExtra("data", sp);
                App.mContext.sendBroadcast(intent);
                break;
            case Constants.BAND_CHARACTERISTIC_DATE:
                long time = Utils.parseDateCharacteristic(chr);
                intent = new Intent(BroadcastConstants.ACTION_DATE_DATA);
                intent.putExtra("data", time);
                App.mContext.sendBroadcast(intent);
                break;
            case Constants.BAND_CHARACTERISTIC_SEDENTARY:
                intent = new Intent(BroadcastConstants.ACTION_SEDENTARY_DATA);
                intent.putExtra("data", chr.getValue());
                App.mContext.sendBroadcast(intent);
                break;
            case Constants.BAND_CHARACTERISTIC_ALARM:
                intent = new Intent(BroadcastConstants.ACTION_ALARM_DATA);
                intent.putExtra("data", chr.getValue());
                App.mContext.sendBroadcast(intent);
                break;
            case Constants.BAND_CHARACTERISTIC_PAIR:
                intent = new Intent(BroadcastConstants.ACTION_PAIR_DATA);
                intent.putExtra("data", chr.getValue());
                App.mContext.sendBroadcast(intent);
                break;
        }
    }

    /**
     * Send data to device
     * @param data
     */
    public void writePacket(byte[] data){
        comm.WriteDataPacket(new Communication.WriteDataTask(UUID.fromString(Constants.BAND_CHARACTERISTIC_NEW_WRITE), data));
    }
}
