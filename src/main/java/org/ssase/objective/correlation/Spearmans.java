package org.ssase.objective.correlation;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class Spearmans implements Correlation {

	private final SpearmansCorrelation sc = new SpearmansCorrelation();
	
	@Override
	public double doCorrelation(double[] x, double[] y) {
	
		return sc.correlation(x, y);
	}

}
