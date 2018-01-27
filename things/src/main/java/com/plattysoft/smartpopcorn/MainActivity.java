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

public class MainActivity extends Activity implements CommandListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PRESENCE_GPIO = "BCM16";
    private static final String RELAY_GPIO = "BCM12";
    private static final long LIGHT_TIMEOUT = 3*3600000; // 3 Minutes as a temporary concept

    private Gpio mRelayGpio;
    private Timer mTimer;
    private ApiServer mApiServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManagerService pms = new PeripheralManagerService();

        // Open the Relay
        try {
            mRelayGpio = pms.openGpio(RELAY_GPIO);
            mRelayGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mApiServer = new ApiServer(this);
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
        if (mRelayGpio != null) {
            try {
                mRelayGpio.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                mRelayGpio = null;
            }
        }
        mApiServer.stop();
    }

    @Override
    public void onCommandReceived(PopcornCommand enumCommand) {
        switch (enumCommand) {
            case START:
                try {
                    // If the relay is off
                    if (! mRelayGpio.getValue()) {
                        cancelTimer();
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                onTimePassed();
                            }
                        }, LIGHT_TIMEOUT);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case CANCEL:
                cancelTimer();
                try {
                    mRelayGpio.setValue(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
    }
}
