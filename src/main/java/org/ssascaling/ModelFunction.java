package org.ssascaling;


public interface ModelFunction {

	public final int ANN = 0;
	
	public final int ARMAX = 1;
	
	public final int RT = 2;
	
	public double predict(double[] xValue);
	
	public double getResidualSumOfSquares();
	
	public double getSMAPE();

	public double getMAPE();
	
	public double getRSquares();
	
	public long getSampleSize ();
	
	public double calculateEachSMAPE(double ideal, double actual);
	
	public double calculateEachMAPE(double ideal, double actual);
}
