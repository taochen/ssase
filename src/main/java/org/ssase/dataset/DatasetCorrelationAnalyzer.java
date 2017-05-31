package org.ssase.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class DatasetCorrelationAnalyzer {

	public static void main(String[] args) {
		List[] r1 =new DatasetCorrelationAnalyzer()
				.readAccuracy("/Users/tao/research/experiments-data/on-off/femosaa/completed_results",
						"/Users/tao/research/experiments-data/on-off/femosaa",
						"sas/rubis_software", true, true, "");
		
		List[] r2 =new DatasetCorrelationAnalyzer()
		.readAccuracy("/Users/tao/research/experiments-data/on-off/femosaa/completed_results",
				"/Users/tao/research/experiments-data/on-off/femosaa",
				"sas/rubis_software", true, false, "");
		
		List[] r3 =new DatasetCorrelationAnalyzer()
		.readAccuracy("/Users/tao/research/experiments-data/on-off/femosaa/completed_results",
				"/Users/tao/research/experiments-data/on-off/femosaa",
				"sas/rubis_software", true, false, "");
		// new
		// DatasetCorrelationAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/amazon-ec2/completed_results",
		// "Execution.txt", "-", false);
		
		List[] r = new List[r1.length];
		
		for (int i = 0; i < r.length;i++) {
			List list = new ArrayList();
			list.addAll(r1[i]);
			list.addAll(r2[i]);
			list.addAll(r3[i]);
			r[i] = list;
		}

		new DatasetCorrelationAnalyzer().print(r, true);
	}

	public void print(List[] r, boolean isOn) {
		// new
		// DatasetCorrelationAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/amazon-ec2/completed_results",
		// "Execution.txt", "-", false);

		List<Double> traces = r[0];
		List<Double> cases = r[1];
		List<Double> cases_detail = r[2];
		List<Integer> cases_index = r[3];

		List<Double> drift = new ArrayList<Double>();
		
		EWMAChartDM dm = new EWMAChartDM();
		int num_of_drift = 0;
		int k = 0;
		
		for (double error : cases_detail) {
			
			
			dm.input(error);
			if(dm.hasChange()) {
				num_of_drift++;
			}
			
			k++;
			int t = 0;
			for (int i : cases_index) {
				t += i;
				if(k == t) {
					// means we are entering new case.
					dm.resetLearning();
					drift.add((double)num_of_drift);
					num_of_drift = 0;
					break;
				}
			}
			
		}
		
		double[] d_cases = new double[cases.size()]; 
		double[] d_traces = new double[traces.size()]; 
		double[] d_drift = new double[drift.size()]; 
		
		for (int i = 0; i < d_cases.length; i++) {
			d_cases[i] = cases.get(i);
		}
		
		for (int i = 0; i < d_traces.length; i++) {
			d_traces[i] = traces.get(i);
		}
		
		for (int i = 0; i < d_drift.length; i++) {
			d_drift[i] = drift.get(i);
		}
		
		
		SpearmansCorrelation sc = new SpearmansCorrelation();
		PearsonsCorrelation pc = new PearsonsCorrelation();
		System.out.print((isOn? "-online " : "-offline ") + "\n");
		
		System.out.print("Drift spearmans: " + sc.correlation(d_cases, d_drift) + "\n");
		System.out.print("Drift pearsons: " + pc.correlation(d_cases, d_drift) + "\n");
		
		System.out.print("CV spearmans: " + sc.correlation(d_cases, d_traces) + "\n");
		System.out.print("CV pearsons: " + pc.correlation(d_cases, d_traces) + "\n");
		
	}

	private List[] readAccuracy(String path, String trace_path,
			String trace_sub, boolean isOn, boolean secondLevel, String learner) {
		// mean CV
		List<Double> traces = new ArrayList<Double>();
		// Accuracy by cases
		List<Double> cases = new ArrayList<Double>();
		// Detailed accuracy for drift detection
		List<Double> cases_detail = new ArrayList<Double>();
		// Detailed index of different cases
		List<Integer> cases_index = new ArrayList<Integer>();
		File f1 = new File(path);
		for (File f2 : f1.listFiles()) {
			if (f2.getName().equals(".DS_Store")) {
				continue;
			}
			System.out.print("Doing " + f2.getName() + "\n");
			for (File f3 : f2.listFiles()) {

				// f4 = qos
				for (File f4 : f3.listFiles()) {
					String p = trace_path + "/" + f2.getName() + "/"
							+ f3.getName() + "/" + trace_sub;
					if (secondLevel) {
						read(p, isOn, f4, f3, traces, cases, cases_detail,
								cases_index, learner);
					} else {
						readOneLevel(p, isOn, f4, f3, traces, cases,
								cases_detail, cases_index, learner);
					}
				}

			}
		}

		return new List[] { traces, cases, cases_detail, cases_index };
	}

	private void printCDF(String path, Map<String, List<Double>> map, String v) {
		List<Double> list = new ArrayList<Double>();
		Set<Double> set = new HashSet<Double>();
		int i = 0;
		for (String key : map.keySet()) {
			set.addAll(map.get(key));
			// list.addAll(map.get(key));
			i += map.get(key).size();
			System.out.print("size " + i + "\n");
		}

		list.addAll(set);
		Collections.sort(list);

		for (String key : map.keySet()) {
			System.out.print(key + "\n");
			List<Double> sub = map.get(key);
			String data = "";
			double pred = -1;
			for (Double d : list) {
				if (d == pred) {
					continue;
				}
				double p = 0.0;
				for (Double d1 : sub) {

					if (d1 <= d) {
						p = p + 1.0;
					}
				}
				p = p / sub.size();
				data = data + d + " " + p + "\n";
				System.out.print("(" + d + "," + p + ")\n");
				pred = d;
			}

			try {
				File f = new File(path + "/cdf/");
				if (!f.exists()) {
					f.mkdirs();
				}
				BufferedWriter bw = new BufferedWriter(new FileWriter(path
						+ "/cdf/" + key + "-" + v.replace(" ", "") + ".txt",
						true));

				// System.out.print(data.toString() + "\n");
				bw.write(data);
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private void readTrace(String path, File f4, File f3, List<Double> cases) {
		File f = new File(path);
		List<String> names = new ArrayList<String>();
		for (File ff4 : f3.listFiles()) {
			names.add(ff4.getName());
		}
		List<Double> total = new ArrayList<Double>();
		for (File subF : f.listFiles()) {
			List<Double> list = new ArrayList<Double>();
			boolean pass = true;
			for (String n : names) {
				if (!n.startsWith(f4.getName())) {
					pass = false;
				}
			}

			if (!pass) {
				continue;
			}
			double mean = 0.0;
			try {

				String line = null;
				// System.out.print("Read " + subFile.getAbsolutePath() +
				// "\n");
				BufferedReader reader = new BufferedReader(new FileReader(subF));
				while ((line = reader.readLine()) != null) {

					list.add(Double.parseDouble(line));

					mean += Double.parseDouble(line);
				}

				reader.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mean = mean / list.size();
			double std = 0.0;
			for (Double d : list) {
				std += Math.pow((d - mean), 2);
			}

			std = Math.pow((std / mean), 0.5);
			total.add(std);
		}

		double all_cv = 0.0;
		for (Double d : total) {
			all_cv += d;
		}

		all_cv = all_cv / total.size();
		cases.add(all_cv);
	}

	private void read(String path, boolean isOn, File f4, File f3,
			List<Double> traces, List<Double> cases, List<Double> cases_detail,
			List<Integer> cases_index, String learner) {
		for (File f5 : f4.listFiles()) {
			
			
			if(!"".equals(learner)) {
				
				if (!f5.getName().equals(learner))
					continue;
				
			}
			
			if (isOn) {

				if (f5.getName().startsWith("off"))
					continue;

			} else {
				if (f5.getName().startsWith("on"))
					continue;
			}
			List<Double> list = new ArrayList<Double>();
			System.out.print("*********Doing " + f5.getName() + "\n");
			for (File f6 : f5.listFiles()) {

				if (!f6.getName().endsWith("_data.rtf")) {
					continue;
				}
				
				
//				if (f6.getName().endsWith("_data.rtf") ||  f6.getName().endsWith("_nano_time.rtf")) {
//					continue;
//				}

				// System.out.print("Doing " + f5.getName() + "\n");
				try {

					int k = 0;
					String line = null;
					// System.out.print("Read " + subFile.getAbsolutePath() +
					// "\n");
					BufferedReader reader = new BufferedReader(new FileReader(
							f6));
					while ((line = reader.readLine()) != null) {

						if (k < list.size()) {
							double value = list.get(k)
									+ Double.parseDouble(line);
							list.add(k, value);
						} else {
							list.add(Double.parseDouble(line));
						}

						k++;
					}

					reader.close();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			double total = 0.0;
			for (Double d : list) {
				double avg = d / 10.0;
				total += avg;
				cases_detail.add(avg);
			}

			// AVG for a case
			total = total / list.size();
			cases.add(total);
			cases_index.add(list.size());
			readTrace(path, f4, f3, traces);
		}
		
	}

	private void readOneLevel(String path, boolean isOn, File f4, File f3,
			List<Double> traces, List<Double> cases, List<Double> cases_detail,
			List<Integer> cases_index, String learner) {
		
		
		if(!"".equals(learner)) {
			
			if (!f4.getName().equals(learner))
				return;
			
		}
		
		if (isOn) {

			if (f4.getName().startsWith("off"))
				return;

		} else {
			if (f4.getName().startsWith("on"))
				return;
		}
			
		List<Double> list = new ArrayList<Double>();
		for (File f5 : f4.listFiles()) {

			
			// for (File f6 : f5.listFiles()) {

			if (!f5.getName().endsWith("_data.rtf")) {
				continue;
			}
			
//			if (f5.getName().endsWith("_data.rtf") ||  f5.getName().endsWith("_nano_time.rtf")) {
//			continue;
//		}
			
			System.out.print("*********Doing " + f5.getName() + "\n");
			// System.out.print("Doing " + f5.getName() + "\n");
			try {

				int k = 0;
				String line = null;
				// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
				BufferedReader reader = new BufferedReader(new FileReader(f5));
				while ((line = reader.readLine()) != null) {

					if (k < list.size()) {
						double value = list.get(k) + Double.parseDouble(line);
						list.add(k, value);
					} else {
						list.add(Double.parseDouble(line));
					}

					k++;
				}

				reader.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// }

		}

		double total = 0.0;
		for (Double d : list) {
			double avg = d / 10.0;
			total += avg;
			cases_detail.add(avg);
		}

		// AVG for a case
		total = total / list.size();
		cases.add(total);
		cases_index.add(list.size());
		readTrace(path, f4, f3, traces);
	}

	private class Vector {
		protected double value;
		protected int size;

		public Vector(double value, int size) {
			super();
			this.value = value;
			this.size = size;
		}

	}
}
