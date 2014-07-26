package org.ssascaling.primitive;

import org.ssascaling.objective.Objective;

public interface Primitive {

	public double[] getArray();	
	
	public void prepareToAddValue(double value);
	
	public double getValue();
	
	public void addValue(long samples);
	
	public void removeHistoreicalValues(int no);
	
	public boolean isDirect(Objective obj);
	
	public double getMax();
	
	public int getGroup();

	public void setGroup(int group);
	
	public long getProvision();
	
	public Type getType();
	
	public void setType(Type type);
	
	public String getName();

	public void setName(String alias);
	
	public String getAlias();
	
	public void resetValues();
}
