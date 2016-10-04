package org.ssase.model.onoff;

import weka.classifiers.meta.AdditiveRegression;

public class ExtendedOfflineBoost extends AdditiveRegression{
	
	public ExtendedOfflineBoost(weka.classifiers.AbstractClassifier learner, int number){
		 m_Classifier = learner;
		 this.m_NumIterations = number;
	}
}
