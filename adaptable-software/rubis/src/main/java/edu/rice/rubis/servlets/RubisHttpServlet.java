/*
 * RUBiS
 * Copyright (C) 2002, 2003, 2004 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: jmob@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Emmanuel Cecchet, Julie Marguerite
 * Contributor(s): Jeremy Philippe
 */
package edu.rice.rubis.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;

import org.ssase.sensor.SensoringController;

/**
 * Provides the method to initialize connection to the database. All the
 * servlets inherit from this class
 */
public abstract class RubisHttpServlet extends HttpServlet {
	/** Controls connection pooling */
	private static boolean enablePooling = true;
	/** Stack of available connections (pool) */
	private static Stack freeConnections = null;
	private static Set<Connection> set = null;
	private static int poolSize = 20;
	private static Properties dbProperties = null;

	private static Object lock = new Object();

	public abstract int getPoolSize(); // Get the pool size for this class

	public static void initConnectionPool(ServletContext sc) {
		InputStream in = null;
		
		try {
			final String path = sc.getRealPath("/");
			// Get rid of the last '/'
			Config.HTMLFilesPath = path;
			// Get the properties for the database connection
			dbProperties = new Properties();
			in = new FileInputStream(path + Config.DatabaseProperties);

			dbProperties.load(in);
			// load the driver
			Class.forName(dbProperties.getProperty("datasource.classname"));

			freeConnections = new Stack();
			set = new HashSet<Connection>();
			initializeConnections();
		} catch (Exception f) {
			f.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
			}
		}
	}

	/** Load the driver and get a connection to the database */
	public void init() throws ServletException {

	}

	  public static void changeConnection (int value)  throws SQLException {
		  synchronized(lock) {
			  
			  // If decrease
			  if (value < poolSize) {
				  
				  int i = 0;
				  int size = poolSize-value;
				  Iterator<Connection> itr = set.iterator();
				  List<Connection> list = new ArrayList<Connection>();
				  Connection c = null;
				  while (itr.hasNext()) {
					  if(i > size) break;
					  
					  c = itr.next(); 
					  list.add(c);
					  freeConnections.remove(c);
					  c.close();
					  i++;
					}
				  set.removeAll(list);
				  poolSize = value;
			  // If increase
			  } else  if (value > poolSize) {
				  
				  for (int i = 0; i < (value-poolSize); i++) {
						// Get connections to the database
					  Connection c = DriverManager.getConnection(
								dbProperties.getProperty("datasource.url"),
								dbProperties.getProperty("datasource.username"),
								dbProperties.getProperty("datasource.password"));
					  set.add(c);
						freeConnections.push(c);
					}
				  poolSize = value;
				  lock.notifyAll();
			  }
//			  
//			System.out.print("Change thread: " + service + " to new maxPool value: " + maxPool.get(service)
//					+ " to new threadPool value: " + threadPool.get(service) + "\n"); 
		  }
	  }
	
	/**
	 * Initialize the pool of connections to the database. The caller must
	 * ensure that the driver has already been loaded else an exception will be
	 * thrown.
	 * 
	 * @exception SQLException
	 *                if an error occurs
	 */
	public static void initializeConnections() throws SQLException {

		synchronized (lock) {
			if (enablePooling)
				for (int i = 0; i < poolSize; i++) {
					// Get connections to the database
					 Connection c = DriverManager.getConnection(
								dbProperties.getProperty("datasource.url"),
								dbProperties.getProperty("datasource.username"),
								dbProperties.getProperty("datasource.password"));
					  set.add(c);
						freeConnections.push(c);
				}
		}
	}

	/**
	 * Closes a <code>Connection</code>.
	 * 
	 * @param connection
	 *            to close
	 */
	public void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (Exception e) {

		}
	}

	/**
	 * Gets a connection from the pool (round-robin)
	 * 
	 * @return a <code>Connection</code> or null if no connection is available
	 */
	public Connection getConnection() {

		synchronized (lock) {
			if (enablePooling) {
				try {
					// Wait for a connection to be available
					while (freeConnections.isEmpty()) {
						try {
							wait();
						} catch (InterruptedException e) {
							System.out
									.println("Connection pool wait interrupted.");
						}
					}
					SensoringController.recordPriorToTask(null, "Connection");
					Connection c = (Connection) freeConnections.pop();
					return c;
				} catch (EmptyStackException e) {
					System.out.println("Out of connections.");
					return null;
				}
			} else {
				try {
					return DriverManager.getConnection(
							dbProperties.getProperty("datasource.url"),
							dbProperties.getProperty("datasource.username"),
							dbProperties.getProperty("datasource.password"));
				} catch (SQLException ex) {
					return null;
				}
			}
		}
	}

	/**
	 * Releases a connection to the pool.
	 * 
	 * @param c
	 *            the connection to release
	 */
	public void releaseConnection(Connection c) {

		synchronized (lock) {
			SensoringController.recordPostToTask(null, 0, "Connection");
			if (enablePooling) {
				boolean mustNotify = freeConnections.isEmpty();
				// Only put back if the current size is smaller than the max size.
				if(!set.contains(c)) {
					this.closeConnection(c);
					return;
				}
				   freeConnections.push(c);
				// Wake up one servlet waiting for a connection (if any)
					if (mustNotify)
						lock.notifyAll();
				
				
			} else {
				closeConnection(c);
			}
		}

	}

	/**
	 * Release all the connections to the database.
	 * 
	 * @exception SQLException
	 *                if an error occurs
	 */
	public void finalizeConnections() throws SQLException {
		synchronized (lock) {
			if (enablePooling) {
				Connection c = null;
				while (!freeConnections.isEmpty()) {
					c = (Connection) freeConnections.pop();
					c.close();
				}
			}
			enablePooling = false;
		}
	}

	/**
	 * Clean up database connections.
	 */
	public void destroy() {
		try {
			finalizeConnections();
		} catch (SQLException e) {
		}
	}

}