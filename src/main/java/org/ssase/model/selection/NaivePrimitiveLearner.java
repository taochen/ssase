package org.ssase.model.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ssase.model.selection.mi.MutualInformation;
import org.ssase.primitive.Primitive;
import org.ssase.qos.QualityOfService;

public class NaivePrimitiveLearner extends PrimitiveLearner{
	private Map<Primitive, Double> newInputesMap = new HashMap<Primitive, Double>();
	private Map<Primitive, Double> total = new HashMap<Primitive, Double>();
	private Map<String, Double> total_all = new HashMap<String, Double>();
	private Map<String, Double> total_direct = new HashMap<String, Double>();
	private Map<String, Double> total_indirect = new HashMap<String, Double>();
	
	private Map<String, List<Double>> direct_indirect = new HashMap<String, List<Double>>();
	private Map<String, List<Double>> intra_direct = new HashMap<String, List<Double>>();
	private Map<String, List<Double>> intra_indirect = new HashMap<String, List<Double>>();
	final LinkedHashMap<Primitive, Map<Primitive, Double[]>> ra = new LinkedHashMap<Primitive, Map<Primitive, Double[]>>();
	private Map<String, List<Double>> all = new HashMap<String, List<Double>>();
	QualityOfService output = null;
	private int counter = 0;
public Set<Primitive> select(QualityOfService output,  Set<Primitive> primitives){
	this.output = output;
	    counter++;
		long time = System.currentTimeMillis();
		//final Map<Integer, List<DependencyPair>> inputMap = new HashMap<Integer,  List<DependencyPair>>();
		final Set<Primitive> inputs = new HashSet<Primitive>();
		final LinkedHashMap<String, Primitive> Dinputs = new LinkedHashMap<String, Primitive>();
		final LinkedHashMap<String, List<Primitive>> inDinputs = new LinkedHashMap<String, List<Primitive>>();
		
		
		
		List<Primitive> direct = new ArrayList<Primitive>(); 
		List<Primitive> indirect = new ArrayList<Primitive>(); 
		List<Primitive> all_p = new ArrayList<Primitive>(); 
		//newInputesMap.clear();
		double value = 0.0;
		//System.out.print("*******count: " + Repository.countDirectForAnObjective(output) + "\n");
		for (Primitive p : primitives) {
			
			 if ((value = MutualInformation.calculateSymmetricUncertainty(output.getArray(), p.getArray())) > 0) {
				 
				 if (p.isDirect(output)) {
					// System.out.print("\"D," + value + ":" + p.getAlias() + " - " + p.getName() + "\",\n");
					
					// inputs.add(p);
					 Dinputs.put(p.getName(), p);
					 //direct.add(p);
				 } else {
					// System.out.print("\"inD," + value + ":" + p.getAlias() + " - " + p.getName()  + "\",\n");
				
					 if (!inDinputs.containsKey(p.getName() )){
						 inDinputs.put(p.getName(), new ArrayList<Primitive>());
					 }
					//******************//
					 inputs.add(p);
					//******************//
					 inDinputs.get(p.getName()).add(p);
					 //indirect.add(p);
				 }
				 
				 newInputesMap.put(p, value);
				 ra.put(p, new HashMap<Primitive, Double[]>());
				// if(total.containsKey(p)) {
				//	 value += total.get(p);
				/// }
				// System.out.print("\"inD," + value + ":" + p.getAlias() + " - " + p.getName()  + "\",\n");
				 //total.put(p, value);
				// all_p.add(p);
				 // This may include unselected primitives. However, it is fine to have them.
				
			 }
		}
		
		
		
		
		for (Primitive p : ra.keySet()) {
			
			
			for (Primitive subp : primitives) {
				
				if (!newInputesMap.containsKey(subp) || p.equals(subp)) {
					continue;
				}
			
				double SU_jc = newInputesMap.get(subp);
				double SU_ic = newInputesMap.get(p);
				
				double SU_ij = MutualInformation.calculateSymmetricUncertainty(subp.getArray(), p.getArray());
				
				double result = (SU_jc >= SU_ic && SU_ij >= SU_ic)? 1 : 0;
				
				ra.get(p).put(subp, new Double[]{SU_ij});
				
			}
		}
		
		
		
		//inputs.remove(Dinputs.get("Concurrency"));
		//inputs.remove(Dinputs.get("Workload"));
		//inputs.remove(Dinputs.get("CPU"));
		
		/*for (Primitive p : inDinputs.get("Concurrency")) {
			if (newInputesMap.get(p) < 0.2) {
			  inputs.remove(p);
			}
		}
		
		for (Primitive p : inDinputs.get("Workload")) {
			if (newInputesMap.get(p) < 0.25) {
			inputs.remove(p);
			}
		}
		
		for (Primitive p : inDinputs.get("Memory")) {
			
			inputs.remove(p);
			
		}*/
		
		/*for (Primitive p : inDinputs.get("Concurrency")) {
		inputs.remove(p);
		// Concurrency, Workload, CPU
		}
		
		for (Primitive p : inDinputs.get("Workload")) {
			inputs.remove(p);
		}
		
		for (Primitive p : inDinputs.get("CPU")) {
			inputs.remove(p);
		}
		
		
		System.out.print("Number of total selected primitives: " + inputs.size() + "\n");
		
		for (Primitive p : inputs) {
			System.out.print("=========================\n");
			System.out.print("Final Selected: " + p.getAlias() + " : " + p.getName() + "\n");
			System.out.print("=========================\n");
		}
		
		System.out.print("Time for primitives selection:" + (System.currentTimeMillis() - time) + "ms\n");*/
		
		return inputs;
	}

	private void doRelevanceAndRedundancyAnalysis(QualityOfService output, final LinkedHashMap<String, Primitive> Dinputs,
	final LinkedHashMap<String, List<Primitive>> inDinputs,
	List<Primitive> direct, 
	List<Primitive> indirect, 
	List<Primitive> all_p){
		double v = 0;		
		double f = 0;
	    int c = 0;
	    double tmp = 0;
	    double rtmp = 0;
	    double finalr = 0;
	    double finalre = 0;
		for (int i = 0; i < direct.size(); i++) {
			
			for (int j = 0; j < direct.size(); j++) {
				
				if (direct.get(i).equals(direct.get(j))) {
					continue;
				}
				
				c++;
			
				v = MutualInformation.calculateSymmetricUncertainty(direct.get(i).getArray(), direct.get(j).getArray());
			 
				if (v > f) {
					f = v;
				}
				tmp += v;
				
			}
		
			
			rtmp +=  MutualInformation.calculateSymmetricUncertainty(direct.get(i).getArray(), output.getArray());
			
		}
		
		finalr += rtmp;
		finalre += tmp;
		//v = v/c; 
		v = f;
		System.out.print("Intra direct: f=" + v + ", c=" + c + ", tmp=" + tmp + ", rtmp=" + rtmp +  "\n");
		if(!all.containsKey("direct")){
			all.put("direct", new ArrayList<Double>());
		}
		all.get("direct").add(tmp);
		
		v = 0;
		c = 0;
		f = 0;
		tmp = 0;
		rtmp = 0;
		double intra_feature = 0;
		int in_c = 0;
		for (int i = 0; i < indirect.size(); i++) {
			for (int j = 0; j < indirect.size(); j++) {
				
				if (indirect.get(i).equals(indirect.get(j))) {
					continue;
				}
				
				
				
				c++;
				v = MutualInformation.calculateSymmetricUncertainty(indirect.get(i).getArray(), indirect.get(j).getArray());
	
				
	            if (indirect.get(i).getName().equals(indirect.get(j).getName() )) {
	            	intra_feature += v;
	            	 in_c++;
				}
				
				if (v > f) {
					f = v;
				}
				tmp += v;
			}
			
			rtmp +=  MutualInformation.calculateSymmetricUncertainty(indirect.get(i).getArray(), output.getArray());
		}
		finalr += rtmp;
		finalre += tmp;
		  //v = v/c;
			v = f;
			System.out.print("Intra indirect: f=" + v + ", c=" + c + ", tmp=" + tmp + ", rtmp=" + rtmp +  "\n");
			System.out.print("Intra indirect: c=" + in_c + " tmp=" + intra_feature + ", rtmp=" + rtmp +  "\n");
			if(!all.containsKey("indirect")){
				all.put("indirect", new ArrayList<Double>());
			}
			if(!all.containsKey("intra_indirect")){
				all.put("intra_indirect", new ArrayList<Double>());
			}
			all.get("intra_indirect").add(intra_feature);
			all.get("indirect").add(tmp);
			
			v = 0;
			c = 0;
		f = 0;
		tmp = 0;
		
		for (Primitive p : direct) {
			for (Primitive subp : indirect) {
				c++;
				
				
				v = MutualInformation.calculateSymmetricUncertainty(p.getArray(), subp.getArray());
	
				if (v > f) {
					f = v;
				}
				tmp += v;
				
			}
			
		}
		
	
		
	   // v = v/c;
		v = f;
		tmp = tmp * 2;
		tmp += finalre;
		
		System.out.print("All: f=" + v + ", c=" + c + ", tmp=" + tmp + ", rtmp=" + finalr +   "\n");
		if(!all.containsKey("direct_indirect")){
			all.put("direct_indirect", new ArrayList<Double>());
		}
		all.get("direct_indirect").add(tmp);
		
		v = 0;
		c = 0;
		f = 0;
		tmp = 0;
		rtmp=0;
		
		for (int i = 0; i < all_p.size(); i++) {
				for (int j = 0; j < all_p.size(); j++) {
					
					if (all_p.get(i).equals(all_p.get(j))) {
						continue;
					}
					
					//if (!all_p.get(i).getName().equals(all_p.get(j).getName())) {
						//continue;
					//}
					
					c++;
					v += MutualInformation.calculateSymmetricUncertainty(all_p.get(i).getArray(), all_p.get(j).getArray());
				   
				}
			//v = v/c;
			
			if(total_all.containsKey((all_p.get(i).isDirect(output)?"D, " : "inD, ") + ":" + all_p.get(i).getAlias() + " - " + all_p.get(i).getName())){
				v += total_all.get((all_p.get(i).isDirect(output)?"D, " : "inD, ") + ":" + all_p.get(i).getAlias() + " - " + all_p.get(i).getName());
			}
			
			total_all.put(((all_p.get(i).isDirect(output)?"D, " : "inD, ") + ":" + all_p.get(i).getAlias() + " - " + all_p.get(i).getName()), v);
			
			System.out.print(((all_p.get(i).isDirect(output)?"D, " : "inD, ") + ":" + all_p.get(i).getAlias() + " - " + all_p.get(i).getName()) + " " +  total_all.get((all_p.get(i).isDirect(output)?"D, " : "inD, ") + ":" + all_p.get(i).getAlias() + " - " + all_p.get(i).getName()) + "\n");
			
			c=0;
			v=0;
		}
		
		
		v = 0;
		c = 0;
	
	/*	for (int i = 0; i < direct.size(); i++) {
			for (int j = 0; j < direct.size(); j++) {
				
				if (direct.get(i).equals(direct.get(j))) {
					continue;
				}
				
				c++;
				v += MutualInformation.calculateSymmetricUncertainty(direct.get(i).getArray(), direct.get(j).getArray());
			   
			}
		v = v/c;
		
		if(total_direct.containsKey((direct.get(i).isDirect(output)?"D, " : "inD, ") + ":" + direct.get(i).getAlias() + " - " + direct.get(i).getName())){
			v += total_direct.get((direct.get(i).isDirect(output)?"D, " : "inD, ") + ":" + direct.get(i).getAlias() + " - " + direct.get(i).getName());
		}
		
		total_direct.put(((direct.get(i).isDirect(output)?"D, " : "inD, ") + ":" + direct.get(i).getAlias() + " - " + direct.get(i).getName()), v);
		
		System.out.print(((direct.get(i).isDirect(output)?"D, " : "inD, ") + ":" + direct.get(i).getAlias() + " - " + direct.get(i).getName()) + " " +  total_direct.get((direct.get(i).isDirect(output)?"D, " : "inD, ") + ":" + direct.get(i).getAlias() + " - " + direct.get(i).getName()) + "\n");
		
		c=0;
		v=0;
	}
		*/
		
		v = 0;
		c = 0;
	
	/*	for (int i = 0; i < indirect.size(); i++) {
			for (int j = 0; j < indirect.size(); j++) {
				
				if (indirect.get(i).equals(indirect.get(j))) {
					continue;
				}
				
				c++;
				v += MutualInformation.calculateSymmetricUncertainty(indirect.get(i).getArray(), indirect.get(j).getArray());
			   
			}
		v = v/c;
		
		if(total_indirect.containsKey((indirect.get(i).isDirect(output)?"D, " : "inD, ") + ":" + indirect.get(i).getAlias() + " - " + indirect.get(i).getName())){
			v += total_indirect.get((indirect.get(i).isDirect(output)?"D, " : "inD, ") + ":" + indirect.get(i).getAlias() + " - " + indirect.get(i).getName());
		}
		
		total_indirect.put(((indirect.get(i).isDirect(output)?"D, " : "inD, ") + ":" + indirect.get(i).getAlias() + " - " + indirect.get(i).getName()), v);
		
		System.out.print(((indirect.get(i).isDirect(output)?"D, " : "inD, ") + ":" + indirect.get(i).getAlias() + " - " + indirect.get(i).getName()) + " " +  total_indirect.get((indirect.get(i).isDirect(output)?"D, " : "inD, ") + ":" + indirect.get(i).getAlias() + " - " + indirect.get(i).getName()) + "\n");
		
		c=0;
		v=0;
	}*/
		
		for (Map.Entry<String, List<Primitive>> e : inDinputs.entrySet()) {
		   
			for (Map.Entry<String, List<Primitive>> sube : inDinputs.entrySet()) {
		   // for (Map.Entry<String, List<Primitive>> ep : inDinputs.entrySet()) {
		    	if (e.getKey().equals(sube.getKey())) {
		    		continue;
		    	}
				
		    	for (Primitive p : e.getValue()){
		    		for (Primitive subp : sube.getValue()){
				      c++;
				      v += MutualInformation.calculateSymmetricUncertainty(p.getArray(), subp.getArray());
		    		}
			    }
		    }
			
			v = v/c;
			if(total_indirect.containsKey(e.getKey())){
				v += total_indirect.get(e.getKey());
			}
			
			total_indirect.put(e.getKey(), v);
			v=0;
			c=0;
		}
	}
	
	public double getValue (Primitive primitive) {
		return newInputesMap.get(primitive);
	}
	
	public void print(){
		double relevance = 0;
		double redundancy = 0;
		for (Primitive p : ra.keySet()) {
			
			if (p.isDirect(output) || !p.getName().equals("Concurrency")) {
				continue;
			}
			
			Map<Primitive, Double[]> map = ra.get(p);
			System.out.print(newInputesMap.get(p) + " : " + p.getAlias() + " - " + p.getName() + "\n");
			relevance += newInputesMap.get(p) ;
			for (Primitive subp : map.keySet()) {
				
				if (!subp.isDirect(output) && !subp.getName().equals("Memory")
						&& !subp.getName().equals("CPU")&& !subp.getName().equals("Workload")
						&& !subp.getName().equals("Concurrency")) {
					continue;
				}
			
				//System.out.print("---------" + (map.get(subp)==null?" do not have MB " : " have MB ") +  subp.getAlias() + " - " + subp.getName() + "\n");
				System.out.print("--------- " + map.get(subp)[0]  + " " +   subp.getAlias() + " - " + subp.getName() + "\n");
				//value += (newInputesMap.get(p) + newInputesMap.get(subp))/(1+map.get(subp)[0]);
				redundancy += map.get(subp)[0];
			}
			 
		}
		System.out.print("************* " + relevance  + "\n");
		System.out.print("************* " + redundancy  + "\n");
	}
	
	public void print1(){
		for (Map.Entry<Primitive, Double> e : total.entrySet()) {
		 System.out.print("\"inD," + (e.getValue()/counter) + ":" + e.getKey().getAlias() + " - " + e.getKey().getName()  + "\",\n");
		}
		
		double intraD = 0;
		double intrainD = 0;
		double in_intrainD = 0;
		double direct_and_indirect = 0;
		System.out.print("Direct=======\n");
		for (Double d : all.get("direct")) {
			System.out.print((d/2)+"\n");
			intraD += d;
		}
	
		System.out.print("Indirect=======\n");
		for (Double d : all.get("indirect")) {
			System.out.print((d/2)+"\n");
			intrainD += d;
		}
		
		System.out.print("Intra indirect=======\n");
		for (Double d : all.get("intra_indirect")) {
			System.out.print((d/2)+"\n");
			in_intrainD += d;
		}
		
		System.out.print("All=======\n");
		for (Double d : all.get("direct_indirect")) {
			System.out.print((d/2)+"\n");
			direct_and_indirect += d;
		}
		
		System.out.print("Intra direct: " + (intraD/counter) +  "\n");
		System.out.print("All: " + (direct_and_indirect/counter) +  "\n");
		System.out.print("Intra indirect: " + (intrainD/counter) +  "\n");
		System.out.print("Intra indirect within the same feature: " + (in_intrainD/counter) +  "\n");
		
		for (Map.Entry<String, Double> e : total_all.entrySet()) {
			System.out.print((e.getValue()/counter) + e.getKey() + "\n");
		}
		/*System.out.print("Within each space, crros dimensions\n");
		
		for (Map.Entry<String, Double> e : total_direct.entrySet()) {
			System.out.print((e.getValue()/counter) + e.getKey() + "\n");
		}
		for (Map.Entry<String, Double> e : total_indirect.entrySet()) {
			System.out.print((e.getValue()/counter) + e.getKey() + "\n");
		}*/
		/*for (Map.Entry<String, Double> e : direct_indirect.entrySet()) {
			 System.out.print( (e.getValue()/counter)  + ":" + e.getKey() +  "\",\n");
			}*/
		
		/**
		 * Redundancy
		 * Intra direct: 4.498833392371297
	All: 559.5169816699243
	Intra indirect: 450.47417527802344
	Intra indirect within the same feature: 248.95378602950757
		 */
	}

}
