package org.ssase.requirement.froas;

import jmetal.core.Solution;

public class SimpleRequirementProposition extends RequirementProposition {
	
	public SimpleRequirementProposition(double d, RequirementPrimitive... primitives) {
		super(d, primitives);
	}

	public SimpleRequirementProposition(RequirementPrimitive... primitives) {
		super(primitives);
	}
	
	public void fuzzilize(Solution s, int index) {
		
		if(s.getObjective(index) == Double.MAX_VALUE/100) {
			//s.setObjective(index, -1); //for p5 only
			return;
		}
		
		double v = function.fuzzilize(s.getObjective(index),d);
		if (v != 0.0 || v != 1.0) {
			v = normalize(v); 
		}
		//System.out.print("max: " + max + " = min: " + min + "= normalized: " + normalize(s.getObjective(index)) + " = original: " + s.getObjective(index) + " = fuzzie: " + v +"\n");
		s.setObjective(index, v);
		
	}
	
	protected double normalize(double value) {	
		
		double max = this.max;
		double min = this.min;
		
		if (primitives.length == 1 && primitives[0] == RequirementPrimitive.AS_GOOD_AS_POSSIBLE_TO_d) {		
			max = max > d? max : d;
			min = min < d? d : min;
		} else if (primitives.length == 2 && primitives[0] == RequirementPrimitive.AS_GOOD_AS_POSSIBLE &&
				primitives[1] == RequirementPrimitive.BETTER_THAN_d){
			max = max > d? d : max;
			min = min < d? min : d;
		} else if (primitives.length == 1 && primitives[0] == RequirementPrimitive.AS_CLOSE_AS_POSSIBLE_TO_d){
			double d1 = Math.abs(max - d);
			double d2 = Math.abs(min - d);
			
			max = d1 > d2? d1 : d2;
			min = d1 > d2? d2 : d1;
		}
		
		// means all values are the same
		if(max == min) {
			return value / max;
		}
		
		return ((value - min) / (max - min));
	}

}
