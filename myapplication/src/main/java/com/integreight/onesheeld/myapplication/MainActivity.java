package com.integreight.onesheeld.myapplication;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.integreight.onesheeld.sdk.OneSheeldConnectionCallback;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.OneSheeldError;
import com.integreight.onesheeld.sdk.OneSheeldErrorCallback;
import com.integreight.onesheeld.sdk.OneSheeldScanningCallback;
import com.integreight.onesheeld.sdk.OneSheeldSdk;

import java.util.List;


public class MainActivity extends ActionBarActivity {
    TextView textView;
    Button button;

    public void onClick(View v) {
        OneSheeldSdk.getManager().setScanningTimeOut(20);
        OneSheeldSdk.getManager().scan();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.hello_world);
        button = (Button) findViewById(R.id.button);
        OneSheeldSdk.init(this);
        OneSheeldSdk.getManager().setRetryCount(4);
//        OneSheeldSdk.getManager().disableDefaultConnectingRetries();

        OneSheeldSdk.getManager().addCallbacks(new OneSheeldScanningCallback() {
            @Override
            public void onStartScan() {
                Log.d("MainActivity", "Scanning Started");
            }

            @Override
            public void onDeviceFind(OneSheeldDevice device) {
                Log.d("MainActivity", "Device Found: " + device.getName());
            }

            @Override
            public void onFinishScan(List<OneSheeldDevice> foundDevices) {
                Log.d("MainActivity", "Finished Scanning: " + foundDevices.size() + " device(s) found");
                for (OneSheeldDevice device : foundDevices) {
                    OneSheeldSdk.getManager().connect(device);
                }
            }
        }, new OneSheeldConnectionCallback() {
            @Override
            public void onConnect(OneSheeldDevice device) {
                Log.d("MainActivity", "Connected To: " + device.getName());
            }

            @Override
            public void onDisconnect(OneSheeldDevice device) {
                Log.d("MainActivity", "Disconnected From: " + device.getName());
            }

            @Override
            public void onConnectionRetry(OneSheeldDevice device, int retryCount) {
                Log.d("MainActivity", "Retried Connection To: " + device.getName()+", Retry #"+retryCount);
//                OneSheeldSdk.getManager().cancelConnecting();
            }
        }, new OneSheeldErrorCallback() {
            @Override
            public void onError(OneSheeldDevice device, OneSheeldError error) {
                Log.d("MainActivity", "Error: " + error.toString() + (device != null ? " in " + device.getName() : ""));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        OneSheeldSdk.getManager().cancelScanning();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
