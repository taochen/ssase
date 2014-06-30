package org.ssascaling.model.timeseries.learner;

//import org.closlaes.timeseries.trainer.apache.OLSMultipleLinearRegression;



import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

public class ApacheOLSLearner extends AbstractLearner {
	
	private OLSMultipleLinearRegression  regression;
	
	@Override
	public double[][] train(double[][] ARx,
			double[][] MAx, double[] y) {
		regression = new OLSMultipleLinearRegression();
		regression.setNoIntercept(true);
		final DataTuple data = super.transfer(ARx, MAx, y);
	
		
		regression.newSampleData(data.y, data.x);
		return transfer(regression.estimateRegressionParameters());
	}
	
	@Override
	public double[] train(double[][] x, double[] y) {
		regression = new OLSMultipleLinearRegression();
		regression.setNoIntercept(true);
		regression.newSampleData(y, x);
		return regression.estimateRegressionParameters();
	}
	
	public double calculateResidualSumOfSquares() {
		return regression.calculateResidualSumOfSquares();
	}

	@Override
	public double calculateRSquares() {
		return regression.calculateRSquared();
	}
	
}
