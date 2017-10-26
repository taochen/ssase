package org.ssase.debt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ssase.debt.classification.CustomWEKAClassifier;
import org.ssase.objective.QualityOfService;
import org.ssase.primitive.Primitive;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import moa.classifiers.AbstractClassifier;

public class TwoPhasePrediction {

	private AbstractClassifier classifier = initializeWEKAClassifier("weka.classifiers.lazy.IBk");
	private List<Double> list = new ArrayList<Double>();
	private ArrayList<Attribute> attrs = new ArrayList<Attribute>();
	private Instances dataRaw = null;
	private double[] pastInputs = new double[2];

	private boolean isFirstTrained = false;
	private QualityOfService qos;

	// Nominal classes
	private List<String> att = new ArrayList<String>();

	public TwoPhasePrediction(QualityOfService qos) {
		this.qos = qos;
		attrs.add(new Attribute("estimation", 0));
		attrs.add(new Attribute("weight", 1));
		att.add("0");
		att.add("1");
		attrs.add(new Attribute("Expert", att, null));
		// attrs.add(new Attribute("Expert", i));
		dataRaw = new Instances("data_instances", attrs, 0);

		// NumericToNominal convert= new NumericToNominal();
		// Filter.useFilter(data, filter)
		dataRaw.setClassIndex(attrs.size() - 1);
		// classifier.resetLearningImpl();

		classifier.prepareForUse();

	}

	public void preTraining() {
		
		if(pastInputs == null) {
			pastInputs = new double[2];
		}
		
		list.add(qos.getArray()[qos.getArray().length - 1] * qos.getMax() / 100);
		pastInputs[0] = getMedian();
		pastInputs[1] = list.get(list.size() - 1) / qos.getConstraint();

	}

	public void trainOnInstance() {
		
		if(pastInputs == null) {
			return;
		}
		
		isFirstTrained = true;
		final Instance trainInst = new DenseInstance(3);

		trainInst.setValue(attrs.get(0), pastInputs[0]);
		trainInst.setValue(attrs.get(1), pastInputs[1]);
		trainInst.setValue(attrs.get(2), qos.isViolate() ? "0" : "1");

		dataRaw.add(trainInst);
		trainInst.setDataset(dataRaw);

		classifier.trainOnInstance(trainInst);
	}

	public int predict() {

		
		if(!isFirstTrained) {
			return 0;
		}
		
		final Instance trainInst = new DenseInstance(3);

		trainInst.setValue(attrs.get(0), pastInputs[0]);
		trainInst.setValue(attrs.get(1), pastInputs[1]);
		trainInst.setValue(attrs.get(2), "0");

		dataRaw.add(trainInst);
		trainInst.setDataset(dataRaw);

		double[] votes = classifier.getVotesForInstance(trainInst);
		trainInst.setDataset(null);

		double largest = 0;
		int index = -1;

		if (votes.length == 1) {
			return (int) votes[0];
		}

		if (votes.length == 2 && votes[0] == 0 && votes[1] == 0) {
			System.out
					.print("Both vote result in 0, thus presume adaptaion needed \n");
			return 0;
		}

		for (int j = 0; j < votes.length; j++) {
			System.out.print("vote " + votes[j] + "\n");
			if (votes[j] > largest) {
				index = j;
				largest = votes[j];
			}
		}

		return index; // 0 = adapt, 1 = no adapt
	}

	public AbstractClassifier initializeWEKAClassifier(String name) {

		CustomWEKAClassifier weka = new CustomWEKAClassifier();
		try {
			weka.createWekaClassifier(name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return weka;
	}

	private double getMedian() {
		Collections.sort(list);
		if (list.size() % 2 == 0)
			return ((double) list.get(list.size() / 2) + (double) list.get(list
					.size() / 2 - 1)) / 2;
		else
			return (double) list.get(list.size() / 2);
	}
}
