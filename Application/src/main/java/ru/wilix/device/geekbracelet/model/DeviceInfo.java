package ru.wilix.device.geekbracelet.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import ru.wilix.device.geekbracelet.i5.Utils;

/**
 * Created by Dmitry on 30.08.2015.
 */
public class DeviceInfo implements Serializable {
    private String model;
    private int oadmode;
    private String swversion;
    private String bleAddr;
    private int displayWidthFont;

    public static DeviceInfo fromData(byte[] data){
        DeviceInfo info = new DeviceInfo();
        info.setModel(Utils.ascii2String(Arrays.copyOfRange(data, 6, 10)));
        info.setOadmode((data[10] * 255) + data[11]);
        info.setSwversion(data[12] + "." + data[13] + "." + data[14] + "." + data[15]);
        info.setBleAddr(Utils.byteArrayToString(Arrays.copyOfRange(data, 16, 22)));
        if (data.length == 29)
            info.setDisplayWidthFont(Utils.bytesToInt(Arrays.copyOfRange(data, 28, 29)));
        else if (data.length == 28)
            info.setDisplayWidthFont(Utils.bytesToInt(Arrays.copyOfRange(data, 27, 28)));
        return info;
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

    public void setModel(String value){
        this.model = value;
    }

    public String getModel(){
        return this.model;
    }

    public void setOadmode(int value){
        this.oadmode = value;
    }

    public int getOadmode(){
        return this.oadmode;
    }

    public void setSwversion(String value){
        this.swversion = value;
    }

    public String getSwversion(){
        return this.swversion;
    }

    public void setBleAddr(String value){
        this.bleAddr = value;
    }

    public String getBleAddr(){
        return this.bleAddr;
    }

    public void setDisplayWidthFont(int value){
        this.displayWidthFont = value;
    }

    public int getDisplayWidthFont(){
        return this.displayWidthFont;
    }
}
