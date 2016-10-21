package org.ssase.debt.classification;

import moa.classifiers.meta.WEKAClassifier;

public class CustomWEKAClassifier extends WEKAClassifier{

	   public void createWekaClassifier(String name) throws Exception {
	        this.classifier = (weka.classifiers.Classifier) Class.forName(name).newInstance();
	        if( this.classifier  instanceof weka.classifiers.lazy.IBk) {
	        	((weka.classifiers.lazy.IBk)this.classifier).setKNN(5);
	        }
	   }
}
