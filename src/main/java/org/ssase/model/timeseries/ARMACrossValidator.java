package org.ssase.model.timeseries;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.model.ann.NeuralNetwork;
import org.ssase.model.validation.AbstractValidator;
import org.ssase.model.validation.Validator;

public class ARMACrossValidator extends AbstractValidator implements Validator {
	
	
	protected static final Logger logger = LoggerFactory
	.getLogger(ARMACrossValidator.class);
	
	private int DEFAULT_CHANGE_ORDER_NO = 
		TimeSeries.DEFAULT_CHANGE_ORDER_NO;
	
	private int DEFAULT_MAX_ORDER_NO = 
		TimeSeries.DEFAULT_MAX_ORDER_NO;
	
	private int threshold = 0;
	
	@Deprecated
	public ARMACrossValidator(){
		
	}
	
	public ARMACrossValidator(double[] config){
		this.DEFAULT_CHANGE_ORDER_NO = (int)config[0];
		this.DEFAULT_MAX_ORDER_NO = (int)config[1];
	}
	
	public int[] validate(double[][] x, double[] y) {
		
		threshold = y.length / (x[0].length + 1);
		
		return searchModelStructure(x, y, DEFAULT_CHANGE_ORDER_NO, 0);
	}
	
	private int[] searchModelStructure (double[][] x, double[]y,
			int order, double previousMAPE){
		logger.info("Order number: " + order);
		do2foldDataSeparation (x,y,order);
	
		if (fittingX.length <= fittingX[0].length) {
			logger.info("Due to not enough data, determined order number: " + (order - DEFAULT_CHANGE_ORDER_NO) + ", with MAPE " + previousMAPE);		   
			return new int[]{order == 1? 1 : order - DEFAULT_CHANGE_ORDER_NO};
		}
		
		if (order > DEFAULT_MAX_ORDER_NO) {
			logger.info("Due to excced the max order, determined order number: " + (order - DEFAULT_CHANGE_ORDER_NO) + ", with MAPE " + previousMAPE);		   
			return new int[]{order == 1? 1 : order - DEFAULT_CHANGE_ORDER_NO};
		}
		
		TimeSeries model = null;
		try {
		    model = new ARMA(fittingX, fittingY1);
		} catch (Exception e) {
			logger.error("Due to " + e + ", determined order number: " + (order - DEFAULT_CHANGE_ORDER_NO) + ", with MAPE " + previousMAPE);		   
			return new int[]{order == 1? 1 : order - DEFAULT_CHANGE_ORDER_NO};
		}
		// For determining the number of order, we only use MAPE as metric
		double MAPE = 0.0;
		double output = 0.0;
		for (int i = 0; i < validatingY.length; i++) {
			output = model.predict(validatingX[i]);
			MAPE += model.calculateEachMAPE(validatingY[i], output);
			logger.info("2 fold cross-validation, Actual=" + output + ", Ideal=" + validatingY[i] + " MAPE " + MAPE);
		}
		MAPE = MAPE/validatingY.length;
		logger.info("Current MAPE: " + MAPE + " vs previous MAPE: " + previousMAPE);
		if (previousMAPE !=0 && MAPE >= previousMAPE) {
			logger.info("Determined order number: " + (order - DEFAULT_CHANGE_ORDER_NO) + ", with MAPE " + previousMAPE);		   
			return new int[]{order - DEFAULT_CHANGE_ORDER_NO};
		}
		
		// The number of sample data is insufficient to reach higher degree
		if (threshold < order + DEFAULT_CHANGE_ORDER_NO) {
			logger.info("Determined order number: " + order + ", with MAPE " + MAPE);
			return new int[]{order};
		}
		
		return searchModelStructure(x, y, order + DEFAULT_CHANGE_ORDER_NO, MAPE);
	}
}
