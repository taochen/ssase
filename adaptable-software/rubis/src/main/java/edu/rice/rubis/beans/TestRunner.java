package edu.rice.rubis.beans;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import net.sf.ehcache.store.cachingtier.OnHeapCachingTier;

import edu.rice.rubis.beans.StandardThreadExecutorWrapper.TaskQueue;
import edu.rice.rubis.beans.StandardThreadExecutorWrapper.TaskThreadFactory;

public class TestRunner extends ThreadPoolExecutor {
	public TestRunner(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory);
		// TODO Auto-generated constructor stub
	}

	protected AtomicInteger submittedTasksCount;
  

    protected void afterExecute(Runnable r, Throwable t) {
       // super.afterExecute(r, t);
       
            System.out.println("After exection");
        
    }
    
    static class TaskQueue extends LinkedBlockingQueue<Runnable> {
        ThreadPoolExecutor parent = null;

        public TaskQueue() {
            super();
        }

        public TaskQueue(int capacity) {
            super(capacity);
        }

        public TaskQueue(Collection<? extends Runnable> c) {
            super(c);
        }

        public void setParent(ThreadPoolExecutor tp) {
            parent = tp;
        }
        
        public boolean force(Runnable o) {
            if ( parent.isShutdown() ) throw new RejectedExecutionException("Executor not running, can't force a command into the queue");
            return super.offer(o); //forces the item onto the queue, to be used if the task is rejected
        }

        public boolean force(Runnable o, long timeout, TimeUnit unit) throws InterruptedException {
            if ( parent.isShutdown() ) throw new RejectedExecutionException("Executor not running, can't force a command into the queue");
            return super.offer(o,timeout,unit); //forces the item onto the queue, to be used if the task is rejected
        }

        public boolean offer(Runnable o) {
            //we can't do any checks
            if (parent==null) return super.offer(o);
            int poolSize = parent.getPoolSize();
            //we are maxed out on threads, simply queue the object
            if (parent.getPoolSize() == parent.getMaximumPoolSize()) return super.offer(o);
            //we have idle threads, just add it to the queue
            //note that we don't use getActiveCount(), see BZ 49730
			//AtomicInteger submittedTasksCount = TestRunner.this.submittedTasksCount;
			//if(submittedTasksCount!=null) {
				//if (submittedTasksCount.get()<=poolSize) return super.offer(o);
			//}
            //if we have less threads than maximum force creation of a new thread
            if (poolSize<parent.getMaximumPoolSize()) return false;
            //if we reached here, we need to add it to the queue
            return super.offer(o);
        }
    }

    // ---------------------------------------------- ThreadFactory Inner Class
   static class TaskThreadFactory implements ThreadFactory {
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        TaskThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement());
           // t.setDaemon(daemon);
           // t.setPriority(getThreadPriority());
            return t;
        }
    }
   
   public static void main1( String [] args) {
	   
	   double d = 170;
	 
	   System.out.print((long)d/100);
	   
		TaskQueue taskqueue = new TaskQueue(Integer.MAX_VALUE);
        TaskThreadFactory tf = new TaskThreadFactory("rr");
	   
	   ThreadPoolExecutor th =   new ThreadPoolExecutor(10, 20, 0, TimeUnit.MILLISECONDS,taskqueue, tf) {
       	
       	
			
	        	
	        	 public void setMaximumPoolSize(int maximumPoolSize) {
	        		 if (maximumPoolSize > this.getMaximumPoolSize()) {
	        			 lock();
	        			 List<Runnable> list = this.drainQueue();
	        			 for (Runnable command : list) {
	        			     this.execute(command);
	        			 }
	        			 
	        			 unlock();
	        		 } else {
	        			 super.setMaximumPoolSize(maximumPoolSize);
	        		 }
	        	 }
	        	
	        	private Field getMainLock() throws NoSuchFieldException {
	        	    Field mainLock = super.getClass().getDeclaredField("mainLock");
	        	    mainLock.setAccessible(true);
	        	    return mainLock;
	        	}
	        	
	        	
	        	private void lock() {
	        	    try {
	        	        Field mainLock = getMainLock();
	        	        Method lock = mainLock.getType().getDeclaredMethod("lock", (Class[])null);
	        	        lock.setAccessible(true);
	        	        lock.invoke(mainLock.get(this), (Object[])null);
	        	    } catch (Exception e){
	        	        e.printStackTrace();
	        	    } 
	        	}
	        	
	        	

	        	private void unlock() {
	        	    try {
	        	        Field mainLock = getMainLock();
	        	        Method unlock = mainLock.getType().getDeclaredMethod("unlock", (Class[])null);
	        	        unlock.setAccessible(true);
	        	        unlock.invoke(mainLock.get(this), (Object[])null);
	        	    } catch (Exception e){
	        	        e.printStackTrace();
	        	    } 
	        	}
	        	
	        	   private List<Runnable> drainQueue() {
	        	        BlockingQueue<Runnable> q = this.getQueue();
	        	        List<Runnable> taskList = new ArrayList<Runnable>();
	        	        q.drainTo(taskList);
	        	        if (!q.isEmpty()) {
	        	            for (Runnable r : q.toArray(new Runnable[0])) {
	        	                if (q.remove(r))
	        	                    taskList.add(r);
	        	            }
	        	        }
	        	        return taskList;
	        	    }
	        	
	        };
	        
	        
	        th.setMaximumPoolSize(30);
   }

    public static void main( String [] args) {
//    	
//    	 TaskQueue taskqueue = new TaskQueue(10000);
//	        TaskThreadFactory tf = new TaskThreadFactory("ddd");
//	       // lifecycle.fireLifecycleEvent(START_EVENT, null);
//	        ThreadPoolExecutor tr = new ThreadPoolExecutor(1,1, 1000, TimeUnit.MILLISECONDS, taskqueue, tf) {
//	        	protected void beforeExecute(Thread t, Runnable r) { 
//	        		super.beforeExecute(t, r);
//	        		System.out.print(r + "before *********\n");
//	        	}
//	        	
//	        	@Override
//				protected void afterExecute(Runnable r, Throwable t) {
//					super.afterExecute(r, t);
//					
//					
//				
//System.out.print(r + "after *********\n");
//					
//				}
//	        };
//	        taskqueue.setParent( (ThreadPoolExecutor) tr);
//	        
//	        tr.execute(new Runnable() {
//                public void run() {
//                    try {
//						Thread.sleep((long)5000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//                }
//            }
//    );
//	        
//        tr.shutdown();
    	
//    	CacheManager manager = CacheManager.create();
//       	CacheConfiguration c = new CacheConfiguration()
//	    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
//	    .eternal(false)
//	    .timeToLiveSeconds(60)
//	    .timeToIdleSeconds(30)
//	    .diskExpiryThreadIntervalSeconds(0).persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP));
//		    		
//    	c.setName("ttt");
//		//Create a Cache specifying its configuration.
//		Cache testCache = new Cache(c
//		 );
//		
//		
//		 //c.getMaxEntriesLocalHeap()
//		 // testCache.getCacheConfiguration().setMaxEntriesLocalHeap(1);
//		 testCache.getCacheConfiguration().setMaxBytesLocalHeap((long)10000);
//		//testCache.getCacheConfiguration().setMaxEntriesLocalDisk((long)10000);
//		testCache.getCacheConfiguration().setMaxBytesLocalDisk((long)20000);
//		  manager.addCache(testCache);
//		  
//		
//		 // testCache.flush();
//		  //testCache.put(new Element(3,3));
////		  System.out.print("Memory Cache number " + testCache.getStatistics().getLocalHeapSize()+ "\n");
////		  System.out.print("Memory Cache size " + testCache.getStatistics().getLocalHeapSizeInBytes() + "\n");
////		//  testCache.getCacheConfiguration().setMaxBytesLocalHeap((long)10000);
////		
//////		  
////		  System.out.print("Disk Cache number " + testCache.getStatistics().getLocalDiskSize()+ "\n");
////		  System.out.print("Disk Cache size " + testCache.getStatistics().getLocalDiskSizeInBytes() + "\n");
//		 // testCache.getCacheConfiguration().setMaxBytesLocalDisk((long)10000);
////		  c.setOverflowToDisk(false);
////	    	c.diskPersistent(false);
//		  net.sf.ehcache.store.CacheStore cs = null;
//		  CacheStoreWrapper cw = null;
//		  //c.setOverflowToDisk(false);
//		try {
////			Field field = testCache.getCacheConfiguration().getClass().getDeclaredField("onHeapPoolUsage");
////			field.setAccessible(true);
////			//System.out.print(field.get(testCache.getCacheConfiguration())+ "***\n");
////			field.set(testCache.getCacheConfiguration(), null);
////			field.setAccessible(false);
////			
////			field = testCache.getCacheConfiguration().getClass().getDeclaredField("maxBytesLocalHeap");
////			field.setAccessible(true);
////			field.set(testCache.getCacheConfiguration(), null);
////			field.setAccessible(false);
//			
//			Field field = testCache.getClass().getDeclaredField("cacheStatus");
//			field.setAccessible(true);
//			
//			Method m = field.get(testCache).getClass().getMethod("changeState", Status.class);
//			m.setAccessible(true);
//			
//			
//			field = testCache.getClass().getDeclaredField("compoundStore");
//			field.setAccessible(true);
//			cs = (net.sf.ehcache.store.CacheStore)field.get(testCache);
//		
//
//			//  System.out.print("Memory Cache number " + cs.getInMemorySizeInBytes()+ "\n");
//			 // System.out.print("Memory Cache size " + testCache.getStatistics().getLocalHeapSizeInBytes() + "\n");
//			//  testCache.getCacheConfiguration().setMaxBytesLocalHeap((long)10000);
//			
////			  
//			 // System.out.print("Disk Cache number " + cs.getOnDiskSizeInBytes()+ "\n");
//			  //System.out.print("Disk Cache size " + testCache.getStatistics().getLocalDiskSizeInBytes() + "\n");
//			//m.invoke(field.get(testCache), Status.STATUS_UNINITIALISED);
//			
//			
//			  cw = new CacheStoreWrapper(testCache);
//			  cw.setMaxBytesLocalHeapWrapper((long)10000);
//			  cw.setMaxBytesLocalDiskWrapper((long)20000);
//			
//			  field.set(testCache, cw);
//			  field.setAccessible(false);
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		PageCacheManager.getInstance().init(null);
		Ehcache cache = CacheManager.create().getEhcache(
				PageCacheManager.getInstance().getCacheNameByMode());
		if (!(cache instanceof BlockingCache)) {
			// decorate and substitute
			BlockingCache newBlockingCache = new BlockingCache(cache);
			CacheManager.create().replaceCacheWithDecoratedCache(cache,
					newBlockingCache);
		}
		BlockingCache blockingCache = (BlockingCache) CacheManager
				.create()
				.getEhcache(PageCacheManager.getInstance().getCacheNameByMode());
 
		  for(int i = 0; i < 200;i++) {
			  blockingCache.put(new Element(i,i));
			  //PageCacheManager.getInstance().add(PageCacheManager.getInstance().getCacheNameByMode(), new Element(i,i));
		  }
		
		  System.out.print("Memory Cache number " + PageCacheManager.getInstance().currentMemorySize()+ "\n");
		 // System.out.print("Actual Memory Cache number " + cw.getHeapBytes()+ "\n");
			 // System.out.print("Memory Cache size " + testCache.getStatistics().getLocalHeapSizeInBytes() + "\n");
			//  testCache.getCacheConfiguration().setMaxBytesLocalHeap((long)10000);
			
//			  
			  System.out.print("Disk Cache number " + PageCacheManager.getInstance().currentDiskSize()+ "\n");
			//  System.out.print("Actual Disk Cache number " + cw.getDiskBytes()+ "\n");
	    	
			//testCache.initialise();
			
			//testCache.removeAll();
		
//		  //testCache.getCacheConfiguration().setMaxBytesLocalDisk((long)1);
//			  cw.setMaxBytesLocalHeapWrapper((long)10000);
//			  cw.setMaxBytesLocalDiskWrapper((long)5000);
//			  
//			 // System.out.print("Memory Cache number " + cs.getInMemorySizeInBytes()+ "\n");
//		testCache.flush();
//		 for(int i = 0; i < 100;i++) {
//			  testCache.put(new Element(i,i));
//			  }
//		 System.out.print("Memory Cache number " + cs.getInMemorySizeInBytes()+ "\n");
//		  System.out.print("Actual Memory Cache number " + cw.getHeapBytes()+ "\n");
//			 // System.out.print("Memory Cache size " + testCache.getStatistics().getLocalHeapSizeInBytes() + "\n");
//			//  testCache.getCacheConfiguration().setMaxBytesLocalHeap((long)10000);
//			
////			  
//			  System.out.print("Disk Cache number " + cs.getOnDiskSizeInBytes()+ "\n");
//			  System.out.print("Actual Disk Cache number " + cw.getDiskBytes()+ "\n");
    }
}
