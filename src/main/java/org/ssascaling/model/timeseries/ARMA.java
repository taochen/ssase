package org.ssascaling.model.timeseries;


import org.ssascaling.AbstractModelFunction;
import org.ssascaling.model.timeseries.learner.Learner;
import org.ssascaling.model.timeseries.learner.LearnerInstanceFactory;

public class ARMA extends AbstractModelFunction implements TimeSeries{
	
	// This ar can also be used to represent all coeff if
	// performance is critical
	private double[] ARcoeff;
	// Actually all the parameters are considered as ARcoeff
	private double[] MAcoeff;
	
	private double[][] x;
	private double[] y;
	
	private double SMAPE = 0.0;
	private double MAPE = 0.0;
	private Learner trainer;
	private long sample;
	
	public ARMA(double[][] ARx,
			double[][] MAx, double[] y) {
		super();
		this.y = y;
		sample = y.length;
		trainer = LearnerInstanceFactory.getTranerInstance();
		train(ARx, MAx, y);
	}
	
	public ARMA(double[][] x, double[] y) {
		super();
		this.x = x;
		this.y = y;
		sample = y.length;
		trainer = LearnerInstanceFactory.getTranerInstance();
		train(x, y);
	}
	
	@Override
	public double predict(double[] ARvalue, double[] MAvalue){
		double result =  0.0;
		for (int i = 0; i < ARvalue.length; i++) {
			result += ARcoeff[i] * ARvalue[i];	
		}
		for (int i = 0; i < MAvalue.length; i++) {
			result += MAcoeff[i] * MAvalue[i];	
		}
		return result;
	}

	private void train(double[][] ARx,
			double[][] MAx, double[] y) {
		// 0 - AR
		// 1 - MA
		double[][] coeffs = trainer.train(ARx, MAx, y);
		ARcoeff = coeffs[0];
		MAcoeff = coeffs[1];
		
	}
	
	private void train(double[][] x, double[] y) {
		// 0 - AR
		// 1 - MA
		ARcoeff = trainer.train(x, y);
		MAcoeff = new double[0];
		double MAPE = 0.0;
		double SMAPE = 0.0;
		double yMean = 0.0;
		int no = 0;
		
		for (int i = 0; i < x.length; i++) {
			double output = predict(x[i]);
			SMAPE += calculateEachSMAPE(y[i], output);
			MAPE += calculateEachMAPE(y[i], output);
			yMean += y[i];
			no++;
		}
		
		this.MAPE = MAPE/no;
		this.SMAPE = SMAPE/no;
	}
	
	public double getResidualSumOfSquares(){
		return trainer.calculateResidualSumOfSquares();
	}
	
	public double getSMAPE(){
		return SMAPE;
	}
	
	public double[] getQuality(){
		return null;
	}
	
	public double getMAPE() {
		return MAPE;
	}
	
	public double getRSquares(){
		return trainer.calculateRSquares();
	}
	
	public double[] getAllCoefficients(){
		double[] coeffs = new double[ARcoeff.length + MAcoeff.length];
		for (int i = 0; i < ARcoeff.length; i++) {
			coeffs[i] = ARcoeff[i];	
		}
		for (int i = 0; i < MAcoeff.length; i++) {
			coeffs[i + ARcoeff.length] = MAcoeff[i];
		}
		return coeffs;
	}

	@Override
	public double predict(double[] xValue) {
		double result =  0.0;
		for (int i = 0; i < xValue.length; i++) {
			result += ARcoeff[i] * xValue[i];	
		}
		return Math.abs(result);
	}
	
	@Override
	public long getSampleSize() {
		return sample;
	}
	
	public void testFilter(){
		
		double output = 0.0;
		double error = 0.0;
		for (int i = 0; i < x.length; i++) {
			output = predict(x[i]);
			error = y[i] - output;
			for (int j = 0; j < ARcoeff.length; j++) {
				ARcoeff[j] += 0.01*error*x[i][j];
			}
		}
		
		double MAPE = 0.0;
		double SMAPE = 0.0;
		double yMean = 0.0;
		int no = 0;
		
		for (int i = 0; i < x.length; i++) {
			output = predict(x[i]);
			SMAPE += calculateEachSMAPE(y[i], output);
			MAPE += calculateEachMAPE(y[i], output);
			yMean += y[i];
			no++;
		}
		
		this.MAPE = MAPE/no;
		this.SMAPE = SMAPE/no;
	}
}
