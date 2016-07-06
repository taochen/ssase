package edu.rice.rubis.servlets;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;

/** This class contains the configuration for the servlets
 * like the path of HTML files, etc ...
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class Config
{

	
  private static Object lock = new Object();	
  private static Map<String, Integer> threadPool = new HashMap<String, Integer>();
  private static Map<String, Integer> maxPool = new HashMap<String, Integer>();
  static {
		threadPool.put("edu.rice.rubis.servlets.AboutMe", 35);
		threadPool.put("edu.rice.rubis.servlets.BrowseCategories", 35);
		threadPool.put("edu.rice.rubis.servlets.BrowseRegions", 35);
		threadPool.put("edu.rice.rubis.servlets.StoreBuyNow", 35);
		threadPool.put("edu.rice.rubis.servlets.BuyNow", 35);
		threadPool.put("edu.rice.rubis.servlets.BuyNowAuth", 35);
		threadPool.put("edu.rice.rubis.servlets.PutBid", 35);
		threadPool.put("edu.rice.rubis.servlets.PutBidAuth", 35);
		threadPool.put("edu.rice.rubis.servlets.PutComment", 35);
		threadPool.put("edu.rice.rubis.servlets.PutCommentAuth", 35);
		threadPool.put("edu.rice.rubis.servlets.RegisterItem", 35);
		threadPool.put("edu.rice.rubis.servlets.RegisterUser", 35);
		threadPool.put("edu.rice.rubis.servlets.SearchItemsByCategory", 35);
		threadPool.put("edu.rice.rubis.servlets.SearchItemsByRegion", 35);
		threadPool.put("edu.rice.rubis.servlets.SellItemForm", 35);
		threadPool.put("edu.rice.rubis.servlets.StoreBid", 35);
		threadPool.put("edu.rice.rubis.servlets.StoreComment", 35);
		threadPool.put("edu.rice.rubis.servlets.ViewBidHistory", 35);
		threadPool.put("edu.rice.rubis.servlets.ViewItem", 35);
		threadPool.put("edu.rice.rubis.servlets.ViewUserInfo", 35);
		
		
		maxPool.put("edu.rice.rubis.servlets.AboutMe", 35);
		maxPool.put("edu.rice.rubis.servlets.BrowseCategories", 35);
		maxPool.put("edu.rice.rubis.servlets.BrowseRegions", 35);
		maxPool.put("edu.rice.rubis.servlets.StoreBuyNow", 35);
		maxPool.put("edu.rice.rubis.servlets.BuyNow", 35);
		maxPool.put("edu.rice.rubis.servlets.BuyNowAuth", 35);
		maxPool.put("edu.rice.rubis.servlets.PutBid", 35);
		maxPool.put("edu.rice.rubis.servlets.PutBidAuth", 35);
		maxPool.put("edu.rice.rubis.servlets.PutComment", 35);
		maxPool.put("edu.rice.rubis.servlets.PutCommentAuth", 35);
		maxPool.put("edu.rice.rubis.servlets.RegisterItem", 35);
		maxPool.put("edu.rice.rubis.servlets.RegisterUser", 35);
		maxPool.put("edu.rice.rubis.servlets.SearchItemsByCategory", 35);
		maxPool.put("edu.rice.rubis.servlets.SearchItemsByRegion", 35);
		maxPool.put("edu.rice.rubis.servlets.SellItemForm", 35);
		maxPool.put("edu.rice.rubis.servlets.StoreBid", 35);
		maxPool.put("edu.rice.rubis.servlets.StoreComment", 35);
		maxPool.put("edu.rice.rubis.servlets.ViewBidHistory", 35);
		maxPool.put("edu.rice.rubis.servlets.ViewItem", 35);
		maxPool.put("edu.rice.rubis.servlets.ViewUserInfo", 35);
		
  }
  
  
  public static void changeThread(String service, int value) {
	  synchronized(lock) {
		  if (!threadPool.containsKey(service))  {
				return;
		  }
		  
		  // If decrease
		  if (value < maxPool.get(service)) {
			  if (value < threadPool.get(service)) {
				  // Decrease the idle ones.
				  threadPool.put(service, value);
			  }
			  maxPool.put(service, value);
		  // If increase
		  } else   if (value > maxPool.get(service)) {
			  int number = threadPool.get(service);
			  // Add the increased threads as idle ones.
			  threadPool.put(service, number + (value - maxPool.get(service)));
			  maxPool.put(service, value);
		  }
		  
		System.out.print("Change thread: " + service + " to new maxPool value: " + maxPool.get(service)
				+ " to new threadPool value: " + threadPool.get(service) + "\n"); 
	  }
  }
  
  public static void getThread(String service)
  {
	  synchronized(lock) {
		if (!threadPool.containsKey(service))  {
			return;
		}
        // Wait for a connection to be available
        while (threadPool.get(service) == 0)
        {
          try
          {  
        	 lock.wait();
          }
          catch (InterruptedException e)
          {
           System.out.println("Thread pool wait interrupted.");
          }
         }

        int number = threadPool.get(service);
        threadPool.put(service, number - 1);
	  }
  }
  
  public static void releaseThread(String service)
  { 
	  synchronized(lock) {
		  if (!threadPool.containsKey(service)) 
				return;
      boolean mustNotify = threadPool.get(service) == 0;
      int number = threadPool.get(service);
      // Only release with respect to the new threshold.
      if ((number + 1) <= maxPool.get(service)) {
          threadPool.put(service, number + 1);
      }
      // Wake up one servlet waiting for a connection (if any)
      if (mustNotify)
    	  lock.notifyAll();
	  }
    
  }
  
  
  public static void main (String[] arg) {
	  
	  for (int i = 0; i < 10; i ++) {
		  getThread("edu.rice.rubis.servlets.AboutMe");
	  }
	  changeThread("edu.rice.rubis.servlets.AboutMe", 30);
	  for (int i = 0; i < 3; i ++) {
		  getThread("edu.rice.rubis.servlets.AboutMe");
	  }
	  for (int i = 0; i < 10; i ++) {
		  releaseThread("edu.rice.rubis.servlets.AboutMe");
	  }
	  for (int i = 0; i < 3; i ++) {
		  releaseThread("edu.rice.rubis.servlets.AboutMe");
	  }
	  
		System.out.print("Change thread:  to new maxPool value: " + maxPool.get("edu.rice.rubis.servlets.AboutMe")
				+ " to new threadPool value: " + threadPool.get("edu.rice.rubis.servlets.AboutMe") + "\n"); 
  }
  
  
  /**
   * Creates a new <code>Config</code> instance.
   *
   */
  Config()
  {
  }

  public static String HTMLFilesPath;
  public static final String DatabaseProperties =
    "WEB-INF/mysql.properties";

  public static final int AboutMePoolSize = 35;
  public static final int BrowseCategoriesPoolSize = 35;
  public static final int BrowseRegionsPoolSize = 35;
  public static final int BuyNowPoolSize = 35;
  public static final int PutBidPoolSize = 35;
  public static final int PutCommentPoolSize = 35;
  public static final int RegisterItemPoolSize = 35;
  public static final int RegisterUserPoolSize = 35;
  public static final int SearchItemsByCategoryPoolSize = 35;
  public static final int SearchItemsByRegionPoolSize = 35;
  public static final int StoreBidPoolSize = 15;
  public static final int StoreBuyNowPoolSize = 15;
  public static final int StoreCommentPoolSize = 10;
  public static final int ViewBidHistoryPoolSize = 35;
  public static final int ViewItemPoolSize = 35;
  public static final int ViewUserInfoPoolSize = 35;
}
