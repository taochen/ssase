package org.ssascaling.objective.reduction;

import java.util.List;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;

import org.ssascaling.clustering.CustomKMean;
import org.ssascaling.clustering.SpearmanDistance;
import org.ssascaling.objective.Objective;

public class JavaMLNeighborhood extends NeighborhoodReduction {

	@SuppressWarnings("rawtypes")
	@Override
	public List[] clustering(List<Objective> objectives) {
		Dataset ds = new DefaultDataset();
		for (Objective obj : objectives) {
			ds.add(new DenseInstance(obj.getArray(),obj));
		}
		long time = System.currentTimeMillis();
		//SpearmanRankCorrelation sc = new SpearmanRankCorrelation();
		//System.out.print("Correlation " + sc.measure(ds.get(1), ds.get(2)) + "\n");
		//SpearmanRankCorrelation
		CustomKMean ckm = new CustomKMean(2, 1000, new SpearmanDistance());
		Dataset[] clusters = ckm.cluster(ds);
		System.out.print("Time taken on clustering: " + ( System.currentTimeMillis() - time) + "\n");
		
		return clusters;
	}

	@Override
	public Object getClassValue(Object instance) {
		return ((Instance)instance).classValue();
	}

}
