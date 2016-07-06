package com.wisen.wisenapp.btsmart;

import android.os.Handler;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by wisen on 2016-07-01.
 */
public class CharacteristicHandlersContainer {
    private HashMap<UUID, HashMap<UUID, Handler>> mHandlers = new HashMap<UUID, HashMap<UUID, Handler>>();

    public void addHandler(UUID service, UUID characteristic, Handler notifyHandler) {
        HashMap<UUID, Handler> subMap = mHandlers.get(service);
        if (subMap == null) {
            subMap = new HashMap<UUID, Handler>();
            mHandlers.put(service, subMap);
        }
        subMap.put(characteristic, notifyHandler);
    }

    public void removeHandler(UUID service, UUID characteristic) {
        HashMap<UUID, Handler> subMap = mHandlers.get(service);
        if (subMap != null) {
            subMap.remove(characteristic);
        }
    }

    public Handler getHandler(UUID service, UUID characteristic) {
        HashMap<UUID, Handler> subMap = mHandlers.get(service);
        if (subMap == null) {
            return null;
        }
        return subMap.get(characteristic);
    }

}