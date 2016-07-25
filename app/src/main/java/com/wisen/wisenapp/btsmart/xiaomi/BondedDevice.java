package com.wisen.wisenapp.btsmart.xiaomi;

/**
 * Created by wisen on 2016-07-25.
 */
public class BondedDevice {

    private String Address;
    private String Name;

    public BondedDevice() {}

    public BondedDevice(String address) {
        Address = address;
    }

    public BondedDevice(String address, String name) {
        Address = address;
        Name = name;
    }

    public String getAddress() {
        return Address;
    }

    public String getName() {
        return Name;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public void setName(String name) {
        Name = name;
    }

    @Override
    public String toString() {
        return "BondedDevice{" +
                "Address='" + Address + '\'' +
                ", Name='" + Name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof BondedDevice) {
            BondedDevice device = (BondedDevice) o;
            return this.Address.equals(device.Address)
                    && this.Name.equals(device.Name);
        }

        return super.equals(o);
    }
}
