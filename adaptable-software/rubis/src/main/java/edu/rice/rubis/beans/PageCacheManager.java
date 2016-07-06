package edu.rice.rubis.beans;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.apache.catalina.Container;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class PageCacheManager {

	private final static PageCacheManager manager = new PageCacheManager();
	private final Map<String, CacheStoreWrapper> map = new HashMap<String, CacheStoreWrapper>();
	private CacheManager cacheManager;
	
	private StandardContext context;
	private HashMap filterConfigs;
	private HashMap backupfilterConfigs;
	// The first filter in the xml file is default.
	// as maxThread, but it can not exceed maxSpare.
	/**********
	 * 
	 * Need to be changed manually.
	 * 
	 */
	private volatile int mode = 1;

	private final String[] caches = new String[] { "SimplePageCachingFilter",
			"SimplePageFragmentCachingFilter" };

	private final long DEAFULT_TIME_TO_LIVE_SEC = 300;
	private final long DEAFULT_TIME_TO_IDLE_SEC = 300;

	private final long DEAFULT_MAX_HEAP_BYTES = 5242880;
	private final long DEAFULT_MAX_DISK_BYTES = 5242880;

	public static PageCacheManager getInstance() {
		return manager;
	}

	public void init(ServletContext sc) {
		if (cacheManager != null) return;
		
		 
		
		cacheManager = CacheManager.create();

		for (String c : caches) {
			this.wrapCacheStore(this.createCache(c));
		}
		
		if (sc != null) {

			StandardEngine engine = (StandardEngine) ServerFactory.getServer()
					.findService("Catalina").getContainer();
			Container container = engine.findChild(engine.getDefaultHost());
			context = (StandardContext) container
					.findChild(sc.getContextPath());

		}

	}
	
	public String[] getFilterNames(){
		return caches;
	}

	// The run has been sync in the sensors.
	public int currentMode() {
		return mode;
	}

	public long currentMemorySize() {
		String c = getCacheNameByMode();
		if (c == null) {
			return 0;
		}

		return map.get(c).getHeapBytes();
	}

	public long currentDiskSize() {
		String c = getCacheNameByMode();
		if (c == null) {
			return 0;
		}

		return map.get(c).getDiskBytes();
	}

	public synchronized void changeMode(int mode) {
		this.mode = mode;
	}

	// public synchronized void changeMaxEntity() {
	//
	// }

	// public synchronized void changeMaxMemorySize(){
	//
	// }
	//
	//
	// public synchronized void changeMaxDiskSize(){
	//
	// }

	public synchronized void changeMaxMemoryAndDiskSize(long m, long d) {

		String c = getCacheNameByMode();
		if (c == null) {
			return;
		}

		map.get(c).setMaxBytesLocalHeapWrapper(m);
		map.get(c).setMaxBytesLocalDiskWrapper(d);
	}
	
	public synchronized void changeMaxMemorySize(long value) {

		String c = getCacheNameByMode();
		if (c == null) {
			return;
		}

		map.get(c).setMaxBytesLocalHeapWrapper(value);
	}

	public synchronized void changeMaxDiskSize(long value) {

		String c = getCacheNameByMode();
		if (c == null) {
			return;
		}
		map.get(c).setMaxBytesLocalDiskWrapper(value);
	}
	
	private Cache createCache(String name) {

		CacheConfiguration c = new CacheConfiguration()
				.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
				.eternal(false)
				.timeToLiveSeconds(DEAFULT_TIME_TO_LIVE_SEC)
				.timeToIdleSeconds(DEAFULT_TIME_TO_IDLE_SEC)
				.diskExpiryThreadIntervalSeconds(0)
				.persistence(
						new PersistenceConfiguration()
								.strategy(Strategy.LOCALTEMPSWAP));

		c.setName(name);
		// Create a Cache specifying its configuration.
		Cache cache = new Cache(c);

		cache.getCacheConfiguration().setMaxBytesLocalHeap(
				DEAFULT_MAX_HEAP_BYTES);
		cache.getCacheConfiguration().setMaxBytesLocalDisk(
				DEAFULT_MAX_DISK_BYTES);

		System.out.print("************* Add cache " + name +  " : " + cache.hashCode() + "\n");
		cacheManager.addCache(cache);

		return cache;
	}

	private void wrapCacheStore(Cache cache) {
		CacheStoreWrapper cw = null;

		try {

			Field field = cache.getClass().getDeclaredField("compoundStore");
			field.setAccessible(true);


			cw = new CacheStoreWrapper(cache);
			cw.setMaxBytesLocalHeapWrapper(DEAFULT_MAX_HEAP_BYTES);
			cw.setMaxBytesLocalDiskWrapper(DEAFULT_MAX_DISK_BYTES);

			field.set(cache, cw);
			field.setAccessible(false);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		map.put(cache.getName(), cw);
	}
	
	public int getMode(){
		return mode;
	}
	
	/**
	 * This is only used for testing.
	 * @param name
	 * @param e
	 */
	public void add(String name, Element e) {
		cacheManager.getCache(name).put(e);
	}

	public String getCacheNameByMode() {
		switch (mode) {
		case 0:
			return null;
		case 1:
			return "SimplePageCachingFilter";
		case 2:
			return "SimplePageFragmentCachingFilter";
		}

		return null;
	}
	
	public synchronized void changeCacheMode(long value) {
		
		//synchronized (m) {

			if (null != getCacheNameByMode()) {			
				filterConfigs.remove(getCacheNameByMode());
			}
			changeMode((int) value);
			if (null != getCacheNameByMode()) {
				System.out.print("**************Adding back " + getCacheNameByMode() + " : " + backupfilterConfigs.get(getCacheNameByMode()) + "\n");
				filterConfigs.put(getCacheNameByMode(), backupfilterConfigs.get(getCacheNameByMode()));
				System.out.print("**************Adding back " + filterConfigs.size() + "\n");
			}

		//}
	}
	
	public void initFilter(){
		

		try {
			Field field = context.getClass().getDeclaredField("filterConfigs");
			field.setAccessible(true);
			filterConfigs = (HashMap) field.get(context);
			field.setAccessible(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if (backupfilterConfigs == null) {
			backupfilterConfigs = new HashMap();
			for (Object key : filterConfigs.keySet()) {
				backupfilterConfigs.put(key, filterConfigs.get(key));
			}
		}
		
		
		for (String name : getFilterNames()) {
			
			if (context.findFilterDef(name) == null || 
					name.equals(getCacheNameByMode())) {
				System.out.print("Keep " + name + "\n");
				continue;
			}
			
				System.out.print("Remove " + name +  " : " + context.findFilterDef(name) + "\n");
				filterConfigs.remove(name);		 
			
		}
		
	
	}

	
	public void printNumberOfCache() {
		String[] names = cacheManager.getCacheNames();
		System.out.print("There are total " + names.length + " caches \n");
		for (String n : names) {

			if (cacheManager.getCache(n) != null)
				System.out.print("Cache: " + n + " : "
						+ cacheManager.getCache(n).hashCode() + "\n");
			else {
				System.out.print("EhCache: " + n + " : "
						+ cacheManager.getEhcache(n).hashCode() + "\n");

				try {
					Field field = cacheManager.getEhcache(n).getClass().getSuperclass()
							.getDeclaredField("underlyingCache");
					field.setAccessible(true);
					Object value = field.get(cacheManager.getEhcache(n));
					field.setAccessible(false);
					System.out.print("Underlying cache: " + n + " : "
							+ value.hashCode() + "\n");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
