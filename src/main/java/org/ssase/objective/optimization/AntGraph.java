package org.ssase.objective.optimization;

import java.util.List;

import org.ssase.primitive.ControlPrimitive;

public class AntGraph {

	private List<ControlPrimitive> primitives;
	
	// The index in the search sequence/ the index in the given primitives.
	private double[][] tau;
	
	
	public ControlPrimitive getPrimitive (int i) {
		return primitives.get(i);
	}
	
	public void update (int i, int j) {
		
		/*double result = (1 - Structure.GRAPH_EVAPORATION) * tau[i][j] + 
	    (Structure.MAX_GRAPH_TAU - Structure.MIN_GRAPH_TAU);
		
		if (result > Structure.MAX_GRAPH_TAU) {
			result = Structure.MAX_GRAPH_TAU;
		} else if (result < Structure.MIN_GRAPH_TAU) {
			result = Structure.MIN_GRAPH_TAU;
		}
		
		tau[i][j] = result;*/
	}
	
	public double[] getTaus(int i) {
		return tau[i];
	}
}
