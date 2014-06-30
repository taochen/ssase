package org.ssascaling.model.timeseries.learner;


public abstract class AbstractLearner implements Learner {

	protected int noOfAR = 0;
	
	protected DataTuple transfer(double[][] ARx,
			double[][] MAx, double[] y){
		double[][] x = new double[y.length][];
		
		
		int size = ((noOfAR = ARx[0].length) + MAx[0].length);		

		if (MAx[0].length != 0) {
			double[] xv = null;
			for (int i = 0; i < y.length; i++) {
				xv = new double[size];
				generateArray(ARx[i], xv, 0);
				generateArray(MAx[i], xv, noOfAR);
				x[i] = xv;			
			}
		} else {
			x = ARx;
		}
		
		return new DataTuple(x, y);
	}
	
	protected double[][] transfer (double[] coeffs) {
		double[][] result = new double[2][];
		if (noOfAR == coeffs.length) {
			result[0] = coeffs;
			result[1] = new double[0];
			return result;
		}
				
		double[] ar = new double[noOfAR];
		double[] ma = new double[coeffs.length - noOfAR];
		for (int i = 0; i < noOfAR; i++) {
			ar[i] = coeffs[i];
		}
		for (int i = noOfAR; i < coeffs.length; i++) {
			ma[i - noOfAR] = coeffs[i];
		}
		result[0] = ar;
		result[1] = ma;
		return result;
	}
	private void generateArray(double[] v, double[] xv, int pos) {
		for (int j = 0; j < v.length; j++) {
			xv[j + pos] = v[j];
		}
	}

	
	protected class DataTuple{
		protected double[][] x;
		protected double[] y;
		public DataTuple(double[][] x, double[] y) {
			super();
			this.x = x;
			this.y = y;
		}	
		
	}



}
