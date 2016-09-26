package org.ssase.model.onoff;

import moa.classifiers.meta.WEKAClassifier;

public class CustomWEKAClassifier extends WEKAClassifier{

	   public void createWekaClassifier(String name) throws Exception {
	        this.classifier = (weka.classifiers.Classifier) Class.forName(name).newInstance();
	   }
}