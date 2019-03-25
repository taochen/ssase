package org.ssase.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

/**
 * The Kruskal-Wallis One-Way Analysis of Variance by Ranks is a non-parametric
 * statistical test determining if (at least) two out of K >= 2 populations have
 * differing medians.
 * <p>
 * <ul>
 * <li>Null Hypothesis: All populations have equal medians.
 * <li>Alternative Hypothesis: Not all populations have equal medians.
 * </ul>
 * <p>
 * Assumptions:
 * <ol>
 * <li>Samples are randomly selected from their corresponding populations
 * <li>Samples are independent
 * <li>The dependent variable (value being sampled) is continuous
 * <li>The underlying distributions of the populations are identical in shape
 * </ol>
 * <p>
 * References:
 * <ol>
 * <li>Kruskal, W.H. and Wallis W.A. "Use of Ranks in One-Criterion Variance
 * Analysis." Journal of the American Statistical Association, 47(260):583-621,
 * 1952.
 * <li>Sheskin, D.J. "Handbook of Parametric and Nonparametric Statistical
 * Procedures, Third Edition." Chapman & Hall/CRC. 2004.
 * </ol>
 */
public class KruskalWallisTest {

	private static class ObservationComparator implements
			Comparator<RankedObservation>, Serializable {

		private static final long serialVersionUID = 284381611483212771L;

		@Override
		public int compare(RankedObservation o1, RankedObservation o2) {
			if (o1.getValue() < o2.getValue()) {
				return -1;
			} else if (o1.getValue() > o2.getValue()) {
				return 1;
			} else {
				return 0;
			}
		}

	}
	
	/*
	 * The number of groups being tested.
	 */
	protected final int numberOfGroups;

	/**
	 * The comparator used for ordering observations.
	 */
	protected final Comparator<RankedObservation> comparator;

	/**
	 * Collection of all ranked observations added to this test.
	 */
	
	/**
	 * Collection of all ranked observations added to this test.
	 */
	protected final List<RankedObservation> data;


	/**
	 * Constructs a Kruskal-Wallis test with the specified number of groups.
	 * 
	 * @param numberOfGroups
	 *            the number of groups being tested
	 */
	public KruskalWallisTest(int numberOfGroups) {
		super();
		this.numberOfGroups = numberOfGroups;
		this.comparator = new ObservationComparator();

		data = new ArrayList<RankedObservation>();

		if (numberOfGroups <= 1) {
			throw new IllegalArgumentException("requires two or more groups");
		}
	}

	
	// make method public

	public void addAll(double[] values, int group) {

		for (double value : values) {
			add(value, group);
		}
	}
	
	public void add(double value, int group) {
		if ((group < 0) || (group >= numberOfGroups)) {
			throw new IllegalArgumentException();
		}

		data.add(new RankedObservation(value, group));
	}
	

	/**
	 * Computes the chi-squared approximation of the Kruskal-Wallis test
	 * statistic. See equation (22-1) in the reference book for details.
	 * 
	 * 
	 * the psychometrica online calculator uses the formula provided by Barry Cohen (2008, see also the review article by Tomczak & Tomczak 2014: "The need to report effect size estimates revisited"):
eta square H = (H Ð k +1) / (n Ð k)
where k is the number of groups and n represents the number of total observations.
	 * 
	 * @return the chi-squared approximation of the Kruskal-Wallis test
	 *         statistic
	 */
	double H() {
		int[] n = new int[numberOfGroups];
		double[] rbar = new double[numberOfGroups];

		for (RankedObservation observation : data) {
			n[observation.getGroup()]++;
			rbar[observation.getGroup()] += observation.getRank();
		}

		double H = 0.0;
		for (int i = 0; i < numberOfGroups; i++) {
			H += Math.pow(rbar[i], 2.0) / n[i];
		}

		int N = data.size();
		System.out.print("N " + N + "\n");
		return (12.0 / (N * (N + 1))) * H - 3.0 * (N + 1);
	}

	/**
	 * Computes the correction factor for ties. See equation (22-3) in the
	 * reference book for details.
	 * 
	 * @return the correction factor for ties
	 */
	double C() {
		int N = data.size();
		double C = 0.0;

		int i = 0;
		while (i < N) {
			int j = i + 1;

			while ((j < N)
					&& (data.get(i).getValue() == data.get(j).getValue())) {
				j++;
			}

			C += Math.pow(j - i, 3.0) - (j - i);
			i = j;
		}

		return 1 - C / (Math.pow(N, 3.0) - N);
	}

	public boolean test(double alpha) {
		update();

		ChiSquaredDistribution dist = new ChiSquaredDistribution(
				numberOfGroups - 1);
		double H = H();
		double C = C();

		if (C == 0.0) {
			// all observations the same
			return false;
		}
		
		// can be used with bonferroni correction a / (k(k-1)/2) to modify the alpha
		System.out.print("H/C value " + (H / C) + "\n");
		System.out.print("chi square value " + H  + "\n");
		System.out.print("test value " + (1.0 - dist.cumulativeProbability(H / C)) + "\n");
		return 1.0 - dist.cumulativeProbability(H / C) < alpha;
	}
	
	protected void update() {
		Collections.sort(data, comparator);

		int i = 0;
		while (i < data.size()) {
			int j = i + 1;
			double rank = i + 1;

			while ((j < data.size())
					&& (data.get(i).getValue() == data.get(j).getValue())) {
				rank += j + 1;
				j++;
			}

			rank /= j - i;

			for (int k = i; k < j; k++) {
				data.get(k).setRank(rank);
			}

			i = j;
		}
	}
	
	public class Observation {

		/**
		 * The value of this observation.
		 */
		private final double value;

		/**
		 * The group from which this observation belongs.
		 */
		private final int group;

		/**
		 * Constructs an observation with the specified value and group.
		 * 
		 * @param value the value of this observation
		 * @param group the group from which this observation belongs
		 */
		public Observation(double value, int group) {
			super();
			this.value = value;
			this.group = group;
		}

		/**
		 * Returns the value of this observation.
		 * 
		 * @return the value of this observation
		 */
		public double getValue() {
			return value;
		}

		/**
		 * Returns the group from which this observation belongs.
		 * 
		 * @return the group from which this observation belongs
		 */
		public int getGroup() {
			return group;
		}

	}
	
	public class RankedObservation extends Observation {

		/**
		 * The rank of this observation.
		 */
		private double rank;

		/**
		 * Constructs a ranked observation with the specified value and group. The
		 * rank of this observation is default to 0.0.
		 * 
		 * @param value the value of this observation
		 * @param group the group from which this observation belongs
		 */
		public RankedObservation(double value, int group) {
			super(value, group);
		}

		/**
		 * Returns the rank of this observation.
		 * 
		 * @return the rank of this observation
		 */
		public double getRank() {
			return rank;
		}

		/**
		 * Sets the rank of this observation.
		 * 
		 * @param rank the new rank for this observation
		 */
		public void setRank(double rank) {
			this.rank = rank;
		}

	}

}
