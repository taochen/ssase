package org.ssase.primitive;

import org.ssase.actuator.Actuator;
import org.ssase.objective.Objective;

public class SoftwareControlPrimitive extends ControlPrimitive {

	public SoftwareControlPrimitive(String name,  String VM_ID, boolean isHardware,
			Type type, Actuator actuator, double provision, double constraint,
			int a,
			double b,
			double g,
			double h,
			double maxProvision,
			boolean isFixedZero) {
		super(name, VM_ID, isHardware, type, actuator, provision, constraint, a, b, g, h, maxProvision, isFixedZero);
		// TODO Auto-generated constructor stub
	}
	@Deprecated
	public SoftwareControlPrimitive(double[] array) {
		super(array);
		// TODO Auto-generated constructor stub
	}
	@Deprecated
	public SoftwareControlPrimitive(double[] array, Objective... objs) {
		super(array, objs);
		// TODO Auto-generated constructor stub
	}
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return this.hashCode();
	}

	// Overwrite this as currently all software control primitives
	// can not be controlled in Dom0.
	public boolean triggerActuator (long... value) {
		System.out.print("Setting " + alias + " with " + type + " "  + value[0] + "\n");
		return false;
	}
}
