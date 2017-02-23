package org.ssase.debt.classification;

import java.util.ArrayList;
import java.util.List;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.bayes.NaiveBayes;
import moa.classifiers.bayes.NaiveBayesMultinomial;
import moa.classifiers.functions.MajorityClass;
import moa.classifiers.functions.SGD;
import moa.classifiers.meta.OzaBag;
import moa.classifiers.meta.WEKAClassifier;
import moa.classifiers.trees.AdaHoeffdingOptionTree;
import moa.classifiers.trees.DecisionStump;
import moa.classifiers.trees.HoeffdingAdaptiveTree;
import moa.classifiers.trees.HoeffdingTree;
import moa.options.ClassOption;
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
	private boolean isFirst = true;
	private boolean useWEKA = false;
	
	private boolean isNormalize = false;
	// Nominal classes
	private List<String> att = new ArrayList<String>();
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
		
		  att.add("0");
		  att.add("1");
  	      attrs.add(new Attribute("Expert",att, null));
		  //attrs.add(new Attribute("Expert", i));
		  dataRaw = new Instances("data_instances", attrs ,0);
		  
		  //NumericToNominal convert= new NumericToNominal();
		  //Filter.useFilter(data, filter)
		  dataRaw.setClassIndex(attrs.size()-1);
		  //System.out.print("Type "+attrs.get(attrs.size()-1).type() + "\n");
		  classifier = initializeClassifier();
		  //classifier.resetLearningImpl();
		  if(!useWEKA)  {
			  classifier.prepareForUse();
		  }
		  
	}
	
	
	public AbstractClassifier initializeClassifier(){
		//return new HoeffdingTree();
		//return new NaiveBayes(); 
		//return new MajorityClass(); // this would probably the same as decision stump
		//return new DecisionStump();
		//return new SGD();
		//return initializeWEKAClassifier("weka.classifiers.lazy.IBk");//knn
		return initializeWEKAClassifier("mlp");
		
		
		//return initializeBagging("trees.HoeffdingTree","bayes.NaiveBayes");
		//return initializeBoosting(
			//	"org.ssase.debt.classification.OnlineMultilayerPerceptron");
	}
	
	public AbstractClassifier initializeBagging(String... clazz){
		ExtendedOzaBag bag = new ExtendedOzaBag();
		bag.ensembleSizeOption.setValue(clazz.length);
		
		for (int i = 0; i < clazz.length; i++) {
			if (bag.learners == null) {
				bag.learners = new ClassOption[clazz.length];
			}
			
			bag.learners[i] = new ClassOption("baseLearner", 'l',
		            "Classifier to train.", Classifier.class, clazz[i]); 
		}
		
		return bag;
	}
	
	public AbstractClassifier initializeBoosting(String... clazz) {
		ExtendedOzaBoost boot = new ExtendedOzaBoost();
		boot.ensembleSizeOption.setValue(clazz.length);
		
		for (int i = 0; i < clazz.length; i++) {
			if (boot.learners == null) {
				boot.learners = new ClassOption[clazz.length];
			}
			
			boot.learners[i] = new ClassOption("baseLearner", 'l',
		            "Classifier to train.", Classifier.class, clazz[i]); 
		}
		
		return boot;
	}
	
	
	public AbstractClassifier initializeWEKAClassifier(String name){
		useWEKA = true;
		
		if("mlp".equals(name)) {
			useWEKA = false;
			org.ssase.debt.classification.OnlineMultilayerPerceptron mlp = 
				new org.ssase.debt.classification.OnlineMultilayerPerceptron();
			mlp.setTrainingTime(5000);
			return mlp;
		}
		
		CustomWEKAClassifier weka = new CustomWEKAClassifier();
		try {
			weka.createWekaClassifier(name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return weka;
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
			  trainInst.setValue(attrs.get(i), isNormalize? p.getArray()[p.getArray().length-2]/100 : p.getArray()[p.getArray().length-2]*p.getMax()/100 );
			  i++;
			  //System.out.print("train " + p.getName() + ":"+ p.getArray()[p.getArray().length-2] + "\n");
		  }
		  
		  // Extent of violation/improvement
		  for (QualityOfService q : qos) {
			  trainInst.setValue(attrs.get(i), isNormalize? q.getExtentOfViolation(false) / q.getConstraint() : q.getExtentOfViolation(false) );			
		      i++;
		      //System.out.print("train " + q.getExtentOfViolation(false) + "\n");
		  }
		   
		  trainInst.setValue(attrs.get(i), String.valueOf(judge));	
		  
		  dataRaw.add(trainInst);
		  trainInst.setDataset(dataRaw);
		 // System.out.print(trainInst.weight() + "\n");
		  
		  if(!isTrained && classifier instanceof org.ssase.debt.classification.OnlineMultilayerPerceptron) {
			  ((org.ssase.debt.classification.OnlineMultilayerPerceptron)classifier).initMLP(trainInst);
		  }
		  
		  classifier.trainOnInstance(trainInst);
		  //System.out.print(classifier.toString() + "\n");
		  isTrained = true;
	}
	
	public void trainOnInstance(int judge, double[] qos, double[] primitives){
		  final Instance trainInst = new DenseInstance(primitives.length + qos.length+ 1);
		
		  int i = 0;
		  for (double d : primitives) {
			  trainInst.setValue(attrs.get(i), d);
			  i++;
			  //System.out.print("train " + p.getName() + ":"+ p.getArray()[p.getArray().length-2] + "\n");
		  }
		  
		  // Extent of violation/improvement
		  for (double d : qos) {
			  trainInst.setValue(attrs.get(i), d);			
		      i++;
		      //System.out.print("train " + q.getExtentOfViolation(false) + "\n");
		  }
		   
		  trainInst.setValue(attrs.get(i), String.valueOf(judge));	
		  
		  dataRaw.add(trainInst);
		  trainInst.setDataset(dataRaw);
		 // System.out.print(trainInst.weight() + "\n");
		  
		  if(!isTrained && classifier instanceof org.ssase.debt.classification.OnlineMultilayerPerceptron) {
			  ((org.ssase.debt.classification.OnlineMultilayerPerceptron)classifier).initMLP(trainInst);
		  }
		  
		  classifier.trainOnInstance(trainInst);
		  System.out.print(classifier.toString() + "\n");
		  isTrained = true;
	}
	
	
	public int predict(List<QualityOfService> qos, List<Primitive> primitives){
		
		if(isFirst) {
			isFirst = false;
			return 0;
		}
		
		if(!isTrained) return 0;
		
		 final Instance trainInst = new DenseInstance(primitives.size() + qos.size() + 1);
			
		  int i = 0;
		  for (Primitive p : primitives) {
			  trainInst.setValue(attrs.get(i), isNormalize? p.getValue() / p.getMax() :  p.getValue());
			  i++;
			  //System.out.print("predicted " +p.getName() + ":"+ p.getArray()[p.getArray().length-1] + "\n");
		  }
		  
		  // Extent of violation/improvement
		  for (QualityOfService q : qos) {
			  trainInst.setValue(attrs.get(i), isNormalize? q.getExtentOfViolation(true) / q.getConstraint() : q.getExtentOfViolation(true));			
		      i++;
		      //System.out.print("predicted " + q.getExtentOfViolation(true) + "\n");
		  }
		  // This should not do anything, just some learner need it for setting range.
		  trainInst.setValue(attrs.get(i), "0");
		  trainInst.setDataset(dataRaw);
		  double[] votes = classifier.getVotesForInstance(trainInst);
		  trainInst.setDataset(null);
		 // System.out.print("classify " + classifier.correctlyClassifies(trainInst)+ "\n");
		  double largest = 0;
		  int index = -1;
		  
		  if(votes.length == 1) {
			  return (int)votes[0];
		  }
		  
		  if(votes.length == 2 && votes[0] == 0 && votes[1] == 0) {
			  System.out.print("Both vote result in 0, thus presume adaptaion needed \n");
			  return 0;
		  }
		  
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
