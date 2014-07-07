package org.ssascaling.primitive;

import org.ssascaling.actuator.Actuator;
import org.ssascaling.objective.Objective;
import org.ssascaling.util.Repository;

public class EnvironmentalPrimitive implements Primitive, Comparable {

	protected double[] array;
	// Used to distingush group for non-primary primitives.
    protected int group = 0;	
	
	//protected boolean isPrimary;
	
	protected double max = 0;
	
	protected double value = -1;
	protected double[] values = null;
	protected double latest;
	
	protected Type type;
	
	
	protected String name;
	// The new values that has not yet being updated. This is raw value.
	protected double[] pendingValues = null;
	// Keep adding and sampling consistent.
	protected int samplingCounter = 0;
	protected int addingCounter = 0;
	public EnvironmentalPrimitive(String alias, Type type) {
		super();
		array = new double[0];
		this.name = alias;
		this.type = type;
	}

	@Deprecated
	public EnvironmentalPrimitive(double[] array) {
		super();
		this.array = array;
	}
	@Deprecated
	public EnvironmentalPrimitive(double[] array,  Objective... objs) {
		super();
		this.array = array;
		if (objs != null) {
			for (Objective obj : objs) {
				Repository.setDirectForAnObjective(obj, this);
			}
		}
		for (int i = 0; i < this.array.length; i++) {
			if (max < this.array[i]) {
				max = this.array[i];
			}
		}
		
		for (int i = 0; i < this.array.length; i++) {
			this.array[i] = this.array[i]*100/max;
		}
	}



	public boolean isDirect(Objective obj) {
		return Repository.isDirectForAnObjective(obj, this);
	}


	@Override
	public double[] getArray() {
		return array;
	}

	
	public synchronized void prepareToAddValue(double value) {
		// If the previous value has not been added.
		if (addingCounter != samplingCounter) {
			
			if (pendingValues == null) {
				pendingValues = new double[]{this.value, value};
			} else {

				double[] newArray = new double[pendingValues.length + 1];
				System.arraycopy(pendingValues, 0, newArray, 0, pendingValues.length);
				newArray[newArray.length - 1] = value;
				pendingValues = newArray;
			}
			
			
		} else {
			pendingValues = new double[]{value};
		}
		samplingCounter++;
	}
	


	@Override
	public synchronized void addValue() {
		values = pendingValues.length == 1? null : pendingValues;
		value = pendingValues[pendingValues.length - 1];
		
		
		if (values != null) {
			for (double v : values) {
				addValue(v);
			}
		} else {
			addValue(value);
		}
		
		pendingValues = null;
	}
	
	

	private void addValue(double value) {
		
		
	
		
		if (value > max) {
			double[] newArray = new double[array.length + 1];
			for (int i = 0; i < newArray.length-1; i++) {
				newArray[i] = array[i]*max/value;
			}
			
			newArray[array.length] = 100;
			
			max = value;
			array = newArray;
		} else {
			double[] newArray = new double[array.length + 1];
			System.arraycopy(array, 0, newArray, 0, array.length);
			
			newArray[array.length] = max == 0? 0 : value*100/max;
			
			array = newArray;
		}
		latest = value;
		addingCounter++;
		
	}

	@Override
	public synchronized void removeHistoreicalValues(int no) {
		double[] newArray = new double[array.length - no];
		System.arraycopy(array, no, newArray, 0, array.length);
		
		array = newArray;
		
	}
	
	public double getMax(){
		return max;
	}
	
	public int getGroup() {
		return group;
	}
	
	public double getValue(){
		return value;
	}
	
	public long getProvision(){ 
		return Math.round(latest);
	}

	public void setGroup(int group) {
		this.group = group;
	}


	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void setType(Type type) {
		this.type = type;
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public void setName(String alias) {
		this.name = alias;
		
	}

	@Override
	public String getAlias() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return this.hashCode();
	}
	
	public double getLatest(){
		return latest;
	}
	
	
	public synchronized void resetValues(){
		values = null;
	}
}
