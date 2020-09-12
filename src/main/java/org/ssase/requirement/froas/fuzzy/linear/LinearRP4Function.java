package org.ssase.requirement.froas.fuzzy.linear;

import org.ssase.requirement.froas.fuzzy.FuzzyFunction;

public class LinearRP4Function implements FuzzyFunction{

	@Override
	public double fuzzilize(double original, double d) {
		if(original <= d) {
			return d - original;
		}
		return 0.0;
	}

}