package org.ssascaling.model.timeseries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssascaling.ModelFunction;
import org.ssascaling.model.selection.StructureSelector;
import org.ssascaling.model.validation.Validator;

public class ARMAStructureSelector implements StructureSelector{

	protected static final Logger logger = LoggerFactory
	.getLogger(ARMAStructureSelector.class);
	private int order = 1;
	private ModelFunction model = null;
	@Override
	public void decideStructure(double[][] x, double[][] y, double meanIdeal, double[] functionConfig,
			double[] structureConfig) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void decideStructure(double[][] x, double[] y, double[] config) {
		Validator validator = (config == null  || config.length == 0)? new ARMACrossValidator() : new ARMACrossValidator(config);
		long time = System.currentTimeMillis();
		order = validator.validate(x, y)[0];
		System.out.print("Time used for determining model strcture: " + (System.currentTimeMillis() - time) + "\n");
		
		final Object[] object = validator.doDataSeparation(x,y, order);
		
		model = new ARMA((double[][])object[0], (double[])object[1]);
		logger.debug("The determined number of order is " + order);
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public ModelFunction getModelFunction() {
		return model;
	}

	@Override
	public Object[] doDataSeparation(double[][] x, double[] y) {
		return new ARMACrossValidator().doDataSeparation(x,y, order);
	}

}
