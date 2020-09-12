package org.ssase.requirement.froas.fuzzy.linear;

import org.ssase.requirement.froas.fuzzy.FuzzyFunction;

public class LinearRP1Function implements FuzzyFunction{

	@Override
	public double fuzzilize(double original, double d) {
		if(original <= d) {
			return 1.0;
		}
		return original - d;
	}

}
