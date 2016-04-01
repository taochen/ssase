package org.ssase.sensor.control;

public class GzipCacheCountSensor extends DiskCacheSizeSensor {

	
	@Override
	public String[] getName() {
		return new String[]{"Gzip-maxEntriesLocalHeap"};
	}


}
