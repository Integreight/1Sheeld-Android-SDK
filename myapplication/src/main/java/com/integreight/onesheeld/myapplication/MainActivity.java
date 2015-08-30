package com.integreight.onesheeld.myapplication;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
    Button scan,connect,disconnect;
    TextView sheeld_name;
    ListView connectedListView, scannedListView;
    private ArrayList<String> connectedDevices, scannedDevices;
    private ArrayList<OneSheeldDevice> oneSheeldScannedDevices,oneSheeldConnnectedDevices;
    private ArrayAdapter<String> connectedDevicesArrayAdapter, scannedDevicesArrayAdapter;
    private OneSheeldDevice selectedConnectedDevice = null, selectedScanedDevice = null;
    static Handler handler;
    public static final int MSG_CONNECT=1,MSG_DISCONNECT=2;

    public void onClickScan(View v) {
        OneSheeldSdk.getManager().setScanningTimeOut(20);
        OneSheeldSdk.getManager().cancelScanning();
        scannedDevices.clear();
        scannedDevicesArrayAdapter.notifyDataSetChanged();
        oneSheeldScannedDevices.clear();
        OneSheeldSdk.getManager().scan();
    }

    public void onClickConnect(View v){
        if (selectedScanedDevice != null){
            OneSheeldSdk.getManager().cancelScanning();
            selectedScanedDevice.connect();
        }
    }

    public void onClickDisconnect(View v){
        if (selectedConnectedDevice != null){
            selectedConnectedDevice.disconnect();
            selectedConnectedDevice = null;
            sheeld_name.setText("Select Connected Device");
        }
    }

    public void onClickBroadcastOn(View v){
        ShieldFrame sf=new ShieldFrame((byte)0x03,(byte)0x01);
        sf.addArgument(true);
        OneSheeldSdk.getManager().broadcastShieldFrame(sf);
    }

    public void onClickDisconnectAll(View v){
        OneSheeldSdk.getManager().disconnectAll();
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
        connectedListView = (ListView) findViewById(R.id.connected_list);
        scannedListView = (ListView) findViewById(R.id.scanned_list);
        scan = (Button) findViewById(R.id.scan);
        connect = (Button) findViewById(R.id.connect_sheeld);
        disconnect = (Button) findViewById(R.id.disconnect_sheeld);
        sheeld_name = (TextView) findViewById(R.id.selected_sheeld_name);
        connectedDevices =new ArrayList<>();
        scannedDevices =new ArrayList<>();
        oneSheeldScannedDevices =new ArrayList<>();
        oneSheeldConnnectedDevices =new ArrayList<>();
        connectedDevicesArrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,connectedDevices);
        scannedDevicesArrayAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, scannedDevices);
        connectedListView.setAdapter(connectedDevicesArrayAdapter);
        scannedListView.setAdapter(scannedDevicesArrayAdapter);
        scannedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedScanedDevice = oneSheeldScannedDevices.get(position);
            }
        });
        connectedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedConnectedDevice = oneSheeldConnnectedDevices.get(position);
                sheeld_name.setText(oneSheeldConnnectedDevices.get(position).getName());
            }
        });
        OneSheeldSdk.setDebugging(true);
        handler = new Handler() {
            String name;
            public void handleMessage(android.os.Message msg) {
                switch (msg.what){
                    case MSG_CONNECT:
                        name = new String((String) msg.obj);
                        if(scannedDevices.indexOf(name) >=0) {
                            scannedDevices.remove(name);
                            connectedDevices.add(name);
                            connectedDevicesArrayAdapter.notifyDataSetChanged();
                            scannedDevicesArrayAdapter.notifyDataSetChanged();
                        }
                        break;
                    case MSG_DISCONNECT:
                        name = new String((String) msg.obj);
                        if (connectedDevices.indexOf(name) >= 0){
                            connectedDevices.remove(name);
                            connectedDevicesArrayAdapter.notifyDataSetChanged();
                        }
                        break;
                }
            }
        };
        OneSheeldSdk.init(this);
        OneSheeldSdk.getManager().setRetryCount(4);
        OneSheeldSdk.getManager().setAutomaticConnectingRetries(true);

        OneSheeldSdk.getManager().addCallbacks(new OneSheeldScanningCallback() {
            @Override
            public void onScanStart() {
//                Log.d("MainActivity", "Scanning Started");

            }

            @Override
            public void onDeviceFind(OneSheeldDevice device) {
//                Log.d("MainActivity", "Device Found: " + device.getName());
                oneSheeldScannedDevices.add(device);
                scannedDevices.add(device.getName());
                scannedDevicesArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinish(List<OneSheeldDevice> foundDevices) {
                //Toast.makeText(getApplicationContext(),"Scan Finished",Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "Finished Scanning: " + foundDevices.size() + " device(s) found");
//                for (OneSheeldDevice device : foundDevices) {
//                    OneSheeldSdk.getManager().connect(device);
////                    OneSheeldSdk.getKnownShields().co;
//
//                }
            }
        }, new OneSheeldConnectionCallback() {
            @Override
            public void onConnect(OneSheeldDevice device) {
                Log.d("MainActivity", "Connected To: " + device.getName());
                device.unMute();
                oneSheeldScannedDevices.remove(device);
                oneSheeldConnnectedDevices.add(device);
                handler.obtainMessage(MSG_CONNECT, device.getName()).sendToTarget();
            }

            @Override
            public void onDisconnect(OneSheeldDevice device) {
                Log.d("MainActivity", "Disconnected From: " + device.getName());
                handler.obtainMessage(MSG_DISCONNECT, device.getName()).sendToTarget();
                oneSheeldConnnectedDevices.remove(device);
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
