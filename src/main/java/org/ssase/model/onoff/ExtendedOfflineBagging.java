package org.ssase.model.onoff;

import weka.classifiers.meta.Bagging;

public class ExtendedOfflineBagging extends Bagging{

	public ExtendedOfflineBagging(weka.classifiers.AbstractClassifier learner, int number){
		 m_Classifier = learner;
		 this.m_NumIterations = number;
	}
}
