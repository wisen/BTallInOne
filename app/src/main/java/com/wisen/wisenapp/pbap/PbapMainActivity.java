package com.wisen.wisenapp.pbap;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothUuid;
import android.bluetooth.client.pbap.BluetoothPbapClient;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.wisen.wisenapp.R;
import com.wisen.wisenapp.bt.DeviceListActivity;
import com.wisen.wisenapp.bt.Saudioclient;

import java.io.IOException;

public class PbapMainActivity extends AppCompatActivity {

    private static final String TAG = "PbapMainActivity";
    private static final boolean D = true;
    private BluetoothAdapter mBluetoothAdapter = null;

    public static BluetoothPbapClient sPbapClient;
    public static BluetoothServiceHandler sHandler = new BluetoothServiceHandler();
    public static BluetoothSocket sBluetoothSocket;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public static class BluetoothServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, msg.toString());
            switch (msg.what) {
                case BluetoothPbapClient.EVENT_PULL_PHONE_BOOK_DONE: {
                    Log.d(TAG, "EVENT_PULL_PHONE_BOOK_DONE");
                    sPbapClient.disconnect();
                }
                break;
                case BluetoothPbapClient.EVENT_SESSION_CONNECTED: {
                    Log.d(TAG, "EVENT_SESSION_CONNECTED");
                }
                break;
                case BluetoothPbapClient.EVENT_SESSION_DISCONNECTED: {
                    Log.d(TAG, "EVENT_SESSION_DISCONNECTED");
                }
                break;
                default: {
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pbap_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    protected void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {

        }
    }

    public void establishPbap(BluetoothDevice device) {
        Log.d(TAG, "Begin to establishPbap");
        if (null != mBluetoothAdapter){
            if (sPbapClient == null) {
                sPbapClient = new BluetoothPbapClient(device, sHandler);
                sPbapClient.connect();
                Log.d(TAG, "after sPbapClient.connect()");
            }
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        if (startPairing(device)){
                            establishSocket(device);
                            establishPbap(device);
                        } else {
                            Toast.makeText(getApplicationContext(),"Pair failed",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Attempt to connect to the device
                        establishPbap(device);
                    }

                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    //do non things
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public boolean startPairing(BluetoothDevice device) {
        // Pairing is unreliable while scanning, so cancel discovery
        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        if (!device.createBond()) {
            return false;
        }

        //mConnectAfterPairing = true;  // auto-connect after pairing
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pbap_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scann_device:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discovery:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            case R.id.pull_phonebook:
                getPhoneBook();
                return true;
        }
        return false;
    }

    public void getPhoneBook() {
        if (sPbapClient != null && sPbapClient.getState() == BluetoothPbapClient.ConnectionState.CONNECTED) {
            Log.d(TAG,"pulling the PhoneBook, it may take a long time ! ");
            sPbapClient.pullPhoneBook(BluetoothPbapClient.PB_PATH);
        } else {
            Log.d(TAG,"-----------------------------");
            Log.d(TAG,"sPbapClient is not ready ! ");
            Log.d(TAG,"sPbapClient:"+ sPbapClient.getState());
            Log.d(TAG,"-----------------------------");

        }
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void showAllAvailableUuids(BluetoothDevice device) {
        Log.d(TAG, "-------------");

        ParcelUuid[] uuids = device.getUuids();
        if (uuids == null || uuids.length == 0) {
            Log.d(TAG, "get uuids failed");
        } else {
            for (ParcelUuid uuid : uuids) {
                Log.d(TAG, "uuid is : " + uuid);
            }
            Log.d(TAG, "-------------");
        }
    }

    public void showBondedState(BluetoothDevice device){
        // bond state
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDED: {
                Log.d(TAG, "BONDED");
            }
            break;
            case BluetoothDevice.BOND_NONE: {
                Log.d(TAG, "NONE");
            }
            break;
            case BluetoothDevice.BOND_BONDING: {
                Log.d(TAG, "BONDING");
            }
            break;
            default: {
            }
        }
    }

    public void showDiscoveryState(){
        if (mBluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "@@@ it is discovering @@@ ");
            mBluetoothAdapter.cancelDiscovery();
        } else {
            Log.d(TAG, "@@@ it is not discovering @@@");
        }
    }

    public void establishSocket(BluetoothDevice device) {

        ParcelUuid uuid = BluetoothUuid.PBAP_PSE;

        showAllAvailableUuids(device);
        showBondedState(device);
        showDiscoveryState();

        try {
            Log.d(TAG, "try to createRfcommSocketToServiceRecord");
            mBluetoothAdapter.cancelDiscovery();

            sBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid.getUuid());

            int connectTimes = 0;
            int maxConnectTimes = 10;
            boolean isConnected = false;
            while (!isConnected && connectTimes < maxConnectTimes) {
                try {
                    sBluetoothSocket.connect();

                } catch (IOException e) {
                    Log.d(TAG, "sBLuetoothSocket.connect() is failed -------from the IOException");
                    try {
                        sBluetoothSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
                Log.d(TAG, "state of sBluetoothSocket : " + sBluetoothSocket.isConnected());

                if (sBluetoothSocket.isConnected()) {
                    isConnected = true;
                }
                connectTimes = connectTimes + 1;
            }


        } catch (IOException e) {
            Log.d(TAG, "createRfcommSocketToServiceRecord failed");
            e.printStackTrace();
        }

    }
}
