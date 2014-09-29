package org.ssascaling.model.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ssascaling.model.selection.mi.MutualInformation;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.qos.QualityOfService;
import org.ssascaling.util.Repository;

public class PrimitiveLearner {

	public static final double threshold = 0.5;

	private Map<Primitive, Double> newInputesMap = new HashMap<Primitive, Double>();
	

	public Set<Primitive> select(QualityOfService output,  Set<Primitive> primitives){
		
		long time = System.currentTimeMillis();
		final Map<Integer, List<DependencyPair>> inputMap = new HashMap<Integer,  List<DependencyPair>>();
		final Set<Primitive> inputs = new HashSet<Primitive>();
		newInputesMap.clear();
		double value = 0.0;
		//System.out.print("*******count: " + Repository.countDirectForAnObjective(output) + "\n");
		for (Primitive p : primitives) {
			
			 if ((value = MutualInformation.calculateSymmetricUncertainty(output.getArray(), p.getArray())) > 0) {
				 
				 if (p.isDirect(output)) {
					// System.out.print("-------D: " + value + " : " + p.getAlias() + " - " + p.getName() + "\n");
					 inputs.add(p);
				 } else {
					 //System.out.print("-------inD: " + value + " : " + p.getAlias() + " - " + p.getName()  + "\n");
					 if (!inputMap.containsKey(p.getGroup())) {
						 inputMap.put(p.getGroup(), new ArrayList<DependencyPair>());
					 }
					 inputMap.get(p.getGroup()).add(new DependencyPair(p,value)); 
				 }
				 // This may include unselected primitives. However, it is fine to have them.
				 newInputesMap.put(p, value);
			 }
		}
		
		System.out.print("Number of direrct primitives: " + inputs.size() + "\n");
		
		
		if (inputMap.size() > 1) {
			throw new RuntimeException("We currently only allow one group for non-direct primitives!");
		}
	
		for (Integer groupID : inputMap.keySet()) {
			final Set<Primitive> nonPrimary = randomOptimizeSelection(inputMap
					.get(groupID));
			for (Primitive p : nonPrimary) {
				inputs.add(p);
			}

		}
		
		
		System.out.print("Number of total selected primitives: " + inputs.size() + "\n");
		
		System.out.print("Time for primitives selection:" + (System.currentTimeMillis() - time) + "ms\n");
		return inputs;
	}
	
	public double getValue (Primitive primitive) {
		return newInputesMap.get(primitive);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Set<Primitive> thresholdSelection (List inputList) {
        Collections.sort(inputList);
		
		Set<Primitive> newInputes = new HashSet<Primitive>();
		
		for (int i = inputList.size() - 1; i > inputList.size() * (1-threshold); i--) {
			newInputes.add(((DependencyPair)inputList.get(i)).getPrimitive());
		}
		
		return newInputes;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Set<Primitive> randomOptimizeSelection (List inputList) {
		//Collections.sort(inputList);
		Set<Primitive> newInputes = new HashSet<Primitive>();
		Set<Primitive> finalInputs = new HashSet<Primitive>();
		double finalp = 0.0;
		for (int k = 0; k < 50000/*the length of random optimization can be changed*/; k++) {
			Collections.shuffle(inputList);
			newInputes.clear();
			double total = 0.0;
			double add = 0.0;
			double minus = 1.0;
			for (int i = inputList.size() - 1; i > -1; i--) {
				

				// System.out.print("Add " +
				// ((DependencyPair)inputList.get(i)).value + " Minus " +
				// ((DependencyPair)inputList.get(i)).getRedundancyValue(newInputes)+
				// "\n");
				double a = ((DependencyPair) inputList.get(i)).value;
				double b = ((DependencyPair) inputList.get(i))
						.getRedundancyValue(newInputes);
				int no = newInputes.size() + 1;
				// System.out.print( (add+a)/no -
				// (minus+b)/(no*no)+" Selected \n");
				//System.out.print("Old " + total + ", New" + (minus + b == 0 ? (add + a) / no
						//: (((add + a) / no) / ((minus + b) / (no * no)))) + "\n");
				
				
				/*if (total < (minus + b == 0 ? (add + a) / no
						: (((add + a) / no) / ((minus + b) / ((no * no - no)/2))))) {

					newInputes.add(((DependencyPair) inputList.get(i))
							.getPrimitive());
					add += a;
					minus += b;
					total = (minus == 0 ? add / no : (add / no)
							/ (minus / ((no * no - no)/2)));
				}*/
				
				if (total < ((add + a) / (minus + b))) {

					newInputes.add(((DependencyPair) inputList.get(i))
							.getPrimitive());
					add += a;
					minus += b;
					total = (add) / (minus);
				}
				
				
			}
			if (finalp < total) {
				///System.out.print("Best so far: " + finalp + ", with number of " + finalInputs.size() + ", Fine " + total + " with number of "
				//+ newInputes.size() + "\n");
				//System.out.print(add + "\n");
				finalp = total;
				finalInputs.clear();			
				finalInputs.addAll(newInputes);
			}
			//System.out.print("Best so far: " + finalp + ", with number of " + finalInputs.size() + ", Fine " + total + " with number of "
					//+ newInputes.size() + "\n");
		}
		System.out.print("Total " + finalp + "\n");
		

		
		for (Primitive p : finalInputs) {
			System.out.print("=========================\n");
			System.out.print("Selected: " + p.getAlias() + " : " + p.getName() + "\n");
			System.out.print("=========================\n");
		}
		
		return finalInputs;
	}
	
	
	private void Shake (Set<DependencyPair> solution) {
		
	}
	
	private class DependencyPair implements Comparable<DependencyPair>{
		private Primitive p;
		private double value;
		
		Map<Primitive, Double> redundancy = new HashMap<Primitive, Double>();
		
		
		public DependencyPair(Primitive p, double value) {
			super();
			this.p = p;
			this.value = value;
		}
		
		
		
		public double getRedundancyValue(Set<Primitive> primitives) {
			double total = 0.0;
			for (Primitive primitive : primitives) {
				if (!redundancy.containsKey(primitive)){
					redundancy.put(primitive, 
					      MutualInformation.calculateSymmetricUncertainty(primitive.getArray(), p.getArray()));
				}
				
				total += redundancy.get(primitive);
			}
			
			return total;
		}
		
		public Primitive getPrimitive() {
			return p;
		}
		
		@Override
		public int compareTo(DependencyPair o) {			
			return this.value < o.value? -1 : 1;
		}
		
		
	}
}
