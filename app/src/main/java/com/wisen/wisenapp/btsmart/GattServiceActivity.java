package com.wisen.wisenapp.btsmart;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.wisen.wisenapp.R;

import java.util.List;

public class GattServiceActivity extends AppCompatActivity {

    private final String TAG = "GattServiceActivity";

    private List<BluetoothGattCharacteristic> mGattCharacterList = null;
    private BluetoothDevice mDeviceToConnect = null;
    private ServiceInfo mServiceinfo = null;
    private BluetoothGattService mGattService = null;
    //private List<BluetoothGattService> mGattServiceList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gatt_service);

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
        //mGattServiceList = BTSmartActivity.getGattServiceList();
        //if(null!=mGattService)
        //Log.d(TAG,"mGattService="+mGattService.toString());

        mGattCharacterList = mGattService.getCharacteristics();
        for (int j = 0; j < mGattCharacterList.size(); j++) {
            Log.d(TAG,
                    "---CharacterName:"
                            + mGattCharacterList.get(j).getUuid());
        }
    }
}
