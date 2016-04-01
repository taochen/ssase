package org.ssase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Interval {

	// those include all measure/resource quality for all services
	private Map<String, List<ValuePair>> xFacts;
	private Map<String, List<ValuePair>> yFacts;
	// The key here is VM ID
	private List<ValuePair> vmXFacts;
	private long timestamp;
	
	public Interval (long timestamp) {
		xFacts = new HashMap<String, List<ValuePair>>();
		yFacts = new HashMap<String, List<ValuePair>>();
		vmXFacts = new ArrayList<ValuePair>();
		this.timestamp = timestamp;
	}
	
	public void setX (String service, String[] names, double[] value) {
		if (!xFacts.containsKey(service)) {
			xFacts.put(service, new ArrayList<ValuePair>());
		}

		
		List<ValuePair> list = xFacts.get(service);
		for (int i = 0; i < value.length; i++) {
			list.add(new ValuePair(names[i], value[i]));
		}
		
	}
	
	public void setY (String service, String[] names, double[] value) {
		if (!yFacts.containsKey(service)) {
			yFacts.put(service, new ArrayList<ValuePair>());
		}
		
		List<ValuePair> list = yFacts.get(service);
		for (int i = 0; i < value.length; i++) {
			list.add(new ValuePair(names[i], value[i]));
		}
	}
	
	
	public void setVMX (String[] names, double[] value) {
		for (int i = 0; i < value.length; i++) {
			vmXFacts.add(new ValuePair(names[i], value[i]));
		}
		
	}
	
	public void print (){
		Set<Map.Entry<String, List<ValuePair>>> set = xFacts.entrySet();
		for (Map.Entry<String, List<ValuePair>> entry : set) {
			System.out.print("Input for " + entry.getKey() + "\n");
			for (ValuePair vp : entry.getValue()) {
				System.out.print(vp.getName() + ": " + vp.getValue() + "\n");
			}
		}
		
		set = yFacts.entrySet();
		for (Map.Entry<String, List<ValuePair>> entry : set) {
			System.out.print("Output for " + entry.getKey() + "\n");
			System.out.print("Output size " + entry.getValue().size() + "\n");
			for (ValuePair vp : entry.getValue()) {
				System.out.print(vp.getName() + ": " + vp.getValue() + "\n");
			}
		}
		
		
		for (ValuePair vp : vmXFacts) {
			System.out.print(vp.getName() + ": " + vp.getValue() + "\n");
		}
		
		System.out.print("################################################\n");
	}
	
	public List<ValuePair> getXData (String service) {
		return xFacts.get(service);
	}
	
	public List<ValuePair> getYData (String service) {
		return yFacts.get(service);
	}
	
	public List<ValuePair> getVMXData () {
		return vmXFacts;
	}
	
	public class ValuePair {
		private double value;
		private String name;
		
		
		
		public ValuePair(String name, double value) {
			super();
			this.value = value;
			this.name = name;
		}
		public double getValue() {
			return value;
		}
		public String getName() {
			return name;
		}
		
	}
	
}
