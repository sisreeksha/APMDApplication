package com.tenpitech.apmd.apmdsdk.helper;

import java.util.ArrayList;

public class ApplicationStringHolder {

    public static String SENSOR_TYPE_PM_VALUE = "01";
    public static String SENSOR_TYPE_HUMIDITY_VALUE = "02";
    public static String SENSOR_TYPE_TEMPERATURE_VALUE = "03";
    public static String SENSOR_TYPE_CO_VALUE = "04";
    public static String SENSOR_TYPE_CO2_VALUE = "05";
    public static String SENSOR_TYPE_NO2_VALUE = "06";
    public static String SENSOR_TYPE_SO2_VALUE = "07";
    public static String SENSOR_TYPE_O3_VALUE = "08";


    public static String SENSOR_TYPE_PM_FIRST_NAME = "PM2.5";
    public static String SENSOR_TYPE_PM_SECOND_NAME = "PM10";
    public static String SENSOR_TYPE_HUMIDITY_NAME = "HUMIDITY";
    public static String SENSOR_TYPE_TEMPERATURE_NAME = "TEMPERATURE";
    public static String SENSOR_TYPE_CO_NAME = "CO";
    public static String SENSOR_TYPE_CO2_NAME = "CO2";
    public static String SENSOR_TYPE_NO2_NAME = "NO2";
    public static String SENSOR_TYPE_SO2_NAME = "SO2";
    public static String SENSOR_TYPE_O3_NAME = "O3";

    public static String SENSOR_UNIT_UG_M3 = "ug/m3";
    public static String SENSOR_UNIT_PERCENTAGE = "%";
    public static String SENSOR_UNIT_DC = "dC";
    public static String SENSOR_UNIT_PPM = "ppm";

    public static final int ACCESS_FINE_LOCATION = 0;

    public static String SERVER_CALL_STRING = "http://115.119.200.83:3080/app/dataPusher";
    //public static String SERVER_CALL_STRING = "https://datastorage.restlet.net/v1/realtimedatabases/";
    public static String DELAYED_SERVER_CALL_STRING="http://115.119.200.83:3080/app/dataPusher";
    //public static String DELAYED_SERVER_CALL_STRING="https://datastorage.restlet.net/v1/delayeddatabases/";

    public static int VAL=0;
    public static int MAX_STORAGE=10000;

    public static long SENSOR_VALUE_SEND_CHECK_TIME_PERIOD =1000;

    public static long SENSOR_BEEP_CHECK_TIME_PERIOD =30000;
    public static long SENSOR_BEEP_CHECK_TIME_DELAY =30000;

    private static long SENSOR_TYPE_COMMON_TIME_GAP = 60000;
    public static long SENSOR_TYPE_PM_FIRST_TIME_GAP = SENSOR_TYPE_COMMON_TIME_GAP;
    public static long SENSOR_TYPE_PM_SECOND_TIME_GAP = SENSOR_TYPE_COMMON_TIME_GAP;
    public static long SENSOR_TYPE_HUMIDITY_TIME_GAP = SENSOR_TYPE_COMMON_TIME_GAP;
    public static long SENSOR_TYPE_TEMPERATURE_TIME_GAP = SENSOR_TYPE_COMMON_TIME_GAP;
    public static long SENSOR_TYPE_CO_TIME_GAP = SENSOR_TYPE_COMMON_TIME_GAP;
    public static long SENSOR_TYPE_CO2_TIME_GAP = SENSOR_TYPE_COMMON_TIME_GAP;
    public static long SENSOR_TYPE_NO2_TIME_GAP = SENSOR_TYPE_COMMON_TIME_GAP;
    public static long SENSOR_TYPE_SO2_TIME_GAP = SENSOR_TYPE_COMMON_TIME_GAP;
    public static long SENSOR_TYPE_O3_TIME_GAP = SENSOR_TYPE_COMMON_TIME_GAP;

}
