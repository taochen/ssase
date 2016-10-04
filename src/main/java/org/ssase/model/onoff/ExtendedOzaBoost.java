package org.ssase.model.onoff;

import weka.core.Instance;
import moa.classifiers.Classifier;
import moa.classifiers.meta.OzaBoost;
import moa.core.DoubleVector;
import moa.core.MiscUtils;
import moa.options.ClassOption;

public class ExtendedOzaBoost extends OzaBoost {

	
	public  moa.classifiers.Classifier[] learners = null;
	
	private double weight = 0.4;
	
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
        
    	this.scms = new double[this.ensemble.length];
		this.swms = new double[this.ensemble.length];
    }
    

    protected double getEnsembleMemberWeight(int i) {
        double em = this.swms[i] / (this.scms[i] + this.swms[i]);
        
        if ((em == 0.0) || (em > 0.5)) {
            return 0.0;
        }
        double Bm = em / (1.0 - em);
        return Math.log(1.0 / Bm);
    }
    
    @Override
    public void trainOnInstanceImpl(Instance inst) {
        double lambda_d = 1.0;
        for (int i = 0; i < this.ensemble.length; i++) {
            double k = this.pureBoostOption.isSet() ? lambda_d : MiscUtils.poisson(lambda_d, this.classifierRandom);
            if (k > 0.0) {
                Instance weightedInst = (Instance) inst.copy();
                weightedInst.setWeight(inst.weight() * k);
                this.ensemble[i].trainOnInstance(weightedInst);
            }
            
            double pred = this.ensemble[i].getVotesForInstance(inst)[0];
           
            if (pred / inst.value(inst.numAttributes()-1) >= weight) {
                this.scms[i] += lambda_d;
                lambda_d *= this.trainingWeightSeenByModel / (2 * this.scms[i]);
            } else {
                this.swms[i] += lambda_d;
                lambda_d *= this.trainingWeightSeenByModel / (2 * this.swms[i]);
            }
            System.out.print(i +" swms[i ]" + this.swms[i]+" scms[i]" +scms[i] +"\n");
        }
    }

    public double[] getVotesForInstance(Instance inst) {
    	  double result = 0D;
    	  int size = 0;
          for (int i = 0; i < this.ensemble.length; i++) {
        	  double memberWeight = getEnsembleMemberWeight(i);
        	  System.out.print("memberWeight " + memberWeight+"\n");
        	  if (memberWeight > 0.0) {
        		  size++;
             	result += memberWeight * this.ensemble[i].getVotesForInstance(inst)[0];
        	  } else {
        		  break;
        	  }
          }
          
          return new double[]{result/size};
    }
}
