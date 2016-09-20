package org.ssase.debt.classification;

import java.util.ArrayList;
import java.util.List;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.bayes.NaiveBayes;
import moa.classifiers.functions.MajorityClass;
import moa.classifiers.trees.AdaHoeffdingOptionTree;
import moa.classifiers.trees.DecisionStump;
import moa.classifiers.trees.HoeffdingAdaptiveTree;
import moa.classifiers.trees.HoeffdingTree;
import weka.core.Attribute;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

import org.ssase.objective.QualityOfService;
import org.ssase.primitive.*;

public class OnlineClassifier {

	
	private AbstractClassifier classifier;
	private ArrayList<Attribute> attrs = new ArrayList<Attribute>();
	private Instances dataRaw = null;
	private boolean isTrained = false;
	// Nominal classes
	//private List<String> att = new ArrayList<String>();
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
		
		  //att.add("0");
		  //att.add("1");
  	      //attrs.add(new Attribute("Expert",att, null));
		  attrs.add(new Attribute("Expert", i));
		  dataRaw = new Instances("data_instances", attrs ,0);
		  
		  //NumericToNominal convert= new NumericToNominal();
		  //Filter.useFilter(data, filter)
		  dataRaw.setClassIndex(attrs.size()-1);
		  System.out.print("Type "+attrs.get(attrs.size()-1).type() + "\n");
		  classifier = new HoeffdingTree();
		  //classifier.resetLearningImpl();
		  classifier.prepareForUse();
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
			  trainInst.setValue(attrs.get(i), p.getArray()[p.getArray().length-2]/100);
			  i++;
			  //System.out.print("train " + p.getName() + ":"+ p.getArray()[p.getArray().length-2] + "\n");
		  }
		  
		  // Extent of violation/improvement
		  for (QualityOfService q : qos) {
			  trainInst.setValue(attrs.get(i), q.getExtentOfViolation(false) / q.getConstraint());			
		      i++;
		      //System.out.print("train " + q.getExtentOfViolation(false) + "\n");
		  }
		   
		  trainInst.setValue(attrs.get(i), judge);	
		  
		  dataRaw.add(trainInst);
		  trainInst.setDataset(dataRaw);
		 // System.out.print(trainInst.weight() + "\n");
		  classifier.trainOnInstance(trainInst);
		  System.out.print(classifier.toString() + "\n");
		  isTrained = true;
	}
	
	public int predict(List<QualityOfService> qos, List<Primitive> primitives){
		
		if(!isTrained) return 0;
		
		 final Instance trainInst = new DenseInstance(primitives.size() + qos.size());
			
		  int i = 0;
		  for (Primitive p : primitives) {
			  trainInst.setValue(attrs.get(i), p.getValue() / p.getMax());
			  i++;
			  //System.out.print("predicted " +p.getName() + ":"+ p.getArray()[p.getArray().length-1] + "\n");
		  }
		  
		  // Extent of violation/improvement
		  for (QualityOfService q : qos) {
			  trainInst.setValue(attrs.get(i), q.getExtentOfViolation(true) / q.getConstraint());			
		      i++;
		      //System.out.print("predicted " + q.getExtentOfViolation(true) + "\n");
		  }
		 // trainInst.setValue(attrs.get(i), 100);
		  trainInst.setDataset(dataRaw);
		  double[] votes = classifier.getVotesForInstance(trainInst);
		  trainInst.setDataset(null);
		 // System.out.print("classify " + classifier.correctlyClassifies(trainInst)+ "\n");
		  double largest = 0;
		  int index = -1;
		  for (int j = 0; j < votes.length;j++) {
			  System.out.print("vote " + votes[j] + "\n");
			  if(votes[j] > largest) {
				  index = j;
				  largest = votes[j];
			  }
		  }
		  
		  return index; // 0 = adapt, 1 = no adapt
	}
}
