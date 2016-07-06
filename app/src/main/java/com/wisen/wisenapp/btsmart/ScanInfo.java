package com.wisen.wisenapp.btsmart;

/**
 * Created by wisen on 2016-07-01.
 */

/**
 * This is a public class for storing Scan result information. Public variables as it is only a structure to store data
 * and doesn't have any associated behaviour.
 *
 */
public class ScanInfo {
    public String name;
    public String address;
    public int appearanceImageResource;
    public int rssi;

    public ScanInfo(String name, String address, int resourceId, int rssi) {
        this.name = name;
        this.address = address;
        this.appearanceImageResource = resourceId;
        this.rssi = rssi;
    }
}