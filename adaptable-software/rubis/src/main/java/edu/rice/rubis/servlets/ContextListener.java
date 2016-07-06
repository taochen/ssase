package edu.rice.rubis.servlets;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.catalina.Container;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardThreadExecutor;
import org.apache.coyote.http11.Http11Protocol;
import org.apache.tomcat.util.net.JIoEndpoint;
import org.apache.catalina.ServerFactory;
import org.ssase.actuator.ActuationReceiver;
import org.ssase.actuator.Invoker;
import org.ssase.primitive.Type;
import org.ssase.sensor.Sensor;
import org.ssase.sensor.SensoringController;
import org.ssase.sensor.control.CacheModeSensor;
import org.ssase.sensor.control.CompressionSensor;

import edu.rice.rubis.beans.PageCacheManager;
import edu.rice.rubis.beans.StandardThreadExecutorWrapper;
import edu.rice.rubis.invoker.*;

public class ContextListener implements ServletContextListener {
	private ActuationReceiver receiver = null;
	private Timer timer = null;

	public void contextDestroyed(ServletContextEvent arg0) {
		SensoringController.destory();
		if (receiver != null)
			receiver.shutdown();
		if (timer != null)
			timer.cancel();
	}

	public void contextInitialized(ServletContextEvent event) {
		try {
			org.apache.catalina.core.StandardThreadExecutor exe = (org.apache.catalina.core.StandardThreadExecutor) ServerFactory
					.getServer().findService("Catalina").findExecutors()[0];
			final StandardThreadExecutorWrapper wrapper = new StandardThreadExecutorWrapper(
					exe);

			ServerFactory.getServer().findService("Catalina")
					.removeExecutor(exe);
			ServerFactory.getServer().findService("Catalina")
					.addExecutor(wrapper);
			((Http11Protocol) ServerFactory.getServer().findService("Catalina")
					.findConnectors()[0].getProtocolHandler())
					.setExecutor(wrapper);
//
//			ServerFactory.getServer().findService("Catalina").findConnectors()[0]
//					.setProperty("compression", "sdfsdfdsf");
			System.out.print(ServerFactory.getServer().findService("Catalina")
					.findConnectors()[0].getProperty("compression")
					+ "****\n");

			/**
			 * timer = new Timer(); timer.schedule(new TimerTask(){
			 * 
			 * @Override public void run() { System.out.print("Pool size: " +
			 *           wrapper.getPoolSize() + "\n");
			 * 
			 *           }
			 * 
			 * 
			 *           }, 1000, 1000);
			 */

			RubisHttpServlet.initConnectionPool(event.getServletContext());
			SensoringController.init(0, event.getServletContext());
			PageCacheManager.getInstance().init(event.getServletContext());

			Sensor cms = SensoringController.getSensor(null, "Compression");
			cms.initInstance(new Object[] {
					ServerFactory.getServer().findService("Catalina")
							.findConnectors()[0].getProtocolHandler(),
					new Sensor.Invoker() {

						public Object execute(Object object) {
							String s = ((Http11Protocol) ServerFactory
									.getServer().findService("Catalina")
									.findConnectors()[0].getProtocolHandler())
									.getCompression();
							return CompressionSensor.getDoubleByString(s);
						}
					} });

			cms = SensoringController.getSensor(null, "cacheMode");
			cms.initInstance(new Object[] { PageCacheManager.getInstance(),
					new Sensor.Invoker() {

						public Object execute(Object object) {
							return PageCacheManager.getInstance().currentMode();
						}
					} });

			cms = SensoringController.getSensor(null, "maxBytesLocalDisk");
			cms.initInstance(new Object[] { PageCacheManager.getInstance(),
					new Sensor.Invoker() {

						public Object execute(Object object) {
							
							if(PageCacheManager.getInstance().getCacheNameByMode() == null) {
								return 0;
							}
							
							return PageCacheManager.getInstance()
									.currentDiskSize();
						}
					} });

			cms = SensoringController.getSensor(null, "maxBytesLocalHeap");
			cms.initInstance(new Object[] { PageCacheManager.getInstance(),
					new Sensor.Invoker() {

						public Object execute(Object object) {
							
							
							if(PageCacheManager.getInstance().getCacheNameByMode() == null) {
								return 0;
							}
							
							return PageCacheManager.getInstance()
									.currentMemorySize();
						}
					} });

			final ActuationReceiver ar = new ActuationReceiver(new Type[] {
					Type.maxThread, Type.minSpareThreads, Type.Connection,
					Type.Compression, Type.query_cache_size, Type.cacheMode,
					Type.maxBytesLocalDisk, Type.maxBytesLocalHeap },
					new Invoker[] { new MaxThreadInvoker(wrapper),
							new MinSpareThreadsInvoker(wrapper),
							new ConnectionInvoker(), new CompressionInvoker(),
							new QueryCacheSizeInvoker(),
							new CacheModeInvoker(), new DiskCacheSizeInvoker(),
							new MemoryCacheSizeInvoker() });
			receiver = ar;
			new Thread(new Runnable() {

				public void run() {
					ar.receive();
				}

			}).start();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
