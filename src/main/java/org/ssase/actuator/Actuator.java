package org.ssase.actuator;

public interface Actuator  {
	
	public boolean execute(String alias, long... value);

}
