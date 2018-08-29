package org.ssase.requirement.fuzzy;

public class RP3Function implements FuzzyFunction {

	private MathFunction func;

	public RP3Function(MathFunction func) {
		this.func = func;
	}

	@Override
	public double fuzzilize(double original, double d) {
		if (original <= d && original != 1) {
			return 1;
		} else if (original == 1) {
			return 0;
		} else {
			if (func instanceof ErrorFunction) {
				double v = func.calculate((6 * (original - d) / (1 - d)) - 3);
				return 0.5 * v;
			}
		}
		return original;

	}

}
