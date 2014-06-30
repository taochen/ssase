package org.ssascaling.model.timeseries.learner.apache;

import org.apache.commons.math3.linear.RealMatrix;

public class Covariance extends
		org.apache.commons.math3.stat.correlation.Covariance {

	 public RealMatrix computeCovarianceMatrix(double[][] data) {
		        return computeCovarianceMatrix(data, true);
	    }
}
