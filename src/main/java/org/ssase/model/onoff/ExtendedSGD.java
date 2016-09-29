package org.ssase.model.onoff;

import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

public class ExtendedSGD extends weka.classifiers.functions.SGD {
	
	  private  moa.classifiers.functions.SGD sgd = new moa.classifiers.functions.SGD();
	
	  public ExtendedSGD(){
		  sgd.prepareForUse();
	  }

	  public void buildClassifier(Instances data) throws Exception {
		  for (int e = 0; e < 100; e++) {
		      for (int i = 0; i < data.numInstances(); i++) {
		        sgd.trainOnInstanceImpl(data.instance(i));
		      }
		    }
	  }
	  
	  @Override
	  public double[] distributionForInstance(Instance inst) throws Exception {
		  return sgd.getVotesForInstance(inst);
	  }
}
