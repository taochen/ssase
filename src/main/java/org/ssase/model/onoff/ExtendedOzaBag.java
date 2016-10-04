package org.ssase.model.onoff;


import weka.core.Instance;
import moa.classifiers.Classifier;
import moa.classifiers.meta.OzaBag;
import moa.core.DoubleVector;
import moa.core.MiscUtils;
import moa.options.ClassOption;

public class ExtendedOzaBag extends OzaBag{

	
	public  moa.classifiers.Classifier[] learners = null;
	
    @Override
    public void resetLearningImpl() {
        this.ensemble = new Classifier[this.ensembleSizeOption.getValue()];
        
        for (int i = 0; i < this.ensemble.length; i++) {
        	Classifier cla = learners[i];
        	if(cla instanceof CustomWEKAClassifier == false) {
        	   cla.resetLearning();
        	}
            this.ensemble[i] = cla;
        }
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        for (int i = 0; i < this.ensemble.length; i++) {
            int k = MiscUtils.poisson(1.0, this.classifierRandom);
            //System.out.print(k + "*****k ******\n");
            if (k > 0) {
                Instance weightedInst = (Instance) inst.copy();
                weightedInst.setWeight(inst.weight() * k);
                this.ensemble[i].trainOnInstance(weightedInst);
            }
        }
    }

    @Override
    public double[] getVotesForInstance(Instance inst) {
        double result = 0D;
        for (int i = 0; i < this.ensemble.length; i++) {
        	result += this.ensemble[i].getVotesForInstance(inst)[0];
        }
        
        return new double[]{result/ensemble.length};
    }

}