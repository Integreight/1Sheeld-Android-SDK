package com.integreight.onesheeld.sampleapplication;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.onesheeld.sdk.OneSheeldBoardRenamingCallback;
import com.integreight.onesheeld.sdk.OneSheeldBoardTestingCallback;
import com.integreight.onesheeld.sdk.OneSheeldConnectionCallback;
import com.integreight.onesheeld.sdk.OneSheeldDataCallback;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.OneSheeldError;
import com.integreight.onesheeld.sdk.OneSheeldErrorCallback;
import com.integreight.onesheeld.sdk.OneSheeldManager;
import com.integreight.onesheeld.sdk.OneSheeldScanningCallback;
import com.integreight.onesheeld.sdk.OneSheeldSdk;
import com.integreight.onesheeld.sdk.ShieldFrame;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
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
    private ArrayList<OneSheeldDevice> oneSheeldConnectedDevices;
    private ArrayAdapter<String> connectedDevicesArrayAdapter;
    private ArrayAdapter<String> scannedDevicesArrayAdapter;
    private OneSheeldDevice selectedConnectedDevice = null;
    private OneSheeldDevice selectedScanedDevice = null;
    private boolean digitalWriteState = false;
    private byte pushButtonShieldId = OneSheeldSdk.getKnownShields().PUSH_BUTTON_SHIELD.getId();
    private byte pushButtonFunctionId = (byte) 0x01;
    private OneSheeldManager oneSheeldManager;
    private char[] nameChars = new char[]{};
    private Random random = new Random();
    private HashMap<String, String> pendingRenames;
    private Dialog bluetoothTestingDialog;
    private EditText bluetoothTestingSendingEditText;
    private EditText bluetoothTestingFramesNumberEditText;
    private EditText bluetoothTestingReceivingEditText;
    private TextView bluetoothSentFramesCounterTextView;
    private TextView bluetoothTestingReceivingFramesCounterTextView;
    private Button bluetoothTestingStartButton;
    private Button bluetoothTestingResetButton;
    private StringBuilder receivedStringBuilder = new StringBuilder();
    private BluetoothTestingSendingThread bluetoothTestingSendingThread;

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
    private OneSheeldBoardTestingCallback testingCallback = new OneSheeldBoardTestingCallback() {
        @Override
        public void onFirmwareTestResult(final OneSheeldDevice device, final boolean isPassed) {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, device.getName() + ": Firmware test result: " + (isPassed ? "Correct" : "Failed"), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onLibraryTestResult(final OneSheeldDevice device, final boolean isPassed) {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, device.getName() + ": Library test result: " + (isPassed ? "Correct" : "Failed"), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onFirmwareTestTimeOut(final OneSheeldDevice device) {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, device.getName() + ": Error, firmware test timeout!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onLibraryTestTimeOut(final OneSheeldDevice device) {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, device.getName() + ": Error, library test timeout!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    private OneSheeldBoardRenamingCallback renamingCallback = new OneSheeldBoardRenamingCallback() {
        @Override
        public void onRenamingAttemptTimeOut(final OneSheeldDevice device) {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, device.getName() + ": Error, renaming attempt failed, retrying!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onAllRenamingAttemptsTimeOut(final OneSheeldDevice device) {

            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, device.getName() + ": Error, all renaming attempts failed!", Toast.LENGTH_SHORT).show();
                }
            });
            pendingRenames.remove(device.getAddress());
        }

        @Override
        public void onRenamingRequestReceivedSuccessfully(final OneSheeldDevice device) {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, device.getName() + ": Renaming request received successfully!", Toast.LENGTH_SHORT).show();
                    if (connectedDevicesNames.contains(pendingRenames.get(device.getAddress()))) {
                        connectedDevicesNames.add(connectedDevicesNames.indexOf(pendingRenames.get(device.getAddress())), device.getName());
                        connectedDevicesNames.remove(pendingRenames.get(device.getAddress()));
                        pendingRenames.remove(device.getAddress());
                        connectedDevicesArrayAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };
    private OneSheeldDataCallback dataCallback = new OneSheeldDataCallback() {
        @Override
        public void onSerialDataReceive(OneSheeldDevice device, int data) {
            receivedStringBuilder.append((char) data);
            if (receivedStringBuilder.length() >= bluetoothTestingReceivingEditText.getText().toString().length()) {
                String compareString = receivedStringBuilder.substring(0, bluetoothTestingReceivingEditText.getText().toString().length());
                if (compareString.equals(bluetoothTestingReceivingEditText.getText().toString())) {
                    receivedStringBuilder.delete(0, bluetoothTestingReceivingEditText.getText().toString().length());
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothTestingReceivingFramesCounterTextView.setText(String.valueOf((Integer.valueOf(bluetoothTestingReceivingFramesCounterTextView.getText().toString()) + 1)));
                        }
                    });
                }
                if (receivedStringBuilder.length() > 0) receivedStringBuilder.deleteCharAt(0);
            }
        }
    };
    private OneSheeldConnectionCallback connectionCallback = new OneSheeldConnectionCallback() {
        @Override
        public void onConnect(final OneSheeldDevice device) {
            oneSheeldScannedDevices.remove(device);
            oneSheeldConnectedDevices.add(device);
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
            device.addTestingCallback(testingCallback);
            device.addRenamingCallback(renamingCallback);
            device.addDataCallback(dataCallback);
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
            oneSheeldConnectedDevices.remove(device);
            if (!scannedDevicesNames.contains(device.getName()) && !oneSheeldScannedDevices.contains(device)) {
                oneSheeldScannedDevices.add(device);
                scannedDevicesNames.add(device.getName());
            }
            bluetoothTestingDialog.dismiss();
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
                    Toast.makeText(MainActivity.this, "Error: " + error.toString() + (device != null ? " in " + device.getName() : ""), Toast.LENGTH_SHORT).show();
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
            selectedConnectedDevice = oneSheeldConnectedDevices.get(position);
            oneSheeldNameTextView.setText(selectedConnectedDevice.getName());
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

    public void onClickRename(View v) {
        if (selectedConnectedDevice != null) {
            pendingRenames.put(selectedConnectedDevice.getAddress(), selectedConnectedDevice.getName());
            selectedConnectedDevice.rename("1Sheeld #" + (selectedConnectedDevice.isTypePlus() ? getRandomChars(2) : getRandomChars(4)));
        }
    }

    public void onClickRenameAll(View v) {
        for (OneSheeldDevice device : oneSheeldConnectedDevices) {
            pendingRenames.put(device.getAddress(), device.getName());
            device.rename("1Sheeld #" + (device.isTypePlus() ? getRandomChars(2) : getRandomChars(4)));
        }
    }

    public void onClickTestBoard(View v) {
        if (selectedConnectedDevice != null) {
            selectedConnectedDevice.test();
        }
    }

    private String getRandomChars(int digitNum) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < digitNum; i++)
            builder.append(nameChars[random.nextInt(nameChars.length)]);
        return builder.toString();
    }

    public void onClickDigitalWrite(View v) {
        if (selectedConnectedDevice != null && pinsSpinner != null) {
            digitalWriteState = !digitalWriteState;
            selectedConnectedDevice.digitalWrite(pinsSpinner.getSelectedItemPosition() + 2, digitalWriteState);
            ((Button) v).setText("Digital Write (" + String.valueOf(digitalWriteState) + ")");
        }
    }

    public void onClickDigitalRead(View v) {
        if (selectedConnectedDevice != null && pinsSpinner != null)
            ((Button) v).setText("DigitalRead (" + selectedConnectedDevice.digitalRead(pinsSpinner.getSelectedItemPosition() + 2) + ")");
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

    public void onClickBluetoothTestingDialog(View v) {
        resetBluetoothTesting();
        bluetoothSentFramesCounterTextView.setText("0");
        bluetoothTestingReceivingFramesCounterTextView.setText("0");
        bluetoothTestingSendingEditText.setText("a0b1c2d3e4f5g6h7i8j9");
        bluetoothTestingFramesNumberEditText.setText("10000");
        bluetoothTestingReceivingEditText.setText("a0b1c2d3e4f5g6h7i8j9");
        bluetoothTestingStartButton.setEnabled(true);
        bluetoothTestingSendingEditText.setEnabled(true);
        bluetoothTestingFramesNumberEditText.setEnabled(true);
        bluetoothTestingDialog.show();
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
        oneSheeldConnectedDevices = new ArrayList<>();
        pendingRenames = new HashMap<>();
        connectedDevicesListView.setAdapter(connectedDevicesArrayAdapter);
        scannedDevicesListView.setAdapter(scannedDevicesArrayAdapter);
        pinsSpinner.setAdapter(pinsArrayAdapter);
        scannedDevicesListView.setOnItemClickListener(scannedDevicesListViewClickListener);
        connectedDevicesListView.setOnItemClickListener(connectedDevicesListViewClickListener);
        initScanningProgressDialog();
        initConnectionProgressDialog();
        initRandomChars();
        initBluetoothTestingDialog();
        initOneSheeldSdk();
    }

    void initBluetoothTestingDialog() {
        bluetoothTestingDialog = new Dialog(this);
        bluetoothTestingDialog.setContentView(R.layout.testing_dialog);
        bluetoothTestingSendingEditText = (EditText) bluetoothTestingDialog.findViewById(R.id.bluetoothTestingSendingEditText);
        bluetoothTestingFramesNumberEditText = (EditText) bluetoothTestingDialog.findViewById(R.id.bluetoothTestingFramesNumberEditText);
        bluetoothTestingReceivingEditText = (EditText) bluetoothTestingDialog.findViewById(R.id.bluetoothTestingReceivingEditText);
        bluetoothSentFramesCounterTextView = (TextView) bluetoothTestingDialog.findViewById(R.id.bluetoothSentFramesCounterTextView);
        bluetoothTestingReceivingFramesCounterTextView = (TextView) bluetoothTestingDialog.findViewById(R.id.bluetoothTestingReceivingFramesCounterTextView);
        bluetoothTestingStartButton = (Button) bluetoothTestingDialog.findViewById(R.id.bluetoothTestingStartButton);
        bluetoothTestingResetButton = (Button) bluetoothTestingDialog.findViewById(R.id.bluetoothTestingResetButton);
        bluetoothTestingStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBluetoothTesting();
            }
        });
        bluetoothTestingResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetBluetoothTesting();
            }
        });
        resetBluetoothTesting();
        bluetoothTestingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                resetBluetoothTesting();
            }
        });
        bluetoothTestingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                resetBluetoothTesting();
            }
        });
    }

    private void resetBluetoothTesting() {
        if (bluetoothTestingSendingThread != null)
            bluetoothTestingSendingThread.stopRunning();
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothSentFramesCounterTextView.setText("0");
                bluetoothTestingReceivingFramesCounterTextView.setText("0");
                bluetoothTestingSendingEditText.setEnabled(true);
                bluetoothTestingFramesNumberEditText.setEnabled(true);
                bluetoothTestingStartButton.setEnabled(true);
            }
        });
        receivedStringBuilder = new StringBuilder();
    }

    private void startBluetoothTesting() {
        if (selectedConnectedDevice != null) {
            if (bluetoothTestingSendingThread != null)
                bluetoothTestingSendingThread.stopRunning();
            bluetoothTestingSendingThread = new BluetoothTestingSendingThread(selectedConnectedDevice, bluetoothTestingSendingEditText.getText().toString(), Integer.valueOf(bluetoothTestingFramesNumberEditText.getText().toString()));
        }
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                bluetoothTestingStartButton.setEnabled(false);
                bluetoothTestingSendingEditText.setEnabled(false);
                bluetoothTestingFramesNumberEditText.setEnabled(false);

            }
        });
    }

    private void initRandomChars() {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch)
            tmp.append(ch);
        for (char ch = 'A'; ch <= 'Z'; ++ch)
            tmp.append(ch);
        nameChars = tmp.toString().toCharArray();
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
        oneSheeldManager.disconnectAll();
        bluetoothTestingDialog.dismiss();
        super.onDestroy();
    }

    private class BluetoothTestingSendingThread extends Thread {
        AtomicBoolean stopRequested;
        OneSheeldDevice device;
        String string;
        int count;

        BluetoothTestingSendingThread(OneSheeldDevice device, String string, int count) {
            stopRequested = new AtomicBoolean(false);
            this.device = device;
            this.string = string;
            this.count = count;
            start();
        }

        private void stopRunning() {
            if (this.isAlive())
                this.interrupt();
            stopRequested.set(true);
        }

        @Override
        public void run() {
            for (int i = 1; i <= count && !this.isInterrupted() && !stopRequested.get(); i++) {
                device.sendSerialData(string.getBytes(Charset.forName("US-ASCII")));
                final int counter = i;
                uiThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!stopRequested.get())
                            bluetoothSentFramesCounterTextView.setText(String.valueOf(counter));
                    }
                });
            }
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!stopRequested.get()) {
                        bluetoothTestingSendingEditText.setEnabled(true);
                        bluetoothTestingFramesNumberEditText.setEnabled(true);
                        bluetoothTestingStartButton.setEnabled(true);
                    }
                }
            });
        }
    }
}
