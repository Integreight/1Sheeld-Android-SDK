package com.integreight.onesheeld.myapplication;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.onesheeld.sdk.OneSheeldConnectionCallback;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.OneSheeldError;
import com.integreight.onesheeld.sdk.OneSheeldErrorCallback;
import com.integreight.onesheeld.sdk.OneSheeldScanningCallback;
import com.integreight.onesheeld.sdk.OneSheeldSdk;
import com.integreight.onesheeld.sdk.ShieldFrame;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    TextView sheeld_name;
    ListView connectedListView, scannedListView;
    Spinner pins;
    ProgressDialog scanningProgressDialog, connectionProgressDialog;
    LinearLayout sheeldContainer;

    private ArrayList<String> connectedDevices, scannedDevices, pinNumbers;
    private ArrayList<OneSheeldDevice> oneSheeldScannedDevices, oneSheeldConnnectedDevices;
    private ArrayAdapter<String> connectedDevicesArrayAdapter, scannedDevicesArrayAdapter, pinsArrayAdapter;
    private OneSheeldDevice selectedConnectedDevice = null, selectedScanedDevice = null;
    static Handler handler;
    public static final int MSG_CONNECT = 1, MSG_DISCONNECT = 2, MSG_TOAST = 0;
    private boolean digitalWriteState = false;

    public void onClickScan(View v) {
        OneSheeldSdk.getManager().setScanningTimeOut(20);
        OneSheeldSdk.getManager().cancelScanning();
        scannedDevices.clear();
        scannedDevicesArrayAdapter.notifyDataSetChanged();
        oneSheeldScannedDevices.clear();
        OneSheeldSdk.getManager().scan();
//        scanningProgressDialog = ProgressDialog.show(this,"Scanning..","Please wait");
        scanningProgressDialog.show();
    }

    public void onClickConnect(View v) {
        if (selectedScanedDevice != null) {
            OneSheeldSdk.getManager().cancelScanning();
            selectedScanedDevice.connect();
//            scanningProgressDialog = ProgressDialog.show(this,"Connecting..","Please wait while connecting to "+selectedScanedDevice.getName());
            connectionProgressDialog.setMessage("Please wait while connecting to " + selectedScanedDevice.getName());
            connectionProgressDialog.show();
        }
    }

    public void onClickDisconnect(View v) {
        if (selectedConnectedDevice != null) {
            selectedConnectedDevice.disconnect();
            selectedConnectedDevice = null;
//            sheeld_name.setText("Select Connected Device");
            sheeldContainer.setVisibility(View.INVISIBLE);
        }
    }

    public void onClickDigitalWrite(View v) {
        if (selectedConnectedDevice != null && pins != null) {
            digitalWriteState = !digitalWriteState;
            selectedConnectedDevice.digitalWrite(pins.getSelectedItemPosition(), digitalWriteState);
            ((Button) v).setText("Digital Write (" + String.valueOf(digitalWriteState) + ")");
        }
    }

    public void onClickDigitalRead(View v) {
        if (selectedConnectedDevice != null && pins != null)
            ((Button) v).setText("DigitalRead (" + selectedConnectedDevice.digitalRead(pins.getSelectedItemPosition()) + ")");
    }

    public void onClickBroadcastOn(View v) {
        ShieldFrame sf = new ShieldFrame((byte) 0x03, (byte) 0x01);
        sf.addArgument(true);
        OneSheeldSdk.getManager().broadcastShieldFrame(sf);
    }

    public void onClickDisconnectAll(View v) {
        OneSheeldSdk.getManager().disconnectAll();
        sheeldContainer.setVisibility(View.INVISIBLE);
    }

    public void onClickBroadcastOff(View v) {
        ShieldFrame sf = new ShieldFrame((byte) 0x03, (byte) 0x01);
        sf.addArgument(false);
        OneSheeldSdk.getManager().broadcastShieldFrame(sf);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sheeldContainer = (LinearLayout) findViewById(R.id.sheeld_container);
        sheeldContainer.setVisibility(View.INVISIBLE);
        connectedListView = (ListView) findViewById(R.id.connected_list);
        scannedListView = (ListView) findViewById(R.id.scanned_list);
        pins = (Spinner) findViewById(R.id.pin_number);
        sheeld_name = (TextView) findViewById(R.id.selected_sheeld_name);
        connectedDevices = new ArrayList<>();
        scannedDevices = new ArrayList<>();
        pinNumbers = new ArrayList<>();
        for (int pinNum = 0; pinNum <= 13; pinNum++)
            pinNumbers.add(String.valueOf(pinNum));
        oneSheeldScannedDevices = new ArrayList<>();
        oneSheeldConnnectedDevices = new ArrayList<>();
        connectedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, connectedDevices);
        scannedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, scannedDevices);
        pinsArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, pinNumbers);
        connectedListView.setAdapter(connectedDevicesArrayAdapter);
        scannedListView.setAdapter(scannedDevicesArrayAdapter);
        pins.setAdapter(pinsArrayAdapter);
        scanningProgressDialog = new ProgressDialog(this);
        scanningProgressDialog.setMessage("Please wait..");
        scanningProgressDialog.setTitle("Scanning");
        scanningProgressDialog.setCancelable(false);
        scanningProgressDialog.setCanceledOnTouchOutside(false);
        connectionProgressDialog = new ProgressDialog(this);
        connectionProgressDialog.setMessage("Please wait while connecting..");
        connectionProgressDialog.setTitle("Connecting");
        connectionProgressDialog.setCancelable(false);
        connectionProgressDialog.setCanceledOnTouchOutside(false);
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
                sheeldContainer.setVisibility(View.VISIBLE);
            }
        });
        OneSheeldSdk.setDebugging(true);
        handler = new Handler() {
            String name;

            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_CONNECT:
                        name = new String((String) msg.obj);
                        if (scannedDevices.indexOf(name) >= 0) {
                            scannedDevices.remove(name);
                            connectedDevices.add(name);
                            connectedDevicesArrayAdapter.notifyDataSetChanged();
                            scannedDevicesArrayAdapter.notifyDataSetChanged();
                        }
                        break;
                    case MSG_DISCONNECT:
                        name = new String((String) msg.obj);
                        if (connectedDevices.indexOf(name) >= 0) {
                            connectedDevices.remove(name);
                            connectedDevicesArrayAdapter.notifyDataSetChanged();
                        }
                        break;
                    case MSG_TOAST:
                        Toast.makeText(MainActivity.this, String.valueOf((String) msg.obj), Toast.LENGTH_LONG).show();
                }
            }
        };
        OneSheeldSdk.init(this);
        OneSheeldSdk.getManager().setRetryCount(0);
        OneSheeldSdk.getManager().setAutomaticConnectingRetries(false);

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
                scanningProgressDialog.dismiss();
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
                connectionProgressDialog.dismiss();
            }

            @Override
            public void onDisconnect(OneSheeldDevice device) {
                Log.d("MainActivity", "Disconnected From: " + device.getName());
                handler.obtainMessage(MSG_DISCONNECT, device.getName()).sendToTarget();
                oneSheeldConnnectedDevices.remove(device);
                if (scannedDevices.indexOf(device.getName()) < 0 && oneSheeldScannedDevices.indexOf(device) < 0) {
                    oneSheeldScannedDevices.add(device);
                    scannedDevices.add(device.getName());
                }
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
                handler.obtainMessage(MSG_TOAST, "Error: " + error.toString() + (device != null ? " in " + device.getName() : "")).sendToTarget();
                if (error == OneSheeldError.BLUETOOTH_CONNECTION_FAILED && connectionProgressDialog != null)
                    connectionProgressDialog.dismiss();
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
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
