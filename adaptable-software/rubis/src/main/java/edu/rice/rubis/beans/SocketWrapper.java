package edu.rice.rubis.beans;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

public class SocketWrapper extends Socket{

	private Socket proxy;
	private BufferedInputStream in;
	 
	
	public SocketWrapper(Socket proxy, BufferedInputStream in) {
		super();
		this.proxy = proxy;
		this.in = in;
	}

	@Override
	public void bind(SocketAddress bindpoint) throws IOException {
		// TODO Auto-generated method stub
		proxy.bind(bindpoint);
	}

	@Override
	public synchronized void close() throws IOException {
		try {
		   in.close();
		   if(!proxy.isClosed()) proxy.close();
		} catch (Throwable t) {
			//do nothing.
		}
	}

	@Override
	public void connect(SocketAddress endpoint, int timeout) throws IOException {
		// TODO Auto-generated method stub
		proxy.connect(endpoint, timeout);
	}

	@Override
	public void connect(SocketAddress endpoint) throws IOException {
		// TODO Auto-generated method stub
		proxy.connect(endpoint);
	}

	@Override
	public SocketChannel getChannel() {
		// TODO Auto-generated method stub
		return proxy.getChannel();
	}

	@Override
	public InetAddress getInetAddress() {
		// TODO Auto-generated method stub
		return proxy.getInetAddress();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return in;
	}

	@Override
	public boolean getKeepAlive() throws SocketException {
		// TODO Auto-generated method stub
		return proxy.getKeepAlive();
	}

	@Override
	public InetAddress getLocalAddress() {
		// TODO Auto-generated method stub
		return proxy.getLocalAddress();
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return proxy.getLocalPort();
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		// TODO Auto-generated method stub
		return proxy.getLocalSocketAddress();
	}

	@Override
	public boolean getOOBInline() throws SocketException {
		// TODO Auto-generated method stub
		return proxy.getOOBInline();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return proxy.getOutputStream();
	}

	@Override
	public int getPort() {
		// TODO Auto-generated method stub
		return proxy.getPort();
	}

	@Override
	public synchronized int getReceiveBufferSize() throws SocketException {
		// TODO Auto-generated method stub
		return proxy.getReceiveBufferSize();
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		// TODO Auto-generated method stub
		return proxy.getRemoteSocketAddress();
	}

	@Override
	public boolean getReuseAddress() throws SocketException {
		// TODO Auto-generated method stub
		return proxy.getReuseAddress();
	}

	@Override
	public synchronized int getSendBufferSize() throws SocketException {
		// TODO Auto-generated method stub
		return proxy.getSendBufferSize();
	}

	@Override
	public int getSoLinger() throws SocketException {
		// TODO Auto-generated method stub
		return proxy.getSoLinger();
	}

	@Override
	public synchronized int getSoTimeout() throws SocketException {
		// TODO Auto-generated method stub
		return proxy.getSoTimeout();
	}

	@Override
	public boolean getTcpNoDelay() throws SocketException {
		// TODO Auto-generated method stub
		return proxy.getTcpNoDelay();
	}

	@Override
	public int getTrafficClass() throws SocketException {
		// TODO Auto-generated method stub
		return proxy.getTrafficClass();
	}

	@Override
	public boolean isBound() {
		// TODO Auto-generated method stub
		return proxy.isBound();
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return proxy.isClosed();
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return proxy.isConnected();
	}

	@Override
	public boolean isInputShutdown() {
		// TODO Auto-generated method stub
		return proxy.isInputShutdown();
	}

	@Override
	public boolean isOutputShutdown() {
		// TODO Auto-generated method stub
		return proxy.isOutputShutdown();
	}

	@Override
	public void sendUrgentData(int data) throws IOException {
		// TODO Auto-generated method stub
		proxy.sendUrgentData(data);
	}

	@Override
	public void setKeepAlive(boolean on) throws SocketException {
		// TODO Auto-generated method stub
		proxy.setKeepAlive(on);
	}

	@Override
	public void setOOBInline(boolean on) throws SocketException {
		// TODO Auto-generated method stub
		proxy.setOOBInline(on);
	}

	@Override
	public void setPerformancePreferences(int connectionTime, int latency,
			int bandwidth) {
		// TODO Auto-generated method stub
		proxy.setPerformancePreferences(connectionTime, latency, bandwidth);
	}

	@Override
	public synchronized void setReceiveBufferSize(int size)
			throws SocketException {
		// TODO Auto-generated method stub
		proxy.setReceiveBufferSize(size);
	}

	@Override
	public void setReuseAddress(boolean on) throws SocketException {
		// TODO Auto-generated method stub
		proxy.setReuseAddress(on);
	}

	@Override
	public synchronized void setSendBufferSize(int size) throws SocketException {
		// TODO Auto-generated method stub
		proxy.setSendBufferSize(size);
	}

	@Override
	public void setSoLinger(boolean on, int linger) throws SocketException {
		// TODO Auto-generated method stub
		proxy.setSoLinger(on, linger);
	}

	@Override
	public synchronized void setSoTimeout(int timeout) throws SocketException {
		// TODO Auto-generated method stub
		proxy.setSoTimeout(timeout);
	}

	@Override
	public void setTcpNoDelay(boolean on) throws SocketException {
		// TODO Auto-generated method stub
		proxy.setTcpNoDelay(on);
	}

	@Override
	public void setTrafficClass(int tc) throws SocketException {
		// TODO Auto-generated method stub
		proxy.setTrafficClass(tc);
	}

	@Override
	public void shutdownInput() throws IOException {
		// TODO Auto-generated method stub
		proxy.shutdownInput();
	}

	@Override
	public void shutdownOutput() throws IOException {
		// TODO Auto-generated method stub
		proxy.shutdownOutput();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return proxy.toString();
	}



	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return proxy.equals(obj);
	}


	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return proxy.hashCode();
	}

}
