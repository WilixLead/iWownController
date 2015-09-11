/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.wilix.device.geekbracelet;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import ru.wilix.device.geekbracelet.i5.Device;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BLEService extends Service {
    private final static String TAG = BLEService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    public static ArrayList<BluetoothGattService> services = new ArrayList();
    public static ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList();
    public static BLEService self;

    private Device device;
    private BluetoothGatt mBluetoothGatt;

    public final static String ACTION_GATT_CONNECTED = "ru.wilix.device.geekbracelet.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ru.wilix.device.geekbracelet.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ru.wilix.device.geekbracelet.ACTION_GATT_SERVICES_DISCOVERED";

    public int mConnectionState = STATE_DISCONNECTED;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public void onCreate() {
        super.onCreate();
        self = this;

        this.device = new Device(this);
        if (!initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            return;
        }
        this.connect(App.sPref.getString("DEVICE_ADDR", ""), true);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public static BLEService getSelf(){
        return self;
    }

    public Device getDevice(){
        return this.device;
    }

    public BluetoothGatt getmBluetoothGatt(){
        return this.mBluetoothGatt;
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address, boolean forseConnect) {
        if (mBluetoothAdapter == null || address == null ||
                (App.sPref.getString("DEVICE_ADDR", "").length() <= 0) && address.length() <= 0 ) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if ( !forseConnect && App.sPref.getString("DEVICE_ADDR", "").length() > 0 &&
                address.equals(App.sPref.getString("DEVICE_ADDR", "")) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            //this.autoReconnectOnTimeout();
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                Log.d(TAG, "Reconnecting");
                return true;
            } else {
                Log.d(TAG, "Connection problem");
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device
        mBluetoothGatt = device.connectGatt(this, true, this.device.comm);
        Log.d(TAG, "Trying to create a new connection.");
        mConnectionState = STATE_CONNECTING;
        //this.autoReconnectOnTimeout();
        this.checkConnection();
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        try {
            mBluetoothGatt.disconnect();
        }catch (Exception e){

        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null)
            return;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        try {
            for (BluetoothGattCharacteristic characteristic : BLEService.characteristics) {
                if (characteristic.getUuid().equals(uuid)) {
                    return characteristic;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isBluetoothAvailable(){
        if (!App.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            return false;

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) App.mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        // Checks if Bluetooth is supported on the device.
        if (bluetoothManager.getAdapter() == null)
            return false;
        return true;
    }

    public void autoReconnectOnTimeout(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    if ( BLEService.getSelf().mConnectionState != STATE_CONNECTED ) {
                        if ( BLEService.getSelf().mConnectionState == STATE_CONNECTING )
                            BLEService.getSelf().disconnect();
                        BLEService.getSelf().connect(App.sPref.getString("DEVICE_ADDR", ""), true);
                    }
                }catch (Exception e){}
            }
        }).start();
    }

    boolean alreadyChecking = false;

    public void checkConnection(){
        alreadyChecking = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if( alreadyChecking )
                        return;
                    alreadyChecking = true;
                    Thread.sleep(120000);
                    if( App.sPref.getString("DEVICE_ADDR", "").length() > 0 ) {
                        if( BLEService.getSelf() != null && BLEService.getSelf().getDevice() != null ){
                            // FIXME bad practice use comm object
                            if (BLEService.getSelf().getDevice().comm.lastDataReceived - (new Date().getTime()) >= 300000 ) {
                                BLEService.getSelf().disconnect();
                                BLEService.getSelf().connect(App.sPref.getString("DEVICE_ADDR", ""), true);
                                checkConnection();
                                return;
                            }
                            BLEService.getSelf().getDevice().askPower();
                        }
                    }
                    checkConnection();
                }catch (Exception e){}
            }
        }).start();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
