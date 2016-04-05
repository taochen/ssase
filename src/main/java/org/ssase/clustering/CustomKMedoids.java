package org.ssase.clustering;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.ssase.objective.QualityOfService;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.DatasetTools;

public class CustomKMedoids {
	/* Distance measure to measure the distance between instances */
	private DistanceMeasure dm;

	/* Number of clusters to generate */
	private int numberOfClusters;

	/* Random generator for selection of candidate medoids */
	private Random rg;

	/* The maximum number of iterations the algorithm is allowed to run. */
	private int maxIterations;


	private Instance[] medoids;

	/**
	 * Creates a new instance of the k-medoids algorithm with the specified
	 * parameters.
	 * 
	 * @param numberOfClusters
	 *            the number of clusters to generate
	 * @param maxIterations
	 *            the maximum number of iteration the algorithm is allowed to
	 *            run
	 * @param DistanceMeasure
	 *            dm the distance metric to use for measuring the distance
	 *            between instances
	 * 
	 */
	public CustomKMedoids(int numberOfClusters, int maxIterations, DistanceMeasure dm) {
		super();
		this.numberOfClusters = numberOfClusters;
		this.maxIterations = maxIterations;
		this.dm = dm;
		rg = new Random(System.currentTimeMillis());
	}

	
	public Dataset[] cluster(Dataset data) {
		Instance[] medoids = new Instance[numberOfClusters];
		Dataset[] output = new DefaultDataset[numberOfClusters];
		for (int i = 0; i < numberOfClusters; i++) {
			int random = rg.nextInt(data.size());
			medoids[i] = data.instance(random);
		}

		boolean changed = true;
		int count = 0;
		while (changed && count < maxIterations) {
			changed = false;
			count++;
			int[] assignment = assign(medoids, data);
			changed = recalculateMedoids(assignment, medoids, output, data);

		}
		System.out.print("Count: " + count + "\n");
		
		this.medoids = medoids;
		return output;

	}

	/**
	 * Assign all instances from the data set to the medoids.
	 * 
	 * @param medoids candidate medoids
	 * @param data the data to assign to the medoids
	 * @return best cluster indices for each instance in the data set
	 */
	private int[] assign(Instance[] medoids, Dataset data) {
		int[] out = new int[data.size()];
		for (int i = 0; i < data.size(); i++) {
			double bestDistance = dm.measure(data.instance(i), medoids[0]);
			int bestIndex = 0;
			for (int j = 1; j < medoids.length; j++) {
				double tmpDistance = dm.measure(data.instance(i), medoids[j]);
				if (dm.compare(tmpDistance, bestDistance)) {
					bestDistance = tmpDistance;
					bestIndex = j;
				}
			}
			out[i] = bestIndex;

		}
		return out;

	}

	/**
	 * Return a array with on each position the clusterIndex to which the
	 * Instance on that position in the dataset belongs.
	 * 
	 * @param medoids
	 *            the current set of cluster medoids, will be modified to fit
	 *            the new assignment
	 * @param assigment
	 *            the new assignment of all instances to the different medoids
	 * @param output
	 *            the cluster output, this will be modified at the end of the
	 *            method
	 * @return the
	 */
	private boolean recalculateMedoids(int[] assignment, Instance[] medoids,
			Dataset[] output, Dataset data) {
		boolean changed = false;
		for (int i = 0; i < numberOfClusters; i++) {
			output[i] = new DefaultDataset();
			for (int j = 0; j < assignment.length; j++) {
				if (assignment[j] == i) {
					output[i].add(data.instance(j));
				}
			}
			if (output[i].size() == 0) { // new random, empty medoid
				medoids[i] = data.instance(rg.nextInt(data.size()));
				changed = true;
			} else {
				Instance centroid = average(output[i]);
				//System.out.print("Average: " + ((QualityOfService)centroid.classValue()).getName() + "\n");
				Instance oldMedoid = medoids[i];
				Iterator<Instance> ite = kNearest(1, centroid, data).iterator();  
				//data.kNearest(1, centroid, dm).iterator();
				
				medoids[i] = ite.next();
				
				//System.out.print("Old: " + ((QualityOfService)oldMedoid.classValue()).getName() + "\n");
				//System.out.print("New: " + ((QualityOfService)medoids[i].classValue()).getName() + "\n");
				if (!medoids[i].equals(oldMedoid)){
					changed = true;
				}
			}
		}
		return changed;
	}
	
	
	private Instance average(Dataset set) {
		
		Instance instance = null;
		double fValue = 0.0;
		for (Instance ins : set) {
			double value = 0.0;
			for (Instance subIns : set) {
				if(!ins.equals(subIns)){
					value += dm.measure(ins, subIns);
				}
			}
			
			if (instance == null || value < fValue ){
				instance = ins;
				fValue = value;
			}
		}
		
		return instance;
	}
	
	
    private Set<Instance> kNearest(int k, Instance inst, Dataset set) {
        Map<Instance, Double> closest = new HashMap<Instance, Double>();
        double max = Double.POSITIVE_INFINITY;
        for (Instance tmp : set) {
            double d = dm.measure(inst, tmp);
            if (!inst.equals(tmp)) {
                closest.put(tmp, d);
                if (closest.size() > k)
                    max = removeFarthest(closest);
            }

        }
        return closest.keySet();
    }
    
    private double removeFarthest(Map<Instance, Double> vector) {
        Instance tmp = null;// ; = vector.get(0);
        double max = 0;
        for (Instance inst : vector.keySet()) {
            double d = vector.get(inst);
            if (d > max) {
                max = d;
                tmp = inst;
            }
        }
        vector.remove(tmp);
        return max;

    }
	
	public Instance[] getMedoids() {
		return medoids;
	}

}
