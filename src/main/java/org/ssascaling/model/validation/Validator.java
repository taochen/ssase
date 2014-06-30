package org.ssascaling.model.validation;

public interface Validator {

	public int[] validate (double[][] x, double[] y);
	
	public int[] validate (double[][] x, double[][] y, double mean);
	
	public Object[] doDataSeparation (double[][] x, double[] y, int order);
}
