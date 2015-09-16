package ru.wilix.device.geekbracelet.model;

import android.content.SharedPreferences;

import ru.wilix.device.geekbracelet.App;

/**
 * Created by Aloyan Dmitry on 16.09.2015
 */
public class AppNotification {
    public static Integer canNotice(String packageName){
        if( App.sPref.contains("appnotif_" + packageName) )
            return App.sPref.getInt("appnotif_" + packageName, 0);
        return 0;
    }

    public static void enableApp(String packageName, Integer type){
        SharedPreferences.Editor ed = App.sPref.edit();
        ed.putInt("appnotif_" + packageName, type);
        ed.apply();
    }

    public static void disableApp(String packageName){
        if( !App.sPref.contains("appnotif_" + packageName) )
            return;
        SharedPreferences.Editor ed = App.sPref.edit();
        ed.remove("appnotif_" + packageName);
        ed.apply();
    }
}
