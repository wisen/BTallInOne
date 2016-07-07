package com.wisen.wisenapp.btsmart;

import android.bluetooth.BluetoothGattService;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by wisen on 2016-07-06.
 */
public class ServiceInfo implements Parcelable {

    public static final String ServiceInfoKeyString = "com.wisen.wisenapp.btsmart.SERVICEINFO";
    private String TAG = "ServiceInfo";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }
/*
    public BluetoothGattService getService() {
        return service;
    }

    public void setService(BluetoothGattService service) {
        this.service = service;
    }
*/
    private String name;
    private String UUID;
   // private BluetoothGattService service;

    public ServiceInfo(String name, String UUID/*, BluetoothGattService service*/) {
        super();
        //setService(service);
        //setName(name);
        //setUUID(UUID);
        this.name = name;
        this.UUID = UUID;
  //      this.service = service;
//        Log.d(TAG, "theService:" + getService().toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ServiceInfo> CREATOR =
            new Parcelable.Creator<ServiceInfo>() {
                public ServiceInfo createFromParcel(Parcel in) {
                    return new ServiceInfo(in.readString(), in.readString()/*, null*/);
                }
                public ServiceInfo[] newArray(int size) {
                    return new ServiceInfo[size];
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(UUID);
        //dest.writeValue(service);
    }
}
