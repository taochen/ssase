package org.ssascaling.objective;

import java.util.List;

import org.ssascaling.observation.listener.Listener;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;

public class Cost implements Objective  {

	
	private ControlPrimitive p;
	
	@Override
	public double[] getArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isQoS() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMin() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBetter(double v1, double v2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double predict(double[] xValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getConstraint() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSatisfied(double[] xValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChangeSignificant(double v1, double v2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addListener(Listener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Objective getMainObjective() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int countObjective() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSensitiveToTheSamePrimitive(Objective another,
			List<Primitive> inputs) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Primitive> getPrimitivesInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isViolate() {
		return p.isViolate();
	}

}
