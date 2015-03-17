package org.ssascaling.objective.optimization;

import java.util.Arrays;
import java.util.List;

import org.ssascaling.primitive.ControlPrimitive;

public class AntValues {

	// The index in the given primitives.
	private double[][] tau;
	
	public AntValues (List<ControlPrimitive> primitives, double value){
		initialiseTau(primitives, value);
	}
	
	
	private void initialiseTau(List<ControlPrimitive> primitives, double value){
		tau = new double[primitives.size()][];
		for (int i = 0; i < primitives.size(); i++) {
			double[] nested = new double[primitives.get(i).getValueVector().length];
			for (int j = 0; j < nested.length; j++ ) {
				nested[j] = value;
			}
			
			tau[i] = nested;
		}
	}

	public void update(int i, int k, double best /*-1 means only for local update*/, 
			double current /*-1 means only for local update*/, boolean isMin, double max, double min,
			double premax, double premin) {
		for (int j = 0; j < tau[i].length; j++) {
			double dealt = 0.0;

			// Means it is selected
			if (j == k) {

				if (best >= 0) {
					dealt = (isMin) ? 1 / (1 + current - best)
							: (1 + current - best);
				} else {
					dealt = max - min;
				}

			}

			double result = (1 - Structure.VALUE_EVAPORATION) * tau[i][j]
					+ dealt;
			
			/*if (dealt !=0) {
				if (best > 0)
					System.out.print("old tau " + tau[i][j] +  " global dealt " + dealt + " new tau " + result + "\n");
				else
			      System.out.print("old tau " + tau[i][j] +  " local dealt " + dealt + " new tau " + result + "\n");
			}*/

			if (result > max) {
				// Mean it happen due to max/min change.
				/*if (tau[i][j] > max && result < premax) {
					result = max * result/premax;
				} else {
				    result = max;
				}*/
				 result = max;
			} else if (result < min) {
	          /* if (tau[i][j] < min && result > premin) {
	        	   result = min * result/premin;
				} else {
				   result = min;
				}*/
				   result = min;
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

		/*double dealt = Structure.MIN_VALUE_TAU;//Structure.MAX_VALUE_TAU - Structure.MIN_VALUE_TAU;

		double result = isGood ? tau[i][k] + dealt : tau[i][k] - dealt;

		if (result > Structure.MAX_VALUE_TAU) {
			result = Structure.MAX_VALUE_TAU;
		} else if (result < Structure.MIN_VALUE_TAU) {
			result = Structure.MIN_VALUE_TAU;
		}*/

		//tau[i][k] = result;

	}

	public double[][] getTaus() {
		return tau;
	}
	
	public void print(){
		/*for (int i = 0; i < tau.length; i++) {
		System.out.print(Arrays.toString(tau[i]) + "\n");
		}*/
	}
}
