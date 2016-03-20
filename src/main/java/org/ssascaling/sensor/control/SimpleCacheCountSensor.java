package org.ssascaling.sensor.control;

public class SimpleCacheCountSensor extends DiskCacheSizeSensor{
	

	@Override
	public String[] getName() {
		return new String[]{"Simple-maxEntriesLocalHeap"};
	}


}
