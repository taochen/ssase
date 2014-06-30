package org.ssascaling.objective;

import java.util.List;
 
import org.ssascaling.observation.listener.Listener;
import org.ssascaling.primitive.Primitive;

public interface Objective {

	
	public double[] getArray();
	
	// True means is QoS, cost otherwise.
	public boolean isQoS();
	
	public boolean isMin();
	
	public boolean isBetter (double v1, double v2);
	
	public double predict(double[] xValue);
	
	public double getConstraint();
	
	public boolean isSatisfied(double[] xValue);
	
	public boolean isChangeSignificant(double v1, double v2);
	
	public void addListener(Listener listener);
	/**
	 * Mainly used by objective to get scalarized objective.
	 * @return
	 */
	public Objective getMainObjective();
	
	public int countObjective();
	
	public boolean isSensitiveToTheSamePrimitive(Objective another, List<Primitive> inputs );
	
	public String getName();
	
	public List<Primitive> getPrimitivesInput();
	
	public boolean isViolate();
}
