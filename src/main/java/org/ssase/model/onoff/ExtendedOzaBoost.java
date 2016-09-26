package org.ssase.model.onoff;

import moa.classifiers.Classifier;
import moa.classifiers.meta.OzaBoost;
import moa.options.ClassOption;

public class ExtendedOzaBoost extends OzaBoost {

	public ClassOption[] learners = null;

	@Override
	public void resetLearningImpl() {
		this.ensemble = new Classifier[this.ensembleSizeOption.getValue()];

		for (int i = 0; i < this.ensemble.length; i++) {
			Classifier cla = ((Classifier) getPreparedClassOption(learners[i]));
			cla.resetLearning();
			this.ensemble[i] = cla.copy();
		}
		
		this.scms = new double[this.ensemble.length];
		this.swms = new double[this.ensemble.length];
	}
}
