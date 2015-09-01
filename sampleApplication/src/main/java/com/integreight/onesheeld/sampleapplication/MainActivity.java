package com.integreight.onesheeld.sampleapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
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
import com.integreight.onesheeld.sdk.OneSheeldManager;
import com.integreight.onesheeld.sdk.OneSheeldScanningCallback;
import com.integreight.onesheeld.sdk.OneSheeldSdk;
import com.integreight.onesheeld.sdk.ShieldFrame;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    private Handler uiThreadHandler = new Handler();
    private Button connectButton;
    private Button disconnectButton;
    private TextView oneSheeldNameTextView;
    private Spinner pinsSpinner;
    private ProgressDialog scanningProgressDialog;
    private ProgressDialog connectionProgressDialog;
    private LinearLayout oneSheeldLinearLayout;
    private ArrayList<String> connectedDevicesNames;
    private ArrayList<String> scannedDevicesNames;
    private ArrayList<OneSheeldDevice> oneSheeldScannedDevices;
    private ArrayList<OneSheeldDevice> oneSheeldConnnectedDevices;
    private ArrayAdapter<String> connectedDevicesArrayAdapter;
    private ArrayAdapter<String> scannedDevicesArrayAdapter;
    private OneSheeldDevice selectedConnectedDevice = null;
    private OneSheeldDevice selectedScanedDevice = null;
    private boolean digitalWriteState = false;
    private byte pushButtonShieldId = OneSheeldSdk.getKnownShields().PUSH_BUTTON_SHIELD.getId();
    private byte pushButtonFunctionId = (byte) 0x01;
    private OneSheeldManager oneSheeldManager;
    private OneSheeldScanningCallback scanningCallback = new OneSheeldScanningCallback() {
        @Override
        public void onDeviceFind(OneSheeldDevice device) {
            oneSheeldScannedDevices.add(device);
            scannedDevicesNames.add(device.getName());
            scannedDevicesArrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFinish(List<OneSheeldDevice> foundDevices) {
            scanningProgressDialog.dismiss();
        }
    };
    private OneSheeldConnectionCallback connectionCallback = new OneSheeldConnectionCallback() {
        @Override
        public void onConnect(final OneSheeldDevice device) {
            oneSheeldScannedDevices.remove(device);
            oneSheeldConnnectedDevices.add(device);
            final String deviceName = device.getName();
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (scannedDevicesNames.indexOf(deviceName) >= 0) {
                        scannedDevicesNames.remove(deviceName);
                        connectedDevicesNames.add(deviceName);
                        connectedDevicesArrayAdapter.notifyDataSetChanged();
                        scannedDevicesArrayAdapter.notifyDataSetChanged();
                    }
                    connectButton.setEnabled(false);
                    disconnectButton.setEnabled(false);
                    oneSheeldLinearLayout.setVisibility(View.INVISIBLE);
                }
            });
            connectionProgressDialog.dismiss();
        }

        @Override
        public void onDisconnect(OneSheeldDevice device) {
            final String deviceName = device.getName();

            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (connectedDevicesNames.indexOf(deviceName) >= 0) {
                        connectedDevicesNames.remove(deviceName);
                        connectedDevicesArrayAdapter.notifyDataSetChanged();
                    }
                    connectButton.setEnabled(false);
                    disconnectButton.setEnabled(false);
                    oneSheeldLinearLayout.setVisibility(View.INVISIBLE);
                }
            });
            oneSheeldConnnectedDevices.remove(device);
            if (scannedDevicesNames.indexOf(device.getName()) < 0 && oneSheeldScannedDevices.indexOf(device) < 0) {
                oneSheeldScannedDevices.add(device);
                scannedDevicesNames.add(device.getName());
            }
        }
    };

    private OneSheeldErrorCallback errorCallback = new OneSheeldErrorCallback() {
        @Override
        public void onError(final OneSheeldDevice device, final OneSheeldError error) {
            if (connectionProgressDialog != null)
                connectionProgressDialog.dismiss();
            if (scanningProgressDialog != null)
                scanningProgressDialog.dismiss();
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Error: " + error.toString() + (device != null ? " in " + device.getName() : ""), Toast.LENGTH_LONG).show();
                }
            });
        }
    };
    private AdapterView.OnItemClickListener scannedDevicesListViewClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectedScanedDevice = oneSheeldScannedDevices.get(position);
            connectButton.setEnabled(true);
        }
    };
    private AdapterView.OnItemClickListener connectedDevicesListViewClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectedConnectedDevice = oneSheeldConnnectedDevices.get(position);
            oneSheeldNameTextView.setText(oneSheeldConnnectedDevices.get(position).getName());
            oneSheeldLinearLayout.setVisibility(View.VISIBLE);
            disconnectButton.setEnabled(true);
        }
    };

    public void onClickScan(View v) {
        scanningProgressDialog.show();
        oneSheeldManager.setScanningTimeOut(20);
        oneSheeldManager.cancelScanning();
        scannedDevicesNames.clear();
        scannedDevicesArrayAdapter.notifyDataSetChanged();
        oneSheeldScannedDevices.clear();
        oneSheeldManager.scan();
    }

    public void onClickConnect(View v) {
        if (selectedScanedDevice != null) {
            oneSheeldManager.cancelScanning();
            connectionProgressDialog.setMessage("Please wait while connecting to " + selectedScanedDevice.getName());
            connectionProgressDialog.show();
            selectedScanedDevice.connect();
        }
    }

    public void onClickDisconnect(View v) {
        if (selectedConnectedDevice != null) {
            selectedConnectedDevice.disconnect();
            selectedConnectedDevice = null;
            oneSheeldLinearLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void onClickDigitalWrite(View v) {
        if (selectedConnectedDevice != null && pinsSpinner != null) {
            digitalWriteState = !digitalWriteState;
            selectedConnectedDevice.digitalWrite(pinsSpinner.getSelectedItemPosition(), digitalWriteState);
            ((Button) v).setText("Digital Write (" + String.valueOf(digitalWriteState) + ")");
        }
    }

    public void onClickDigitalRead(View v) {
        if (selectedConnectedDevice != null && pinsSpinner != null)
            ((Button) v).setText("DigitalRead (" + selectedConnectedDevice.digitalRead(pinsSpinner.getSelectedItemPosition()) + ")");
    }

    public void onClickSendOnFrame(View v) {
        if (selectedConnectedDevice != null) {
            ShieldFrame sf = new ShieldFrame(pushButtonShieldId, pushButtonFunctionId);
            sf.addArgument(true);
            selectedConnectedDevice.sendShieldFrame(sf);
        }
    }

    public void onClickSendOffFrame(View v) {
        if (selectedConnectedDevice != null) {
            ShieldFrame sf = new ShieldFrame(pushButtonShieldId, pushButtonFunctionId);
            sf.addArgument(false);
            selectedConnectedDevice.sendShieldFrame(sf);
        }
    }

    public void onClickBroadcastOn(View v) {
        ShieldFrame sf = new ShieldFrame(pushButtonShieldId, pushButtonFunctionId);
        sf.addArgument(true);
        oneSheeldManager.broadcastShieldFrame(sf);
    }

    public void onClickBroadcastOff(View v) {
        ShieldFrame sf = new ShieldFrame(pushButtonShieldId, pushButtonFunctionId);
        sf.addArgument(false);
        oneSheeldManager.broadcastShieldFrame(sf);
    }

    public void onClickDisconnectAll(View v) {
        oneSheeldManager.disconnectAll();
        oneSheeldLinearLayout.setVisibility(View.INVISIBLE);
        disconnectButton.setEnabled(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        oneSheeldLinearLayout = (LinearLayout) findViewById(R.id.onesheeld_container);
        connectButton = (Button) findViewById(R.id.connect_1sheeld);
        disconnectButton = (Button) findViewById(R.id.disconnect_1sheeld);
        ListView connectedDevicesListView = (ListView) findViewById(R.id.connected_list);
        ListView scannedDevicesListView = (ListView) findViewById(R.id.scanned_list);
        oneSheeldNameTextView = (TextView) findViewById(R.id.selected_1sheeld_name);
        pinsSpinner = (Spinner) findViewById(R.id.pin_number);
        connectedDevicesNames = new ArrayList<>();
        scannedDevicesNames = new ArrayList<>();
        connectedDevicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, connectedDevicesNames);
        scannedDevicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scannedDevicesNames);
        ArrayList<String> pinNumbers = new ArrayList<>();
        for (int pinNum = 2; pinNum <= 13; pinNum++)
            pinNumbers.add(String.valueOf(pinNum));
        ArrayAdapter<String> pinsArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pinNumbers);
        oneSheeldLinearLayout.setVisibility(View.INVISIBLE);
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(false);
        oneSheeldScannedDevices = new ArrayList<>();
        oneSheeldConnnectedDevices = new ArrayList<>();
        connectedDevicesListView.setAdapter(connectedDevicesArrayAdapter);
        scannedDevicesListView.setAdapter(scannedDevicesArrayAdapter);
        pinsSpinner.setAdapter(pinsArrayAdapter);
        scannedDevicesListView.setOnItemClickListener(scannedDevicesListViewClickListener);
        connectedDevicesListView.setOnItemClickListener(connectedDevicesListViewClickListener);
        initScanningProgressDialog();
        initConnectionProgressDialog();
        initOneSheeldSdk();
    }

    private void initOneSheeldSdk() {
        OneSheeldSdk.setDebugging(true);
        OneSheeldSdk.init(this);
        oneSheeldManager = OneSheeldSdk.getManager();
        oneSheeldManager.setConnectionRetryCount(1);
        oneSheeldManager.setAutomaticConnectingRetries(true);
        oneSheeldManager.addScanningCallback(scanningCallback);
        oneSheeldManager.addConnectionCallback(connectionCallback);
        oneSheeldManager.addErrorCallback(errorCallback);
    }

    private void initScanningProgressDialog() {
        scanningProgressDialog = new ProgressDialog(this);
        scanningProgressDialog.setMessage("Please wait..");
        scanningProgressDialog.setTitle("Scanning");
        scanningProgressDialog.setCancelable(true);
        scanningProgressDialog.setCanceledOnTouchOutside(true);
        scanningProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                oneSheeldManager.cancelScanning();
            }
        });
    }

    private void initConnectionProgressDialog() {
        connectionProgressDialog = new ProgressDialog(this);
        connectionProgressDialog.setMessage("Please wait while connecting..");
        connectionProgressDialog.setTitle("Connecting");
        connectionProgressDialog.setCancelable(false);
        connectionProgressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onDestroy() {
        oneSheeldManager.cancelScanning();
        super.onDestroy();
    }
}
