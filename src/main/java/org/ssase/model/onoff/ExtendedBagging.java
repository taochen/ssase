package org.ssase.model.onoff;

import weka.classifiers.meta.Bagging;

public class ExtendedBagging extends Bagging{

	public ExtendedBagging(){
		 m_Classifier = new weka.classifiers.trees.M5P();
	}
}
