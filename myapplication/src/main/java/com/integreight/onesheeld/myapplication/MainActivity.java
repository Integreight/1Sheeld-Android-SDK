package com.integreight.onesheeld.myapplication;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.integreight.onesheeld.sdk.OneSheeldConnectionCallback;
import com.integreight.onesheeld.sdk.OneSheeldDataCallback;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.OneSheeldError;
import com.integreight.onesheeld.sdk.OneSheeldErrorCallback;
import com.integreight.onesheeld.sdk.OneSheeldScanningCallback;
import com.integreight.onesheeld.sdk.OneSheeldSdk;
import com.integreight.onesheeld.sdk.ShieldFrame;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    Button button;
    Spinner spinner;
    private ArrayList<String> devices;
    private ArrayList<OneSheeldDevice> oneSheeldDevices;
    public void onClick(View v) {
        OneSheeldSdk.getManager().setScanningTimeOut(20);
        OneSheeldSdk.getManager().cancelScanning();
        devices.clear();
        oneSheeldDevices.clear();
        devices.add("Nothing Selected");
        OneSheeldSdk.getManager().scan();
    }

    public void onClickBroadcastOn(View v){
        ShieldFrame sf=new ShieldFrame((byte)0x03,(byte)0x01);
        sf.addArgument(true);
        OneSheeldSdk.getManager().broadcastShieldFrame(sf);
    }

    public void onClickBroadcastOff(View v){
        ShieldFrame sf=new ShieldFrame((byte)0x03,(byte)0x01);
        sf.addArgument(false);
        OneSheeldSdk.getManager().broadcastShieldFrame(sf);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner= (Spinner) findViewById(R.id.spinner);
        button = (Button) findViewById(R.id.button);
        devices=new ArrayList<>();
        oneSheeldDevices=new ArrayList<>();
        devices.add("Nothing Selected");
        final ArrayAdapter<String> devicesArrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,devices);
        spinner.setAdapter(devicesArrayAdapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                Log.d("MainActivity", devices.get(position));
                OneSheeldSdk.getManager().cancelScanning();
                oneSheeldDevices.get(position - 1).connect();
                oneSheeldDevices.get(position - 1).addDataCallback(new OneSheeldDataCallback() {
                    @Override
                    public void onShieldFrameReceive(ShieldFrame frame) {
                        Log.d("MainActivity","Frame:"+frame.getArgumentAsString(0));
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        OneSheeldSdk.init(this);
        OneSheeldSdk.getManager().setRetryCount(4);
        OneSheeldSdk.setDebugging(true);
        OneSheeldSdk.getManager().setAutomaticConnectingRetries(true);

        OneSheeldSdk.getManager().addCallbacks(new OneSheeldScanningCallback() {
            @Override
            public void onScanStart() {
//                Log.d("MainActivity", "Scanning Started");

            }

            @Override
            public void onDeviceFind(OneSheeldDevice device) {
//                Log.d("MainActivity", "Device Found: " + device.getName());
                oneSheeldDevices.add(device);
                devices.add(device.getName());
                devicesArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinish(List<OneSheeldDevice> foundDevices) {
//                Log.d("MainActivity", "Finished Scanning: " + foundDevices.size() + " device(s) found");
//                for (OneSheeldDevice device : foundDevices) {
//                    OneSheeldSdk.getManager().connect(device);
////                    OneSheeldSdk.getKnownShields().co;
//
//                }
            }
        }, new OneSheeldConnectionCallback() {
            @Override
            public void onConnect(OneSheeldDevice device) {
//                Log.d("MainActivity", "Connected To: " + device.getName());
            }

            @Override
            public void onDisconnect(OneSheeldDevice device) {
//                Log.d("MainActivity", "Disconnected From: " + device.getName());
            }

            @Override
            public void onConnectionRetry(OneSheeldDevice device, int retryCount) {
//                Log.d("MainActivity", "Retried Connection To: " + device.getName()+", Retry #"+retryCount);
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
