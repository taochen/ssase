package org.ssascaling.clustering;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;

public class CustomIterativeKMeans  {
	
	protected static final Logger logger = LoggerFactory
	.getLogger(CustomIterativeKMeans.class);
    /**
     * XXX add doc
     */
    private int kMin;

    /**
     * XXX add doc
     */
    private int kMax;

    /**
     * XXX add doc
     */
    private ClusterEvaluation ce;

    /**
     * XXX add doc
     */
    private DistanceMeasure dm;

    /**
     * XXX add doc
     */
    private int iterations;
    
    
    public CustomIterativeKMeans(int kMin, int kMax, int iterations, DistanceMeasure dm, ClusterEvaluation ce) {
        this.kMin = kMin;
        this.kMax = kMax;
        this.iterations = iterations;
        this.dm = dm;
        this.ce = ce;
    }

    
    
    public Dataset[] cluster(Dataset data) {
    	CustomKMean km = new CustomKMean(this.kMin, this.iterations, this.dm);
        Dataset[] bestClusters = km.cluster(data);
        double bestScore = 0.0;
        try {
           bestScore = this.ce.score(bestClusters);
        } catch (Exception e) {
        	logger.error("Error: " + e + ", ignore and carry on");
        	for (Instance d : data) {
        		System.out.print(d.get(0) + "\n");
        	}
        }
        for (int i = kMin + 1; i <= kMax; i++) {
            km = new CustomKMean(i, this.iterations, this.dm);
            Dataset[] tmpClusters = km.cluster(data);
            double tmpScore = 0.0;
            try {
            	tmpScore = this.ce.score(tmpClusters);
            } catch (Exception e) {
            	logger.error(i + " Error: " + e + ", ignore and carry on");
            	continue;
            }
            if (bestScore == 0.0 || this.ce.compareScore(bestScore, tmpScore)) {
                bestScore = tmpScore;
                bestClusters = tmpClusters;
            }
        }
        return bestClusters;
    }
}
