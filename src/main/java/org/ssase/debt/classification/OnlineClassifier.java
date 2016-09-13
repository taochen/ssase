package org.ssase.debt.classification;

import java.util.ArrayList;
import java.util.List;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.trees.HoeffdingTree;
import weka.core.Attribute;

import org.ssase.objective.QualityOfService;
import org.ssase.primitive.*;

public class OnlineClassifier {

	
	private AbstractClassifier classifier;
	private ArrayList<Attribute> attrs = new ArrayList<Attribute>();
	private Instances dataRaw = null;
	
	
	public OnlineClassifier(List<QualityOfService> qos, List<Primitive> primitives){
  	  
  	      int i = 0;
		  for (Primitive p : primitives) {
			  attrs.add(new Attribute(p.getName(), i)); 			
		      i++;
		  }
		  
		  // Extent of violation/improvement
		  for (QualityOfService q : qos) {
			  attrs.add(new Attribute(q.getName(), i)); 			
		      i++;
		  }
		
  	      attrs.add(new Attribute("Expert",i));
		  
		  dataRaw = new Instances("data_instances", attrs ,0);
		  dataRaw.setClassIndex(attrs.size()-1);
		  
		  classifier = new HoeffdingTree();
		  classifier.resetLearningImpl();
	}
	
	/**
	 * We use raw data for training for now.
	 * @param judge
	 * @param qos
	 * @param primitives
	 */
	public void trainOnInstance(int judge, List<QualityOfService> qos, List<Primitive> primitives){
		  final Instance trainInst = new DenseInstance(primitives.size() + qos.size() + 1);
		
		  int i = 0;
		  for (Primitive p : primitives) {
			  trainInst.setValue(attrs.get(i), p.getValue());
			  i++;
		  }
		  
		  // Extent of violation/improvement
		  for (QualityOfService q : qos) {
			  trainInst.setValue(attrs.get(i), q.getExtentOfViolation(true));			
		      i++;
		  }
		  
		  trainInst.setValue(attrs.get(i), judge);	
		  
		  dataRaw.add(trainInst);
		  
		  classifier.trainOnInstance(trainInst);
	}
	
	public int predict(List<QualityOfService> qos, List<Primitive> primitives){
		 final Instance trainInst = new DenseInstance(primitives.size() + qos.size());
			
		  int i = 0;
		  for (Primitive p : primitives) {
			  trainInst.setValue(attrs.get(i), p.getValue());
			  i++;
		  }
		  
		  // Extent of violation/improvement
		  for (QualityOfService q : qos) {
			  trainInst.setValue(attrs.get(i), q.getExtentOfViolation(true));			
		      i++;
		  }
		  
		  
		  double[] votes = classifier.getVotesForInstance(trainInst);
		  
		  double largest = 0;
		  int index = -1;
		  for (int j = 0; j < votes.length;j++) {
			  if(votes[j] > largest) {
				  index = j;
				  largest = votes[j];
			  }
		  }
		  
		  return index; // 0 = adapt, 1 = no adapt
	}
}
