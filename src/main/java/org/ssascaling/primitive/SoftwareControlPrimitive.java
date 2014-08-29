package org.ssascaling.primitive;

import org.ssascaling.actuator.Actuator;
import org.ssascaling.objective.Objective;

public class SoftwareControlPrimitive extends ControlPrimitive {

	public SoftwareControlPrimitive(String alias,  String VM_ID, boolean isHardware,
			Type type, Actuator actuator, double provision, double constraint,
			int a,
			double b,
			double g,
			double h,
			double maxProvision) {
		super(alias, VM_ID, isHardware, type, actuator, provision, constraint, a, b, g, h, maxProvision);
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

}
