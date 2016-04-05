package org.ssase.model.sam.ml.ann;

import org.ssase.model.sam.ModelFunction;
/*
 * TODO
 * 1. amend these configuration to each QoS basis
 * 
 */
public interface NeuralNetwork extends ModelFunction {

	// Not in used
	public final int DEFAULT_STRUCTURE_SELECTION_TIME_LIMIT = 5000; //ms
	
	public final int DEFAULT_MAX_TRAINING_TIME_LIMIT = 15000; //15000//ms
	
	
	//public final double DEFAULT_INCREMENTAL_MAPE_PRECENTAGE = 0.15; //%
	
	public final int DEFAULT_SELECTION_LIMIT = 5; //cycle
	
	//public final int DEFAULT_EVALUATE_INTERATION = 500; 
	
	public final int DEFAULT_CHANGE_HIDDEN_NO = 3; 
	
	public final int DEFAULT_MAX_HIDDEN_NO = 30; 
	// Both RSS and MAPE
	public final double DEFAULT_BEST_ERROR_PERCENTAGE= 0.15;// 10%? this should be calculated by RMAPE % and RS/RV %
	
	public final double DEFAULT_BEST_ERROR_PERCENTAGE_ON_STRUCTURE_SELECTION= 0.35;
	

	public final double DEFAULT_WOREST_ERROR_PERCENTAGE= 0.01; // 99% accuracy to prevent over-fitting

	// Used to trigger finding structure together with matrix change
	// Not used
	public final double DEFAULT_PREDICTION_ERROR_PERCENTAGE= 0.1;
	
	public int getNumberOfHidden ();

}
