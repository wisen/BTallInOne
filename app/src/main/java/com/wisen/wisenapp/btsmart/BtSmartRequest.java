package com.wisen.wisenapp.btsmart;

import android.os.Handler;

import java.util.UUID;

/**
 * Created by wisen on 2016-07-01.
 */
// A simple data structure to be used in the request queue.
class BtSmartRequest {
    public enum RequestType {
        CHARACTERISTIC_NOTIFICATION, READ_CHARACTERISTIC, READ_DESCRIPTOR, READ_RSSI, WRITE_CHARACTERISTIC, WRITE_DESCRIPTOR
    };

    public RequestType type;
    public UUID serviceUuid;
    public UUID characteristicUuid;
    public UUID descriptorUuid;
    public Handler notifyHandler;
    public int requestId;
    public byte[] value;

    public BtSmartRequest(RequestType type, int requestId, UUID service, UUID characteristic, UUID descriptor,
                          Handler handler) {
        this.type = type;
        this.serviceUuid = service;
        this.characteristicUuid = characteristic;
        this.descriptorUuid = descriptor;
        this.notifyHandler = handler;
    }

    public BtSmartRequest(RequestType type, int requestId, UUID service, UUID characteristic, UUID descriptor,
                          Handler handler, byte[] value) {
        this.type = type;
        this.serviceUuid = service;
        this.characteristicUuid = characteristic;
        this.descriptorUuid = descriptor;
        this.notifyHandler = handler;
        this.value = value;
    }
}