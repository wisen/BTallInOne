package com.wisen.wisenapp.btsmart;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.IntentFilter;

/**
 * Created by wisen on 2016-07-07.
 */
public class BTSmartUtil {

    private final static IntentFilter mAdapterIntentFilter = new IntentFilter();

    public static void initAdapterIntentFilter(){
        mAdapterIntentFilter.addAction(BtSmartService.ACTION_GATT_CONNECTED);
        mAdapterIntentFilter.addAction(BtSmartService.ACTION_DATA_AVAILABLE);
        mAdapterIntentFilter.addAction(BtSmartService.ACTION_GATT_DISCONNECTED);
        mAdapterIntentFilter.addAction(BtSmartService.ACTION_GATT_SERVICES_DISCOVERED);
    }

    public static IntentFilter getmAdapterIntentFilter(){
        initAdapterIntentFilter();
        return mAdapterIntentFilter;
    }

    public static String getPropertiesStr(int properties){
        String str = "";
        if ((properties & BluetoothGattCharacteristic.PROPERTY_BROADCAST) != 0)
            str += 'b';//brocast
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0)
            str += 'r';//read
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0)
            str += 'w';//write
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0)
            str += 'n';//notification
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0)
            str += 'i';//indication

        return str;
    }

}
