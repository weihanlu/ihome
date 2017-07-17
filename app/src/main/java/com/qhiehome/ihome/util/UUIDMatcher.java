package com.qhiehome.ihome.util;

import java.util.HashMap;
import java.util.UUID;

public class UUIDMatcher {

    private HashMap<UUID, Integer> mUUIDMap = new HashMap<>();

    public void addUUID(UUID paramUUID, int paramInt) {
        this.mUUIDMap.put(paramUUID, paramInt);
    }

    public int matchUUID(UUID paramUUID) {
        if (mUUIDMap.containsKey(paramUUID)) {
            return mUUIDMap.get(paramUUID);
        } else {
            return -1;
        }
    }

}
