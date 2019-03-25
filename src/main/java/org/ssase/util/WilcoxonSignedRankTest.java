package org.ssase.util;


import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;
import org.apache.commons.math3.util.FastMath;

/**
 * An implementation of the Wilcoxon signed-rank test.
 * 
 * @version $Id: WilcoxonSignedRankTest.java 1244107 2012-02-14 16:17:55Z erans
 *          $
 */
public class WilcoxonSignedRankTest {

	/** Ranking algorithm. */
	private NaturalRanking naturalRanking;

	/**
	 * Create a test instance where NaN's are left in place and ties get the
	 * average of applicable ranks. Use this unless you are very sure of what
	 * you are doing.
	 */
	public WilcoxonSignedRankTest() {
		naturalRanking = new NaturalRanking(NaNStrategy.FIXED,
				TiesStrategy.AVERAGE);
	}

	/**
	 * Create a test instance using the given strategies for NaN's and ties.
	 * Only use this if you are sure of what you are doing.
	 * 
	 * @param nanStrategy
	 *            specifies the strategy that should be used for Double.NaN's
	 * @param tiesStrategy
	 *            specifies the strategy that should be used for ties
	 */
	public WilcoxonSignedRankTest(final NaNStrategy nanStrategy,
			final TiesStrategy tiesStrategy) {
		naturalRanking = new NaturalRanking(nanStrategy, tiesStrategy);
	}

	/**
	 * Ensures that the provided arrays fulfills the assumptions.
	 * 
	 * @param x
	 *            first sample
	 * @param y
	 *            second sample
	 * @throws NullArgumentException
	 *             if {@code x} or {@code y} are {@code null}.
	 * @throws NoDataException
	 *             if {@code x} or {@code y} are zero-length.
	 * @throws DimensionMismatchException
	 *             if {@code x} and {@code y} do not have the same length.
	 */
	private void ensureDataConformance(final double[] x, final double[] y)
			throws NullArgumentException, NoDataException,
			DimensionMismatchException {

		if (x == null || y == null) {
			throw new NullArgumentException();
		}
		if (x.length == 0 || y.length == 0) {
			throw new NoDataException();
		}
		if (y.length != x.length) {
			throw new DimensionMismatchException(y.length, x.length);
		}
	}

	/**
	 * Calculates y[i] - x[i] for all i
	 * 
	 * @param x
	 *            first sample
	 * @param y
	 *            second sample
	 * @return z = y - x
	 */
	private double[] calculateDifferences(final double[] x, final double[] y) {

		final double[] z = new double[x.length];

		for (int i = 0; i < x.length; ++i) {
			z[i] = y[i] - x[i];
		}

		return z;
	}

	/**
	 * Calculates |z[i]| for all i
	 * 
	 * @param z
	 *            sample
	 * @return |z|
	 * @throws NullArgumentException
	 *             if {@code z} is {@code null}
	 * @throws NoDataException
	 *             if {@code z} is zero-length.
	 */
	private double[] calculateAbsoluteDifferences(final double[] z)
			throws NullArgumentException, NoDataException {

		if (z == null) {
			throw new NullArgumentException();
		}

		if (z.length == 0) {
			throw new NoDataException();
		}

		final double[] zAbs = new double[z.length];

		for (int i = 0; i < z.length; ++i) {
			zAbs[i] = FastMath.abs(z[i]);
		}

		return zAbs;
	}

	/**
	 * Computes the <a
	 * href="http://en.wikipedia.org/wiki/Wilcoxon_signed-rank_test"> Wilcoxon
	 * signed ranked statistic</a> comparing mean for two related samples or
	 * repeated measurements on a single sample.
	 * <p>
	 * This statistic can be used to perform a Wilcoxon signed ranked test
	 * evaluating the null hypothesis that the two related samples or repeated
	 * measurements on a single sample has equal mean.
	 * </p>
	 * <p>
	 * Let X<sub>i</sub> denote the i'th individual of the first sample and
	 * Y<sub>i</sub> the related i'th individual in the second sample. Let
	 * Z<sub>i</sub> = Y<sub>i</sub> - X<sub>i</sub>.
	 * </p>
	 * <p>
	 * <strong>Preconditions</strong>:
	 * <ul>
	 * <li>The differences Z<sub>i</sub> must be independent.</li>
	 * <li>Each Z<sub>i</sub> comes from a continuous population (they must be
	 * identical) and is symmetric about a common median.</li>
	 * <li>The values that X<sub>i</sub> and Y<sub>i</sub> represent are
	 * ordered, so the comparisons greater than, less than, and equal to are
	 * meaningful.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param x
	 *            the first sample
	 * @param y
	 *            the second sample
	 * @return wilcoxonSignedRank statistic (the larger of W+ and W-)
	 * @throws NullArgumentException
	 *             if {@code x} or {@code y} are {@code null}.
	 * @throws NoDataException
	 *             if {@code x} or {@code y} are zero-length.
	 * @throws DimensionMismatchException
	 *             if {@code x} and {@code y} do not have the same length.
	 */
	public double wilcoxonSignedRank(final double[] x, final double[] y)
			throws NullArgumentException, NoDataException,
			DimensionMismatchException {

		ensureDataConformance(x, y);

		// throws IllegalArgumentException if x and y are not correctly
		// specified
		final double[] z = calculateDifferences(x, y);
		final double[] zAbs = calculateAbsoluteDifferences(z);

		final double[] ranks = naturalRanking.rank(zAbs);

		double Wplus = 0;

		for (int i = 0; i < z.length; ++i) {
			if (z[i] > 0) {
				Wplus += ranks[i];
			}
		}

		final long N = x.length;
		final double Wminus = (((double) (N * (N + 1))) / 2.0) - Wplus;

		return FastMath.max(Wplus, Wminus);
	}

	public double getEffectSize(final double[] x, final double[] y)
			throws NullArgumentException, NoDataException,
			DimensionMismatchException {

		ensureDataConformance(x, y);

		// throws IllegalArgumentException if x and y are not correctly
		// specified
		final double[] z = calculateDifferences(x, y);
		final double[] zAbs = calculateAbsoluteDifferences(z);

		final double[] ranks = naturalRanking.rank(zAbs);

		double Wplus = 0;

		for (int i = 0; i < z.length; ++i) {
			if (z[i] > 0) {
				Wplus += ranks[i];
			}
		}

		final long N = x.length;
		final double Wminus = (((double) (N * (N + 1))) / 2.0) - Wplus;
		
		long no = 0;
		for (int i = 0; i < x.length; i++) {
			no += i + 1;
		}
		//System.out.print("Wplus "+ Wplus+"\n" );
		//System.out.print("Wminus "+ Wminus+"\n" );
		//System.out.print("no "+ no+"\n" );

		return Math.abs(Wplus - Wminus)/no;
	}

	/**
	 * Algorithm inspired by
	 * http://www.fon.hum.uva.nl/Service/Statistics/Signed_Rank_Algorihms.html#C
	 * by Rob van Son, Institute of Phonetic Sciences & IFOTT, University of
	 * Amsterdam
	 * 
	 * @param Wmax
	 *            largest Wilcoxon signed rank value
	 * @param N
	 *            number of subjects (corresponding to x.length)
	 * @return two-sided exact p-value
	 */
	private double calculateExactPValue(final double Wmax, final long N) {

		// Total number of outcomes (equal to 2^N but a lot faster)
		final long m = 1 << N;

		long largerRankSums = 0;

		for (int i = 0; i < m; ++i) {
			long rankSum = 0;

			// Generate all possible rank sums
			for (int j = 0; j < N; ++j) {

				// (i >> j) & 1 extract i's j-th bit from the right
				if (((i >> j) & 1) == 1) {
					rankSum += j + 1;
				}
			}

			if (rankSum >= Wmax) {
				++largerRankSums;
			}
		}
		System.out.print("m" + m + "\n");
		System.out.print("largerRankSums" + largerRankSums + "\n");
		/*
		 * largerRankSums / m gives the one-sided p-value, so it's multiplied
		 * with 2 to get the two-sided p-value
		 */
		return 2 * ((double) largerRankSums) / ((double) m);
	}

	/**
	 * @param Wmin
	 *            smallest Wilcoxon signed rank value
	 * @param N
	 *            number of subjects (corresponding to x.length)
	 * @return two-sided asymptotic p-value
	 */
	private double calculateAsymptoticPValue(final double Wmin, final long n) {
		long N = n;
		final double ES = (double) (N * (N + 1)) / 4.0;

		/*
		 * Same as (but saves computations): final double VarW = ((double) (N *
		 * (N + 1) * (2*N + 1))) / 24;
		 */
		final double VarS = ES * ((double) (2 * N + 1) / 6.0);

		// - 0.5 is a continuity correction
		final double z = (Wmin - ES - 0.5) / FastMath.sqrt(VarS);
		System.out.print("z "+ z+"\n" );
		//System.out.print("VarS "+ VarS+"\n" );
		//System.out.print("ES "+ ES+"\n" );
		final NormalDistribution standardNormal = new NormalDistribution(0, 1);
		
//		double x = z / (1.0 * FastMath.sqrt(2.0));
//		final double ret = Gamma.regularizedGammaP(0.5, x * x, 1.0e-15, 10000);
//		System.out.print("ret "+ ret+"\n" );
//		return (1 + (x < 0 ? -ret : ret));
//		
		System.out.print("Z/sqrt N = " + Math.abs(z)/Math.sqrt(n) + "\n");
		return 2 * standardNormal.cumulativeProbability(z);
	}

	/**
	 * Returns the <i>observed significance level</i>, or <a href=
	 * "http://www.cas.lancs.ac.uk/glossary_v1.1/hyptest.html#pvalue">
	 * p-value</a>, associated with a <a
	 * href="http://en.wikipedia.org/wiki/Wilcoxon_signed-rank_test"> Wilcoxon
	 * signed ranked statistic</a> comparing mean for two related samples or
	 * repeated measurements on a single sample.
	 * <p>
	 * Let X<sub>i</sub> denote the i'th individual of the first sample and
	 * Y<sub>i</sub> the related i'th individual in the second sample. Let
	 * Z<sub>i</sub> = Y<sub>i</sub> - X<sub>i</sub>.
	 * </p>
	 * <p>
	 * <strong>Preconditions</strong>:
	 * <ul>
	 * <li>The differences Z<sub>i</sub> must be independent.</li>
	 * <li>Each Z<sub>i</sub> comes from a continuous population (they must be
	 * identical) and is symmetric about a common median.</li>
	 * <li>The values that X<sub>i</sub> and Y<sub>i</sub> represent are
	 * ordered, so the comparisons greater than, less than, and equal to are
	 * meaningful.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param x
	 *            the first sample
	 * @param y
	 *            the second sample
	 * @param exactPValue
	 *            if the exact p-value is wanted (only works for x.length <= 30,
	 *            if true and x.length > 30, this is ignored because
	 *            calculations may take too long)
	 * @return p-value
	 * @throws NullArgumentException
	 *             if {@code x} or {@code y} are {@code null}.
	 * @throws NoDataException
	 *             if {@code x} or {@code y} are zero-length.
	 * @throws DimensionMismatchException
	 *             if {@code x} and {@code y} do not have the same length.
	 * @throws NumberIsTooLargeException
	 *             if {@code exactPValue} is {@code true} and {@code x.length} >
	 *             30
	 * @throws ConvergenceException
	 *             if the p-value can not be computed due to a convergence error
	 * @throws MaxCountExceededException
	 *             if the maximum number of iterations is exceeded
	 */
	public double wilcoxonSignedRankTest(final double[] x, final double[] y,
			final boolean exactPValue) throws NullArgumentException,
			NoDataException, DimensionMismatchException,
			NumberIsTooLargeException, ConvergenceException,
			MaxCountExceededException {

		ensureDataConformance(x, y);

		final long N = x.length;
		final double Wmax = wilcoxonSignedRank(x, y);

		if (exactPValue && N > 30) {
			throw new NumberIsTooLargeException(N, 30, true);
		}

		if (exactPValue) {
			return calculateExactPValue(Wmax, N);
		} else {
			final double Wmin = ((double) (N * (N + 1)) / 2.0) - Wmax;
			return calculateAsymptoticPValue(Wmin, N);
		}
	}
}