package org.ssase.primitive;

import java.util.Arrays;

import org.ssase.actuator.Actuator;
import org.ssase.executor.Executor;
import org.ssase.objective.Objective;
import org.ssase.util.Repository;
import org.ssase.util.Timer;

public abstract class ControlPrimitive implements Primitive, Comparable{
	
	public static boolean isPreLoad = true;

	protected double[] array;
	// Used to distingush group for non-primary primitives.
	
    protected int group = 0;	

    // The optional value of this CP, provision values
    // should be always >= 0.
    protected double[] valueVector;
    
    // The differences between different optional value.
    // This is for both software and hardware CP.
    protected int a;
    protected int safe = 2;
    
    // The precentage to trigger change of the max provision value.
    protected double b;
    
    // The precentage of increasing/decreasing the max possible provision.
    // This is only for hardware CP.
    protected double g;

    // A safe min value of this control primitive.
    protected double h;
	//protected boolean isPrimary;
	
	protected boolean isHardware = false;
	
	protected double max = 0;//fake

	// Value for next update
	protected double value = -1;
	protected double[] values = null;
	// Current setup value;
	protected double provision;
	
	protected Type type;
	
	// E.g., Workload, Thread
	protected String name;

	protected Actuator actuator;

	// E.g., VM_ID or VM_ID - service name
	protected String alias;
	
	protected double constraint; 
	
	// The new values that has not yet being updated. This is raw value.
	protected double[] pendingValues = null;
	// Keep adding and sampling consistent.
	protected int samplingCounter = 0;
	protected int addingCounter = 0;
	protected Timer timer = new Timer();
	

	// Use this when this control primitive can be switch off.
	protected boolean isFixedZero = false;
	public ControlPrimitive(
			String name, 
			String VMIDorService,
			boolean isHardware, 
			Type type,
			Actuator actuator, 
			double provision, 
			double constraint,
			int a,
			double b,
			double g,
			double h,
			double maxProvision,
			boolean isFixedZero) {
		super();
		array = new double[0];
		this.alias = VMIDorService;
		this.name = name;
		this.isHardware = isHardware;
		this.type = type;
		this.actuator = actuator;
		this.constraint = constraint;
		this.provision = provision;
		this.a = a;
		this.g = g;
		this.b = b;
		this.h = h;
		valueVector = new double[]{maxProvision};
		this.isFixedZero = isFixedZero;
		
		if(!Executor.isEnableLowerBoundUpdate /*This is triggered in every sampling interval
		so if it is not enable, then we need to initilize the range here*/) {
			updateValueVector(h, valueVector[valueVector.length - 1]);
		}
	}

	@Deprecated
	public ControlPrimitive(double[] array) {
		super();
		this.array = array;
	}
	
	@Deprecated
	public ControlPrimitive(double[] array, Objective... objs /*These objectives see this primitive as direct primitive*/) {
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
	
	@Deprecated
	public ControlPrimitive(double[] array,  boolean isHardware, Objective... objs) {
		super();
		this.array = array;
		if (objs != null) {
			for (Objective obj : objs) {
				Repository.setDirectForAnObjective(obj, this);
			}
		}
		this.isHardware = isHardware;
		for (int i = 0; i < this.array.length; i++) {
			if (max < this.array[i]) {
				max = this.array[i];
			}
		}
		
		for (int i = 0; i < this.array.length; i++) {
			this.array[i] = this.array[i]*100/max;
		}
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
	public synchronized void addValue(long samples) {
		
        int no = (int)(samples - array.length);
		
		if (no == 1) {
			values = null;
		} else {
			values = new double[no];
			System.arraycopy(pendingValues, 0, values, 0, no);
		}
		value = pendingValues[pendingValues.length - 1];
		
		if (values != null) {
			for (double v : values) {
				addValue(v);
			}
			triggerMinProvisionUpdate(values);
		} else {
			addValue(value);
			triggerMinProvisionUpdate(value);
		}
		
		if (no != pendingValues.length) {
			double[] newValues = new double[pendingValues.length-no];
			System.arraycopy(pendingValues, no, newValues, 0, newValues.length);
			pendingValues = newValues;
		} else {
			pendingValues = null;
		}
		
	}
	
	

	private void addValue(double value) {
		
		
		
		
		if (value > max) {
			double[] newArray = new double[array.length + 1];
			for (int i = 0; i < newArray.length - 1; i++) {
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
		
		addingCounter++;
		
	}

	@Override
	public synchronized void removeHistoreicalValues(int no) {
		double[] newArray = new double[array.length - no];
		System.arraycopy(array, no, newArray, 0, array.length);
		
		array = newArray;
		
	}
	
	public boolean triggerActuator (long... value) {
		return actuator.execute(alias, value);
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

	public void setGroup(int group) {
		this.group = group;
	}
	
	public double[] getValueVector() {
		return valueVector;
	}
	
	public double getProvision(){
		return provision;
	}
	
	public Type getType() {
		return type;
	}
	

	public String getName() {
		return name;
	}

	public void setProvision(long provision){
		this.provision = provision;
	}

	public void setName(String alias) {
		this.name = alias;
	}
	
	public String getAlias(){
		return alias;
	}
	
	public void setActuator(Actuator actuator) {
		this.actuator = actuator;
	}


	public void setType(Type type) {
		this.type = type;
	}


	public boolean isDirect(Objective obj) {
		return Repository.isDirectForAnObjective(obj, this);
	}

	public boolean isHardware() {
		return isHardware;
	}

	public void setHardware(boolean isHardware) {
		this.isHardware = isHardware;
	}

	public void setValueVector(double[] valueVector) {
		this.valueVector = valueVector;
	}

	public boolean isViolate() {
		// Utilization is lower that a threshold, means over provision
		boolean result = value/provision < constraint;
		if (result) {
			timer.increaseTimer();
		}
		
		return timer.isValidViolation();
	}
	
	public synchronized void resetValues(){
		values = null;
	}
	
	/**
	 * Get the difference between optional values.
	 * @return
	 */
	public int getDifference (){
		return a;
	}
	
	protected void triggerMinProvisionUpdate (double[] values){
		double value = values[0];
		for (double v : values) {
			if ( v < value) {
				value = v;
			}
		}
		
		triggerMinProvisionUpdate(value);
	}
	
	/**
	 * 
	 * @param decidedValue
	 * @param threshold the remaining hardware resources or the min resources that can be removed (remove the VM)
	 * @return true if need to scale in/out
	 */
	@SuppressWarnings("unused")
	public double triggerMaxProvisionUpdate (boolean isIncrease, double decidedValue, double threshold){
		if(!Executor.isEnableUpperBoundUpdate) {
			return 0;
		}
		
		// If increase
		if (isIncrease && decidedValue / valueVector[valueVector.length - 1] > b  &&
				value / valueVector[valueVector.length - 1] > b  /*consider the latest observed value as well*/) {
			// Need scale out
			if (Math.ceil(valueVector[valueVector.length - 1] * g) > threshold) {
				//System.out.print(Math.round(valueVector[valueVector.length - 1] * g) + " : " + threshold+ "\n");
				return Double.NaN;
			}
			
			System.out.print(name + " - " +  alias + "'s max provision change from " + valueVector[valueVector.length - 1]  + " to "+ 
					Math.ceil(valueVector[valueVector.length - 1] * (1.0+g)) + "\n");
			
			double result = Math.ceil(valueVector[valueVector.length - 1] * g) ;
			
			updateValueVector(valueVector[0], valueVector[valueVector.length - 1] * (1.0+g));
			return result;
		// If decrease
		} else if (!isIncrease && decidedValue / valueVector[valueVector.length - 1] < b &&
					value / valueVector[valueVector.length - 1] < b /*consider the latest observed value as well*/) {
			
			// Need scale in
			if (Math.ceil(valueVector[valueVector.length - 1] * (1.0-g)) < threshold) {
				return Double.NaN;
			}
			
		
			
			// The max should be always bigger than the decidedValue.
			if (valueVector[valueVector.length - 1] * (1.0-g) <= decidedValue ||
					valueVector[valueVector.length - 1] * (1.0-g) <= valueVector[0] + safe*a) {
				return 0;
			}
			
			System.out.print(name + " - " +  alias + "'s max provision change from " + valueVector[valueVector.length - 1]  + " to "+ 
					Math.ceil(valueVector[valueVector.length - 1] * (1.0-g)) + "\n");
			
		
			double result =  Math.ceil(valueVector[valueVector.length - 1] * g);
			
			updateValueVector(valueVector[0], valueVector[valueVector.length - 1] * (1.0-g));	
			return result;
		}
		
		return 0;
		
	}
	
	public void outputCurrentVector(){
		System.out.print(this.getAlias() + " Min: " + valueVector[0] + ", Max: " + valueVector[valueVector.length - 1] +"\n");
	}
	
	@SuppressWarnings("unused")
	protected void triggerMinProvisionUpdate (double value){
		if(!Executor.isEnableLowerBoundUpdate) {
			return;
		}
		// Make the min provision a bit larger than the actual usage.
		value = (value < h)? h : value;
		
		if (isPreLoad) {
			value = h;
		}
		// For hardware CP, it is safer to use the latest value as the min value.
		if (this instanceof HardwareControlPrimitive) {
			if (Math.ceil(value) != 0) {
				//System.out.print(name + " - " + alias + ", New min provsion value: " + Math.round(value) +", max provision value: " + valueVector[valueVector.length - 1] + "\n");
				updateValueVector(value, valueVector[valueVector.length - 1]);
			}
		} else {	
			if (Math.ceil(value) != 0 /*&& Math.ceil(value) < valueVector[0]*/) {
				//System.out.print(name + " - " + alias + ", New min provsion value: " + Math.round(value) +", max provision value: " + valueVector[valueVector.length - 1] + "\n");
				updateValueVector(value, valueVector[valueVector.length - 1]);
			}
		}
	}
	
	protected void updateValueVector (double minProvision, double maxProvision){
		int max = (int)Math.ceil(maxProvision);
		int min = (int)Math.ceil(minProvision);
		
		
		// This may occur on Xen for CPU cap.
		if (min > max) {
			max = (int)Math.ceil(minProvision * (valueVector[valueVector.length - 1] / valueVector[0]));
		}
		
		
		int length = 1 + (max - min)/a;
//		if (length % a != 0) {
//			length += 1;
//		}
		
		if(isFixedZero) {
			valueVector = new double[length+1]; 
			valueVector[0] = 0D;
			double value = min - a;
			for (int i = 1; i < valueVector.length; i++) {
				if (value + a >= max) {
					value = max;
				} else {
					value = value + a;
				}
				
				
				valueVector[i] = value;
			}
		} else {
			
			valueVector = new double[length]; 
			double value = min - a;
			for (int i = 0; i < valueVector.length; i++) {
				if (value + a >= max) {
					value = max;
				} else {
					value = value + a;
				}
				
				
				valueVector[i] = value;
			}
		}
		
		//System.out.print(name + " new optional value: " + Arrays.toString(valueVector) + "\n");
	}
	
}
