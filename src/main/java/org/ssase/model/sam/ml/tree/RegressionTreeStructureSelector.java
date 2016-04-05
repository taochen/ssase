package org.ssase.model.sam.ml.tree;

import org.ssase.model.sam.ModelFunction;
import org.ssase.model.selection.StructureSelector;

public class RegressionTreeStructureSelector implements StructureSelector {

	private ModelFunction model = null;
	
	@Override
	public void decideStructure(double[][] x, double[][] y, double meanIdeal, double[] functionConfig,
			double[] structureConfig) {
		// TODO Auto-generated method stub

	}

	@Override
	public void decideStructure(double[][] x, double[] y, double[] config) {
		model = new RegressionTree(x, y);

	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ModelFunction getModelFunction() {
		return model;
	}

	@Override
	public Object[] doDataSeparation(double[][] x, double[] y) {
		// TODO Auto-generated method stub
		return null;
	}

}
