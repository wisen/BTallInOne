package com.wisen.wisenapp.pbap;

/**
 * Created by wisen on 2016-07-27.
 */
public class ContactInfo {

    private String name;
    private String phoneNum;
    private ContactType type;

    public ContactInfo(String name, String phoneNum, ContactType type) {
        this.name = name;
        this.phoneNum = phoneNum;
        this.type = type;
    }

    public ContactType getType() {
        return type;
    }

    public void setType(ContactType type) {
        this.type = type;
    }

    public enum ContactType{
        PHONE, EMAIL,
    };

    public ContactInfo(String name, String phoneNum) {
        this.name = name;
        this.phoneNum = phoneNum;
    }

    public ContactInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
}
