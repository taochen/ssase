package org.ssascaling.actuator;

public interface Actuator  {
	
	public boolean execute(String alias, long... value);

}
