package org.ssase.model.timeseries;

import org.ssase.ModelFunction;


public interface TimeSeries extends ModelFunction {
	
	// Used to trigger finding structure together with matrix change
	// Not used
	public final double DEFAULT_PREDICTION_ERROR_PERCENTAGE= 0.1;
	
	public static final int DEFAULT_CHANGE_ORDER_NO = 1;
	
	public static final int DEFAULT_MAX_ORDER_NO = 3;

	public double predict(double[] ARvalue, double[] MAvalue);
	
	public double[] getAllCoefficients();
}
