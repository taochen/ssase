package org.ssascaling.model.ann;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssascaling.ModelFunction;
import org.ssascaling.model.selection.StructureSelector;
import org.ssascaling.model.validation.Validator;

public class NeuralNetworkStructureSelector implements StructureSelector{

	protected static final Logger logger = LoggerFactory
	.getLogger(NeuralNetworkStructureSelector.class);
	
	private int hidden = 0;
	private ModelFunction model = null;
	@Override
	public void decideStructure(double[][] x, double[][] y, double meanIdeal, 
			double[] functionConfig,
			double[] structureConfig) {
		Validator validator = (structureConfig == null || structureConfig.length == 0)? new NeuralNetworkCrossValidator() : new NeuralNetworkCrossValidator(structureConfig, functionConfig);
		long time = System.currentTimeMillis();
		hidden = validator.validate(x, y, meanIdeal)[0];
		System.out.print("Time used for determining model strcture: " + (System.currentTimeMillis() - time) + "\n");
		model = new EncogFeedForwardNeuralNetwork(x,y,meanIdeal,hidden,0, true, functionConfig);
		logger.debug("The determined number of hidden neuron is " + hidden);
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return hidden;
	}

	
	public void decideStructure(double[][] x, double[] y, double[] config) {
		// TODO Auto-generated method stub
		
	}
	
	public ModelFunction getModelFunction() {
		return model;
	}

	@Override
	public Object[] doDataSeparation(double[][] x, double[] y) {
		// TODO Auto-generated method stub
		return null;
	}

}
