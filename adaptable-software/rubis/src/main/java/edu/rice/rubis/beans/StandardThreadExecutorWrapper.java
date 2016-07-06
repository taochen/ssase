package edu.rice.rubis.beans;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.reflect.Method;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardThreadExecutor;
import org.ssase.sensor.SensoringController;

/**
 * When decrease the max thread size, it needs to throw exception from
 * afterExecution otherwise the thread would continue to take tasks from the
 * queue.
 * 
 * When use the original ThreadPoolExecutor, one needs to override the taskqueue
 * to prevent the invalidity of max thread size attribute in case the queue is
 * unbounded.
 * 
 * @author tao
 * 
 */
public class StandardThreadExecutorWrapper extends
		org.apache.catalina.core.StandardThreadExecutor {

	private org.apache.catalina.core.StandardThreadExecutor proxy;
	protected int maxQueueSize = Integer.MAX_VALUE;

	private final Map<Runnable, Long> perf = new ConcurrentHashMap<Runnable, Long>();
	private final Map<Runnable, Thread> taskToThread = new ConcurrentHashMap<Runnable, Thread>();
	public static final Map<Thread, Long> postPerf = new ConcurrentHashMap<Thread, Long>();

	public static AtomicInteger test = new AtomicInteger(0);

	public StandardThreadExecutorWrapper(
			org.apache.catalina.core.StandardThreadExecutor proxy) {
		this.proxy = proxy;

	}

	public void start() throws LifecycleException {
		proxy.start();
		for (LifecycleListener f : proxy.findLifecycleListeners()) {
			super.addLifecycleListener(f);
		}
		super.name = proxy.getName();
		super.namePrefix = proxy.getNamePrefix();
		super.maxIdleTime = proxy.getMaxIdleTime();
		super.maxThreads = proxy.getMaxThreads();
		super.minSpareThreads = proxy.getMinSpareThreads();

		// super.submittedTasksCount = proxy.s
		// super.threadPriority
		System.out.print(maxIdleTime + " init*******\n");
		// lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);
		TaskQueue taskqueue = new TaskQueue(maxQueueSize);
		TaskThreadFactory tf = new TaskThreadFactory(namePrefix);
		// lifecycle.fireLifecycleEvent(START_EVENT, null);

		executor = new ThreadPoolExecutor(getMinSpareThreads(),
				getMaxThreads(), maxIdleTime, TimeUnit.MILLISECONDS, taskqueue,
				tf) {

			protected void beforeExecute(Thread t, Runnable r) {
				super.beforeExecute(t, r);
				Long value = perf.get(r);
				if (value != null) {
					postPerf.put(t, value);
					taskToThread.put(r, t);
					perf.remove(r);
					
				}

				//System.out.print("Before execution!" + t + "\n");
			}

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				super.afterExecute(r, t);

				AtomicInteger atomic = submittedTasksCount;
				if (atomic != null) {
					atomic.decrementAndGet();
				}

				Thread th = taskToThread.get(r);
				//System.out.print("After execution!" + th + "\n");
				// Release all the stored instances.
				if (th != null) {
					postPerf.remove(th);
					taskToThread.remove(r);
				}
				// System.out.print(postPerf.size()+" **\n ");

				if (this.getPoolSize() > this.getMaximumPoolSize()) {
					throw new RuntimeException("Force end thread");
				}
			}

			public void setCorePoolSize(int corePoolSize) {
				// In case this is setup before maxThread, then
				// change the maxThread to a temp-safe value, which
				// will be formally updated to valid one later on.
				// This should not occur if this is set later than maxThread.
				if(corePoolSize > this.getMaximumPoolSize()) {
					try {
						Field field = ThreadPoolExecutor.class
								.getDeclaredField("maximumPoolSize");
						field.setAccessible(true);
						field.set(this, corePoolSize);
						field.setAccessible(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				super.setCorePoolSize(corePoolSize);
			}
			
			public void setMaximumPoolSize(int maximumPoolSize) {
				
				// In case this is setup before minSpareThread, then
				// change the minSpareThread to a temp-safe value, which
				// will be formally updated to valid one later on.
				// This should not occur if this is set later than minSpareThread.
				if(maximumPoolSize < this.getCorePoolSize()) {
					try {
						Field field = ThreadPoolExecutor.class
								.getDeclaredField("corePoolSize");
						field.setAccessible(true);
						field.set(this, maximumPoolSize);
						field.setAccessible(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				int old = this.getMaximumPoolSize();
				super.setMaximumPoolSize(maximumPoolSize);
				// System.out.print("old " + old + " max " +
				// this.getMaximumPoolSize()+ "\n");
				// If increase, then drain the queue and resubmit.
				if (maximumPoolSize > old) {
					lock();
					List<Runnable> list = this.drainQueue();
					// System.out.print("queue size " + list.size() + "\n");
					for (Runnable command : list) {
						this.execute(command);
					}

					unlock();
				}
			}

			private Field getMainLock() throws NoSuchFieldException {
				Field mainLock = ThreadPoolExecutor.class
						.getDeclaredField("mainLock");
				mainLock.setAccessible(true);
				return mainLock;
			}

			private void lock() {
				try {
					Field mainLock = getMainLock();
					Method lock = mainLock.getType().getDeclaredMethod("lock",
							(Class[]) null);
					lock.setAccessible(true);
					lock.invoke(mainLock.get(this), (Object[]) null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			private void unlock() {
				try {
					Field mainLock = getMainLock();
					Method unlock = mainLock.getType().getDeclaredMethod(
							"unlock", (Class[]) null);
					unlock.setAccessible(true);
					unlock.invoke(mainLock.get(this), (Object[]) null);
				} catch (Exception e) {
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
		taskqueue.setParent((ThreadPoolExecutor) executor);
		submittedTasksCount = new AtomicInteger();
		// System.out.print(executor.getClass() + "init*******\n");
		// lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);
	}

	public void execute(Runnable command) {
		sense(command);
		super.execute(command);
		/**synchronized (test) {
			int r = test.incrementAndGet();
			if (r == 2) {
				new Thread(new Runnable() {

					public void run() {
						try {
							Thread.sleep((long) 5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						System.out.print("Increase the max thread size ! \n");
						executor.setMaximumPoolSize(2);

					}

				}).start();

			}
		}**/
	}

	public void stop() throws LifecycleException {
		proxy.stop();
		if (executor != null)
			executor.shutdown();
		executor = null;
		submittedTasksCount = null;
	}

	private void sense(Runnable command) {

		try {
			Field field = command.getClass().getDeclaredField("socket");
			field.setAccessible(true);
			Object value = field.get(command);

			BufferedInputStream bs = new BufferedInputStream(
					((Socket) value).getInputStream());

			// Mark the position
			bs.mark(Integer.MAX_VALUE);
			byte[] contents = new byte[1024];

			int i = bs.read(contents);
			// System.out.print("Incoming traffic " + command + " " + ((Socket)
			// value).getInputStream().available()+ "\n");
			// Means this is the actual request to content.
			if (i > 0) {

				/**
				 * synchronized(test) { int r = test.incrementAndGet(); if(r ==
				 * 3) { executor.setMaximumPoolSize(1);
				 * executor.setCorePoolSize(1); } }
				 **/
				perf.put(command, System.currentTimeMillis());
				//System.out.print(" " + command + " Request started at "
					//	+ perf.get(command) + "\n");

				String strFileContents = new String(contents, 0, i);
				//System.out.println(strFileContents + "" + "\n");
				String[] s = strFileContents.split(" ");
				//System.out.println(s.length > 1 ? s[1] : "" + "\n");
				String[] sub = s[1].split("/");
				//System.out.println("*********Cache heap " + PageCacheManager.getInstance().currentMemorySize() + "\n");
				//System.out.println("*********Cache disk " + PageCacheManager.getInstance().currentDiskSize() + "\n");
				
				//System.out.println("*********Dirty url " + sub[sub.length-1] + "\n");
				//System.out.println("*********Clean url " + sub[sub.length-1].split("\\?")[0] + "\n");
				
				
				SensoringController.recordPriorToTask(null,
				 "Workload-" + sub[sub.length-1].split("\\?")[0]/*this is the url of a service, without the query string*/);
				//PageCacheManager.getInstance().printNumberOfCache();
			}

			// Rest the position of the stream, so that it can be read later on.
			bs.reset();
			// Use a wrapper that always return the buffer stream.
			Socket sw = new SocketWrapper(((Socket) value), bs);

			field.set(command, sw);
			field.setAccessible(false);
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	// ---------------------------------------------- TaskQueue Inner Class
	class TaskQueue extends LinkedBlockingQueue<Runnable> {
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
			if (parent.isShutdown())
				throw new RejectedExecutionException(
						"Executor not running, can't force a command into the queue");
			return super.offer(o); // forces the item onto the queue, to be used
									// if the task is rejected
		}

		public boolean force(Runnable o, long timeout, TimeUnit unit)
				throws InterruptedException {
			if (parent.isShutdown())
				throw new RejectedExecutionException(
						"Executor not running, can't force a command into the queue");
			return super.offer(o, timeout, unit); // forces the item onto the
													// queue, to be used if the
													// task is rejected
		}

		public boolean offer(Runnable o) {
			// we can't do any checks
			if (parent == null)
				return super.offer(o);
			int poolSize = parent.getPoolSize();
			// we are maxed out on threads, simply queue the object
			if (parent.getPoolSize() == parent.getMaximumPoolSize())
				return super.offer(o);
			// we have idle threads, just add it to the queue
			// note that we don't use getActiveCount(), see BZ 49730
			AtomicInteger submittedTasksCount = StandardThreadExecutorWrapper.this.submittedTasksCount;
			if (submittedTasksCount != null) {
				if (submittedTasksCount.get() <= poolSize)
					return super.offer(o);
			}
			// if we have less threads than maximum force creation of a new
			// thread
			if (poolSize < parent.getMaximumPoolSize())
				return false;
			// if we reached here, we need to add it to the queue
			return super.offer(o);
		}
	}

	// ---------------------------------------------- ThreadFactory Inner Class
	class TaskThreadFactory implements ThreadFactory {
		final ThreadGroup group;
		final AtomicInteger threadNumber = new AtomicInteger(1);
		final String namePrefix;

		TaskThreadFactory(String namePrefix) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
					.getThreadGroup();
			this.namePrefix = namePrefix;
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix
					+ threadNumber.getAndIncrement());
			t.setDaemon(daemon);
			t.setPriority(getThreadPriority());
			return t;
		}
	}

}
