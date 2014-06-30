package org.ssascaling.objective.optimization;

import java.util.List;

import org.ssascaling.primitive.ControlPrimitive;

public class AntValues {

	// The index in the given primitives.
	private double[][] tau;
	
	public AntValues (List<ControlPrimitive> primitives){
		initialiseTau(primitives);
	}
	
	
	private void initialiseTau(List<ControlPrimitive> primitives){
		tau = new double[primitives.size()][];
		for (int i = 0; i < primitives.size(); i++) {
			double[] nested = new double[primitives.get(i).getValueVector().length];
			for (int j = 0; j < primitives.size(); j++ ) {
				nested[j] = Structure.MIN_VALUE_TAU;
			}
			
			tau[i] = nested;
		}
	}

	public void update(int i, int k, double best /*-1 means only for local update*/, 
			double current /*-1 means only for local update*/, boolean isMin) {

		for (int j = 0; j < tau[i].length; j++) {
			double dealt = 0.0;

			// Means it is selected
			if (j == k) {

				if (best >= 0) {
					dealt = (isMin) ? 1 / (1 + current - best)
							: (1 + current - best);
				} else {
					dealt = Structure.MAX_VALUE_TAU - Structure.MIN_VALUE_TAU;
				}

			}

			double result = (1 - Structure.VALUE_EVAPORATION) * tau[i][j]
					+ dealt;

			if (result > Structure.MAX_VALUE_TAU) {
				result = Structure.MAX_VALUE_TAU;
			} else if (result < Structure.MIN_VALUE_TAU) {
				result = Structure.MIN_VALUE_TAU;
			}

			tau[i][j] = result;

		}
	}
	
	
	/**
	 * Used to cope with dynamics.
	 * @param i
	 * @param k
	 * @param isGood
	 */
	public void update(int i, int k, boolean isGood) {

		double dealt = Structure.MAX_VALUE_TAU - Structure.MIN_VALUE_TAU;

		double result = isGood ? tau[i][k] + dealt : tau[i][k] - dealt;

		if (result > Structure.MAX_VALUE_TAU) {
			result = Structure.MAX_VALUE_TAU;
		} else if (result < Structure.MIN_VALUE_TAU) {
			result = Structure.MIN_VALUE_TAU;
		}

		tau[i][k] = result;

	}

	public double[][] getTaus() {
		return tau;
	}
}
