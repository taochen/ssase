package org.ssase.requirement.froas.fuzzy.linear;

import org.ssase.requirement.froas.fuzzy.FuzzyFunction;

public class OriginalFunction implements FuzzyFunction{

	@Override
	public double fuzzilize(double original, double d) {
		return original;
	}

}
