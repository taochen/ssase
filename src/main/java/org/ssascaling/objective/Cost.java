package org.ssascaling.objective;

import java.util.List;

import org.ssascaling.observation.listener.Listener;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;

/**
 * The cost is per-service-instance.
 * @author tao
 *
 */
public class Cost implements Objective  {

	// Actually only control primitives.
	private List<Primitive> inputs;
	// price per single unit, even the margin could be different
	// e.g., charge every 5 mb memory, but here we need the price
	// for each 1 mb memory.
	
	// but memory also need to divide by the number of services co-located on the VM.
	private double[] prices;
	private double constraint;
	
	private String name;
	
	
	public Cost(String name, List<Primitive> p, double[] prices, double constraint) {
		super();
		this.name = name;
		this.inputs = p;
		this.prices = prices;
		this.constraint = constraint;
	}

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
		return true;
	}

	@Override
	public boolean isBetter(double v1, double v2) {
		// TODO Auto-generated method stub
		return v1 <= v2;
	}

	@Override
	public double predict(double[] xValue) {
		double result = 0;
		for (int i = 0; i < prices.length; i++) {
			result += xValue[i] * prices[i];
		}
		return result;
	}

	@Override
	public double getConstraint() {
		// TODO Auto-generated method stub
		return constraint;
	}

	@Override
	public boolean isSatisfied(double[] xValue) {
		// TODO Auto-generated method stub
		return constraint >= predict(xValue);
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
		return this;
	}

	@Override
	public int countObjective() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSensitiveToTheSamePrimitive(Objective another,
			List<Primitive> anotherInputs) {
		
		boolean result = false;
		
		if (another != null) {
			result = another.isSensitiveToTheSamePrimitive(null, inputs);		
		} else {
			
			for (Primitive p : inputs) {
				
				if (result) {
					break;
				}
				
				
				for (Primitive subP : anotherInputs) {
					if(subP.equals(p)) {
						result = true;
						break;
					}
				}
				
			}
		}
		
		return result;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Primitive> getPrimitivesInput() {
		return inputs;
	}

	/**
	 * Cost can be ensured during optimization/simulation,
	 * 
	 * therefore it is not flucuated and changed.
	 */
	@Override
	public boolean isViolate() {
		
		for (Primitive p : inputs) {
			if (((ControlPrimitive)p).isViolate()){
				return true;
			}
		}
		
		return false;
	}

	@Override
	public double getCurrentPrediction() {
		double[] xValue = new double[inputs.size()];
		for (int i = 0; i < xValue.length; i++) {			
			xValue[i] = inputs.get(i).getProvision();		 
		}
		return predict(xValue);
	}
	
	public void setInputs ( List<Primitive> inputs) {
		this.inputs = inputs;
	}

	@Override
	public double getMax() {
		// TODO Auto-generated method stub
		return 0;
	}
}
