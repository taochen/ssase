package org.ssase.model.iapm;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import moa.classifiers.AbstractClassifier;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.evaluation.WindowClassificationPerformanceEvaluator;
import moa.streams.ArffFileStream;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;

public class Main {

	/**
	 * @param args
	 * @throws Exception
	 */
	// data stream properties
	public static double[][] classPercentage;// class percentage of each class
												// (time decayed) at each time
												// step. 1st index - number of
												// total time steps; 2nd index -
												// number of classes in data
												// streams
	public static boolean imbalance;// whether the current data stream is
									// imbalanced
	public static int numClasses;// number of classes
	public static int[] numInstances;// number of instances of each class
	public static ArrayList<Integer> classIndexMinority = new ArrayList<Integer>();// class
																					// indice
																					// of
																					// current
																					// minority
																					// classes
	public static ArrayList<Integer> classIndexMajority = new ArrayList<Integer>();// class
																					// indice
																					// of
																					// current
																					// majority
																					// classes
	public static ArrayList<Integer> classIndexNormal = new ArrayList<Integer>();// class
																					// indice
																					// of
																					// other
																					// classes
	// performance at current time step
	public static double[] currentClassRecall_decay;// time decayed recall value
													// of each class at current
													// time step at current run
	public static double[][][] classRecall_prequential;// prequential recall
														// value of each class
														// at each time step at
														// each run
	public static double[][] gmean_prequential;// gmean of prequential recalls
												// at each time step at each run
	public static double[][][] classRecall_decay;// time decayed recall value of
													// each class at each time
													// step at each run
	public static double[][] gmean_decay;// gmean of time decayed recalls at
											// each time step at each run

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String path = "D:\\syw\\IJCAI16\\Data\\Artificial\\Static\\";
		String fname = "Gaussian_1Min1Maj";
		String fext = ".arff";
		String sampleMode = "OB";
		// the time of start tracking the prequential performance metrics: 50
		// for artificial static data; 250 for artificial dynamic data;
		// 27 for chess data; 877 for tweet-7class data
		int prequentialstart = 50;

		boolean resetMetric = false;
		int[] resetTimes = {};
		int numRun = 100;

		// ins: just for setting data properties and initialising performance
		// arrays
		DataSource source = new DataSource(path + fname + fext);
		Instances ins = source.getDataSet();
		if (ins.classIndex() == -1)
			ins.setClassIndex(ins.numAttributes() - 1);
		int numTimeStep = ins.numInstances();
		numClasses = ins.numClasses();

		classRecall_prequential = new double[numTimeStep][numClasses][numRun];
		gmean_prequential = new double[numTimeStep][numRun];
		classRecall_decay = new double[numTimeStep][numClasses][numRun];
		gmean_decay = new double[numTimeStep][numRun];

		for (int run = 0; run < numRun; run++) {
			System.out.println("Run " + (run + 1));
			String file = path + fname + fext;
			source = new DataSource(file);
			Instances train = source.getDataSet();
			if (train.classIndex() == -1)
				train.setClassIndex(train.numAttributes() - 1);
			Instance fistInst = train.instance(0);// get the first instance of
													// the data stream, for
													// initializing MLP

			// Obtain data stream
			ArffFileStream data = new ArffFileStream(file, -1);
			data.prepareForUse();

			classPercentage = new double[train.numInstances()][numClasses];
			numInstances = new int[numClasses];
			currentClassRecall_decay = new double[numClasses];

			// local variables
			int[] numInstancesCorrect = new int[numClasses]; // number of
																// instances of
																// each class
																// correctly
																// classified
			int[] numInstancesIncorrect = new int[numClasses]; // number of
																// instances of
																// each class
																// misclassified
																// into
			// int[] numInstancesCorrect_afterchange = new int[numClasses];
			// //number of instances of each class correctly classified after
			// change in class imbalance
			// int[] numInstances_afterchange = new int[numClasses];//number of
			// instances of each class after class imbalance changes

			double delta1 = 0.4;
			double delta2 = 0.3;
			double sizedecayfactor = 0.9;// theta
			double recalldecayfactor = 0.9;// theta'
			double numSamplesCorrect = 0;// number of correctly classified
											// samples from the beginning
			int numSamples_Total = 0; // number of processed samples from the
										// beginning
			boolean isCorrect = true;
			int predictedLabel, realLabel;

			// initialize online models
			OzaBag model = (OzaBag) initializeOnlineModel(run, data, fistInst);

			// choose an evaluator for performance assessment
			ClassificationPerformanceEvaluator evaluator = new WindowClassificationPerformanceEvaluator();

			// online training loop: test the current instance first, then used
			// to update the learner (prequential)
			while (data.hasMoreInstances()) {

				Instance trainInst = data.nextInstance();
				double[] prediction = model.getVotesForInstance(trainInst);
				evaluator.addResult(trainInst, prediction);
				predictedLabel = Utils.maxIndex(prediction);
				realLabel = (int) trainInst.classValue();
				if (predictedLabel == realLabel)
					isCorrect = true;
				else
					isCorrect = false;

				numSamples_Total++;
				if (numSamples_Total > prequentialstart) {
					numInstances[realLabel] = numInstances[realLabel] + 1;
					// numInstances_afterchange[realLabel]++;
					if (isCorrect) {
						numSamplesCorrect = numSamplesCorrect + 1;
						numInstancesCorrect[realLabel] = numInstancesCorrect[realLabel] + 1;
						// numInstancesCorrect_afterchange[realLabel]++;
					} else {
						numInstancesIncorrect[predictedLabel] = numInstancesIncorrect[predictedLabel] + 1;
					}
				}

				// update class percentages
				updateClassPercentage(realLabel, numSamples_Total,
						sizedecayfactor);
				// if(numSamples_Total > 0){
				// for(int t = 0; t < numClasses; t++)
				// System.out.print(classPercentage[numSamples_Total-1][t] +
				// ",");
				// System.out.println();
				// }

				// train online model
				if (sampleMode.equals("MOOB"))
					MOOB_adaptive(trainInst, model, numSamples_Total);
				else if (sampleMode.equals("MUOB"))
					MUOB_adaptive(trainInst, model, numSamples_Total);
				else
					model.trainOnInstance(trainInst);

				// update time decayed recall
				updateDecayRecall(realLabel, isCorrect, recalldecayfactor);
				double temp = 1;
				for (int i = 0; i < numClasses; i++) {
					classRecall_decay[numSamples_Total - 1][i][run] = currentClassRecall_decay[i];
					temp = temp * currentClassRecall_decay[i];
				}
				gmean_decay[numSamples_Total - 1][run] = Math.pow(temp,
						(double) 1 / numClasses);
				// System.out.println(classPercentage[numSamples_Total-1][0] +
				// "\t" + classPercentage[numSamples_Total-1][1] + "\t"
				// +currentClassRecall_decay[0] + "\t" +
				// currentClassRecall_decay[1] + "\t" + isCorrect);

				// class imbalance detection
				imbalanceStatus(delta1, delta2, numSamples_Total);

				// Output performance
				temp = 1;
				for (int i = 0; i < numClasses; i++) {
					// resetting at every 500 time step when the change happens
					// if(numInstances_afterchange[i]!=0)
					// classRecall_prequential[numSamples_Total-1][i][run] =
					// (double)numInstancesCorrect_afterchange[i]/numInstances_afterchange[i];
					// No resetting at 500 time step
					if (numInstances[i] != 0)
						classRecall_prequential[numSamples_Total - 1][i][run] = (double) numInstancesCorrect[i]
								/ numInstances[i];
					else
						classRecall_prequential[numSamples_Total - 1][i][run] = 0;
					temp *= classRecall_prequential[numSamples_Total - 1][i][run];
				}
				// System.out.println(classRecall_prequential[numSamples_Total-1][0][run]+", "+classRecall_prequential[numSamples_Total-1][1][run]);
				gmean_prequential[numSamples_Total - 1][run] = Math.pow(temp,
						(double) 1 / numClasses);

				/*
				 * //reset number of instances and number of correctly
				 * classified examples after every time the change happens
				 * if(resetMetric){ if(inArray(resetTimes, numSamples_Total)) {
				 * for(int i = 0; i < numClasses; i++){
				 * numInstances_afterchange[i] = 0;
				 * numInstancesCorrect_afterchange[i] = 0; } } }
				 */
			}// while
		}// for numRun
		printPerformance(prequentialstart);
	}

	/** Initialize Online Bagging */
	public static AbstractClassifier initializeOnlineModel(int seed,
			ArffFileStream data, Instance fistInst) {
		OzaBag model = new OzaBag();
		// model.baseLearnerOption.setValueViaCLIString("functions.Perceptron");//default
		// learning rate is 1
		model.baseLearnerOption
				.setValueViaCLIString("src.OnlineMultilayerPerceptron");// default
																		// learning
																		// rate
																		// is
																		// 0.3
		// model.baseLearnerOption.setValueViaCLIString("bayes.NaiveBayes");
		// model.baseLearnerOption.setValueViaCLIString("trees.HoeffdingTree");//default
		// of OzaBag

		model.ensembleSizeOption.setValue(11);
		model.randomSeedOption.setValue(seed);// model.randomSeedOption.setValue((int)System.currentTimeMillis());
		if (model.baseLearnerOption.getValueAsCLIString().equals(
				"src.OnlineMultilayerPerceptron")) {
			model.firtInst = fistInst;
		}
		model.setModelContext(data.getHeader());
		model.prepareForUse();
		return model;
	}

	// Multi-class Oversampling Online Bagging using adaptive sampling rates
	public static void MOOB_adaptive(Instance currentInstance, OzaBag model,
			int numSamplesTotal) {
		Integer classLabel = new Integer((int) currentInstance.classValue());
		double lambda = 1.0;
		int cp_max = Utils.maxIndex(classPercentage[numSamplesTotal - 1]);
		model.trainOnInstanceImpl(currentInstance, (double) lambda * cp_max
				/ classPercentage[numSamplesTotal - 1][classLabel]);
	}

	// Multi-class Oversampling Online Bagging using fixed sampling rates
	public static void MOOB_fix(Instance currentInstance, OzaBag model,
			int numSamplesTotal) {
		Integer classLabel = new Integer((int) currentInstance.classValue());
		double lambda = 1.0;
		double[] samplingRates = { 76 / 70, 76 / 76, 76 / 17, 76 / 13, 76 / 9,
				76 / 29 };
		model.trainOnInstanceImpl(currentInstance, (double) lambda
				* classPercentage[numSamplesTotal - 1][classLabel]
				* samplingRates[classLabel]);
	}

	// Multi-class Undersampling Online Bagging using adaptive sampling rates
	public static void MUOB_adaptive(Instance currentInstance, OzaBag model,
			int numSamplesTotal) {
		Integer classLabel = new Integer((int) currentInstance.classValue());// the
																				// class
																				// label
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

	// Multi-class Undersampling Online Bagging using fixed sampling rates
	public static void MUOB_fix(Instance currentInstance, OzaBag model,
			int numSamplesTotal) {
		Integer classLabel = new Integer((int) currentInstance.classValue());
		double lambda = 1.0;
		double[] samplingRates = { 9 / 70, 9 / 76, 9 / 17, 9 / 13, 9 / 9,
				9 / 29 };
		model.trainOnInstanceImpl(currentInstance, (double) lambda
				* classPercentage[numSamplesTotal - 1][classLabel]
				* samplingRates[classLabel]);
	}

	/** class imbalance detection method */
	public static void imbalanceStatus(double delta1, double delta2,
			int numSamplesTotal) {
		int[] classIndexAscend = Utils
				.sort(classPercentage[numSamplesTotal - 1]);
		classIndexMinority.clear();
		classIndexMajority.clear();
		classIndexNormal.clear();

		for (int m = 0; m < numClasses - 1; m++) {
			if (numInstances[classIndexAscend[m]] == 0)
				continue;// start from the non-zero size class
			for (int n = m + 1; n < numClasses; n++) {
				if ((classPercentage[numSamplesTotal - 1][classIndexAscend[n]]
						- classPercentage[numSamplesTotal - 1][classIndexAscend[m]] > delta1)
						&& (currentClassRecall_decay[classIndexAscend[n]]
								- currentClassRecall_decay[classIndexAscend[m]] > delta2)) {
					// classIndexAscend[m] is the minority and
					// classIndexAscend[n] is the majority
					if (!classIndexMinority.contains(new Integer(
							classIndexAscend[m]))) {
						classIndexMinority
								.add(new Integer(classIndexAscend[m]));
						// System.out.println("Class "+classIndexAscend[m]+" is added to the minority");
					}
					if (!classIndexMajority.contains(new Integer(
							classIndexAscend[n]))) {
						classIndexMajority
								.add(new Integer(classIndexAscend[n]));
						// System.out.println("Class "+classIndexAscend[n]+" is added to the majority");
					}
				}
			}
		}
		for (int k = 0; k < numClasses; k++) {
			if (numInstances[classIndexAscend[k]] == 0)
				continue;// start from the non-zero size class
			while (classIndexMinority
					.contains(new Integer(classIndexAscend[k]))
					&& classIndexMajority.contains(new Integer(
							classIndexAscend[k]))) {
				classIndexMajority.remove(new Integer(classIndexAscend[k]));
			}
			if ((!classIndexMinority.contains(new Integer(classIndexAscend[k])))
					&& (!classIndexMajority.contains(new Integer(
							classIndexAscend[k]))))
				classIndexNormal.add(new Integer(classIndexAscend[k]));
		}
		if (classIndexMinority.isEmpty() && classIndexMajority.isEmpty())
			imbalance = false;
		else
			imbalance = true;
	}

	/** update percentage of classes at each time step with time decay */
	public static void updateClassPercentage(int realLabel,
			int numSamplesTotal, double sizedecayfactor) {
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

	/** update time decayed recall of classes at each time step */
	public static void updateDecayRecall(int realLabel, boolean isCorrect,
			double recalldecayfactor) {
		if (isCorrect && numInstances[realLabel] == 1)
			currentClassRecall_decay[realLabel] = 1;
		else if (isCorrect)
			currentClassRecall_decay[realLabel] = currentClassRecall_decay[realLabel]
					* recalldecayfactor + (1 - recalldecayfactor);
		else if (!isCorrect)
			currentClassRecall_decay[realLabel] = currentClassRecall_decay[realLabel]
					* recalldecayfactor;
	}

	/**
	 * check whether the number is an element of the array return true if it is.
	 */
	public static boolean inArray(int[] array, int number) {
		for (int i = 0; i < array.length; i++) {
			if (number == array[i])
				return true;
		}
		return false;
	}

	public static void printPerformance(int prequentialstart) {

		System.out.print("Time Step \t ");
		for (int nob = 1; nob <= numClasses; nob++)
			System.out.print("Class" + nob + " Recall Mean \t Class" + nob
					+ " Recall Std \t");
		System.out.println("G-mean Mean \t G-mean Std");

		for (int noa = prequentialstart; noa < gmean_prequential.length; noa++) {
			System.out.print((noa + 1) + "\t");
			for (int nob = 0; nob < numClasses; nob++)
				System.out.print(Utils.mean(classRecall_prequential[noa][nob])
						+ "\t"
						+ Math.sqrt(Utils
								.variance(classRecall_prequential[noa][nob]))
						+ "\t");
			System.out.println(Utils.mean(gmean_prequential[noa]) + "\t"
					+ Math.sqrt(Utils.variance(gmean_prequential[noa])));
		}

		for (int nob = 1; nob <= numClasses; nob++)
			System.out.print("Final Class" + nob + " Recall \t");
		System.out.println("Final G-mean");
		for (int noa = 0; noa < gmean_prequential[0].length; noa++) {
			for (int nob = 0; nob < numClasses; nob++)
				System.out
						.print(classRecall_prequential[gmean_prequential.length - 1][nob][noa]
								+ "\t");
			System.out
					.println(gmean_prequential[gmean_prequential.length - 1][noa]);
		}
	}
}
