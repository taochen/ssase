package org.ssase.model.onoff;

import java.util.Random;

import moa.core.DoubleVector;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

public class ExtendedSGD extends weka.classifiers.functions.SGD {
	
	  private  InternalExtendedSGD sgd = new InternalExtendedSGD();
	
	 
	  
	  public ExtendedSGD(){
		  sgd.prepareForUse();
	  }

	  public void buildClassifier(Instances data) throws Exception {
		  for (int e = 0; e < 100; e++) {
		      for (int i = 0; i < data.numInstances(); i++) {
		        sgd.trainOnInstanceImpl(data.instance(i), 100);
		      }
		    }
	  }
	  
	  @Override
	  public double[] distributionForInstance(Instance inst) throws Exception {
		  return sgd.getVotesForInstance(inst);
	  }
	  
	  private class InternalExtendedSGD extends moa.classifiers.functions.SGD {
		  private int times = 1;
		  public void trainOnInstanceImpl(Instance instance, int times) {
			  this.times = times;
			  trainOnInstanceImpl(instance);
		  }
		  
	    public void trainOnInstanceImpl(Instance instance) {

	        if (m_weights == null) {
	            m_weights = new DoubleVector(); 
	            m_bias = 0.0;
	        }

	        if (!instance.classIsMissing()) {

	            double wx = dotProd(instance, m_weights, instance.classIndex());

	            double y;
	            double z;
	            if (instance.classAttribute().isNominal()) {
	                y = (instance.classValue() == 0) ? -1 : 1;
	                z = y * (wx + m_bias);
	            } else {
	                y = instance.classValue();
	                z = y - (wx + m_bias);
	                y = 1;
	            }

	            // Compute multiplier for weight decay
	            double multiplier = 1.0;
	            if (m_numInstances == 0) {
	                multiplier = 1.0 - (m_learningRate * m_lambda) / (m_t/times + 1);
	            } else {
	                multiplier = 1.0 - (m_learningRate * m_lambda) / m_numInstances;
	            }
	            for (int i = 0; i < m_weights.numValues(); i++) {
	                m_weights.setValue(i,m_weights.getValue (i) * multiplier);
	            }

	            // Only need to do the following if the loss is non-zero
	            if (m_loss != HINGE || (z < 1)) {

	                // Compute Factor for updates
	                double factor = m_learningRate * y * dloss(z);

	                // Update coefficients for attributes
	                int n1 = instance.numValues();
	                for (int p1 = 0; p1 < n1; p1++) {
	                    int indS = instance.index(p1);
	                    if (indS != instance.classIndex() && !instance.isMissingSparse(p1)) {
	                        m_weights.addToValue(indS, factor * instance.valueSparse(p1));
	                    }
	                }

	                // update the bias
	                m_bias += factor;
	            }
	            
	           
	              m_t++;
	            
	        }
	    }
	  }

}
