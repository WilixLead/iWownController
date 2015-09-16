package ru.wilix.device.geekbracelet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import ru.wilix.device.geekbracelet.App;
import ru.wilix.device.geekbracelet.BLEService;
import ru.wilix.device.geekbracelet.BroadcastConstants;
import ru.wilix.device.geekbracelet.GoogleFitConnector;
import ru.wilix.device.geekbracelet.i5.Constants;
import ru.wilix.device.geekbracelet.model.Sport;

/**
 * Created by Aloyan Dmitry on 30.08.2015
 */
public class Receiver extends BroadcastReceiver {
    private static boolean isIncomingCallMuted = false;
    private static boolean hasIncomingCall = false;
    private static int beforeAjustVolume = 0;

    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();
        if( action != "android.intent.action.BOOT_COMPLETED" &&
                (BLEService.getSelf() == null || BLEService.getSelf().getDevice()== null) )
            return;

        switch (action){
            case "android.intent.action.BOOT_COMPLETED":
                // App should init
                break;
            case BroadcastConstants.ACTION_NEW_NOTIFICATION_RECEIVED:
                NotificationMonitor.Notif nf = (NotificationMonitor.Notif)intent.getSerializableExtra("data");
                BLEService.getSelf().getDevice().sendAlert(nf.fromName, nf.getDeviceNoticeType());
                break;
            case BroadcastConstants.ACTION_INCOMING_CALL:
                hasIncomingCall = true;
                stopLocatior(context); // If we receive call and locate the phone, need end locator

                if( App.sPref.getBoolean("cbx_action_mute_onclick", false) )
                    BLEService.getSelf().getDevice().setSelfieMode(true); // Set one click mode for mute

                String callid = intent.getStringExtra("data");
                BLEService.getSelf().getDevice().sendCall(callid); // Show call in device
                break;
            case BroadcastConstants.ACTION_END_CALL:
                hasIncomingCall = false;
                BLEService.getSelf().getDevice().sendCallEnd();
                if( isIncomingCallMuted ) { // If we mute call, we need restore it
                    ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    BLEService.getSelf().getDevice().setSelfieMode(false);
                }
                break;
            case BroadcastConstants.ACTION_SELFIE:
                // If one click and we have ringing, need to mute
                if( hasIncomingCall && App.sPref.getBoolean("cbx_action_mute_onclick", false) ){
                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    beforeAjustVolume = am.getStreamVolume(AudioManager.STREAM_RING);
                    am.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    CallReceiver.rejectCall(context, 1);
                    isIncomingCallMuted = true;
                    BLEService.getSelf().getDevice().setSelfieMode(false);
                    return;
                }
                stopLocatior(context);
                BLEService.getSelf().getDevice().setSelfieMode(false);
                break;
            case BroadcastConstants.ACTION_PLAYPAUSE:
                // Call reject
                if( hasIncomingCall && App.sPref.getBoolean("cbx_action_reject_on_long", false) ){
                    CallReceiver.rejectCall(context, 0);
                    return;
                }

                // Locator service
                if( !hasIncomingCall && App.sPref.getBoolean("cbx_action_locator_on_long", false) ){
                    startLocator(context);
                    BLEService.getSelf().getDevice().setSelfieMode(true);
                    return;
                }
                break;

            case BroadcastConstants.ACTION_SPORT_DATA:
                Sport sport = (Sport)intent.getSerializableExtra("data");
                if( App.sPref.getBoolean("fit_connected", false) )
                    GoogleFitConnector.publish(sport);
                break;

            case BroadcastConstants.ACTION_CONNECT_TO_GFIT:
                if( App.sPref.getBoolean("fit_connected", false) )
                    if( BLEService.getSelf() != null && BLEService.getSelf().getDevice() != null )
                        BLEService.getSelf().getDevice().subscribeForSportUpdates();
                break;
            case BroadcastConstants.ACTION_GATT_CONNECTED:
                if( App.sPref.getBoolean("fit_connected", false) )
                    BLEService.getSelf().getDevice().subscribeForSportUpdates();
                break;
        }
    }

    private static MediaPlayer player = new MediaPlayer();
    private static int lastVolume;

    private void startLocator(Context context){
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            lastVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

            AssetFileDescriptor afd = context.getAssets().openFd("beep_4_times.mp3");
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.setLooping(true);
            player.prepare();
            player.start();
            autoOffLocator();
        }catch (Exception e){}
    }

    private void stopLocatior(Context context){
        try {
            if (player.isPlaying()) {
                player.stop();
                player.release();

                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, lastVolume, 0);
            }
        }catch (Exception e){}
    }

    private void autoOffLocator(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(120000); // 2 min
                    if( player.isPlaying() ) {
                        player.stop();
                        player.release();
                    }
                }catch (Exception e){}
            }
        }).start();
    }
}
