package org.ssase.requirement.fuzzy;

public class RP7Function implements FuzzyFunction {

	
	private MathFunction func;
	
	public RP7Function(MathFunction func) {
		this.func = func;
	}
	@Override
	public double fuzzilize(double original, double d) {
		
		if (original == 0) {
			return 1;
		} else if (original == 1) {
			return 0;
		} else if (original == d) {
			return 0.5;
		} else {
			if (func instanceof ErrorFunction) {
				
				if(original < d) {
					double v = func.calculate((6*original/d) - 3);			
					return 0.5 + (0.25 * v);
				} else {
					double v = func.calculate((6 * (original - d) / (1 - d)) - 3);
					
					return 0.25 * v;
				}
				
				
			}
		}
		return original;

	}

}
