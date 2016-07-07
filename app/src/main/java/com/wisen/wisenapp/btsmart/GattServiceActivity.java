package com.wisen.wisenapp.btsmart;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.wisen.wisenapp.R;

import java.util.List;

public class GattServiceActivity extends AppCompatActivity {

    private final String TAG = "GattServiceActivity";

    private List<BluetoothGattCharacteristic> mGattCharacterList = null;
    private BluetoothDevice mDeviceToConnect = null;
    private ServiceInfo mServiceinfo = null;
    private BluetoothGattService mGattService = null;
    private BluetoothGatt mGatt = null;
    private BtSmartService mBTSmartService = null;
    //private List<BluetoothGattService> mGattServiceList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gatt_service);

        registerReceiver(mGattUpdateReceiver, BTSmartUtil.getmAdapterIntentFilter());

        Intent intent = getIntent();
        mDeviceToConnect = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
        Log.d(TAG,"device name="+mDeviceToConnect.getName());
        mServiceinfo = (ServiceInfo)intent.getParcelableExtra(ServiceInfo.ServiceInfoKeyString);
        //mGattService = mServiceinfo.getService();
        Log.d(TAG,"name="+mServiceinfo.getName());
        Log.d(TAG,"uuid="+mServiceinfo.getUUID());

        mGattService = BTSmartActivity.getGattService();
        if (null != mGattService)
            Log.d(TAG,"uuid="+mGattService.getUuid());
        mGatt = BTSmartActivity.getGatt();
        //mGattServiceList = BTSmartActivity.getGattServiceList();
        //if(null!=mGattService)
        //Log.d(TAG,"mGattService="+mGattService.toString());

        mBTSmartService = BTSmartActivity.getBTSmartService();

        mGattCharacterList = mGattService.getCharacteristics();
        for (BluetoothGattCharacteristic gattCharacteristic:mGattCharacterList) {
            mGatt.readCharacteristic(gattCharacteristic);
            int flag = gattCharacteristic.getProperties();
            final byte[] data = gattCharacteristic.getValue();
            if(data != null && data.length > 0){
                Log.d(TAG,
                        "---CharacterName:"
                                + new String(data));
            }
            Log.d(TAG,
                    "---CharacterName:"
                            + gattCharacteristic.getUuid());
            Log.d(TAG,
                    "---Characterflag:"
                            + flag);
            List<BluetoothGattDescriptor> listDescriptor = gattCharacteristic.getDescriptors();
            for (BluetoothGattDescriptor descriptor : listDescriptor) {

                Log.d(TAG,
                        "------Descriptor:"
                                + descriptor.getUuid());
                Log.d(TAG,
                        "------DescriptorPermission:"
                                + descriptor.getPermissions());;
            }
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BtSmartService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (BtSmartService.ACTION_GATT_DISCONNECTED.equals(action)) {
                finish();
            } else if (BtSmartService.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
            } else if (BtSmartService.ACTION_DATA_AVAILABLE.equals(action)) {
            }
        }
    };

    public void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
        Toast.makeText(this, "Disconnected from device:"+mDeviceToConnect.getName(), Toast.LENGTH_LONG).show();
        mGatt.close();
        if(null != mBTSmartService){
            mBTSmartService.disconnect();
        }
        super.onDestroy();
    }
}
