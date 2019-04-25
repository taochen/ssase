package org.ssase.requirement.froas.fuzzy;


public interface FuzzyFunction {

	/**
	 * The inputs here are all normalized
	 * @param original
	 * @param d
	 * @return
	 */
	public double fuzzilize(double original, double d);
}
