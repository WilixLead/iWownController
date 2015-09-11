package ru.wilix.device.geekbracelet.model;

import android.bluetooth.BluetoothGattCharacteristic;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.GregorianCalendar;

import ru.wilix.device.geekbracelet.i5.Utils;

/**
 * Created by Dmitry on 30.08.2015.
 */
public class Sport implements Serializable {
    private int bcc = 0;
    private int minute = 0;
    private int hour = 0;
    private int day = 0;
    private int month = 0;
    private int year = 0;
    private int flag = 0;
    private int steps = 0;
    private float distance = 0;
    private float calorie = 0;
    private int type = 0;

    public static final int TYPE_LOCAL_SPORT = 0;
    public static final int TYPE_DAILY_A = 1;
    public static final int TYPE_DAILY_B = 2;

    public static Sport fromBytes(byte[] data, int type){
        try {
            Sport sport = new Sport();
            if( type != TYPE_LOCAL_SPORT ) {
                sport.setBcc(Utils.bytesToInt(Arrays.copyOfRange(data, 4, 5)));
                sport.setYear(Utils.bytesToInt(Arrays.copyOfRange(data, 7, 8)));
                sport.setMonth(Utils.bytesToInt(Arrays.copyOfRange(data, 6, 7)));
                sport.setDay(Utils.bytesToInt(Arrays.copyOfRange(data, 5, 6)));
                sport.setSteps(Utils.bytesToInt(Arrays.copyOfRange(data, 8, 12)));
                sport.setDistance(((float) Utils.bytesToInt(Arrays.copyOfRange(data, 12, 16))) * 0.1f);
                sport.setCalorie(((float) Utils.bytesToInt(Arrays.copyOfRange(data, 16, 20))) * 0.1f);
                sport.setType(type);
            }else{
                sport.setBcc(Utils.bytesToInt(Arrays.copyOfRange(data, 4, 5)));
                sport.setMinute(Utils.bytesToInt(Arrays.copyOfRange(data, 5, 6)));
                sport.setHour(Utils.bytesToInt(Arrays.copyOfRange(data, 6, 7)));
                sport.setDay(Utils.bytesToInt(Arrays.copyOfRange(data, 7, 8)) + 1);
                sport.setMonth(Utils.bytesToInt(Arrays.copyOfRange(data, 8, 9)) + 1);
                sport.setYear(Utils.bytesToInt(Arrays.copyOfRange(data, 9, 10)) + 2000);
                sport.setFlag(Utils.bytesToInt(Arrays.copyOfRange(data, 10, 11)));
                sport.setSteps(Utils.bytesToInt(Arrays.copyOfRange(data, 11, 13)));
                sport.setDistance(((float) Utils.bytesToInt(Arrays.copyOfRange(data, 13, 15))) * 0.1f);
                sport.setCalorie(((float) Utils.bytesToInt(Arrays.copyOfRange(data, 15, 17))) * 0.1f);
                sport.setType(TYPE_LOCAL_SPORT);
            }
            return sport;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create new Sport model from Characteristic
     * @param chr Characteristic
     * @return
     */
    public static Sport fromCharacteristic(BluetoothGattCharacteristic chr, boolean daily){
        Sport sp = new Sport();
        if( !daily ) {
            sp.setBcc(chr.getIntValue(17, 0).intValue());
            sp.setMinute(chr.getIntValue(17, 1).intValue());
            sp.setHour(chr.getIntValue(17, 2).intValue());
            sp.setDay(chr.getIntValue(17, 3).intValue() + 1);
            sp.setMonth(chr.getIntValue(17, 4).intValue() + 1);
            sp.setYear(chr.getIntValue(17, 5).intValue() + 2000);
            sp.setFlag(chr.getIntValue(17, 6).intValue());
            sp.setSteps(chr.getIntValue(18, 7).intValue());
            sp.setDistance(((float) chr.getIntValue(18, 9).intValue()) * 0.1f);
            sp.setCalorie(((float) chr.getIntValue(17, 11).intValue()) * 0.1f);
            sp.setType(TYPE_LOCAL_SPORT);
        }else{
            if( chr.getValue().length == 12 ){
                sp.setSteps(chr.getIntValue(20, 0).intValue());
                sp.setDistance(((float) chr.getIntValue(20, 4).intValue()) * 0.1f);
                sp.setCalorie(((float) chr.getIntValue(10, 8).intValue()) * 0.1f);
                sp.setType(TYPE_DAILY_A);
            }else{
                sp.setBcc(chr.getIntValue(17, 0).intValue());
                sp.setDay(chr.getIntValue(17, 1).intValue() + 1);
                sp.setMonth(chr.getIntValue(17, 2).intValue() + 1);
                sp.setYear(chr.getIntValue(17, 3).intValue() + 2000);
                sp.setSteps(chr.getIntValue(36, 4).intValue());
                sp.setDistance(((float) chr.getIntValue(20, 8).intValue()) * 0.1f);
                sp.setCalorie(((float) chr.getIntValue(20, 12).intValue()) * 0.1f);
                sp.setType(TYPE_DAILY_B);
            }
        }
        return sp;
    }

    /**
     * Return timestamp for this sport point
     * @return
     */
    public long getTimestamp(){
        if( this.year == 0 )
            return new GregorianCalendar().getTimeInMillis();
        return new GregorianCalendar(this.year, this.month, this.day, this.hour, this.minute).getTimeInMillis();
    }

    public String toString(){
        String buff = "";
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ( (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL )
                continue;
            try {
                buff += field.getName() + ": " + field.get(this) + " ";
            } catch (Exception e) {
            }
        }
        return buff;
    }

    public void setBcc(int value){
        this.bcc = value;
    }

    public int getBcc(){
        return this.bcc;
    }

    public void setMinute(int value){
        this.minute = value;
    }

    public int getMinute(){
        return this.minute;
    }

    public void setHour(int value){
        this.hour = value;
    }

    public int getHour(){
        return this.hour;
    }

    public void setDay(int value){
        this.day = value;
    }

    public int getDay(){
        return this.day;
    }

    public void setMonth(int value){
        this.month = value;
    }

    public int getMonth(){
        return this.month;
    }

    public void setYear(int value){
        this.year = value;
    }

    public int getYear(){
        return this.year;
    }

    public void setFlag(int value){
        this.flag = value;
    }

    public int getFlag(){
        return this.flag;
    }

    public void setSteps(int value){
        this.steps = value;
    }

    public int getSteps(){
        return this.steps;
    }

    public void setDistance(float value){
        this.distance = value;
    }

    public float getDistance(){
        return this.distance;
    }

    public void setCalorie(float value){
        this.calorie = value;
    }

    public float getCalorie(){
        return this.calorie;
    }

    public void setType(int value){
        this.type = value;
    }

    public int getType(){
        return this.type;
    }
}