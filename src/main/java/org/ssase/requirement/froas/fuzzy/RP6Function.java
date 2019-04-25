package org.ssase.requirement.froas.fuzzy;

public class RP6Function implements FuzzyFunction {

	
	private MathFunction func;
	
	public RP6Function(MathFunction func) {
		this.func = func;
	}
	public double fuzzilize(double original, double d) {
		if (original == 0) {
			return 1;
		} else if (original >= d && original != 0) {
			return 0;
		} else {
			if (func instanceof ErrorFunction) {
				double v = func.calculate((6 * original / d) - 3);
				return 0.5 * v;
			}
		}
		return original;
		
	}
}
