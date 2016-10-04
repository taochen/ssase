package org.ssase.util.test;

import java.util.ArrayList;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.direct.NelderMeadSimplex;
import org.apache.commons.math3.optimization.direct.SimplexOptimizer;
import org.ssase.model.Model;
import org.ssase.primitive.Primitive;
import org.ssase.util.Util;

import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

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
		
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();

	
		attrs.add(new Attribute("ttt", 0));
		

		attrs.add(new Attribute("oo", 1));

		Instances dataRaw = new Instances("data_instances", attrs, 0);
		dataRaw.setClassIndex(dataRaw.numAttributes() - 1);
		
		int k =100000;
		

		for (int i = 0; i < k; i++) {
			final Instance trainInst = new DenseInstance(2);
			trainInst.setValue(
					attrs.get(0), i);
			trainInst.setValue(
					attrs.get(1), i+1);
			
			dataRaw.add(trainInst);
			trainInst.setDataset(dataRaw);
		}

		IBk knn = new IBk();
		
		try {
			knn.buildClassifier(dataRaw);
		} catch (Exception e1) {
			// TODO Auto-generated catch block 1192000
			e1.printStackTrace();
		}
		//knn = new IBk();
		long t = System.currentTimeMillis();
		Instance in  = dataRaw.instance(1);
		try {
			knn.buildClassifier(dataRaw);
			//knn.updateClassifier(in);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.print((System.currentTimeMillis() - t));
		
		// Now, let's tell the user about it:
		double[] point = pair.getPoint();
	}
}
