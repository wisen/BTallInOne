package com.wisen.wisenapp.btsmart;

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

}
