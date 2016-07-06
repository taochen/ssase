package edu.rice.rubis.beans;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.search.SearchException;
import net.sf.ehcache.search.attribute.AttributeExtractor;
import net.sf.ehcache.store.AuthoritativeTier;
import net.sf.ehcache.store.CachingTier;
import net.sf.ehcache.store.ElementValueComparator;
import net.sf.ehcache.store.Policy;
import net.sf.ehcache.store.StoreListener;
import net.sf.ehcache.store.StoreQuery;
import net.sf.ehcache.terracotta.TerracottaNotRunningException;
import net.sf.ehcache.writer.CacheWriterManager;

public class CacheStoreWrapper implements net.sf.ehcache.store.Store {

	private volatile long maxBytesLocalHeapWrapper = 0;
	private volatile long maxBytesLocalDiskWrapper = 0;

	net.sf.ehcache.store.CacheStore cache;
	CacheConfiguration config;

	// Memory
	CachingTier<Object, Element> cachingTier = null;
	//Disk
	AuthoritativeTier authoritativeTier = null;

	public CacheStoreWrapper(net.sf.ehcache.Cache original) {

		try {

			Field field = original.getClass().getDeclaredField("compoundStore");
			field.setAccessible(true);
			cache = (net.sf.ehcache.store.CacheStore) field
					.get(original);

			field = cache.getClass().getDeclaredField("cachingTier");
			field.setAccessible(true);
			cachingTier = (CachingTier) field.get(cache);

			field = cache.getClass().getDeclaredField("authoritativeTier");
			field.setAccessible(true);
			authoritativeTier = (AuthoritativeTier) field.get(cache);

		} catch (Exception e) {
			e.printStackTrace();
		}

	
		this.config = original.getCacheConfiguration();
	}
	
	public long getDiskBytes(){
		return authoritativeTier.getOnDiskSizeInBytes();
	}
	
	public long getHeapBytes(){
		
		if (maxBytesLocalHeapWrapper != 0){
			return cache.getInMemorySizeInBytes();
		}
		
		return cachingTier.getInMemorySizeInBytes();
	}

	public void setMaxBytesLocalHeapWrapper(long maxBytesLocalHeapWrapper) {

		if (maxBytesLocalHeapWrapper != 0) {
			config.setMaxBytesLocalHeap(maxBytesLocalHeapWrapper);	
		} else {
			cachingTier.clear();
			
		}

		this.maxBytesLocalHeapWrapper = maxBytesLocalHeapWrapper;
	}

	public void setMaxBytesLocalDiskWrapper(long maxBytesLocalDiskWrapper) {

		if (maxBytesLocalDiskWrapper != 0) {
			config.setMaxBytesLocalDisk(maxBytesLocalDiskWrapper);			
		} else {
			authoritativeTier.removeAll();
			
		}

		this.maxBytesLocalDiskWrapper = maxBytesLocalDiskWrapper;
	}

	public void addStoreListener(StoreListener listener) {
		// TODO Auto-generated method stub
		cache.addStoreListener(listener);
	}

	public void removeStoreListener(StoreListener listener) {
		// TODO Auto-generated method stub
		cache.removeStoreListener(listener);
	}

	public boolean put(final Element element) throws CacheException {
		if (maxBytesLocalHeapWrapper == 0) {
			cachingTier.clear();
			try {
				//System.out.print(authoritativeTier.getInMemorySizeInBytes()+ "\n");
				
				return authoritativeTier.put(element);
			} catch (RuntimeException e) {
				authoritativeTier.flush(element);
				throw e;
			} finally {
				try {
					authoritativeTier.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cachingTier.remove(element.getObjectKey());
			}

		} else if (maxBytesLocalDiskWrapper == 0) {
			authoritativeTier.removeAll();
			if (cachingTier.remove(element.getObjectKey()) != null
					|| cachingTier.loadOnPut()) {
				try {
					final boolean[] hack = new boolean[1];
					if (cachingTier.get(element.getObjectKey(),
							new Callable<Element>() {
								public Element call() throws Exception {
									// final Lock lock = daLock.readLock();
									// lock.lock();
									// try {
									// //hack[0] =
									// authoritativeTier.putFaulted(element);
									// return element;
									// } finally {
									// lock.unlock();
									// }
									return element;
								}
							}, false) == element) {
						return hack[0];
					}
				} catch (Throwable e) {
					cachingTier.remove(element.getObjectKey());
					if (e instanceof RuntimeException) {
						throw (RuntimeException) e;
					}
					throw new CacheException(e);
				}
			}
		}

		return cache.put(element);
	}

	public void putAll(Collection<Element> elements) throws CacheException {
		// TODO Auto-generated method stub
		cache.putAll(elements);
	}

	public boolean putWithWriter(Element element,
			CacheWriterManager writerManager) throws CacheException {
		// TODO Auto-generated method stub
		return cache.putWithWriter(element, writerManager);
	}

	public Element get(Object key) {
		// TODO Auto-generated method stub
		return cache.get(key);
	}

	public Element getQuiet(Object key) {
		// TODO Auto-generated method stub
		return cache.getQuiet(key);
	}

	public List getKeys() {
		// TODO Auto-generated method stub
		return cache.getKeys();
	}

	public Element remove(Object key) {
		// TODO Auto-generated method stub
		return cache.remove(key);
	}

	public void removeAll(Collection<?> keys) {
		// TODO Auto-generated method stub
		cache.removeAll(keys);
	}

	public Element removeWithWriter(Object key, CacheWriterManager writerManager)
			throws CacheException {
		// TODO Auto-generated method stub
		return cache.removeWithWriter(key, writerManager);
	}

	public void removeAll() throws CacheException {
		// TODO Auto-generated method stub
		cache.removeAll();
	}

	public Element putIfAbsent(Element element) throws NullPointerException {
		// TODO Auto-generated method stub
		return cache.putIfAbsent(element);
	}

	public Element removeElement(Element element,
			ElementValueComparator comparator) throws NullPointerException {
		// TODO Auto-generated method stub
		return cache.removeElement(element, comparator);
	}

	public boolean replace(Element old, Element element,
			ElementValueComparator comparator) throws NullPointerException,
			IllegalArgumentException {
		// TODO Auto-generated method stub
		return cache.replace(old, element, comparator);
	}

	public Element replace(Element element) throws NullPointerException {
		// TODO Auto-generated method stub
		return cache.replace(element);
	}

	public synchronized void dispose() {
		// TODO Auto-generated method stub
		cache.dispose();
	}

	public int getSize() {
		// TODO Auto-generated method stub
		return cache.getSize();
	}

	public int getInMemorySize() {
		// TODO Auto-generated method stub
		return cache.getInMemorySize();
	}

	public int getOffHeapSize() {
		// TODO Auto-generated method stub
		return cache.getOffHeapSize();
	}

	public int getOnDiskSize() {
		// TODO Auto-generated method stub
		return cache.getOnDiskSize();
	}

	public int getTerracottaClusteredSize() {
		// TODO Auto-generated method stub
		return cache.getTerracottaClusteredSize();
	}

	public long getInMemorySizeInBytes() {
		// TODO Auto-generated method stub
		return cache.getInMemorySizeInBytes();
	}

	public long getOffHeapSizeInBytes() {
		// TODO Auto-generated method stub
		return cache.getOffHeapSizeInBytes();
	}

	public long getOnDiskSizeInBytes() {
		// TODO Auto-generated method stub
		return cache.getOnDiskSizeInBytes();
	}

	public boolean hasAbortedSizeOf() {
		// TODO Auto-generated method stub
		return cache.hasAbortedSizeOf();
	}

	public Status getStatus() {
		// TODO Auto-generated method stub
		return cache.getStatus();
	}

	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return cache.containsKey(key);
	}

	public boolean containsKeyOnDisk(Object key) {
		// TODO Auto-generated method stub
		return cache.containsKeyOnDisk(key);
	}

	public boolean containsKeyOffHeap(Object key) {
		// TODO Auto-generated method stub
		return cache.containsKeyOffHeap(key);
	}

	public boolean containsKeyInMemory(Object key) {
		// TODO Auto-generated method stub
		return cache.containsKeyInMemory(key);
	}

	public void expireElements() {
		// TODO Auto-generated method stub
		cache.expireElements();
	}

	public void flush() throws IOException {
		// TODO Auto-generated method stub
		cache.flush();
	}

	public boolean bufferFull() {
		// TODO Auto-generated method stub
		return cache.bufferFull();
	}

	public Policy getInMemoryEvictionPolicy() {
		// TODO Auto-generated method stub
		return cache.getInMemoryEvictionPolicy();
	}

	public void setInMemoryEvictionPolicy(Policy policy) {
		// TODO Auto-generated method stub
		cache.setInMemoryEvictionPolicy(policy);
	}

	public Object getInternalContext() {
		// TODO Auto-generated method stub
		return cache.getInternalContext();
	}

	public boolean isCacheCoherent() {
		// TODO Auto-generated method stub
		return cache.isCacheCoherent();
	}

	public boolean isClusterCoherent() throws TerracottaNotRunningException {
		// TODO Auto-generated method stub
		return cache.isClusterCoherent();
	}

	public boolean isNodeCoherent() throws TerracottaNotRunningException {
		// TODO Auto-generated method stub
		return cache.isNodeCoherent();
	}

	public void setNodeCoherent(boolean coherent)
			throws UnsupportedOperationException, TerracottaNotRunningException {
		// TODO Auto-generated method stub
		cache.setNodeCoherent(coherent);
	}

	public void waitUntilClusterCoherent()
			throws UnsupportedOperationException,
			TerracottaNotRunningException, InterruptedException {
		// TODO Auto-generated method stub
		cache.waitUntilClusterCoherent();
	}

	public Object getMBean() {
		// TODO Auto-generated method stub
		return cache.getMBean();
	}

	public void setAttributeExtractors(
			Map<String, AttributeExtractor> extractors) {
		// TODO Auto-generated method stub
		cache.setAttributeExtractors(extractors);
	}

	public Results executeQuery(StoreQuery query) throws SearchException {
		// TODO Auto-generated method stub
		return cache.executeQuery(query);
	}

	public <T> Attribute<T> getSearchAttribute(String attributeName) {
		// TODO Auto-generated method stub
		return cache.getSearchAttribute(attributeName);
	}

	public Set<Attribute> getSearchAttributes() {
		// TODO Auto-generated method stub
		return cache.getSearchAttributes();
	}

	public Map<Object, Element> getAllQuiet(Collection<?> keys) {
		// TODO Auto-generated method stub
		return cache.getAllQuiet(keys);
	}

	public Map<Object, Element> getAll(Collection<?> keys) {
		// TODO Auto-generated method stub
		return cache.getAll(keys);
	}

	public void recalculateSize(Object key) {
		// TODO Auto-generated method stub
		cache.recalculateSize(key);
	}

}
