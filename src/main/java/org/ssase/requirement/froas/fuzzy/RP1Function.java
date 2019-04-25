package org.ssase.requirement.froas.fuzzy;


public class RP1Function implements FuzzyFunction{

	
	private MathFunction func;
	
	public RP1Function(MathFunction func) {
		this.func = func;
	}
	
	@Override
	public double fuzzilize(double original, double d) {
		if(original == 0) {
			return 1;
		} else if(original == 1) {
			return 0;
		} else {			
			if(func instanceof ErrorFunction) {
				double v = func.calculate(6*original - 3);			
				return 0.5 * v;
			}
		}
		
		return original;
		
	}
	
}
