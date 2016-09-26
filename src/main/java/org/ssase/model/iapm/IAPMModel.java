package org.ssase.model.iapm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.IterativeMultiKMeans;
import net.sf.javaml.clustering.evaluation.AICScore;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.distance.EuclideanDistance;

import org.ssase.model.Model;
import org.ssase.model.sam.ModelFunction;
import org.ssase.model.sam.ml.ann.NeuralNetworkStructureSelector;
import org.ssase.model.sam.ml.timeseries.ARMAStructureSelector;
import org.ssase.model.sam.ml.tree.RegressionTreeStructureSelector;
import org.ssase.model.selection.PrimitiveLearner;
import org.ssase.model.selection.StructureSelector;
import org.ssase.objective.QualityOfService;
import org.ssase.observation.event.ModelChangeEvent;
import org.ssase.observation.listener.ModelListener;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;

import moa.classifiers.AbstractClassifier;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.evaluation.WindowClassificationPerformanceEvaluator;
import moa.streams.ArffFileStream;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * 
 * Could have 3 changes:
 * 
 * 1. feature changes 2. class member changes 3. class number changes
 * 
 * @author tao
 * 
 */
public class IAPMModel implements Model {
	/**
	 * @param args
	 * @throws Exception
	 */
	// data stream properties
	private double[][] classPercentage;// class percentage of each class (time
										// decayed) at each time step. 1st index
										// - number of total time steps; 2nd
										// index - number of classes in data
										// streams
	private boolean imbalance;// whether the current data stream is imbalanced
	private int numClasses;// number of classes

	private int numSamples_Total = 0; // number of processed samples from the
										// beginning
	private OzaBag model = null;
	private ClassificationPerformanceEvaluator evaluator = null;
	private SampleCluster sampleCluster = null;

	private String name;

	protected Set<Primitive> possibleInputs;

	protected List<Primitive> inputs = new ArrayList<Primitive>();
	protected QualityOfService output;

	private double[] xMax;
	private double yMax;

	private PrimitiveLearner primitiveLearner = new PrimitiveLearner();
	private Set<ModelListener> listeners = new HashSet<ModelListener>();

	private Dataset ds = new DefaultDataset();
	private Instances dataRaw = null;

	private int run = 0;
	private String sampleMode = "";
	private ArrayList<Attribute> attrs = new ArrayList<Attribute>();

	public IAPMModel(String name, Set<Primitive> possibleInputs,
			QualityOfService output) {
		super();
		this.name = name;
		this.possibleInputs = possibleInputs;
		this.output = output;
	}

	public void selectPrimititvesAndTrainModels() {
		boolean isFeaturesChanged = false;
		//
		// output.expend();
		// for (Primitive p : possibleInputs) {
		// if (p instanceof ControlPrimitive) {
		// ((ControlPrimitive)p).expend();
		//
		// } else {
		// ((EnvironmentalPrimitive)p).expend();
		// }
		// }
		//

		// This set needs to be transfered into a sorted collection.
		Set<Primitive> newInputs = primitiveLearner.select(output,
				possibleInputs);

		if (newInputs.size() != inputs.size()) {
			inputs.clear();

			inputs.addAll(newInputs);
			isFeaturesChanged = true;
		} else {

			for (Primitive p : newInputs) {
				if (!inputs.contains(p)) {
					inputs.clear();
					inputs.addAll(newInputs);
					isFeaturesChanged = true;
					break;
				}
			}

		}
		System.out.print("Number of inputs: " + inputs.size() + "\n");

		// for (Primitive p : inputs) {
		// System.out.print(p.getName() + "\n");
		// }

		if (inputs.size() == 0) {
			return;
		}

		yMax = output.getMax();
		xMax = new double[inputs.size()];
		for (int i = 0; i < inputs.size(); i++) {
			xMax[i] = inputs.get(i).getMax();
		}

		// Reset everything
		if (isFeaturesChanged) {
			// TODO might be use the recorded historical data together?
			model = null;
			run = 0;
			numSamples_Total = 0;

			// Copy the new instances

			attrs = new ArrayList<Attribute>();

			int i = 0;
			for (Primitive p : inputs) {
				attrs.add(new Attribute(p.getName(), i));
				i++;
			}

			attrs.add(new Attribute(output.getName(), i));

			System.out.print(attrs.get(attrs.size() - 1).type()
					+ "***********type!!!\n");
			dataRaw = new Instances("data_instances", attrs, 0);
			dataRaw.setClassIndex(dataRaw.numAttributes() - 1);
			// Notify the region control when primitives selection result
			// change.
			// At this level, the optimization algorithm would be also notified
			// to
			// abort the optimization.
			for (ModelListener listener : listeners) {
				listener.updateWhenModelChange(new ModelChangeEvent(true,
						output));
			}

		}

		if (dataRaw == null) {

			int i = 0;

			for (Primitive p : inputs) {
				attrs.add(new Attribute(p.getName(), i));
				i++;
			}

			attrs.add(new Attribute(output.getName(), i));

			dataRaw = new Instances("data_instances", attrs, 0);
		}

		runClustering();
		try {
			run(convertIntoWekaInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void runClustering() {
		ds.add(new net.sf.javaml.core.DenseInstance(new double[] { output
				.getValue() / output.getMax() }));
		Dataset[] clusters = null;
		if (ds.size() == 1) {
			Dataset newD = new DefaultDataset();
			newD.add(new net.sf.javaml.core.DenseInstance(new double[] { output
					.getValue() / output.getMax() }));
			clusters = new Dataset[] { newD };
		} else {

			Clusterer kmean = new IterativeMultiKMeans(1, 2, 1, 1,
					new EuclideanDistance(), new AICScore());
			System.out.print("Start clustering\n");
			clusters = kmean.cluster(ds);
			System.out.print("Finsih clustering\n");

		}
		Arrays.sort(clusters, new Comparator<Dataset>() {

			@Override
			public int compare(Dataset arg0, Dataset arg1) {
				return arg0.size() > arg1.size() ? -1 : 1;
			}

		});

		double[] newClassCount = new double[clusters.length];
		// Value to class mapping
		Map<Double, Integer> map = new HashMap<Double, Integer>();
		int classLabel = 0;
		for (Dataset d : clusters) {
			System.out.println("Cluster *****" + d.size() + "\n");
			for (net.sf.javaml.core.Instance i : d) {
				map.put(i.get(0), classLabel);
				// System.out.println(i.get(0)+"\n");
			}

			newClassCount[classLabel] = d.size();

			classLabel++;
		}

		int instancesNo = sampleCluster == null ? 1 : sampleCluster
				.getInstancesNo() + 1;
		int numClasses = sampleCluster == null ? ds.size() : sampleCluster
				.getNewClassCount().length;

		sampleCluster = new SampleCluster(numClasses, map, instancesNo,
				newClassCount);

	}

	private Instance convertIntoWekaInstance() {

		final Instance trainInst = new DenseInstance(inputs.size() + 1);

		for (int i = 0; i < inputs.size(); i++) {
			trainInst.setValue(attrs.get(i), inputs.get(i).getValue()
					/ inputs.get(i).getMax());
		}

		trainInst.setValue(attrs.get(attrs.size() - 1), output.getValue()
				/ output.getMax());

		dataRaw.add(trainInst);
		trainInst.setDataset(dataRaw);
		dataRaw.setClassIndex(trainInst.numAttributes() - 1);

		return trainInst;
	}

	private void run(Instance trainInst/* use only one instance per time step */)
			throws Exception {

		int instancesNo = sampleCluster.getInstancesNo();
		int numClasses = sampleCluster.getNumClasses();
		double[] newClassCount = sampleCluster.getNewClassCount();
		if (numSamples_Total == 6) {
			System.out.print("");
		}
		sampleMode = "MOOB";
		this.numClasses = numClasses;// ins.numClasses();//To Tao: this is the
										// number of classes in data. But your
										// case is a pre-defined category
										// separating regression values. You can
										// set it to a specific value depending
										// on how many categories you have. I
										// didn't change this.

		double sizedecayfactor = 0.9;// theta
		
		int predictedLabel, realLabel;

		// initialize online models
		if (model == null) {

			// numClasses is sorted in decending order.
			classPercentage = new double[instancesNo][numClasses];
			// numInstances = new int[numClasses];
			// currentClassRecall_decay = new double[numClasses];

			model = (OzaBag) initializeOnlineModel(run, /* data, */trainInst);
			// choose an evaluator for performance assessment
			// evaluator = new WindowClassificationPerformanceEvaluator();
		} else {
			// Assume only one new sample per time step.
			// We assume that although the member of each class (and the number
			// of class) can change,
			// every time we just use the most up to date clustering result.
			// (i.e., updated arrays)

			classPercentage = updateClassPresentage(instancesNo, numClasses,
					newClassCount, classPercentage);
		}

	
		// TO TAO: you need to change the output type of your MLP here.
		// Now, the MLP output is in "double[] prediction".
		// The size of double[] is the number of classes. E.g. prediction[0] for
		// class1 is 0.2, prediction[1] for class2 is 0.8.
		// Then the final predicted class will be class2 with the larger value.
		// Now, I still assume MLP output is double[], with size 1 -- i.e. the
		// regression value.
		// Instance trainInst = data.nextInstance();
		double[] prediction = model.getVotesForInstance(trainInst);// I assume
																	// prediction[0]
																	// is the
																	// regression
																	// value.
		// evaluator.addResult(trainInst, prediction);
		// predictedLabel = value2class(prediction[0]);
		realLabel = value2class(trainInst.classValue());// To Tao: in your case,
														// the current training
														// instance's class
														// value should be a
														// regression value.

		numSamples_Total++;
		// To Tao: this block of code is to track classification performance. If
		// you don't need them, you can delete or change depending on how you
		// evaluate your case.
	

		// update class percentages. This is the class imbalance detection,
		// outputting the percentage of each class/category. It is used in MOOB
		// and MUOB algorithms.
		updateClassPercentage(realLabel, numSamples_Total, sizedecayfactor);

		// train online model
		if (sampleMode.equals("MOOB"))
			MOOB_adaptive(trainInst, model, numSamples_Total);
		else if (sampleMode.equals("MUOB"))
			MUOB_adaptive(trainInst, model, numSamples_Total);
		else
			model.trainOnInstanceImpl(dataRaw);// model.trainOnInstance(trainInst);


		run++;
	}

	/** Initialize Online Bagging */
	public AbstractClassifier initializeOnlineModel(int seed, /*
															 * ArffFileStream
															 * data,
															 */Instance fistInst) {
		OzaBag model = new OzaBag();
		// model.baseLearnerOption.setValueViaCLIString("functions.Perceptron");//default
		// learning rate is 1
		model.baseLearnerOption
				.setValueViaCLIString("org.ssase.model.iapm.OnlineMultilayerPerceptron");// default
																							// learning
																							// rate
																							// is
																							// 0.3
		// model.baseLearnerOption.setValueViaCLIString("bayes.NaiveBayes");
		// model.baseLearnerOption.setValueViaCLIString("trees.HoeffdingTree");//default
		// of OzaBag

		model.ensembleSizeOption.setValue(1);
		model.randomSeedOption.setValue(seed);// model.randomSeedOption.setValue((int)System.currentTimeMillis());
		if (model.baseLearnerOption.getValueAsCLIString().equals(
				"org.ssase.model.iapm.OnlineMultilayerPerceptron")) {
			model.firtInst = fistInst;
		}
		// System.out.print(fistInst.a + "\n");
		// model.setModelContext(data.getHeader());
		model.prepareForUse();
		// model.resetLearningImpl();
		return model;
	}

	// Multi-class Oversampling Online Bagging using adaptive sampling rates
	private void MOOB_adaptive(Instance currentInstance, OzaBag model,
			int numSamplesTotal) {
		Integer classLabel = new Integer(
				value2class(currentInstance.classValue()));
		double lambda = 1.0;
		int cp_max = Utils.maxIndex(classPercentage[numSamplesTotal - 1]);
		model.trainOnInstanceImpl(currentInstance, (double) lambda * cp_max
				/ classPercentage[numSamplesTotal - 1][classLabel]);
	}

	// Multi-class Undersampling Online Bagging using adaptive sampling rates
	private void MUOB_adaptive(Instance currentInstance, OzaBag model,
			int numSamplesTotal) {
		Integer classLabel = new Integer(
				value2class(currentInstance.classValue()));// the class label
															// index
		double lambda = 1.0;
		int cp_min = Utils.minIndex(classPercentage[numSamplesTotal - 1]);
		double rate = (double) classPercentage[numSamplesTotal - 1][cp_min]
				/ classPercentage[numSamplesTotal - 1][classLabel];
		if (rate < 0.01)
			model.trainOnInstanceImpl(currentInstance, (double) lambda * 0.01);
		else
			model.trainOnInstanceImpl(currentInstance, (double) lambda * rate);
	}

	

	/** update percentage of classes at each time step with time decay */
	private void updateClassPercentage(int realLabel, int numSamplesTotal,
			double sizedecayfactor) {
		System.out.print("numSamplesTotal: " + numSamplesTotal + "\n");
		System.out.print("numClasses: " + numClasses + "\n");
		System.out.print("classPercentage: " + classPercentage.length + "\n");
		System.out
				.print("classPercentage: " + classPercentage[0].length + "\n");
		if (numSamplesTotal > 1) {
			for (int t = 0; t < numClasses; t++) {
				if (t == realLabel)
					classPercentage[numSamplesTotal - 1][t] = classPercentage[numSamplesTotal - 2][t]
							* sizedecayfactor + (1 - sizedecayfactor);
				else
					classPercentage[numSamplesTotal - 1][t] = classPercentage[numSamplesTotal - 2][t]
							* sizedecayfactor;
			}
		} else {
			classPercentage[numSamplesTotal - 1][realLabel] = 1;
		}
	}



	/**
	 * check whether the number is an element of the array return true if it is.
	 */
	private boolean inArray(int[] array, int number) {
		for (int i = 0; i < array.length; i++) {
			if (number == array[i])
				return true;
		}
		return false;
	}

	

	private int value2class(double val) {
		// int classIndex = 0;
		return sampleCluster.convertValueToClassIndex(val);
	}

	/**
	 * Update the current time step only.
	 * 
	 * @param instancesNo
	 * @param numClasses
	 * @param classPercentage
	 * @return
	 */
	private double[][] updateClassPresentage(int instancesNo,
			int numNewClasses, double[][] classPercentage) {

		double[][] copyOfClassPercentage = new double[instancesNo][1];
		System.arraycopy(classPercentage, 0, copyOfClassPercentage, 0,
				classPercentage.length);
		copyOfClassPercentage[copyOfClassPercentage.length - 1][0] = numNewClasses;

		return copyOfClassPercentage;
	}

	private double[][] updateClassPresentage(int instancesNo, int numClasses,
			double[] newClassCount, double[][] classPercentage) {

		double[][] copyOfClassPercentage = null;
		if (numClasses > classPercentage[0].length) {
			copyOfClassPercentage = new double[instancesNo][numClasses];
			for (int i = 0; i < classPercentage.length; i++) {
				copyOfClassPercentage[i] = new double[numClasses];
				for (int j = 0; j < numClasses; j++) {
					if (j < classPercentage[i].length) {
						copyOfClassPercentage[i][j] = classPercentage[i][j];
					} else {
						copyOfClassPercentage[i][j] = 0;
					}
				}
			}
			copyOfClassPercentage[copyOfClassPercentage.length - 1] = newClassCount;
		} else if (numClasses < classPercentage[0].length) {
			copyOfClassPercentage = new double[instancesNo][classPercentage[0].length];
			System.arraycopy(classPercentage, 0, copyOfClassPercentage, 0,
					classPercentage.length);

			double[] copy_newClassCount = new double[classPercentage[0].length];
			System.arraycopy(newClassCount, 0, copy_newClassCount, 0,
					newClassCount.length);

			for (int i = copy_newClassCount.length - newClassCount.length; i < copy_newClassCount.length; i++) {
				copy_newClassCount[i] = 0;
			}
			copyOfClassPercentage[copyOfClassPercentage.length - 1] = copy_newClassCount;
		} else {
			copyOfClassPercentage = new double[instancesNo][numClasses];
			System.arraycopy(classPercentage, 0, copyOfClassPercentage, 0,
					classPercentage.length);
			copyOfClassPercentage[copyOfClassPercentage.length - 1] = newClassCount;
		}

		return copyOfClassPercentage;
	}

	@Override
	public List<Primitive> getInputs() {
		// TODO Auto-generated method stub
		return inputs;
	}

	@Override
	public QualityOfService getOutput() {
		// TODO Auto-generated method stub
		return output;
	}

	@Override
	public void updateNewlyError(double[] xValue, double yValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateNewlyErrorWithReturn(double[] result) {
		// TODO Auto-generated method stub

	}

	@Override
	public double[] updateNewlyErrorWithReturn(double[] xValue, double yValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double predict(double[] xValue, boolean isSU, double a, double b) {
		return predict(xValue, -1);
	}

	@Override
	public double predict(double[] xValue, int index) {
		final Instance trainInst = new DenseInstance(xValue.length);

		for (int i = 0; i < xValue.length; i++) {
			trainInst.setValue(attrs.get(i), xValue[i]);
		}

		double[] p = sampleMode == "MOOB" ? model
				.getVotesForInstance(trainInst) : model.predict(trainInst);

		System.out.print("size*********" + p.length + "\n");
		for (double d : p) {
			System.out.print("vote*********" + d + "\n");
		}

		return Math.abs(p[0] * 100);
	}

	@Override
	public double getYMax() {
		return yMax;
	}

	@Override
	public double getXMax(int i) {
		return xMax[i];
	}

	@Override
	public void addListener(ModelListener listener) {
		for (ModelListener inlistener : listeners) {
			if (listener.getClass().isInstance(inlistener)) {
				listeners.remove(inlistener);
			}
		}

		listeners.add(listener);

	}

	public Primitive get(int i) {
		return inputs.get(i);
	}

	public int getSize() {
		return inputs.size();
	}

	public int countFunction() {
		return 1;
	}
}
