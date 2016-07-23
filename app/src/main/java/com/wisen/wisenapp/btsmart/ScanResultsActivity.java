package com.wisen.wisenapp.btsmart;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wisen.wisenapp.R;
import com.wisen.wisenapp.bt.DeviceListActivity;
import com.wisen.wisenapp.bt.Saudioclient;
import com.wisen.wisenapp.bt.Saudioserver;
import com.wisen.wisenapp.btsmart.xiaomi.HackXiaoMiActivity;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class ScanResultsActivity extends AppCompatActivity {

    private final String TAG = "ScanResults";

    private static final int REQUEST_ENABLE_BT = 1;

    // Adjust this value to control how long scan should last for. Higher values
    // will drain the battery more.
    private static final long SCAN_PERIOD = 100000;

    ListView mScanListView = null;

    private static final HashMap<Integer, Integer> appearances = new HashMap<Integer, Integer>();

    private static ArrayList<ScanInfo> mScanResults = new ArrayList<ScanInfo>();

    private static HashSet<String> mScanAddreses = new HashSet<String>();

    private static ScanResultsAdapter mScanResultsAdapter;

    private BluetoothAdapter mBtAdapter = null;

    private static Handler mHandler = new Handler();

    private BluetoothLeScanner mBluetoothLeScanner = null;

    private Button mScanButton = null;

    private TextView mTextStatus = null;

    private static final int APPEARANCE_HEART_RATE = 832;

    private List<ScanFilter> mScanfilters = null;

    private boolean scan_flag = true;
    // Used to filter scan results by UUID
    private static UUID uuidFilter[] = { BtSmartService.BtSmartUuid.HRP_SERVICE.getUuid() };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_results2);

        // Prevent screen rotation.
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        mScanListView = (ListView) this.findViewById(R.id.scanListView);
        mScanResultsAdapter = new ScanResultsAdapter(this, mScanResults);
        mScanListView.setAdapter(mScanResultsAdapter);
        mScanListView.setOnItemClickListener(mDeviceClickListener);

        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = btManager.getAdapter();

        mScanButton = (Button) findViewById(R.id.buttonScan);
        mScanButton.setOnClickListener(mScanButtonListener);
        mTextStatus = (TextView) findViewById(R.id.textStatus);

        mScanfilters = new ArrayList<ScanFilter>();

        // Display a dialogue requesting Bluetooth to be enabled if it isn't
        // already.
        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothLeScanner = mBtAdapter.getBluetoothLeScanner();
            scanLeDevice(true);
        }
    }

    /**
     * Start or stop scanning. Only scan for a limited amount of time defined by SCAN_PERIOD.
     *
     * @param enable
     *            Set to true to enable scanning, false to stop.
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                    .build();

            //List<ScanFilter> filters = new ArrayList<ScanFilter>();

            ScanFilter filter = new ScanFilter.Builder()
                    //.setServiceUuid(new ParcelUuid(UUID.fromString(getString(R.string.ble_test_uuid))))
                    .build();
            //filters.add(filter);
            mScanfilters.add(filter);

            // Stops scanning after a predefined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //mBtAdapter.stopLeScan(mLeScanCallback);
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    mTextStatus.setText("Scan stoped.");
                    //mScanButton.setEnabled(true);
                    mScanButton.setText("Start Scan");
                }
            }, SCAN_PERIOD);
            mScanResults.clear();
            mScanAddreses.clear();
            //mBtAdapter.startLeScan(uuidFilter, mLeScanCallback);
            mBluetoothLeScanner.startScan(mScanfilters, settings, mScanCallback);

            mTextStatus.setText("Scanning...");
            //mScanButton.setEnabled(false);
            mScanButton.setText("Stop Scan");
        } else {
            mBluetoothLeScanner.stopScan(mScanCallback);
            mTextStatus.setText("Scan stoped.");
            //mScanButton.setEnabled(true);
            mScanButton.setText("Start Scan");
        }
    }

    /**
     * Callback for scan results.
     */
    /*
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Get the numerical appearance value.
                    int appearanceKey = BtSmartService.getAppearanceFromScanRecord(scanRecord);
                    // Look up the appearance value in the appearances map to
                    // see if an icon has been specified.
                    int imageResource = 0;
                    if (appearances.containsKey(appearanceKey)) {
                        imageResource = appearances.get(appearanceKey);
                    }

                    // Construct a ScanInfo object to add to the list.
                    ScanInfo scanResult = new ScanInfo(device.getName(), device.getAddress(), imageResource, rssi);

                    // Check that this isn't a device we have already seen, and
                    // add it to the list.
                    if (!mScanAddreses.contains(device.getAddress())) {
                        mScanAddreses.add(device.getAddress());
                        mScanResults.add(scanResult);
                        mScanResultsAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };*/
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = null;
            String device_name = null;

            if (null == result) {
                Log.d(TAG, "ScanCallback: result is null!!");
            }
            device = result.getDevice();
            if (null == device)
                return;
            device_name = device.getName();
            if (null == device_name)
                return;

            StringBuilder builder = new StringBuilder(device_name);

            // Construct a ScanInfo object to add to the list.
            ScanInfo scanResult = new ScanInfo(device_name, device.getAddress(), 0, result.getRssi());
            //builder.append("\n").append(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));
            // Check that this isn't a device we have already seen, and
            // add it to the list.
            if (!mScanAddreses.contains(device.getAddress())) {
                mScanAddreses.add(device.getAddress());
                mScanResults.add(scanResult);
                mScanResultsAdapter.notifyDataSetChanged();
            }
            //mTextStatus.setText(builder.toString());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("BLE", "Discovery onScanFailed: " + errorCode);
            super.onScanFailed(errorCode);
        }
    };

    /**
     * Callback activated after the user responds to the enable Bluetooth dialogue.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            mBluetoothLeScanner = mBtAdapter.getBluetoothLeScanner();
            scanLeDevice(true);
        } else {
            Toast.makeText(this, "Bluetooth not enabled.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * When the Activity is resumed, clear the scan results list.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mScanResults.clear();
        mScanResultsAdapter.notifyDataSetChanged();
        mScanAddreses.clear();

        if (null != mBtAdapter) {
            mBluetoothLeScanner = mBtAdapter.getBluetoothLeScanner();
            if (null != mBluetoothLeScanner)
                scanLeDevice(true);
        }
    }

    /**
     * Click handler for the scan button that starts scanning for BT Smart devices.
     */
    View.OnClickListener mScanButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*
            if (mBtAdapter.isEnabled()) {
                scanLeDevice(true);
            }*/
            if (scan_flag) {
                scanLeDevice(true);
                scan_flag = false;
            } else {
                scanLeDevice(false);
                scan_flag = true;
            }
        }
    };

    /**
     * The on-click listener for selecting a device.
     */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            scanLeDevice(false);
            ScanInfo info = (ScanInfo) mScanResultsAdapter.getItem(position);
            BluetoothDevice deviceToConnect = mBtAdapter.getRemoteDevice(info.address);
            startMainActivity(deviceToConnect);
        }
    };

    /**
     * Launch the main activity to connect to the device.
     *
     * @param deviceToConnect
     *            The Bluetooth device the user selected.
     */
    private void startMainActivity(BluetoothDevice deviceToConnect) {
        Intent intent = null;
        if (deviceToConnect.getName().toLowerCase().contains("mi")){
            intent = new Intent(this, HackXiaoMiActivity.class);
        } else {
            intent = new Intent(this, BTSmartActivity.class);
        }

        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, deviceToConnect);
        this.startActivity(intent);
    }

    /**
     * The adapter that allows the contents of ScanInfo objects to be displayed in the ListView. The device name,
     * address, RSSI and the icon specified in appearances.xml are displayed.
     */
    private class ScanResultsAdapter extends BaseAdapter {
        private Activity activity;
        private ArrayList<ScanInfo> data;
        private LayoutInflater inflater = null;

        public ScanResultsAdapter(Activity a, ArrayList<ScanInfo> object) {
            activity = a;
            data = object;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return data.size();
        }

        public Object getItem(int position) {
            return data.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (convertView == null)
                vi = inflater.inflate(R.layout.list_row, null);

            TextView nameText = (TextView) vi.findViewById(R.id.name);
            TextView addressText = (TextView) vi.findViewById(R.id.address);
            TextView rssiText = (TextView) vi.findViewById(R.id.rssi);
            ImageView thumbImage = (ImageView) vi.findViewById(R.id.list_image);

            ScanInfo info = (ScanInfo) data.get(position);
            nameText.setText(info.name);
            addressText.setText(info.address);
            rssiText.setText(String.valueOf(info.rssi) + "dBm");
            thumbImage.setImageResource(R.drawable.bleicon);
            /*
            if (info.appearanceImageResource != 0) {
                thumbImage.setImageResource(info.appearanceImageResource);
            }*/
            return vi;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scan_filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_the_scan_filter:

                return true;

        }
        return false;
    }
}
