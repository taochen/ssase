package org.ssascaling.model.timeseries.learner;


public interface Learner {

	public double[][] train(double[][] ARx,
			double[][] MAx, double[] y);
	
	public double calculateResidualSumOfSquares();
	
	public double[] train(double[][] x, double[] y);
	
	public double calculateRSquares();
	
	public double getIntercept();
}
