package org.ssase.model.sam.ml.timeseries.learner;

import java.io.Serializable;


public interface Learner extends Serializable {

	public double[][] train(double[][] ARx,
			double[][] MAx, double[] y);
	
	public double calculateResidualSumOfSquares();
	
	public double[] train(double[][] x, double[] y);
	
	public double calculateRSquares();
	
	public double getIntercept();
}
