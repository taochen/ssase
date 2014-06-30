package org.ssascaling.clustering;

import java.util.HashMap;
import java.util.Map;

import org.ssascaling.objective.correlation.Spearmans;

import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.AbstractCorrelation;

public class SpearmanDistance extends AbstractCorrelation {
	
	private Map<Instance, Map<Instance, Double>> map = new HashMap<Instance, Map<Instance, Double>>();
	private Spearmans spear = new Spearmans();
	public SpearmanDistance(){
	}

	@Override
	public double measure(Instance x, Instance y) {
	
		//System.out.print("x= " + x.hashCode() +" y= " + y.hashCode()+"\n");
		//System.out.print("x= " + x.get(0) +" y= " + y.get(0)+"\n");
		//if (x.classValue() == null){

		//}
		
		if (!map.containsKey(x)) {
			map.put(x,  new HashMap<Instance, Double>());
		}
		
		if (!map.containsKey(y)) {
			map.put(y,  new HashMap<Instance, Double>());
		}
		
		
		if (!map.get(x).containsKey(y)) {
			double[] xValue = new double[x.size()];
			double[] yValue = new double[y.size()];
			for (int i = 0; i <  x.noAttributes();i++){
				xValue[i] = x.get(i);
			    yValue[i] = y.get(i);
			}
			double value = 1 - spear.doCorrelation(xValue, yValue);
			
			map.get(x).put(y, value);
			map.get(y).put(x, value);
		}
		
		return map.get(x).get(y);
		//System.out.print("x= " + x.classValue() +" y= " + y.classValue()+"\n");
		//return (x.classValue().equals(y.classValue()))? 0 : map.get(x.classValue()).get(y.classValue());
	}

}
