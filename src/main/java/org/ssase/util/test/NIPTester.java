package org.ssase.util.test;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.direct.NelderMeadSimplex;
import org.apache.commons.math3.optimization.direct.SimplexOptimizer;
import org.ssase.util.Util;

public class NIPTester {

	public static void main(String[] a) {
		//org.apache.commons.math3.optimization.direct.
		/*
		 * First, we need to define a parameterized function.
		 * For this example, let's assume that it is hard to get at the
		 * derivative, so we implement a MultivariateRealFunction.
		 * Let's use the Rosenbrock function:
		 * http://en.wikipedia.org/wiki/Rosenbrock_function
		 */
		MultivariateFunction function = new MultivariateFunction() {
			@Override
			public double value(double[] point) {
				double x = point[0];
				double y = point[1];
				return (1 - x) * (1 - x) + 100 * (y - x * x) * (y - x * x);
			}
		};
		SimplexOptimizer optimizer = new SimplexOptimizer(1e-5, 1e-10);
		optimizer.setSimplex(new NelderMeadSimplex(new double[] { 0.2, 0.2 }));
		PointValuePair pair = optimizer.optimize(10000, function, GoalType.MINIMIZE, new double[] { 0, 0 });

		System.out.print(Util.sigmoid(5.3133290144803808) + "\n");
		System.out.print(Util.reverseSigmoid(0.9999514800308) + "\n");
		// Now, let's tell the user about it:
		double[] point = pair.getPoint();
	}
}
