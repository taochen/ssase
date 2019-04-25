package org.ssase.requirement.froas.fuzzy;

public class RP2Function implements FuzzyFunction {

	
	private MathFunction func;
	
	public RP2Function(MathFunction func) {
		this.func = func;
	}
	@Override
	public double fuzzilize(double original, double d) {
		if(original <= d) {
			return 1;
		} else  {
			return 0;
		} 
	}

}
