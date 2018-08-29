package org.ssase.requirement.fuzzy;

import org.apache.commons.math3.special.Erf;

public class ErrorFunction implements MathFunction{

	public double calculate(double x) {
		return Erf.erfc(x);
	}
}
