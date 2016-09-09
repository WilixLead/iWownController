package ru.wilix.device.geekbracelet.i5;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import ru.wilix.device.geekbracelet.App;
import ru.wilix.device.geekbracelet.BLEService;
import ru.wilix.device.geekbracelet.BroadcastConstants;

/**
 * Created by Dmitry on 29.08.2015.
 */
public class Communication extends BluetoothGattCallback {
    private static final String TAG = "i5Communication";
    private static BLEService bleService;
    public static int apiVersion = 1;
    public static long lastDataReceived = 0;

    private Device device;

    public Communication(BLEService bleService, Device device){
        this.bleService = bleService;
        this.device = device;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Intent intent;
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            BLEService.getSelf().mConnectionState = BLEService.STATE_CONNECTED;
            intent = new Intent(BroadcastConstants.ACTION_GATT_CONNECTED);
            App.mContext.sendBroadcast(intent);

            Log.i(TAG, "Connected to GATT server.");
            Log.i(TAG, "Attempting to start service discovery.");
            BLEService.getSelf().getmBluetoothGatt().discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            intent = new Intent(BroadcastConstants.ACTION_GATT_DISCONNECTED);
            App.mContext.sendBroadcast(intent);

            Log.i(TAG, "Disconnected from GATT server.");
            BLEService.getSelf().mConnectionState = BLEService.STATE_DISCONNECTED;
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, "onServicesDiscovered received: " + status);
            for (BluetoothGattService gattService : gatt.getServices()) {
                BLEService.services.add(gattService);
                for(BluetoothGattCharacteristic chr : gattService.getCharacteristics()){
                    BLEService.characteristics.add(chr);
                }
            }
            BluetoothGattCharacteristic notify_chr =
                bleService.getCharacteristic(UUID.fromString(Constants.BAND_CHARACTERISTIC_NEW_NOTIFY));

            //If FW version 2.x.x.x
            if(notify_chr == null)
            {
                notify_chr = bleService.getCharacteristic(UUID.fromString(Constants.BAND_CHARACTERISTIC_NEW_INDICATE));
                apiVersion = 2;
            }

            gatt.setCharacteristicNotification(notify_chr, true);

            // Set Descriptor for receive notices
            BluetoothGattDescriptor descriptor = notify_chr.getDescriptor(
                    UUID.fromString(Constants.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID));

            if ((notify_chr.getProperties() & 32) != 0)
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            else if ((notify_chr.getProperties() & 16) != 0)
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            else
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

            if( gatt.writeDescriptor(descriptor) )
                Log.d(TAG, "Descriptor is set");
            else
                Log.e(TAG, "Can't set descriptor!");

            Intent intent = new Intent(BroadcastConstants.ACTION_GATT_SERVICES_DISCOVERED);
            App.mContext.sendBroadcast(intent);
        } else {
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.i(TAG, "CHR Read");
        if (status == BluetoothGatt.GATT_SUCCESS) {
            parseCharacteristic(characteristic);
            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "CHR Changed");
        parseCharacteristic(characteristic);
        //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
    }

    public void parseCharacteristic(BluetoothGattCharacteristic chr){
        lastDataReceived = new Date().getTime();

        if( apiVersion >= 1 )
            this.device.parserAPIv1(chr);
        else
            this.device.parserAPIv0(chr);
    }

    public void WriteDataPacket(WriteDataTask task){
        ArrayList<WriteDataTask> tasks = new ArrayList<>();
        tasks.add(task);
        WriteDataPacket(tasks);
    }

    public void WriteDataPacket(ArrayList<WriteDataTask> tasks){
        final ArrayList<WriteDataTask> ptasks = tasks;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(WriteDataTask task : ptasks) {
                        Thread.sleep(240);
                        task.run();
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static class WriteDataTask implements Runnable{
        private byte[] data;
        private UUID uuid;

        /**
         * Create write task for packet manager
         * @param uuid - Write to characteristic
         * @param data - data for write
         */
        public WriteDataTask(UUID uuid, byte[] data){
            this.data = data;
            this.uuid = uuid;
        }

        @Override
        public void run() {
            try {
                BluetoothGattCharacteristic characteristic = bleService.getCharacteristic(uuid);
                if (characteristic != null && BLEService.getSelf().getmBluetoothGatt() != null) {
                    characteristic.setValue(data);
                    BLEService.getSelf().getmBluetoothGatt().writeCharacteristic(characteristic);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
