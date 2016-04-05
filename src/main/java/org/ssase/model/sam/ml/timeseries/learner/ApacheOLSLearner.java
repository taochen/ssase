package org.ssase.model.sam.ml.timeseries.learner;

//import org.closlaes.timeseries.trainer.apache.OLSMultipleLinearRegression;



import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

public class ApacheOLSLearner extends AbstractLearner {
	
	private OLSMultipleLinearRegression  regression;
	
	private double intercept;
	
	@Override
	public double[][] train(double[][] ARx,
			double[][] MAx, double[] y) {
		regression = new OLSMultipleLinearRegression();
		regression.setNoIntercept(true);
		final DataTuple data = super.transfer(ARx, MAx, y);
	
		
		regression.newSampleData(data.y, data.x);
		
		
		 intercept = regression.isNoIntercept()? 0 : regression.estimateRegressionParameters()[0];
		   
		   double[] result = null;
		   if(regression.isNoIntercept()){
			   result = regression.estimateRegressionParameters();
		   } else {
			   double[] para = regression.estimateRegressionParameters();
			   double[] array = new double[para.length-1];
			   System.arraycopy(para, 1, array, 0, para.length);
			   result = array;
		   }
			   
		
		return transfer(result);
	}
	
	@Override
	public double[] train(double[][] x, double[] y) {
		try {
		   regression = new OLSMultipleLinearRegression();
		   regression.setNoIntercept(true);
		   regression.newSampleData(y, x);
		
		   intercept = regression.isNoIntercept()? 0 : regression.estimateRegressionParameters()[0];
		   
		   double[] result = null;
		   if(regression.isNoIntercept()){
			   result = regression.estimateRegressionParameters();
		   } else {
			   double[] para = regression.estimateRegressionParameters();
			   double[] array = new double[para.length-1];
			   System.arraycopy(para, 1, array, 0, para.length-1);
			   result = array;
		   }
			   
		   
		   return result;
		} catch (Exception e) {
			System.err.print("X=" + x.length + ", Y=" + y.length + "\n");
			e.printStackTrace();
			
			//System.err.print("Too little samples for trainning ARMAX or the matrix is singular.\n");
			return new double[0];
		}
	}
	
	public double calculateResidualSumOfSquares() {
		return regression.calculateResidualSumOfSquares();
	}

	@Override
	public double calculateRSquares() {
		return regression.calculateRSquared();
	}

	@Override
	public double getIntercept() {
		// TODO Auto-generated method stub
		return intercept;
	}
	
}
