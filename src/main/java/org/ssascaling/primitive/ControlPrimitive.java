package org.ssascaling.primitive;

import org.ssascaling.actuator.Actuator;
import org.ssascaling.objective.Objective;
import org.ssascaling.util.Repository;

public abstract class ControlPrimitive implements Primitive, Comparable{

	protected double[] array;
	// Used to distingush group for non-primary primitives.
	
    protected int group = 0;	

    // The optional value of this CP
    protected double[] valueVector;

	//protected boolean isPrimary;
	
	protected boolean isHardware = false;
	
	protected double max = 0;//fake

	// Value for next update
	protected double value;
	protected double[] values = null;
	// Current setup value;
	protected long provision;
	
	protected Type type;
	
	// E.g., Workload, Thread
	protected String name;

	protected Actuator actuator;

	// E.g., VM_ID or VM_ID - service name
	protected String alias;
	
	protected double constraint; 

	public ControlPrimitive(
			String alias, 
			String VMIDorService,
			boolean isHardware, 
			Type type,
			Actuator actuator, 
			long provision, 
			double constraint,
			double[] valueVector) {
		super();
		array = new double[0];
		this.alias = VMIDorService;
		this.name = alias;
		this.isHardware = isHardware;
		this.type = type;
		this.actuator = actuator;
		this.constraint = constraint;
		this.provision = provision;
		this.valueVector = valueVector;
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

	public void prepareToAddValue(double value) {
		this.value = value;
	}
	

	public void prepareToAddValue(double[] values) {
		this.values = values;
		this.value  = values [ values.length - 1];
	}

	@Override
	public void addValue() {
		if (values != null) {
			for (double v : values) {
				addValue(v);
			}
		} else {
			addValue(value);
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
		
	}

	@Override
	public void removeHistoreicalValues(int no) {
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
	
	public long getProvision(){
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
	
}
