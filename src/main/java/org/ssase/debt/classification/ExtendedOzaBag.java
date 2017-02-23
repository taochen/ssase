package org.ssase.debt.classification;

import moa.classifiers.Classifier;
import moa.classifiers.meta.OzaBag;
import moa.options.ClassOption;

public class ExtendedOzaBag extends OzaBag{

	
	public ClassOption[] learners = null;
	
    @Override
    public void resetLearningImpl() {
        this.ensemble = new Classifier[this.ensembleSizeOption.getValue()];
        
        for (int i = 0; i < this.ensemble.length; i++) {
        	Classifier cla = ((Classifier) getPreparedClassOption(learners[i]));
        	cla.resetLearning();
            this.ensemble[i] = cla.copy();
        }
    }

}
