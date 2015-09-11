package ru.wilix.device.geekbracelet.i5;

/**
 * Created by Dmitry on 29.08.2015.
 */
public class Constants {
    public static final String BAND_SERVICE_BATTERY = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String BAND_SERVICE_INFO = "00001800-0000-1000-8000-00805f9b34fb";
    public static final String BAND_SERVICE_MAIN = "f000ff00-0451-4000-b000-000000000000";
    public static final String BAND_SERVICE_MAIN_NEW = "0000ff20-0000-1000-8000-00805f9b34fb";
    public static final String BAND_SERVICE_PHONE_ALERT = "f000ff10-0451-4000-b000-000000000000";
    public static final String UPDATE_SERVICE_MAIN = "00001530-0000-1000-8000-00805f9b34fb";
    public static final String HZ = "00001801-0000-1000-8000-00805f9b34fb";

    public static final String BAND_CHARACTERISTIC_ALARM = "f000ff01-0451-4000-b000-000000000000";
    public static final String BAND_CHARACTERISTIC_BATTERY = "00002a19-0000-1000-8000-00805f9b34fb";
    public static final String BAND_CHARACTERISTIC_DAILY = "f000ff07-0451-4000-b000-000000000000";
    public static final String BAND_CHARACTERISTIC_DATE = "f000ff05-0451-4000-b000-000000000000";
    public static final String BAND_CHARACTERISTIC_INFO = "00002a00-0000-1000-8000-00805f9b34fb";
    public static final String BAND_CHARACTERISTIC_LED = "f000ff04-0451-4000-b000-000000000000";
    public static final String BAND_CHARACTERISTIC_NEW_NOTIFY = "0000ff22-0000-1000-8000-00805f9b34fb";
    public static final String BAND_CHARACTERISTIC_NEW_WRITE = "0000ff21-0000-1000-8000-00805f9b34fb";
    public static final String BAND_CHARACTERISTIC_PAIR = "f000ff06-0451-4000-b000-000000000000";
    public static final String BAND_CHARACTERISTIC_PHONE_ALERT = "f000ff11-0451-4000-b000-000000000000";
    public static final String BAND_CHARACTERISTIC_POWER_SAVING = "f000ff09-0451-4000-b000-000000000000";
    public static final String BAND_CHARACTERISTIC_SCALE_DATA = "0000fff4-0000-1000-8000-00805f9b34fb";
    public static final String BAND_CHARACTERISTIC_SCALE_SERVICE_MAIN = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String BAND_CHARACTERISTIC_SCALE_SETTING = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static final String BAND_CHARACTERISTIC_SEDENTARY = "f000ff08-0451-4000-b000-000000000000";
    public static final String BAND_CHARACTERISTIC_SPORT = "f000ff03-0451-4000-b000-000000000000";
    public static final String BAND_CHARACTERISTIC_USER = "f000ff02-0451-4000-b000-000000000000";
    public static final String CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    public static final String CHR1 = "00002a01-0000-1000-8000-00805f9b34fb";
    public static final String CHR2 = "00002a02-0000-1000-8000-00805f9b34fb";
    public static final String CHR3 = "00002a03-0000-1000-8000-00805f9b34fb";
    public static final String CHR4 = "00002a04-0000-1000-8000-00805f9b34fb";
    public static final String CHR5 = "00002a05-0000-1000-8000-00805f9b34fb";

    public static final int ALERT_TYPE_CALL = 1;
    public static final int ALERT_TYPE_MESSAGE = 2;
    public static final int ALERT_TYPE_CLOUD = 3;
    public static final int ALERT_TYPE_ERROR = 4;

    /**
     * // HEADER GROUPS //
     * DEVICE = 0
     * CONFIG = 1
     * DATALOG = 2
     * MSG = 3
     * PHONE_MSG = 4 ???
     *
     * // CONFIG ///
     * CMD_ID_CONFIG_GET_AC = 5
     * CMD_ID_CONFIG_GET_BLE = 3
     * CMD_ID_CONFIG_GET_HW_OPTION = 9
     * CMD_ID_CONFIG_GET_NMA = 7
     * CMD_ID_CONFIG_GET_TIME = 1
     *
     * CMD_ID_CONFIG_SET_AC = 4
     * CMD_ID_CONFIG_SET_BLE = 2
     * CMD_ID_CONFIG_SET_HW_OPTION = 8
     * CMD_ID_CONFIG_SET_NMA = 6
     * CMD_ID_CONFIG_SET_TIME = 0
     *
     * // DATALOG //
     * CMD_ID_DATALOG_CLEAR_ALL = 2
     * CMD_ID_DATALOG_GET_BODY_PARAM = 1
     * CMD_ID_DATALOG_SET_BODY_PARAM = 0
     *
     * CMD_ID_DATALOG_GET_CUR_DAY_DATA = 7
     *
     * CMD_ID_DATALOG_START_GET_DAY_DATA = 3
     * CMD_ID_DATALOG_START_GET_MINUTE_DATA = 5
     *
     * CMD_ID_DATALOG_STOP_GET_DAY_DATA = 4
     * CMD_ID_DATALOG_STOP_GET_MINUTE_DATA = 6
     *
     * // DEVICE //
     * CMD_ID_DEVICE_GET_BATTERY = 1
     * CMD_ID_DEVICE_GET_INFORMATION = 0
     * CMD_ID_DEVICE_RESE = 2
     * CMD_ID_DEVICE_UPDATE = 3
     *
     * // MSG //
     * CMD_ID_MSG_DOWNLOAD = 1
     * CMD_ID_MSG_MULTI_DOWNLOAD_CONTINUE = 3
     * CMD_ID_MSG_MULTI_DOWNLOAD_END = 4
     * CMD_ID_MSG_MULTI_DOWNLOAD_START = 2
     * CMD_ID_MSG_UPLOAD = 0
     *
     * // PHONE_MSG //
     * CMD_ID_PHONE_ALERT = 1
     * CMD_ID_PHONE_PRESSKEY = 0
     */

    public static final byte APIv1_DATA_DEVICE_INFO = 0;
    public static final byte APIv1_DATA_DEVICE_POWER = 1;
    public static final byte APIv1_SET_DEVICE_DATE = 16;
    public static final byte APIv1_DATA_DEVICE_DATE = 17;
    public static final byte APIv1_SET_DEVICE_BLE = 18;
    public static final byte APIv1_DATA_DEVICE_BLE = 19;
    public static final byte APIv1_SET_DEVICE_CLOCK_ALARM = 20;
    public static final byte APIv1_SET_DEVICE_CONFIG = 24;
    public static final byte APIv1_DATA_DEVICE_CONFIG = 25;
    public static final byte APIv1_SET_USER_PARAMS = 32;
    public static final byte APIv1_DATA_USER_PARAMS = 33;
    public static final byte APIv1_DATA_SUBSCRIBE_FOR_SPORT = 35;
    public static final byte APIv1_DATA_LOCAL_SPORT = 37;
    public static final byte APIv1_DATA_DAILY_SPORT2 = 39;
    public static final byte APIv1_SET_ALERT_DATA = 49;
    public static final byte APIv1_SET_ALERT_DATA2 = 50;
    public static final byte APIv1_SET_ALERT_DATA3 = 51;
    public static final byte APIv1_SET_ALERT_DATA4 = 52;
    public static final byte APIv1_DATA_SELFIE = 64;
    public static final byte APIv1_SET_END_CALL = 65;
}
