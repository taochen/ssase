package org.ssase.requirement.froas;

import jmetal.core.Solution;

public class SimpleRequirementProposition extends RequirementProposition {
	
	public SimpleRequirementProposition(double d, RequirementPrimitive... primitives) {
		super(d, primitives);
	}

	public SimpleRequirementProposition(RequirementPrimitive... primitives) {
		super(primitives);
	}
	
	public void setD(double d) {
		this.d = d;
	}
	
	public void fuzzilize(Solution s, int index) {
		
		if(s.getObjective(index) == Double.MAX_VALUE/100) {
			//s.setObjective(index, -1); //for p5 only
			return;
		}
		
		double v = function.fuzzilize(s.getObjective(index),d);
		//System.out.print("before norm: " + v + "\n");
		// 0.0 is the best
		// note that this is oppose to the paper, as in the paper, we need argmax, but the optimization is argmin - the paper has merely a different representation
		if (v != 0.0 && v != 1.0) {
			v = normalize(v); 
		}
		//System.out.print("d: " + d + ", max: " + max + ", min: " + min + ", original: " + s.getObjective(index) + ", fuzzy: " + v +"\n");
		s.setObjective(index, v);
		
	}
	
	public double normalize(double value) {	
		
		double max = this.max;
		double min = this.min;
		
		if (primitives.length == 1 && primitives[0] == RequirementPrimitive.AS_GOOD_AS_POSSIBLE_TO_d) {		
			//max = max > d? max : d;
			//min = min < d? d : min;
			min = d;
		} else if (primitives.length == 2 && primitives[0] == RequirementPrimitive.AS_GOOD_AS_POSSIBLE &&
				primitives[1] == RequirementPrimitive.BETTER_THAN_d){
			//max = max > d? d : max;
			//min = min < d? min : d;
			max = d;
		} else if (primitives.length == 2 && primitives[0] == RequirementPrimitive.AS_CLOSE_AS_POSSIBLE_TO_d){
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
	
	public double fuzzilize(double value) {
		if(value == Double.MAX_VALUE/100) {
			//s.setObjective(index, -1); //for p5 only
			return value;
		}
		
		double v = function.fuzzilize(value,d);
		if (v != 0.0 && v != 1.0) {
			v = normalize(v); 
		}
		
		
		
		//System.out.print("d="+ d + ", max=" + max + ", min=" + min + ", original=" + value + ", fuzzy=" + v +"\n");
		return v;
	}
	
	
	/**
	 * This is for tesing only
	 * @param value
	 * @return
	 */
	public double fuzzilize_1(double value) {
		if(value == Double.MAX_VALUE/100) {
			//s.setObjective(index, -1); //for p5 only
			return value;
		}
		
		/*double v = function.fuzzilize(value,d);
		if (v != 0.0 && v != 1.0) {
			v = normalize(v); 
		}*/
		
		
		double v = test_normalize(value);
	
		if(Double.isInfinite(v)) {
			return v;
		}
		
		v = ((v - min) / (max - min));
		
		//System.out.print("d="+ d + ", max=" + max + ", min=" + min + ", original=" + value + ", fuzzy=" + v +"\n");
		return v;
	}
	
    public double test_normalize(double value) {	
		
		double max = this.max;
		double min = this.min;
		
		if (primitives.length == 1 && primitives[0] == RequirementPrimitive.AS_GOOD_AS_POSSIBLE_TO_d) {		
			//max = max > d? max : d;
			//min = min < d? d : min;
			//min = d;
			if (value <= d) {
				return d;
			} else {
				return value;
			}
			
		} else if (primitives.length == 2 && primitives[0] == RequirementPrimitive.AS_GOOD_AS_POSSIBLE &&
				primitives[1] == RequirementPrimitive.BETTER_THAN_d){
			//max = max > d? d : max;
			//min = min < d? min : d;
			//max = d;
			if (value <= d) {
				return value;
			} else {
				return Double.NEGATIVE_INFINITY;
			}
		} else if (primitives.length == 1 && primitives[0] == RequirementPrimitive.AS_GOOD_AS_POSSIBLE) {	
			return value;
		} else {
			if (value <= d) {
				return d;
			} else {
				return Double.NEGATIVE_INFINITY;
			}
		}
		
		
	}
	

}
