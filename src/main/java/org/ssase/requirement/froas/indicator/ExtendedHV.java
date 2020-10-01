package org.ssase.requirement.froas.indicator;

import java.util.ArrayList;
import java.util.List;

import org.femosaa.util.HV;
import org.ssase.requirement.froas.RequirementProposition;

public class ExtendedHV extends HV {

	  public double req_hypervolume(double[][] front, RequirementProposition[] rp, double[] maxs, double[] mins) {
		  
		  double[][] new_front = new double[front.length][front[0].length];
		  
		  for (int i = 0; i < rp.length; i++) {
			  rp[i].updateNormalizationBounds(maxs[i]);
			  rp[i].updateNormalizationBounds(mins[i]);
		  }
		  
		  
		  for (int i = 0; i < front.length; i++) {
			  
			  double[] v = new double[front[i].length];
			  for (int j = 0; j < front[i].length; j++) {
				  v[j] = rp[j].fuzzilize(front[i][j]);
			  }
			  new_front[i] = v;
			  
		  }
		  
		  return super.hypervolume(new_front);
	  }
	  
	  
     public double req_hypervolume(double[][] front, RequirementProposition[] rp) {
		  
		  double[][] new_front = new double[front.length][front[0].length];
		
		  
		  for (int i = 0; i < front.length; i++) {
			  
			  double[] v = new double[front[i].length];
			  for (int j = 0; j < front[i].length; j++) {
				  v[j] = rp[j].fuzzilize(front[i][j]);
			  }
			  new_front[i] = v;
			  
			 
			  //System.out.print(front[i][0] + "," + front[i][1] + "=" + v[0] + "," + v[1] + "\n");
		  }
		  
		  //System.out.print("---------\n");
		  
		  return super.hypervolume(new_front);
	  }
     
      public double req_hypervolume_1(double[][] front, RequirementProposition[] rp) {
    	  
    	  List<Double[]> list = new ArrayList<Double[]>();
		  
		 
		
		  
		  for (int i = 0; i < front.length; i++) {
			  
			  double[] v = new double[front[i].length];
			  boolean iskeep = true;
			  for (int j = 0; j < front[i].length; j++) {
				  v[j] = rp[j].fuzzilize(front[i][j]);
				  if(Double.isInfinite(v[j])) {
					  iskeep = false;
				  }
			  }
			  
			  if(!iskeep) {
				  continue;
			  }
			  
			  list.add(new Double[] {v[0],v[1]});
			  //new_front[i] = v;
			  
			 
			  //System.out.print(front[i][0] + "," + front[i][1] + "=" + v[0] + "," + v[1] + "\n");
		  }
		  
		  if(list.size() == 0) {
			  return 0;
		  }
		  
		  double[][] new_front = new double[list.size()][list.get(0).length];
		  
		  for (int i = 0; i < list.size(); i++) {
			  new_front[i] = new double[] {list.get(i)[0],list.get(i)[1]};
		  }
		  
		  
		  //System.out.print("---------\n");
		  
		  return super.hypervolume(new_front);
	  }
}
