package org.ssascaling.primitive;

import org.ssascaling.actuator.Actuator;
import org.ssascaling.objective.Objective;
import org.ssascaling.util.Repository;

public abstract class ControlPrimitive implements Primitive, Comparable{

	protected double[] array;
	// Used to distingush group for non-primary primitives.
	
    protected int group = 0;	

    // The optional value of this CP, provision values
    // should be always >= 0.
    protected double[] valueVector;
    
    // The differences between different optional value.
    // This is for both software and hardware CP.
    protected int a;
    
    // The precentage to trigger change of the max provision value.
    protected double b;
    
    // The precentage of increasing/decreasing the max possible provision.
    // This is only for hardware CP.
    protected double g;

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

	public ControlPrimitive(
			String alias, 
			String VMIDorService,
			boolean isHardware, 
			Type type,
			Actuator actuator, 
			double provision, 
			double constraint,
			int a,
			double b,
			double g,
			double maxProvision) {
		super();
		array = new double[0];
		this.alias = VMIDorService;
		this.name = alias;
		this.isHardware = isHardware;
		this.type = type;
		this.actuator = actuator;
		this.constraint = constraint;
		this.provision = provision;
		this.a = a;
		this.g = g;
		this.b = b;
		valueVector = new double[]{maxProvision};
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
		return value/provision < constraint;
	}
	
	public synchronized void resetValues(){
		values = null;
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
	public double triggerMaxProvisionUpdate (double decidedValue, double threshold){
	 
		// If increase
		if (decidedValue / valueVector[valueVector.length - 1] > b  &&
				value / valueVector[valueVector.length - 1] > b  /*consider the latest observed value as well*/) {
			
			if (Math.round(valueVector[valueVector.length - 1] * (1+g)) - valueVector[valueVector.length - 1] > threshold) {
				return Double.NaN;
			}
			
			System.out.print(name + " - " +  alias + "'s max provision change from " + valueVector[valueVector.length - 1]  + " to "+ 
					(valueVector[valueVector.length - 1] * (1+g)) + "\n");
			
			double result = valueVector[valueVector.length - 1] - Math.round(valueVector[valueVector.length - 1] * (1+g)) ;
			
			updateValueVector(valueVector[0], valueVector[valueVector.length - 1] * (1+g));
			return result;
		// If decrease
		} else if (decidedValue / valueVector[valueVector.length - 1] < b &&
					value / valueVector[valueVector.length - 1] < b /*consider the latest observed value as well*/) {
			
			if (Math.round(valueVector[valueVector.length - 1] * (1-g)) < threshold) {
				return Double.NaN;
			}
			
			System.out.print(name + " - " +  alias + "'s max provision change from " + valueVector[valueVector.length - 1]  + " to "+ 
					(valueVector[valueVector.length - 1] * (1-g)) + "\n");
			
		
			double result = valueVector[valueVector.length - 1] - Math.round(valueVector[valueVector.length - 1] * (1-g));
			
			updateValueVector(valueVector[0], valueVector[valueVector.length - 1] * (1-g));	
			return result;
		}
		
		return 0;
		
	}
	
	protected void triggerMinProvisionUpdate (double value){
		if (Math.round(value) != 0 && Math.round(value) < valueVector[0]) {
			System.out.print(name + " - " + alias + ", New min provsion value: " + Math.round(value) +", max provision value: " + valueVector[valueVector.length - 1] + "\n");
			updateValueVector(value, valueVector[valueVector.length - 1]);
		}
	}
	
	protected void updateValueVector (double minProvision, double maxProvision){
		int max = (int)Math.round(maxProvision);
		int min = (int)Math.round(minProvision);
		
		// This should not occur outside test cases.
		if (min > max) {
			min = 0;
		}
		
		int length = max - min;
		if (length % a != 0) {
			length += 1;
		}
		
		valueVector = new double[length]; 
		double value = min;
		for (int i = 0; i < length; i++) {
			if (value + i*a > max) {
				value = max;
			} else {
				value = value + i*a;
			}
			
			
			valueVector[i] = value;
		}
	}
	
}
