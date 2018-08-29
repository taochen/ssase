package org.ssase.requirement;

import jmetal.core.Solution;
import org.ssase.requirement.fuzzy.*;

/**
 * 
 * Requirement aware quality attribute.
 * 
 * @author tao
 * 
 */

public class RequirementProposition  {
	private RequirementPrimitive[] primitives;
	// Fitness function
	private FuzzyFunction function;

	private double d = Double.NEGATIVE_INFINITY;

	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;

	// The actual math function
	private Class clazz = ErrorFunction.class;



	public RequirementProposition(double d, RequirementPrimitive... primitives) {
		this.d = d;
		this.primitives = primitives;
		setup();
	}

	public RequirementProposition(RequirementPrimitive... primitives) {
		this.primitives = primitives;
		setup();
	}

	/**
	 * 
	 * @param set population before environmental selection
	 * @return
	 */
	public void fuzzilize(Solution s, int index) {
		
		double v = 0 - function.fuzzilize(normalize(s.getObjective(index)),
				normalize(d));
		s.setObjective(index, v);
		
	}
	
	

	public String toString() {

		String s = "";
		for (RequirementPrimitive rp : primitives) {
			s += rp + " ^ ";
		}

		return s;
	}

	public void updateNormalizationBounds(double v) {
		if (v > this.max) {
			this.max = v;
		}

		if (v < this.min) {
			this.min = v;
		}
		

	}

	private double normalize(double value) {
		return (value - min) / (max - min);
	}

	// Mapping proposition to the actual fuzzy function.
	private void setup() {
		
		if(!Double.isInfinite(d)) {
			if (d > this.max) {
				this.max = d;
			}

			if (d < this.min) {
				this.min = d;
			}
		}
		
		try {
			if (primitives.length == 1) {

				if (RequirementPrimitive.AS_GOOD_AS_POSSIBLE
						.equals(primitives[0])) {

					function = new RP1Function(
							(MathFunction) clazz.newInstance());

				} else if (RequirementPrimitive.BETTER_THAN_d
						.equals(primitives[0])) {

					function = new RP2Function(
							(MathFunction) clazz.newInstance());

				} else if (RequirementPrimitive.AS_GOOD_AS_POSSIBLE_TO_d
						.equals(primitives[0])) {

					function = new RP3Function(
							(MathFunction) clazz.newInstance());

				} else if (RequirementPrimitive.AS_CLOSE_AS_POSSIBLE_TO_d
						.equals(primitives[0])) {

					function = new RP4Function(
							(MathFunction) clazz.newInstance());

				} else if (RequirementPrimitive.AS_FAR_AS_POSSIBLE_FROM_d
						.equals(primitives[0])) {

					function = new RP5Function(
							(MathFunction) clazz.newInstance());

				}

			} else if (primitives.length == 2) {

				if (RequirementPrimitive.AS_GOOD_AS_POSSIBLE
						.equals(primitives[0])
						|| RequirementPrimitive.BETTER_THAN_d
								.equals(primitives[1])) {

					function = new RP6Function(
							(MathFunction) clazz.newInstance());

				} else if (RequirementPrimitive.AS_GOOD_AS_POSSIBLE
						.equals(primitives[0])
						|| RequirementPrimitive.AS_GOOD_AS_POSSIBLE_TO_d
								.equals(primitives[1])) {

					function = new RP7Function(
							(MathFunction) clazz.newInstance());

				}
			}

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
