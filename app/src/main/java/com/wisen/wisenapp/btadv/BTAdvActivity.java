package com.wisen.wisenapp.btadv;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wisen.wisenapp.R;
import com.wisen.wisenapp.btsmart.BTSmartProfile;

import org.w3c.dom.Text;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BTAdvActivity extends AppCompatActivity {

    private final String TAG = "BTAdvActivity";

    private Button btn_is_support_peri = null;
    private Button btn_start_adv = null;
    private Button btn_stop_adv = null;
    private TextView tv_is_support_peri = null;
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser = null;
    private AdvertiseData adv_data = null;
    private AdvertiseCallback adv_callback = null;
    private AdvertiseSettings adv_setting = null;
    private ParcelUuid mUUID = null;

    private int gattCharacteristicData = 0;

    public static String gattServiceUUID = "039AFFF0-2C94-11E3-9E06-0002A5D5C51B";
    public static String gattCharacteristicUUID = "039AFFA1-2C94-11E3-9E06-0002A5D5C51B";
    private BluetoothGattServer gattServer = null;
    private BluetoothGattCharacteristic gattCharacteristic = null;
    private BluetoothGattService gattService = null;
    private BluetoothGattDescriptor gattdescriptor = null;
    private static final int REQUEST_ENABLE_BT = 1;

    private void initgattServer(BluetoothManager bluetoothManager){
        gattCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(BTSmartProfile.BTS_SOne_COneUUID),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        gattCharacteristic.setValue("s1c1v1");

        gattdescriptor = new BluetoothGattDescriptor(UUID.fromString(BTSmartProfile.BTS_SOne_COne_D1UUID), BluetoothGattDescriptor.PERMISSION_READ);
        gattCharacteristic.addDescriptor(gattdescriptor);
        gattdescriptor = new BluetoothGattDescriptor(UUID.fromString(BTSmartProfile.BTS_SOne_COne_D2UUID), BluetoothGattDescriptor.PERMISSION_READ);
        gattCharacteristic.addDescriptor(gattdescriptor);
        gattdescriptor = new BluetoothGattDescriptor(UUID.fromString(BTSmartProfile.BTS_SOne_COne_D3UUID), BluetoothGattDescriptor.PERMISSION_READ);
        gattCharacteristic.addDescriptor(gattdescriptor);

        gattService = new BluetoothGattService(UUID.fromString(BTSmartProfile.BTS_ServiceOneUUID),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        if (null != gattCharacteristic && null != gattService) {
            gattService.addCharacteristic(gattCharacteristic);
        } else {
            Log.d(TAG, "gattCharacteristic or gattService create failed!!");
        }

        if(null != bluetoothManager){
            gattServer = bluetoothManager.openGattServer(this,
                    new BluetoothGattServerCallback() {

                        @Override
                        public void onConnectionStateChange(BluetoothDevice device,
                                                            int status, int newState) {
                            Log.d(TAG, "onConnectionStateChange device:" + device.getName() + " status=" + status + " newState=" + newState);
                            super.onConnectionStateChange(device, status, newState);
                        }

                        @Override
                        public void onServiceAdded(int status,
                                                   BluetoothGattService service) {

                            Log.d(TAG, "service added");
                            super.onServiceAdded(status, service);
                        }

                        @Override
                        public void onCharacteristicReadRequest(
                                BluetoothDevice device, int requestId, int offset,
                                BluetoothGattCharacteristic characteristic) {
                            Log.d(TAG, "onCharacteristicReadRequest");
                            super.onCharacteristicReadRequest(device, requestId,
                                    offset, characteristic);
                        }

                        @Override
                        public void onCharacteristicWriteRequest(
                                BluetoothDevice device, int requestId,
                                BluetoothGattCharacteristic characteristic,
                                boolean preparedWrite, boolean responseNeeded,
                                int offset, byte[] value) {
                            Log.d(TAG, "onCharacteristicWriteRequest");
                            super.onCharacteristicWriteRequest(device, requestId,
                                    characteristic, preparedWrite, responseNeeded,
                                    offset, value);
                        }

                        @Override
                        public void onDescriptorReadRequest(BluetoothDevice device,
                                                            int requestId, int offset,
                                                            BluetoothGattDescriptor descriptor) {
                            Log.d(TAG, "onDescriptorReadRequest");
                            super.onDescriptorReadRequest(device, requestId,
                                    offset, descriptor);
                        }

                        @Override
                        public void onDescriptorWriteRequest(
                                BluetoothDevice device, int requestId,
                                BluetoothGattDescriptor descriptor,
                                boolean preparedWrite, boolean responseNeeded,
                                int offset, byte[] value) {
                            Log.d(TAG, "onDescriptorWriteRequest");
                            super.onDescriptorWriteRequest(device, requestId,
                                    descriptor, preparedWrite, responseNeeded,
                                    offset, value);
                        }

                        @Override
                        public void onExecuteWrite(BluetoothDevice device,
                                                   int requestId, boolean execute) {
                            Log.d(TAG, "onExecuteWrite");
                            super.onExecuteWrite(device, requestId, execute);
                        }

                    });
        }

        if(null != gattServer){
            gattServer.addService(gattService);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btadv);

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        /*
        if(mBluetoothAdapter ==  null){
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_LONG).show();
            finish();
        }

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
            finish();
        }
        */
        // Display a dialogue requesting Bluetooth to be enabled if it isn't
        // already.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        }

        tv_is_support_peri = (TextView)findViewById(R.id.tv_is_support_peri);

        btn_is_support_peri = (Button)findViewById(R.id.btn_is_support_peri);
        btn_is_support_peri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothLeAdvertiser == null){
                    Log.e(TAG, "the device not support peripheral");
                    tv_is_support_peri.setText("peripheral is not supported by setting.");
                } else {
                    Log.e(TAG, "the device support peripheral mode");
                    tv_is_support_peri.setText("the device support peripheral mode.");
                }

                if (!mBluetoothAdapter.isMultipleAdvertisementSupported()){
                    Log.e(TAG, "the device not support MultipleAdvertisement");
                    tv_is_support_peri.setText("the device not support MultipleAdvertisement.");
                }
            }
        });

        btn_start_adv = (Button)findViewById(R.id.btn_start_adv);
        btn_start_adv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mBluetoothLeAdvertiser) {
                    Log.e(TAG, "startAdvertising ...");
                    tv_is_support_peri.setText("startAdvertising ...");
                    mBluetoothLeAdvertiser.startAdvertising(get_AdvertiseSetting(), get_AdvertiseData(), get_AdvertiseCallback());
                }
            }
        });

        btn_stop_adv = (Button)findViewById(R.id.btn_stop_adv);
        btn_stop_adv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAdvertise();
            }
        });

        //mUUID = new ParcelUuid(UUID.fromString(getString(R.string.ble_test_uuid)));
        mUUID = new ParcelUuid(UUID.fromString(gattServiceUUID));

        initgattServer(mBluetoothManager);


    }

    protected AdvertiseSettings get_AdvertiseSetting(){
        if (null != mBluetoothLeAdvertiser) {
            Log.e(TAG, "build AdvertiseSettings..");
            adv_setting = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(true)
                    .build();
        }

        return adv_setting;
    }

    protected AdvertiseData get_AdvertiseData(){
        if (null != mBluetoothLeAdvertiser) {
            Log.e(TAG, "build AdvertiseData..");
            adv_data = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .addServiceUuid(mUUID)
                    //.addServiceData(mUUID, "Data".getBytes(Charset.forName("UTF-8")))
                    .build();
        }

        return adv_data;
    }

    protected AdvertiseCallback get_AdvertiseCallback(){
        if (null != mBluetoothLeAdvertiser) {
            adv_callback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    Log.e(TAG, "Advertising onStartSuccess!!");
                    tv_is_support_peri.setText("Advertising onStartSuccess!!");
                    super.onStartSuccess(settingsInEffect);
                }

                @Override
                public void onStartFailure(int errorCode) {
                    Log.e(TAG, "Advertising onStartFailure: " + errorCode );
                    tv_is_support_peri.setText("Advertising onStartFailure: " + errorCode );
                    super.onStartFailure(errorCode);
                }
            };
        }

        return adv_callback;
    }

    private void stopAdvertise() {
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(get_AdvertiseCallback());
            mBluetoothLeAdvertiser = null;
        }
    }

    /**
     * Callback activated after the user responds to the enable Bluetooth dialogue.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        } else {
            Toast.makeText(this, "Bluetooth not enabled.", Toast.LENGTH_LONG).show();
        }
    }
}
