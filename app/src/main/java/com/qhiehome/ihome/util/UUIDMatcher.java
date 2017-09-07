package com.qhiehome.ihome.util;

import java.util.HashMap;
import java.util.UUID;

public final class UUIDMatcher {
	private HashMap<UUID, Integer> mUUIDMap = new HashMap<UUID, Integer>();

	public void addUUID(UUID paramUUID, int paramInt) {
		this.mUUIDMap.put(paramUUID, Integer.valueOf(paramInt));
	}

	public int matchUUID(UUID paramUUID) {
		if (this.mUUIDMap.containsKey(paramUUID)) {
			return mUUIDMap.get(paramUUID);
		} else {
			return -1;
		}
	}
}
