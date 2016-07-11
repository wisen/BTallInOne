package com.wisen.wisenapp.btsmart;

/**
 * Created by wisen on 2016-07-11.
 */
public class CharacteristicInfo {

    private String UUID;
    private String Properties;
    private String Values;

    public String getValues() {
        return Values;
    }

    public String getUUID() {
        return UUID;
    }

    public String getProperties() {
        return Properties;
    }

    public CharacteristicInfo(String UUID, String Properties, String Values) {
        super();
        this.UUID = UUID;
        this.Properties = Properties;
        this.Values = Values;
    }

    @Override
    public boolean equals(Object o) {
        boolean flag = o instanceof CharacteristicInfo;
        if(false == flag)
            return flag;

        CharacteristicInfo info = (CharacteristicInfo)o;

        if(this.getUUID().equals(info.getUUID())) {
            flag = true;
        }

        return flag;
    }
}
