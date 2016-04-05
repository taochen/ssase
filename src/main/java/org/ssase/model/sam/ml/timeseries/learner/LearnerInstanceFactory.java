package org.ssase.model.sam.ml.timeseries.learner;

public class LearnerInstanceFactory {

	public static Learner getTranerInstance(){
		return new ApacheOLSLearner();
	}
}
