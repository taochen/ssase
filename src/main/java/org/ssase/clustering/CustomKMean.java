package org.ssase.clustering;

import java.util.Random;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.tools.DatasetTools;

public class CustomKMean  {

	/**
	 * The number of clusters.
	 */
	private int numberOfClusters = -1;

	/**
	 * The number of iterations the algorithm should make. If this value is
	 * Integer.INFINITY, then the algorithm runs until the centroids no longer
	 * change.
	 * 
	 */
	private int numberOfIterations = -1;

	/**
	 * Random generator for this clusterer.
	 */
	private Random rg;

	/**
	 * The distance measure used in the algorithm, defaults to Euclidean
	 * distance.
	 */
	private DistanceMeasure dm;

	/**
	 * The centroids of the different clusters.
	 */
	private Instance[] centroids;
	
	
	private static final long MAX_TIME_LIMIT = 8000;
	
    public CustomKMean(int clusters, int iterations, DistanceMeasure dm) {
        this.numberOfClusters = clusters;
        this.numberOfIterations = iterations;
        this.dm = dm;
        rg = new Random(System.currentTimeMillis());
    }


	public Dataset[] cluster(Dataset data) {
		if (data.size() == 0)
			throw new RuntimeException("The dataset should not be empty");
		if (numberOfClusters == 0)
			throw new RuntimeException("There should be at least one cluster");
		// Place K points into the space represented by the objects that are
		// being clustered. These points represent the initial group of
		// centroids.
		// DatasetTools.
		Instance min = DatasetTools.minAttributes(data);
		Instance max = DatasetTools.maxAttributes(data);
		this.centroids = new Instance[numberOfClusters];
		int instanceLength = data.instance(0).noAttributes();
		for (int j = 0; j < numberOfClusters; j++) {
			double[] randomInstance = new double[instanceLength];
			for (int i = 0; i < instanceLength; i++) {
				double dist = Math.abs(max.value(i) - min.value(i));
				randomInstance[i] = (float) (min.value(i) + rg.nextDouble()
						* dist);

			}
			this.centroids[j] = new DenseInstance(randomInstance);
		}

		int iterationCount = 0;
		boolean centroidsChanged = true;
		boolean randomCentroids = true;
		long time = System.currentTimeMillis();
		while ((randomCentroids && System.currentTimeMillis() - time < MAX_TIME_LIMIT)
				|| (iterationCount < this.numberOfIterations && centroidsChanged)) {
			iterationCount++;
			// Assign each object to the group that has the closest centroid.
			int[] assignment = new int[data.size()];
			for (int i = 0; i < data.size(); i++) {
				int tmpCluster = 0;
				double minDistance = dm.measure(centroids[0], data.instance(i));
				for (int j = 1; j < centroids.length; j++) {
					double dist = dm.measure(centroids[j], data.instance(i));
					if (dm.compare(dist, minDistance)) {
						minDistance = dist;
						tmpCluster = j;
					}
				}
				assignment[i] = tmpCluster;

			}
			// When all objects have been assigned, recalculate the positions of
			// the K centroids and start over.
			// The new position of the centroid is the weighted center of the
			// current cluster.
			double[][] sumPosition = new double[this.numberOfClusters][instanceLength];
			int[] countPosition = new int[this.numberOfClusters];
			for (int i = 0; i < data.size(); i++) {
				Instance in = data.instance(i);
				for (int j = 0; j < instanceLength; j++) {

					sumPosition[assignment[i]][j] += in.value(j);

				}
				countPosition[assignment[i]]++;
			}
			centroidsChanged = false;
			randomCentroids = false;
			for (int i = 0; i < this.numberOfClusters; i++) {
				if (countPosition[i] > 0) {
					double[] tmp = new double[instanceLength];
					for (int j = 0; j < instanceLength; j++) {
						tmp[j] = (float) sumPosition[i][j] / countPosition[i];
					}
					Instance newCentroid = new DenseInstance(tmp);
					if (dm.measure(newCentroid, centroids[i]) > 0.0001) {
						centroidsChanged = true;
						centroids[i] = newCentroid;
					}
				} else {
					double[] randomInstance = new double[instanceLength];
					for (int j = 0; j < instanceLength; j++) {
						double dist = Math.abs(max.value(j) - min.value(j));
						randomInstance[j] = (float) (min.value(j) + rg
								.nextDouble() * dist);

					}
					randomCentroids = true;
					this.centroids[i] = new DenseInstance(randomInstance);
				}

			}

		}
		Dataset[] output = new Dataset[centroids.length];
		for (int i = 0; i < centroids.length; i++)
			output[i] = new DefaultDataset();
		for (int i = 0; i < data.size(); i++) {
			int tmpCluster = 0;
			double minDistance = dm.measure(centroids[0], data.instance(i));
			for (int j = 0; j < centroids.length; j++) {
				double dist = dm.measure(centroids[j], data.instance(i));
				if (dm.compare(dist, minDistance)) {
					minDistance = dist;
					tmpCluster = j;
				}
			}
			output[tmpCluster].add(data.instance(i));

		}
		return output;
	}

	
	public Instance[] getMedoids() {
		return centroids;
	}

	
	
}
