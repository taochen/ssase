package org.ssase.model.sam.ml.ann;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.model.sam.validation.AbstractValidator;
import org.ssase.model.sam.validation.Validator;

public class NeuralNetworkCrossValidator extends AbstractValidator implements Validator{

	protected static final Logger logger = LoggerFactory
	.getLogger(NeuralNetworkCrossValidator.class);
	
	private int DEFAULT_CHANGE_HIDDEN_NO = 
		NeuralNetwork.DEFAULT_CHANGE_HIDDEN_NO;
	
	
	private int DEFAULT_MAX_HIDDEN_NO = 
		NeuralNetwork.DEFAULT_MAX_HIDDEN_NO;
	
	
	private double DEFAULT_BEST_ERROR_PERCENTAGE_ON_STRUCTURE_SELECTION = 
		NeuralNetwork.DEFAULT_BEST_ERROR_PERCENTAGE_ON_STRUCTURE_SELECTION;
	
	private double[] fConfig;
	
	@Deprecated
	public NeuralNetworkCrossValidator(){
		
	}
	
	public NeuralNetworkCrossValidator(double[] config, double[] fConfig) {
		this.fConfig = fConfig;
		this.DEFAULT_CHANGE_HIDDEN_NO = (int)config[0];
		this.DEFAULT_MAX_HIDDEN_NO = (int)config[1];
		this.DEFAULT_BEST_ERROR_PERCENTAGE_ON_STRUCTURE_SELECTION = config[2];
	}

	@Override
	/**
	 * The approach here is the implementation of 2 fold cross-validation
	 */
	public int[] validate(double[][] x, double[][] y, double mean) {
		
		do2foldDataSeparation (x,y);
		
		return searchNetworkStructure(x,y,mean,DEFAULT_CHANGE_HIDDEN_NO, 0);
	}
	
	private int[] searchNetworkStructure (double[][] x, double[][] y, double mean, int currentHidden, double previousMAPE) {
		logger.info("Hidden number: " + currentHidden);
		if (currentHidden > DEFAULT_MAX_HIDDEN_NO) {
			 logger.info("Due to excced the default max number, determined Hidden number: " + (currentHidden - DEFAULT_CHANGE_HIDDEN_NO) + ", with MAPE " + previousMAPE);
			 return new int[]{currentHidden - DEFAULT_CHANGE_HIDDEN_NO};
		}
		NeuralNetwork model = new EncogFeedForwardNeuralNetwork(fittingX,fittingY,meanIdeal,currentHidden,0, true, fConfig);
		// For determining the number of hidden neurons, we only use MAPE as metric
		// To define quality, we use both RS and SMAPE
		double MAPE = 0.0;
		double output = 0.0;
		for (int i = 0; i < validatingY.length; i++) {
			output = model.predict(validatingX[i]);
			MAPE += model.calculateEachMAPE(validatingY[i], output);
			logger.debug("2 fold cross-validation, Actual=" + output + ", Ideal=" + validatingY[i]);
		}
		MAPE = MAPE/validatingY.length;
		logger.info("Current MAPE: " + MAPE + " vs previous MAPE: " + previousMAPE);
		if (previousMAPE !=0 && previousMAPE < MAPE && previousMAPE < 1 && MAPE < 1 /*This is to prevent non-sensed result*/) {
			NeuralNetwork testModel = new EncogFeedForwardNeuralNetwork(x,y,mean,currentHidden - DEFAULT_CHANGE_HIDDEN_NO,0, true, fConfig);
			//It also need to make sure that the hidden number is trainable
			if (testModel.getMAPE() <= DEFAULT_BEST_ERROR_PERCENTAGE_ON_STRUCTURE_SELECTION) {
			   logger.info("Determined Hidden number: " + (currentHidden - DEFAULT_CHANGE_HIDDEN_NO) + ", with MAPE " + previousMAPE);
			   return new int[]{currentHidden - DEFAULT_CHANGE_HIDDEN_NO};
			}
			
			logger.info("Failure on determined Hidden number: " + (currentHidden - DEFAULT_CHANGE_HIDDEN_NO) + ", as it can only be trained with MAPE " + testModel.getMAPE());
		}
		
		return searchNetworkStructure(x,y,mean,currentHidden + DEFAULT_CHANGE_HIDDEN_NO, MAPE);
	}
}
