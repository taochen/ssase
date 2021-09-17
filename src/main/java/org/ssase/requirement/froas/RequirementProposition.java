package org.ssase.requirement.froas;

import jmetal.core.Solution;

import org.ssase.requirement.froas.fuzzy.*;
import org.ssase.requirement.froas.fuzzy.linear.LinearRP1Function;
import org.ssase.requirement.froas.fuzzy.linear.LinearRP2Function;
import org.ssase.requirement.froas.fuzzy.linear.LinearRP3Function;
import org.ssase.requirement.froas.fuzzy.linear.OriginalFunction;

/**
 * 
 * Requirement aware quality attribute.
 * 
 * @author tao
 * 
 */

public class RequirementProposition {
	protected RequirementPrimitive[] primitives;
	// Fitness function
	protected FuzzyFunction function;

	protected double d = Double.NEGATIVE_INFINITY;

	protected double min = Double.MAX_VALUE;
	protected double max = Double.NEGATIVE_INFINITY;//Double.MIN_VALUE;

	// The actual math function
	protected Class clazz = ErrorFunction.class;

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

		if (s.getObjective(index) == Double.MAX_VALUE / 100) {
			// s.setObjective(index, -1); //for p5 only
			return;
		}

		double v = 0.0 - function.fuzzilize(normalize(s.getObjective(index)), normalize(d));
		// System.out.print("max: " + max + " = min: " + min + "= normalized: " + normalize(s.getObjective(index)) + " = original: " + s.getObjective(index) + " = fuzzie: " + v +"\n");
		s.setObjective(index, v);

	}
	
	public void fuzzilize(Solution s, int index, double increment) {
		
	}

	/**
	 * This is for tesing only
	 * 
	 * @param value
	 * @return
	 */
	public double fuzzilize(double value) {
		// System.out.print(normalize(value) + ":" + normalize(d) + "\n");
		// System.out.print(max + ":" + min + "\n");
		return function.fuzzilize(normalize(value), normalize(d));
	}

	public String toString() {

		String s = "";
		for (RequirementPrimitive rp : primitives) {
			s += rp + " ^ ";
		}

		return s;
	}

	public void updateNormalizationBounds(double v) {

		if (v == Double.MAX_VALUE / 100) {
			return;
		}

		if (v > this.max) {
			this.max = v;
		}

		if (v < this.min) {
			this.min = v;
		}

	}
	
	public void resetNormalizationBounds() {
		min = Double.MAX_VALUE;
		max = Double.NEGATIVE_INFINITY;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public double normalize(double value) {
		//System.out.print("max " + max + ", min " + min + "\n");
		// means all values are the same
		if (max == min) {
			//return value / max;
			return value > 0? value / max : -1.0 * (value / max);
		}
		
		/*if(value < 0) {
			max = Double.MIN_VALUE;
		}
		
		if(value > 0) {
			min = Double.MIN_VALUE;
		}*/

		//System.out.print("max " + max + ", min " + min + "\n");
		/*if(value > max || value < min) {
			System.out.print("max " + max + ", min " + min + ", with value " + value + "\n");
		}*/
		
		return ((value - min) / (max - min));
		//return value > 0 ? value / (value + 1) : value / (value - 1);
	}

	// Mapping proposition to the actual fuzzy function.
	private void setup() {

		if (!Double.isInfinite(d)) {
			if (d > this.max) {
				this.max = d;
			}

			if (d < this.min) {
				this.min = d;
			}
		}

		try {

			if (this instanceof SimpleRequirementProposition) {

				if (primitives.length == 1) {
					if (RequirementPrimitive.AS_GOOD_AS_POSSIBLE.equals(primitives[0])) {

						function = new OriginalFunction();

					} else if (RequirementPrimitive.BETTER_THAN_d.equals(primitives[0])) {

						function = new LinearRP2Function();

					} else if (RequirementPrimitive.AS_GOOD_AS_POSSIBLE_TO_d.equals(primitives[0])) {

						function = new LinearRP1Function();

					}
				} else if (primitives.length == 2) {
					if (RequirementPrimitive.AS_GOOD_AS_POSSIBLE.equals(primitives[0])
							&& RequirementPrimitive.BETTER_THAN_d.equals(primitives[1])) {

						function = new LinearRP3Function();

					}
				}
			} else {

				if (primitives.length == 1) {

					if (RequirementPrimitive.AS_GOOD_AS_POSSIBLE.equals(primitives[0])) {

						function = new RP1Function((MathFunction) clazz.newInstance());

					} else if (RequirementPrimitive.BETTER_THAN_d.equals(primitives[0])) {

						function = new RP2Function((MathFunction) clazz.newInstance());

					} else if (RequirementPrimitive.AS_GOOD_AS_POSSIBLE_TO_d.equals(primitives[0])) {

						function = new RP3Function((MathFunction) clazz.newInstance());

					} else if (RequirementPrimitive.AS_CLOSE_AS_POSSIBLE_TO_d.equals(primitives[0])) {

						function = new RP4Function((MathFunction) clazz.newInstance());

					} else if (RequirementPrimitive.AS_FAR_AS_POSSIBLE_FROM_d.equals(primitives[0])) {

						function = new RP5Function((MathFunction) clazz.newInstance());

					}

				} else if (primitives.length == 2) {

					if (RequirementPrimitive.AS_GOOD_AS_POSSIBLE.equals(primitives[0])
							&& RequirementPrimitive.BETTER_THAN_d.equals(primitives[1])) {

						function = new RP6Function((MathFunction) clazz.newInstance());

					} else if (RequirementPrimitive.AS_GOOD_AS_POSSIBLE.equals(primitives[0])
							&& RequirementPrimitive.AS_GOOD_AS_POSSIBLE_TO_d.equals(primitives[1])) {

						function = new RP7Function((MathFunction) clazz.newInstance());

					}
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
