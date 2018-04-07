package org.soa.wsdream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jmetal.core.SolutionSet;
import jmetal.core.Solution;
import jmetal.util.Ranking;

import org.femosaa.core.SAS;
import org.femosaa.core.SASAlgorithmAdaptor;
import org.femosaa.util.HV;
import org.femosaa.util.IGD;


/**
 * overhead (ms):
 * AOSeed - 182 + 90
 * HSeed - 176
 * SOSeed - 174 + 90
 * RSeed - 182
 * @author tao
 *
 */
public class Data {

	private static final String prefix = "/Users/tao/research/experiments-data/seed/results/";
	private static final List<Double[][]> norm = new ArrayList<Double[][]>(); // new
	private static Double[][] raw_norm = new Double[3][2];																		// double[3][2];
	private static final String[] names = new String[] { "AOSeed", 
			"SOSeed", "HSeed", "RSeed", "NONE" };
	
	private static final String[] seeds = new String[] { "10", "30",
		"50", "70", "90" };

	private static SolutionSet reference = new SolutionSet();
	private static double[][] referenceFront;
	static HV hv = new HV();
	static IGD igd = new IGD();
	
	
	static {
		for (int i = 0; i < 30; i++) {
			norm.add(new Double[3][2]);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*-------HV--------*/
		
		readAvgHVWithDiffSeeds("10AS3/nsgaii/");
//		readAvgIGDWithDiffSeed("15AS3/nsgaii/");
//		 readMaxAndMinWithRaw("15AS3/nsgaii/50/");
//		 readAvgHV("15AS3/nsgaii/50/");
		
//		 readMaxAndMinWithRaw("5AS1/nsgaii/50/");
//		 readAvgHV("5AS1/nsgaii/50/");
	
//		 readMaxAndMinWithRaw("5AS1/moead/50/");
//		 readAvgHV("5AS1/moead/50/");
		 
//		 readMaxAndMinWithRaw("5AS1/ibea/50/");
//		 readAvgHV("5AS1/ibea/50/");
		// change 10%, e.g., change from 1 to 10 of min of the first AS
		

		/*-------IGD--------*/
//		readMaxAndMinWithRaw("15AS3/nsgaii/50/");
//		readReference("15AS3/nsgaii/50/");
//		readAvgIGD("15AS3/nsgaii/50/");

		
		/*-------Quality--------*/
//		 readAvgQuality("15AS3/nsgaii/50/");

	}

	public static void readAvgIGDWithDiffSeed(String path) {
		for (String s : seeds) {
			readMaxAndMinWithRaw(path + s + "/");
		}
		
		readReferenceWithSeeds(path);
		
		for (String s : seeds) {
			System.out.print("--------------- seed no " + s + " ---------------\n");
			readAvgIGD(path + s + "/");
		}
	}
	
	public static void readAvgHVWithDiffSeeds(String path) {
		for (String s : seeds) {
			readMaxAndMinWithRaw(path + s + "/");
		}
		
		for (String s : seeds) {
			System.out.print("--------------- seed no " + s + " ---------------\n");
			readAvgHV(path + s + "/");
		}
	}
	
	
	public static void readAvgIGD(String path) {

		for (String s : names) {
			File file = new File(prefix + path + s + "/SolutionSetWithGen.rtf");
			Double[] d = new Double[10];
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				int k = 0;
				int no = 0;
				// int run = 0;
				String line = "";
				double[][] front = new double[100][3];
				while ((line = reader.readLine()) != null) {
					// System.out.print(line+ "\n");
					if (line.startsWith("----")) {

						if (Integer.parseInt(line.split(":")[1])
								% SASAlgorithmAdaptor.logGenerationOfObjectiveValue == 0) {

							d[k] = d[k] == null ? igd.invertedGenerationalDistance(front, referenceFront) : d[k]
									+ igd.invertedGenerationalDistance(front, referenceFront);
							front = new double[100][3];
							no = 0;
							// run++;
							k++;
							if (k > 9) {
								k = 0;
							}
						}

					} else {
						String[] split = line.split(",");
//						front[no][0] = (Double.parseDouble(split[0]) - norm
//								.get(k)[0][0])
//								/ (norm.get(k)[0][1] - norm.get(k)[0][0]);
//						front[no][1] = (Double.parseDouble(split[1]) - norm
//								.get(k)[1][0])
//								/ (norm.get(k)[1][1] - norm.get(k)[1][0]);
//						front[no][2] = (Double.parseDouble(split[2]) - norm
//								.get(k)[2][0])
//								/ (norm.get(k)[2][1] - norm.get(k)[2][0]);

						
						front[no][0] = (Double.parseDouble(split[0]) - raw_norm
								[0][0])
								/ (raw_norm[0][1] - raw_norm[0][0]);
						front[no][1] = (Double.parseDouble(split[1]) - raw_norm[1][0])
								/ (raw_norm[1][1] - raw_norm[1][0]);
						front[no][2] = (Double.parseDouble(split[2]) - raw_norm[2][0])
								/ (raw_norm[2][1] - raw_norm[2][0]);
						
						no++;

						// d[k][0] = d[k][0] == null?
						// Double.parseDouble(split[0]) :
						// d[k][0] > Double.parseDouble(split[0])?
						// Double.parseDouble(split[0]) : d[k][0];
						// d[k][1] = d[k][1] == null?
						// Double.parseDouble(split[1]) :
						// d[k][1] > Double.parseDouble(split[1])?
						// Double.parseDouble(split[1]) : d[k][1];
						// d[k][2] = d[k][2] == null?
						// Double.parseDouble(split[2]) :
						// d[k][2] > Double.parseDouble(split[2])?
						// Double.parseDouble(split[2]) : d[k][2];
					}
				}

				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.print(path + ": " + s + "\n");

			int i = 1;
			for (Double dd : d) {
				System.out.print("run " + i + ", IGD= " + (dd / 30) + "\n");
				i++;
			}

			// System.out.print("HV= " + (r/300) + "\n");
		}
	}
	
	public static void readAvgHV(String path) {
		
		
		Map<String, Double> map = new HashMap<String, Double>();
		
		for (String s : names) {
			File file = new File(prefix + path + s + "/InitialSolutionSet.rtf");
			if(!file.exists()) {
				continue;
			}
			
			double hv_value = 0.0;
			int k = 0;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
			
				int no = 0;
				// int run = 0;
				String line = "";
				double[][] front = new double[100][3];
				while ((line = reader.readLine()) != null) {
					// System.out.print(line+ "\n");
					if (line.startsWith("----")) {
						
						hv_value += hv.hypervolume(front);

						no = 0;
						k++;
					} else {
						String[] split = line.split(",");

						
						front[no][0] = (Double.parseDouble(split[0]) - raw_norm
								[0][0])
								/ (raw_norm[0][1] - raw_norm[0][0]);
						front[no][1] = (Double.parseDouble(split[1]) - raw_norm[1][0])
								/ (raw_norm[1][1] - raw_norm[1][0]);
						front[no][2] = (Double.parseDouble(split[2]) - raw_norm[2][0])
								/ (raw_norm[2][1] - raw_norm[2][0]);
						
						
						no++;
					}
				}

				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			map.put(s, hv_value / k);
		}
		
		

		for (String s : names) {
			File file = new File(prefix + path + s + "/SolutionSetWithGen.rtf");
			if(!file.exists()) {
				continue;
			}
			Double[] d = new Double[10];
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				int k = 0;
				int no = 0;
				// int run = 0;
				String line = "";
				double[][] front = new double[100][3];
				while ((line = reader.readLine()) != null) {
					// System.out.print(line+ "\n");
					if (line.startsWith("----")) {

						if (Integer.parseInt(line.split(":")[1])
								% SASAlgorithmAdaptor.logGenerationOfObjectiveValue == 0) {

							d[k] = d[k] == null ? hv.hypervolume(front) : d[k]
									+ hv.hypervolume(front);
							front = new double[100][3];
							no = 0;
							// run++;
							k++;
							if (k > 9) {
								k = 0;
							}
						}

					} else {
						String[] split = line.split(",");
//						front[no][0] = (Double.parseDouble(split[0]) - norm
//								.get(k)[0][0])
//								/ (norm.get(k)[0][1] - norm.get(k)[0][0]);
//						front[no][1] = (Double.parseDouble(split[1]) - norm
//								.get(k)[1][0])
//								/ (norm.get(k)[1][1] - norm.get(k)[1][0]);
//						front[no][2] = (Double.parseDouble(split[2]) - norm
//								.get(k)[2][0])
//								/ (norm.get(k)[2][1] - norm.get(k)[2][0]);

						
						front[no][0] = (Double.parseDouble(split[0]) - raw_norm
								[0][0])
								/ (raw_norm[0][1] - raw_norm[0][0]);
						front[no][1] = (Double.parseDouble(split[1]) - raw_norm[1][0])
								/ (raw_norm[1][1] - raw_norm[1][0]);
						front[no][2] = (Double.parseDouble(split[2]) - raw_norm[2][0])
								/ (raw_norm[2][1] - raw_norm[2][0]);
						
						
						no++;

						// d[k][0] = d[k][0] == null?
						// Double.parseDouble(split[0]) :
						// d[k][0] > Double.parseDouble(split[0])?
						// Double.parseDouble(split[0]) : d[k][0];
						// d[k][1] = d[k][1] == null?
						// Double.parseDouble(split[1]) :
						// d[k][1] > Double.parseDouble(split[1])?
						// Double.parseDouble(split[1]) : d[k][1];
						// d[k][2] = d[k][2] == null?
						// Double.parseDouble(split[2]) :
						// d[k][2] > Double.parseDouble(split[2])?
						// Double.parseDouble(split[2]) : d[k][2];
					}
				}

				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.print(path + ": " + s + "\n");

			int i = 1;
			System.out.print("run 0, HV= " + map.get(s) + "\n");
			for (Double dd : d) {
				System.out.print("run " + i + ", HV= " + (dd / 30) + "\n");
				i++;
			}
			i = 1;
			System.out.print("(0,"+ map.get(s) + ")\n");
			for (Double dd : d) {
				System.out.print("(" + (i*500) + ","+ (dd / 30) + ")\n");
				i++;
			}

			// System.out.print("HV= " + (r/300) + "\n");
		}
	}

	public static void readAvgQuality(String path) {

		String[] r = new String[3];
		for (String s : names) {
			File file = new File(prefix + path + s + "/SolutionSetWithGen.rtf");
			Double[][] d = new Double[10][3];
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				int k = 0;
				// int run = 0;
				String line = "";

				while ((line = reader.readLine()) != null) {
					// System.out.print(line+ "\n");
					if (line.startsWith("----")) {

						if (Integer.parseInt(line.split(":")[1])
								% SASAlgorithmAdaptor.logGenerationOfObjectiveValue == 0) {
							// run++;
							k++;
							if (k > 9) {
								k = 0;
							}
						}

					} else {
						String[] split = line.split(",");
						d[k][0] = d[k][0] == null ? Double
								.parseDouble(split[0]) : d[k][0]
								+ Double.parseDouble(split[0]);
						d[k][1] = d[k][1] == null ? Double
								.parseDouble(split[1]) : d[k][1]
								+ Double.parseDouble(split[1]);
						d[k][2] = d[k][2] == null ? Double
								.parseDouble(split[2]) : d[k][2]
								+ Double.parseDouble(split[2]);
						// d[k][0] = d[k][0] == null?
						// Double.parseDouble(split[0]) :
						// d[k][0] > Double.parseDouble(split[0])?
						// Double.parseDouble(split[0]) : d[k][0];
						// d[k][1] = d[k][1] == null?
						// Double.parseDouble(split[1]) :
						// d[k][1] > Double.parseDouble(split[1])?
						// Double.parseDouble(split[1]) : d[k][1];
						// d[k][2] = d[k][2] == null?
						// Double.parseDouble(split[2]) :
						// d[k][2] > Double.parseDouble(split[2])?
						// Double.parseDouble(split[2]) : d[k][2];
					}
				}

				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.print(path + ": " + s + "\n");
			int i = 1;
			DecimalFormat f = new DecimalFormat("0.000");
			for (Double[] dd : d) {
			
				
				dd[0] = dd[0] / 3000;
				dd[1] = dd[1] / 3000;
				dd[2] = dd[2] / 3000;
				System.out.print("run " + i + ", [0] " + dd[0] + ", [1] "
						+ dd[1] + ", [2] " + dd[2] + "\n");
				
				if(i == 10) {
				
					r[0] += f.format(dd[0]) + "&";
					r[1] += f.format(1.0/dd[1]) + "&";
					r[2] += f.format(dd[2]) + "&";
				}
				i++;
			}
		}
		
		for (String s : r) {
			System.out.print(s + "\n");
		}
	}

	public static void readMaxAndMin(String path) {

		for (String s : names) {
			File file = new File(prefix + path + s + "/SolutionSetWithGen.rtf");
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				int k = 0;
				String line = "";
				Double[][] d = norm.size() <= k ? new Double[3][2] : norm
						.get(k);
				while ((line = reader.readLine()) != null) {
					// System.out.print(line+ "\n");
					if (line.startsWith("----")) {

						if (Integer.parseInt(line.split(":")[1]) == SASAlgorithmAdaptor.logGenerationOfObjectiveValue) {
							if (norm.size() <= k) {
								norm.add(d);
							} else {
								norm.set(k, d);
							}

							k++;
							d = norm.size() <= k ? new Double[3][2] : norm
									.get(k);

						}

					} else {
						String[] split = line.split(",");
						if (d[0][0] == null
								|| (d[0][0] != null && d[0][0] > Double
										.parseDouble(split[0]))) {
							d[0][0] = Double.parseDouble(split[0]);
						}

						if (d[1][0] == null
								|| (d[0][0] != null && d[1][0] > Double
										.parseDouble(split[1]))) {
							d[1][0] = Double.parseDouble(split[1]);
						}

						if (d[2][0] == null
								|| (d[0][0] != null && d[2][0] > Double
										.parseDouble(split[2]))) {
							d[2][0] = Double.parseDouble(split[2]);
						}

						if (d[0][1] == null
								|| (d[0][0] != null && d[0][1] < Double
										.parseDouble(split[0]))) {
							d[0][1] = Double.parseDouble(split[0]);
						}

						if (d[1][1] == null
								|| (d[0][0] != null && d[1][1] < Double
										.parseDouble(split[1]))) {
							d[1][1] = Double.parseDouble(split[1]);
						}

						if (d[2][1] == null
								|| (d[0][0] != null && d[2][1] < Double
										.parseDouble(split[2]))) {
							d[2][1] = Double.parseDouble(split[2]);
						}
					}
				}

				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		int k = 1;
		for (Double[][] d : norm) {
			System.out.print(k + " run\n");
			System.out.print("d[0] " + d[0][0] + " : " + d[0][1] + "\n");
			System.out.print("d[1] " + d[1][0] + " : " + d[1][1] + "\n");
			System.out.print("d[2] " + d[2][0] + " : " + d[2][1] + "\n");
			k++;
		}
	}
	
	public static void readMaxAndMinWithRaw(String path) {
		readMaxAndMinWithRaw(path, "/SolutionSetWithGen.rtf");
		readMaxAndMinWithRaw(path, "/InitialSolutionSet.rtf");
	}
	
	public static void readMaxAndMinWithRaw(String path, String name) {
		Double[][] d = new Double[3][2];
		for (String s : names) {
			File file = new File(prefix + path + s + name);
			if(!file.exists()) {
				d = null;
				continue;
			}
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				int k = 0;
				String line = "";
				
				while ((line = reader.readLine()) != null) {
					// System.out.print(line+ "\n");
					if (line.startsWith("----")) {

						

					} else {
						String[] split = line.split(",");
						if (d[0][0] == null
								|| (d[0][0] != null && d[0][0] > Double
										.parseDouble(split[0]))) {
							d[0][0] = Double.parseDouble(split[0]);
						}

						if (d[1][0] == null
								|| (d[0][0] != null && d[1][0] > Double
										.parseDouble(split[1]))) {
							d[1][0] = Double.parseDouble(split[1]);
						}

						if (d[2][0] == null
								|| (d[0][0] != null && d[2][0] > Double
										.parseDouble(split[2]))) {
							d[2][0] = Double.parseDouble(split[2]);
						}

						if (d[0][1] == null
								|| (d[0][0] != null && d[0][1] < Double
										.parseDouble(split[0]))) {
							d[0][1] = Double.parseDouble(split[0]);
						}

						if (d[1][1] == null
								|| (d[0][0] != null && d[1][1] < Double
										.parseDouble(split[1]))) {
							d[1][1] = Double.parseDouble(split[1]);
						}

						if (d[2][1] == null
								|| (d[0][0] != null && d[2][1] < Double
										.parseDouble(split[2]))) {
							d[2][1] = Double.parseDouble(split[2]);
						}
					}
				}

				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		if(d == null) {
			return;
		}
		
		raw_norm = d;
		
		
			System.out.print("d[0] " + d[0][0] + " : " + d[0][1] + "\n");
			System.out.print("d[1] " + d[1][0] + " : " + d[1][1] + "\n");
			System.out.print("d[2] " + d[2][0] + " : " + d[2][1] + "\n");
			
		
	}

	public static void readReference(String path) {

		for (String s : names) {
			File file = new File(prefix + path + s + "/SolutionSet.rtf");
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
			
				String line = "";
				while ((line = reader.readLine()) != null) {
					// System.out.print(line+ "\n");
					if (line.startsWith("----")) {

					
						

					} else {
						String[] split = line.split(",");
						Solution sol = new Solution(3);

						// 9 means the last run of the generation
//						double v1 = (Double.parseDouble(split[0]) - norm.get(9)[0][0])
//								/ (norm.get(9)[0][1] - norm.get(9)[0][0]);
//						double v2 = (Double.parseDouble(split[1]) - norm.get(9)[1][0])
//								/ (norm.get(9)[1][1] - norm.get(9)[1][0]);
//						double v3 = (Double.parseDouble(split[2]) - norm.get(9)[2][0])
//								/ (norm.get(9)[2][1] - norm.get(9)[2][0]);
						
						double v1 = (Double.parseDouble(split[0]) - raw_norm[0][0])
						/ (raw_norm[0][1] - raw_norm[0][0]);
				double v2 = (Double.parseDouble(split[1]) - raw_norm[1][0])
						/ (raw_norm[1][1] - raw_norm[1][0]);
				double v3 = (Double.parseDouble(split[2]) - raw_norm[2][0])
						/ (raw_norm[2][1] - raw_norm[2][0]);

						sol.setObjective(0, v1);
						sol.setObjective(1, v2);
						sol.setObjective(2, v3);

						reference.add(sol);
					}
				}

				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.print("Number of points in ref " + reference.size() + "\n");

		Ranking rank = new Ranking(reference);
		reference = rank.getSubfront(0);

		System.out.print("Number of nondominated points in ref "
				+ reference.size() + "\n");
		
		referenceFront = new double[reference.size()][3];
		Iterator itr = reference.iterator();
		int i = 0;
		while (itr.hasNext()) {
			Solution s = (Solution)itr.next();
			referenceFront[i][0] = s.getObjective(0);
			referenceFront[i][1] = s.getObjective(1);
			referenceFront[i][2] = s.getObjective(2);
			i++;
		}
	}
	
	public static void readReferenceWithSeeds(String path) {

		for (String seed : seeds) {
			
		
		
		for (String s : names) {
			File file = new File(prefix + path + "/" + seed + "/" + s + "/SolutionSet.rtf");
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
			
				String line = "";
				int k = 0;
				while ((line = reader.readLine()) != null) {
					
					// System.out.print(line+ "\n");
					if (line.startsWith("----")) {

						k++;
						
						

					} else {
						
						if(k != 9) {
							continue;
						}
						
						String[] split = line.split(",");
						Solution sol = new Solution(3);

						// 9 means the last run of the generation
//						double v1 = (Double.parseDouble(split[0]) - norm.get(9)[0][0])
//								/ (norm.get(9)[0][1] - norm.get(9)[0][0]);
//						double v2 = (Double.parseDouble(split[1]) - norm.get(9)[1][0])
//								/ (norm.get(9)[1][1] - norm.get(9)[1][0]);
//						double v3 = (Double.parseDouble(split[2]) - norm.get(9)[2][0])
//								/ (norm.get(9)[2][1] - norm.get(9)[2][0]);
						
						double v1 = (Double.parseDouble(split[0]) - raw_norm[0][0])
						/ (raw_norm[0][1] - raw_norm[0][0]);
				double v2 = (Double.parseDouble(split[1]) - raw_norm[1][0])
						/ (raw_norm[1][1] - raw_norm[1][0]);
				double v3 = (Double.parseDouble(split[2]) - raw_norm[2][0])
						/ (raw_norm[2][1] - raw_norm[2][0]);

						sol.setObjective(0, v1);
						sol.setObjective(1, v2);
						sol.setObjective(2, v3);

						reference.add(sol);
					}
				}

				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		}

		System.out.print("Number of points in ref " + reference.size() + "\n");

		Ranking rank = new Ranking(reference);
		reference = rank.getSubfront(0);

		System.out.print("Number of nondominated points in ref "
				+ reference.size() + "\n");
		
		referenceFront = new double[reference.size()][3];
		Iterator itr = reference.iterator();
		int i = 0;
		while (itr.hasNext()) {
			Solution s = (Solution)itr.next();
			referenceFront[i][0] = s.getObjective(0);
			referenceFront[i][1] = s.getObjective(1);
			referenceFront[i][2] = s.getObjective(2);
			i++;
		}
	}
}
