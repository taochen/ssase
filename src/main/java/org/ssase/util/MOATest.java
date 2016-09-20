package org.ssase.util;

import weka.core.Instance;
import moa.classifiers.Classifier;
import moa.classifiers.trees.HoeffdingTree;
import moa.streams.generators.RandomRBFGenerator;

public class MOATest {

	/**
	 * @paramargs
	 */
	public static void main(String[] args) {
		int numInstances = 10000;

		Classifier learner = new HoeffdingTree();
		RandomRBFGenerator stream = new RandomRBFGenerator();
		stream.prepareForUse();

		learner.setModelContext(stream.getHeader());
		learner.prepareForUse();

		
		
		int numberSamplesCorrect = 0;
		int numberSamples = 0;
		boolean isTesting = true;
		while (stream.hasMoreInstances() && numberSamples < numInstances) {
			Instance trainInst = stream.nextInstance();
			System.out.print("trainInst.numAttributes() " + trainInst.dataset().attribute(10).type() + "\n");
			if (isTesting) {
				double[] vote = learner.getVotesForInstance(trainInst);
				
				for (double d : vote) {
					System.out.print("vote " + d + "\n");
				}
				
				if (learner.correctlyClassifies(trainInst)) {
					numberSamplesCorrect++;
				}
			}
			numberSamples++;
			learner.trainOnInstance(trainInst);
			System.out.print(learner.toString() + "\n");
		}
		double accuracy = 100.0 * (double) numberSamplesCorrect
				/ (double) numberSamples;
		System.out.println(numberSamples + " instances processed with " + accuracy
				+ "% accuracy");

	}

}
