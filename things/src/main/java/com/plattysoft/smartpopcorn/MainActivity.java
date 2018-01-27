package com.plattysoft.smartpopcorn;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PRESENCE_GPIO = "BCM16";
    private static final String RELAY_GPIO = "BCM12";
    private static final long LIGHT_TIMEOUT = 5000;

    private Gpio mPresenceGpio;
    private Gpio mRelayGpio;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManagerService pms = new PeripheralManagerService();
        // Open the presence sensor
        try {
            mPresenceGpio = pms.openGpio(PRESENCE_GPIO);
            mPresenceGpio.setDirection(Gpio.DIRECTION_IN);
            mPresenceGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mPresenceGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    try {
                        onPresenceStateChanged(gpio.getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Open the Relay
        try {
            mRelayGpio = pms.openGpio(RELAY_GPIO);
            mRelayGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onPresenceStateChanged(boolean value) {
        Log.e("onPresenceStateChanged", " presence: "+value);
        // Turn the ligh on for TIME
        if (value) {
            try {
                mRelayGpio.setValue(true);
                // start a timer to turn it off
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer.purge();
                }
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        onTimePassed();
                    }
                }, LIGHT_TIMEOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void onTimePassed() {
        Log.e("onTimePassed", " onTimePassed: ");
        try {
            mRelayGpio.setValue(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
