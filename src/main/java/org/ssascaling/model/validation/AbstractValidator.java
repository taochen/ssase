package org.ssascaling.model.validation;

public class AbstractValidator implements Validator {

	
	protected double[][] fittingX = null;
	protected double[][] fittingY = null;
	protected double[] fittingY1 = null;
	
	protected double[][] validatingX = null;
	protected double[] validatingY = null;
	
	
	protected double meanIdeal = 0.0;
	
	@Override
	public int[] validate(double[][] x, double[] y) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int[] validate(double[][] x, double[][] y, double mean) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void do2foldDataSeparation (double[][] x, double[][] y){

		
		int point = x.length/2;
		
		fittingX = new double[point][x[0].length];
		fittingY = new double[point][1];
		
		validatingX = new double[x.length%2 == 0? point : point + 1][x[0].length];
		validatingY = new double[x.length%2 == 0?  point : point + 1];	
		
		
		for (int i = 0; i < point; i++) {
			for (int j = 0; j < x[i].length; j++) {
				fittingX[i][j] = x[i][j];
				validatingX[i][j] = x[i + point][j];
			}
			fittingY[i][0] = y[i][0];
			meanIdeal += y[i][0];
			validatingY[i] = y[i + point][0];
		}
		
		/*
		 * If the total sample size is odd 
		 */
		if (x.length%2 != 0) {
			for (int j = 0; j < x[x.length-1].length; j++) {
				validatingX[point][j] = x[x.length-1][j];
			}
			validatingY[point] = y[x.length-1][0];
		}
		
		meanIdeal = meanIdeal/point;
	}
	
	/**
	 * We split data considering the historical value as entry in 
	 * sensitive primitives matrix (that is, their order)
	 * 
	 * 
	 * We assume that the x are setup already, e.g. the workload of t-1 is with
	 * the QoS at t
	 * @param x
	 * @param y
	 */
    public Object[] doDataSeparation (double[][] x, double[] y, int order){
        int point = x.length - order;
		
        double[][] fittingX = new double[point][(x[0].length + 1) * order];
        double[] fittingY1 = new double[point];
		
		
		for (int i = order; i < x.length; i++) {
			for (int j = 0; j < x[i].length + 1; j++) {
				// Need to add the y value as input
				if (j == x[i].length) {
					for (int k = 0; k < order; k++) {
						fittingX[i - order][j*order + k] = y[i - k - 1];
					}
					
				} else {
					for (int k = 0; k < order; k++) {
						fittingX[i - order][j*order + k] = x[i - k][j];
					}
					
				}				
			}
			fittingY1[i - order] = y[i];
		}
		return new Object[]{fittingX, fittingY1};
    }
	/**
	 * We split data considering the historical value as entry in 
	 * sensitive primitives matrix (that is, their order)
	 * 
	 * 
	 * We assume that the x are setup already, e.g. the workload of t-1 is with
	 * the QoS at t
	 * @param x
	 * @param y
	 */
    protected void do2foldDataSeparation (double[][] x, double[] y, int order){

		
		int point = (x.length - order)/2;
		
		fittingX = new double[point][(x[0].length + 1) * order];
		fittingY1 = new double[point];
		
		validatingX = new double[(x.length - order)%2 == 0? point : point + 1][(x[0].length + 1) * order];
		validatingY = new double[(x.length - order)%2 == 0? point : point + 1];	
		
		
		for (int i = order; i < x.length/2; i++) {
			for (int j = 0; j < x[i].length + 1; j++) {
				// Need to add the y value as input
				if (j == x[i].length) {
					for (int k = 0; k < order; k++) {
						fittingX[i - order][j*order + k] = y[i - k - 1];
						validatingX[i - order][j*order + k] = y[i + point - k - 1];
					}
					
				} else {
					for (int k = 0; k < order; k++) {
						fittingX[i - order][j*order + k] = x[i - k][j];
						validatingX[i - order][j*order + k] = x[i + point - k][j];
					}
					
				}
				
				
			}
			fittingY1[i - order] = y[i];
			meanIdeal += y[i];
			validatingY[i - order] = y[i + point];
		}
		
		/*
		 * If the total sample size is odd 
		 */
		if ((x.length - order)%2 != 0) {
			for (int j = 0; j < x[x.length-1].length + 1; j++) {
				// Need to add the y value as input
				if (j == x[x.length-1].length) {
					for (int k = 0; k < order; k++) {
						validatingX[point][j*order + k] = y[x.length-2 - k ];
					}
					
				} else {
					for (int k = 0; k < order; k++) {
						validatingX[point][j*order + k] = x[x.length-1 - k][j];
					}
					
				}
				
			}
			validatingY[point] = y[x.length-1];
		}
		int n = 0;
		for (int i = 0;i < validatingX[0].length;i++) {
			//System.out.print(validatingX[0][i] + " : " + (n=n+1) + "\n");
		}
		
		meanIdeal = meanIdeal/point;
	}
}
