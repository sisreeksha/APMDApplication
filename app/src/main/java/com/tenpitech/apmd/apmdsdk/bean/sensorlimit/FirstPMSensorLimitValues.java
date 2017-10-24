package com.tenpitech.apmd.apmdsdk.bean.sensorlimit;

public class FirstPMSensorLimitValues implements SensorLimitValues {

    private float GOOD_LOW_VALUE = 0;

    @Override
    public float get_good_low_value() {
        return GOOD_LOW_VALUE;
    }

    private float GOOD_HIGH_VALUE = 50;

    @Override
    public float get_good_high_value() {
        return GOOD_HIGH_VALUE;
    }

    private float SATISFACTORY_LOW_VALUE = 51;

    @Override
    public float get_satisfactory_low_value() {
        return SATISFACTORY_LOW_VALUE;
    }

    private float SATISFACTORY_HIGH_VALUE = 100;

    @Override
    public float get_satisfactory_high_value() {
        return SATISFACTORY_HIGH_VALUE;
    }

    private float MODERATE_LOW_VALUE = 101;

    @Override
    public float get_moderate_low_value() {
        return MODERATE_LOW_VALUE;
    }

    private float MODERATE_HIGH_VALUE = 250;

    @Override
    public float get_moderate_high_value() {
        return MODERATE_HIGH_VALUE;
    }

    private float POOR_LOW_VALUE = 251;

    @Override
    public float get_poor_low_value() {
        return POOR_LOW_VALUE;
    }

    private float POOR_HIGH_VALUE = 350;

    @Override
    public float get_poor_high_value() {
        return POOR_HIGH_VALUE;
    }

    private float VERY_POOR_LOW_VALUE = 351;

    @Override
    public float get_very_poor_low_value() {
        return VERY_POOR_LOW_VALUE;
    }

    private float VERY_POOR_HIGH_VALUE = 430;

    @Override
    public float get_very_poor_high_value() {
        return VERY_POOR_HIGH_VALUE;
    }

    private float SEVERE_HIGH_VALUE = 431;

    @Override
    public float get_severe_low_value() {
        return SEVERE_HIGH_VALUE;
    }
}
