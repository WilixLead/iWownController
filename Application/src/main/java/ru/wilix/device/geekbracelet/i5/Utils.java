package ru.wilix.device.geekbracelet.i5;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by Dmitry on 30.08.2015.
 */
public class Utils {
    /**
     * Parse characteristic and return date in seconds
     * @param chr
     * @return
     */
    public static long parseDateCharacteristic(BluetoothGattCharacteristic chr){
        int year = chr.getIntValue(17, 0).intValue() + 2000;
        int month = chr.getIntValue(17, 1).intValue() + 1;
        int day = chr.getIntValue(17, 2).intValue() + 1;
        int hour = chr.getIntValue(17, 3).intValue();
        int minute = chr.getIntValue(17, 4).intValue();
        int second = chr.getIntValue(17, 5).intValue();
        return new GregorianCalendar(year, month, day, hour, minute, second).getTime().getTime() / 1000;
    }

    /**
     * Format header for device packet
     * @param grp - Group of commands
     * @param cmd - Command
     * @return - formated byte
     */
    public static byte form_Header(int grp, int cmd) {
        return (byte) (((((byte) grp) & 15) << 4) | (((byte) cmd) & 15));
    }

    /**
     * Convert incomig data to wristband packet
     * @param prefix - unknow now
     * @param header - still unknow
     * @param datas - ByteArray to create packet
     * @return - byte code for device
     */
    public static byte[] getDataByte(boolean prefix, byte header, ArrayList<Byte> datas) {
        byte[] commonData = new byte[4];
        if (prefix) {
            commonData[0] = (byte) 33;
        } else {
            commonData[0] = (byte) 34;
        }
        commonData[1] = (byte) -1;
        commonData[2] = header;
        if (datas != null) {
            commonData[3] = (byte) datas.size();
            byte[] data = new byte[datas.size()];
            for (int i = 0; i < datas.size(); i++) {
                data[i] = ((Byte) datas.get(i)).byteValue();
            }
            return concat(commonData, data);
        }
        commonData[3] = (byte) 0;
        return commonData;
    }

    public static String ascii2String(byte[] bytes2) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes2.length; i++) {
            if ((char)(bytes2[i]) != '\u0000') {
                sb.append((char)(bytes2[i]));
            }
        }
        return sb.toString();
    }

    public static String bytesToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder(bytes.length);
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(String.format("%02X", new Object[]{Byte.valueOf(bytes[i])}));
        }
        return stringBuilder.toString();
    }

    public static String byteArrayToString(byte[] copyOfRange) {
        StringBuilder sb = new StringBuilder();
        for (byte b : copyOfRange) {
            String i = Integer.toHexString(b & 255);
            if (i.length() == 1) {
                i = new StringBuilder("0").append(i).toString();
            }
            sb.append(i);
        }
        return sb.toString();
    }

    public static int bytesToInt(byte[] bytes) {
        if (bytes.length == 1) {
            return bytes[0] & 255;
        }
        if (bytes.length == 4) {
            return (((bytes[0] & 255) | ((bytes[1] << 8) & 65280)) | ((bytes[2] << 16) & 16711680)) | ((bytes[3] << 24) & -16777216);
        }
        if (bytes.length == 2) {
            return (bytes[0] & 255) | ((bytes[1] << 8) & 65280);
        }
        if (bytes.length == 3) {
            return ((bytes[0] & 255) | ((bytes[1] << 8) & 65280)) | ((bytes[2] << 16) & 16711680);
        }
        return 0;
    }

    /**
     * Concat helper. TODO check other concat functions for replace this
     * @param a - argument A
     * @param b - argument B
     * @return - summ of A and B
    //     */
    public static byte[] concat(byte[] a, byte[] b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        byte[] c = new byte[(a.length + b.length)];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
