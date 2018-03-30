package org.ssase.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 * drift sample size 51
trace sample size 88
-online 
Drift spearmans: 0.6552941176470592 p<.001
Drift pearsons: 0.5942744859098545
CV spearmans: -0.5105315064633157 p<.001
CV pearsons: -0.40908798671336966
drift sample size 51
trace sample size 88
-online 
Drift spearmans: -0.79710407239819 p<.001
Drift pearsons: -0.805335336185247
CV spearmans: -0.007854601810430708 p=.889
CV pearsons: 0.33497670794479145


drift sample size 45
trace sample size 88
-offline 
Drift spearmans: 0.45177865612648216 p=.002
Drift pearsons: 0.40880824038991426
CV spearmans: -0.35558803846289316 p=.020
CV pearsons: -0.30655318426701283
drift sample size 45
trace sample size 88
-offline 
Drift spearmans: -0.18326745718050075 p=.238
Drift pearsons: -0.13721756266036866
CV spearmans: 0.04524321087668677 p=.689
CV pearsons: 0.32358279638428933


 * 
 * @author tao
 * 
 * 
 * 
 * larger rsd means less error cos it creates a chance of diverse samples.
 * larger spear on drift does not mean worse, it is just more sensitive to drift, the error are different things.
 *
 */
public class DatasetCorrelationAnalyzer {

	/**
	 * 
	 * rubis online
	 * Drift spearmans: 0.9278260869565216
Drift pearsons: 0.9520708899219287
CV spearmans: -0.2782815615152101
CV pearsons: 0.17413074245372098
	 * 
	 * rubis offline
	 * Drift spearmans: 0.9881422924901186
Drift pearsons: 0.9698435381043956
CV spearmans: -0.11698501511351211
CV pearsons: 0.23438936225997342

asos online
Drift spearmans: 0.6783216783216783
Drift pearsons: 0.771350521766401
CV spearmans: 0.2121212121212123
CV pearsons: 0.06749444692318847

asos offline
Drift spearmans: 0.923076923076923
Drift pearsons: 0.8805615556338793
CV spearmans: -0.5636363636363637
CV pearsons: -0.47728106713656065
	 */
	private static List<Double> drift;
	private static List<Double> data_size;
	
	private static String omitted = "";
	
	public static void main1(String[] args) {
		long n = 65270;
		System.out.print("N "+ (n * (n+1))+"\n" );
	}

	public static void main(String[] args) {
		
		boolean isOn = false;
		
//		List[] r1 =			new DatasetCorrelationAnalyzer()
//		.readAccuracy("/Users/tao/research/experiments-data/on-off/femosaa/completed_results",
//		"/Users/tao/research/experiments-data/on-off/femosaa/correlation",
//		"sas/rubis_software", isOn, true, "");
////		
////	
		List[] r2 =new DatasetCorrelationAnalyzer()
		.readAccuracy("/Users/tao/research/experiments-data/on-off/wsdream/processed/completed_results",
				"/Users/tao/research/experiments-data/on-off/wsdream/processed/correlation",
				"", isOn, false, "");
////		
////		
//		List[] r3 =new DatasetCorrelationAnalyzer()
//		.readAccuracy("/Users/tao/research/experiments-data/on-off/amazon-ec2/completed_results",
//				"/Users/tao/research/experiments-data/on-off/amazon-ec2/dataset/correlation",
//				"", isOn, false, "");
//		
//		
////		
//		List[] r11 =			new DatasetCorrelationAnalyzer()
//				.readTime("/Users/tao/research/experiments-data/on-off/femosaa/completed_results",
//						"/Users/tao/research/experiments-data/on-off/femosaa/correlation",
//						"sas/rubis_software", isOn, true, "");
////		
////	
//		List[] r22 =new DatasetCorrelationAnalyzer()
//		.readTime("/Users/tao/research/experiments-data/on-off/wsdream/processed/completed_results",
//				"/Users/tao/research/experiments-data/on-off/wsdream/processed/correlation",
//				"", isOn, false, "");
//		
//		
//		List[] r33 =new DatasetCorrelationAnalyzer()
//		.readTime("/Users/tao/research/experiments-data/on-off/amazon-ec2/completed_results",
//				"/Users/tao/research/experiments-data/on-off/amazon-ec2/dataset/correlation",
//				"", isOn, false, "");
//		
		
		// new
		// DatasetCorrelationAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/amazon-ec2/completed_results",
		// "Execution.txt", "-", false);
		
		List[] r = new List[r2.length];
		
		for (int i = 0; i < r.length;i++) {
			List list = new ArrayList();
//			list.addAll(r1[i]);
			list.addAll(r2[i]);
//			list.addAll(r3[i]);
			r[i] = list;
		}
//		
//       List[] rr = new List[r11.length];
//		
//		for (int i = 0; i < rr.length;i++) {
//			List list = new ArrayList();
//			list.addAll(r11[i]);
//			list.addAll(r22[i]);
//			list.addAll(r33[i]);
//			rr[i] = list;
//		}

		new DatasetCorrelationAnalyzer().print(r, isOn);
//		new DatasetCorrelationAnalyzer().printTime(rr, isOn);
	}

	public void print(List[] r, boolean isOn) {
		// new
		// DatasetCorrelationAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/amazon-ec2/completed_results",
		// "Execution.txt", "-", false);

		List<Double> traces = r[0];
		List<Double> cases = r[1];
		List<Double> cases_detail = r[2];
		List<Integer> cases_index = r[3];

		drift = new ArrayList<Double>();
		data_size = new ArrayList<Double>();
		
		//EWMAChartDM dm = new EWMAChartDM();
		ADWIN dm = new ADWIN();
		int num_of_drift = 0;
		int k = 0;
		
		double counter = 1.0;
		
		for (double error : cases_detail) {
			//System.out.print("error " + error+"\n");
			data_size.add(counter);
//			dm.input(error);
//			if(dm.hasChange()) {
//				num_of_drift++;
//				//System.out.print("num_of_drift " + num_of_drift+"\n");
//			}
			if(dm.setInput(error)){
				num_of_drift++;
			}
			counter++;
			k++;
			int t = 0;
			for (int i : cases_index) {
				t += i;
				if(k == t) {
					// means we are entering new case.
					//dm.resetLearning();
					dm = new ADWIN();
					drift.add((double)num_of_drift);
					num_of_drift = 0;
					counter = 1.0;
					break;
				}
			}
			
		}
		
		
	
		double[] d_size = new double[data_size.size()];
		double[] d_cases_detail = new double[cases_detail.size()]; 
		
		
		Map<Double, List<Double>> drm = new HashMap<Double, List<Double>>();
		Map<Double, List<Double>> rsd = new HashMap<Double, List<Double>>();
		
		
		List<Double> s_drm = new ArrayList<Double>();
		List<Double> s_rsd = new ArrayList<Double>();
		for (int i = 0; i < traces.size(); i++) {
			if(!rsd.containsKey(traces.get(i))) {
				rsd.put(traces.get(i), new ArrayList<Double>());
				s_rsd.add(traces.get(i));
			}
			
			rsd.get(traces.get(i)).add(cases.get(i));
		}
		
		for (int i = 0; i < drift.size(); i++) {
			if(!drm.containsKey(drift.get(i))) {
				drm.put(drift.get(i), new ArrayList<Double>());
				s_drm.add(drift.get(i));
			}
			
			drm.get(drift.get(i)).add(cases.get(i));
		}
		
		
	
		
		
		
		for (int i = 0; i < d_size.length; i++) {
			d_size[i] = data_size.get(i);
		}
		
		for (int i = 0; i < d_cases_detail.length; i++) {
			d_cases_detail[i] = cases_detail.get(i);
		}
		
		
		double[] d_traces = new double[s_rsd.size()]; 
		double[] d_drift = new double[s_drm.size()];
		double[] drift_cases = new double[s_drm.size()]; 
		double[] rsd_cases = new double[s_rsd.size()]; 
		
		for (int i = 0; i < d_drift.length; i++) {
			d_drift[i] = s_drm.get(i);
			
			double m = 0.0;
			double s = Double.MIN_VALUE;
			for (double d : drm.get(d_drift[i])) {
				m += d;
//				if(d > s) {
//					s = d;
//				}
			}
			
			m = m / drm.get(d_drift[i]).size();
			
			drift_cases[i] = m;
		}
		
		for (int i = 0; i < d_traces.length; i++) {
			d_traces[i] = s_rsd.get(i);
			
			double m = 0.0;
			for (double d : rsd.get(d_traces[i])) {
				m += d;
			}
			
			m = m / rsd.get(d_traces[i]).size();
			
			rsd_cases[i] = m;
		}
		
		
		
		
		System.out.print("drift sample size " + drift_cases.length+"\n");
		System.out.print("trace sample size " + rsd_cases.length+"\n");
		SpearmansCorrelation sc = new SpearmansCorrelation();
		PearsonsCorrelation pc = new PearsonsCorrelation();
		System.out.print((isOn? "-online " : "-offline ") + "\n");
		
		System.out.print("Drift spearmans: " + sc.correlation(drift_cases, d_drift) + "\n");
		System.out.print("Drift pearsons: " + pc.correlation(drift_cases, d_drift) + "\n");
		
		System.out.print("CV spearmans: " + sc.correlation(rsd_cases, d_traces) + "\n");
		System.out.print("CV pearsons: " + pc.correlation(rsd_cases, d_traces) + "\n");
		
		//System.out.print("Size spearmans: " + sc.correlation(d_cases_detail, d_size) + "\n");
		
		
		System.out.print("--------drift---------\n");
		double d = 0.0;
		for (int i = 0; i < drift_cases.length; i++) {
			System.out.print("("+d_drift[i]+","+drift_cases[i]+")\n");
			d += d_drift[i];
		}
		System.out.print("Overall drifit: " + (d/d_drift.length) + "\n");
		d = 0.0;
		System.out.print("--------rsd---------\n");
		for (int i = 0; i < rsd_cases.length; i++) {
			System.out.print("("+d_traces[i]+","+rsd_cases[i]+")\n");
			d += d_traces[i];
		}
		System.out.print("Overall RSD: " + (d/d_traces.length) + "\n");
	}
	
	

	public void printTime(List[] r, boolean isOn) {
		// new
		// DatasetCorrelationAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/amazon-ec2/completed_results",
		// "Execution.txt", "-", false);

		List<Double> traces = r[0];
		List<Double> cases = r[1];
		List<Double> cases_detail = r[2];

		//double[] d_cases = new double[cases.size()]; 

		double[] d_size = new double[data_size.size()];
		double[] d_cases_detail = new double[cases_detail.size()]; 
		
		
		
		Map<Double, List<Double>> drm = new HashMap<Double, List<Double>>();
		Map<Double, List<Double>> rsd = new HashMap<Double, List<Double>>();
		
		
		List<Double> s_drm = new ArrayList<Double>();
		List<Double> s_rsd = new ArrayList<Double>();
		for (int i = 0; i < traces.size(); i++) {
			if(!rsd.containsKey(traces.get(i))) {
				rsd.put(traces.get(i), new ArrayList<Double>());
				s_rsd.add(traces.get(i));
			}
			
			rsd.get(traces.get(i)).add(cases.get(i));
		}
		
		for (int i = 0; i < drift.size(); i++) {
			if(!drm.containsKey(drift.get(i))) {
				drm.put(drift.get(i), new ArrayList<Double>());
				s_drm.add(drift.get(i));
			}
			
			drm.get(drift.get(i)).add(cases.get(i));
		}
		
		
	
		
		
		
		for (int i = 0; i < d_size.length; i++) {
			d_size[i] = data_size.get(i);
		}
		
		for (int i = 0; i < d_cases_detail.length; i++) {
			d_cases_detail[i] = cases_detail.get(i);
		}
		
		
		double[] d_traces = new double[s_rsd.size()]; 
		double[] d_drift = new double[s_drm.size()];
		double[] drift_cases = new double[s_drm.size()]; 
		double[] rsd_cases = new double[s_rsd.size()]; 
		
		for (int i = 0; i < d_drift.length; i++) {
			d_drift[i] = s_drm.get(i);
			
			double m = 0.0;
			double s = Double.MIN_VALUE;
			for (double d : drm.get(d_drift[i])) {
				m += d;
//				if(d > s) {
//					s = d;
//				}
			}
			
			m = m / drm.get(d_drift[i]).size();
			
			drift_cases[i] = m;
		}
		
		for (int i = 0; i < d_traces.length; i++) {
			d_traces[i] = s_rsd.get(i);
			
			double m = 0.0;
			for (double d : rsd.get(d_traces[i])) {
				m += d;
			}
			
			m = m / rsd.get(d_traces[i]).size();
			
			rsd_cases[i] = m;
		}
		
		
		
		
		System.out.print("drift sample size " + drift_cases.length+"\n");
		System.out.print("trace sample size " + rsd_cases.length+"\n");
		SpearmansCorrelation sc = new SpearmansCorrelation();
		PearsonsCorrelation pc = new PearsonsCorrelation();
		System.out.print((isOn? "-online " : "-offline ") + "\n");
		
		System.out.print("Drift spearmans: " + sc.correlation(drift_cases, d_drift) + "\n");
		System.out.print("Drift pearsons: " + pc.correlation(drift_cases, d_drift) + "\n");
		
		System.out.print("CV spearmans: " + sc.correlation(rsd_cases, d_traces) + "\n");
		System.out.print("CV pearsons: " + pc.correlation(rsd_cases, d_traces) + "\n");
		
		//System.out.print("Size spearmans: " + sc.correlation(d_cases_detail, d_size) + "\n");
		
		
		System.out.print("--------drift---------\n");
		for (int i = 0; i < drift_cases.length; i++) {
			System.out.print("("+d_drift[i]+","+drift_cases[i]+")\n");
		}
		System.out.print("--------rsd---------\n");
		for (int i = 0; i < rsd_cases.length; i++) {
			System.out.print("("+d_traces[i]+","+rsd_cases[i]+")\n");
		}
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
			if (f2.getName().equals(".DS_Store") || f2.getName().equals("cdf") ) {
				continue;
			}
			System.out.print("Doing " + f2.getName() + "\n");
			for (File f3 : f2.listFiles()) {
				if (f3.getName().equals(".DS_Store") || f3.getName().equals("cdf") ) {
					continue;
				}
				System.out.print("Doing " + f3.getName() + "\n");
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
	
	
	private List[] readTime(String path, String trace_path,
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
			if (f2.getName().equals(".DS_Store") || f2.getName().equals("cdf") ) {
				continue;
			}
			System.out.print("Doing " + f2.getName() + "\n");
			for (File f3 : f2.listFiles()) {
				if (f3.getName().equals(".DS_Store") || f3.getName().equals("cdf") ) {
					continue;
				}
				System.out.print("Doing " + f3.getName() + "\n");
				// f4 = qos
				for (File f4 : f3.listFiles()) {
					String p = trace_path + "/" + f2.getName() + "/"
							+ f3.getName() + "/" + trace_sub;
					if (secondLevel) {
						readTime(p, isOn, f4, f3, traces, cases, cases_detail,
								cases_index, learner);
					} else {
						readOneLevelTime(p, isOn, f4, f3, traces, cases,
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
		
//		if("rubis_software".equals(f.getName())) {
//			names.add("Response Time.rtf");
//			names.add("Energy.rtf");
//		} else {
////			for (File ff4 : f.getParentFile().listFiles()) {
////				//System.out.print(ff4.getName()+"\n");
////				names.add(ff4.getName());
////			}
//				names.add("rtdata.rtf");
//				names.add("tpdata.rtf");
//				names.add("Execution.txt");
//		}
//		
		
		//System.out.print(f.getName()+"\n");
		List<Double> total = new ArrayList<Double>();
		for (File subF : f.listFiles()) {
			List<Double> list = new ArrayList<Double>();
			
			if(".DS_Store".equals(subF.getName()) || (omitted+".rtf").equals(subF.getName())) {
				continue;
			}
			
			boolean pass = true;
			for (String n : names) {
				if (n.equals(subF.getName())) {
					pass = false;
				}
			}
			//System.out.print("pass " + pass +"\n");
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

					double n = Double.parseDouble(line);
					if(Double.isNaN(n)) {
						n = 0.0;
					}
					
					list.add(n);

					mean += n;
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

			std = Math.pow((std/list.size()), 0.5);
			std = std / mean;
			//System.out.print("mean " + mean+"\n");
			//System.out.print("std " + std+"\n");
			if(mean != 0) {
				total.add(std);
			}
			
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
		
		if(f4.getName().equals(omitted)) {
			return;
		}
		
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
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			for (Double d : list) {
				double avg = d / 10.0;
				total += avg;
				
				if(avg < min) {
					min = avg;
				}
				if(avg > max) {
					max = avg;
				}
				
			}
			
			
			double m = total;
			m = m / list.size();
			total = 0.0;
			for (Double d : list) {
				double avg = (d/10-min)/(max-min);
				//total += Math.pow((m - d/10), 2);
			    cases_detail.add(avg);
				total += avg ;
				//System.out.print(" (d-min)/(max-min) " +  (d-min)/(max-min) + "\n");
			}
			
			//total = Math.pow( total/list.size(), 0.5);
			//total = total/m;
			// AVG for a case
			total = total / list.size();
			
			System.out.print("total " + total + "\n");
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
		
		if(f3.getName().equals(omitted)) {
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
//
			if (!f5.getName().endsWith("_data.rtf")) {
				continue;
			}
			
	
			
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
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for (Double d : list) {
			double avg = d / 10.0;
			total += avg;
			//cases_detail.add(avg);
			if(avg < min) {
				min = avg;
			}
			if(avg > max) {
				max = avg;
			}
			
		}
		
		double m = total;
		m = m / list.size();
		total = 0.0;
		for (Double d : list) {
			double avg = (d/10-min)/(max-min);
			//total += Math.pow((m - d/10), 2);
		    cases_detail.add(avg);
			total += avg ;
			//System.out.print(" (d-min)/(max-min) " +  (d-min)/(max-min) + "\n");
		}
		
		//total = Math.pow( total/list.size(), 0.5);
		//total = total/m;
		// AVG for a case
		total = total / list.size();
		System.out.print("total " + total + "\n");
		cases.add(total);
		cases_index.add(list.size());
		readTrace(path, f4, f3, traces);
	}
	
	
	private void readTime(String path, boolean isOn, File f4, File f3,
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

//				if (!f6.getName().endsWith("_data.rtf")) {
//					continue;
//				}
				
				
				if (f6.getName().endsWith("_data.rtf") ||  f6.getName().endsWith("_nano_time.rtf")) {
					continue;
				}

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
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			for (Double d : list) {
				double avg = d / 10.0;
				total += avg;
				
				if(avg < min) {
					min = avg;
				}
				if(avg > max) {
					max = avg;
				}
				
			}
			
			
			double m = total;
			m = m / list.size();
			total = 0.0;
			for (Double d : list) {
				double avg = (d/10-min)/(max-min);
				//total += Math.pow((m - d/10), 2);
			    cases_detail.add(avg);
				total += avg ;
				//System.out.print(" (d-min)/(max-min) " +  (d-min)/(max-min) + "\n");
			}
			
			//total = Math.pow( total/list.size(), 0.5);
			//total = total/m;
			// AVG for a case
			total = total / list.size();
			
			System.out.print("total " + total + "\n");
			cases.add(total);
			cases_index.add(list.size());
			readTrace(path, f4, f3, traces);
		}
		
	}

	private void readOneLevelTime(String path, boolean isOn, File f4, File f3,
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

//			if (!f5.getName().endsWith("_data.rtf")) {
//				continue;
//			}
			
			if (f5.getName().endsWith("_data.rtf") ||  f5.getName().endsWith("_nano_time.rtf")) {
			continue;
		}
			
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
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for (Double d : list) {
			double avg = d / 10.0;
			total += avg;
			
			if(avg < min) {
				min = avg;
			}
			if(avg > max) {
				max = avg;
			}
			
		}
		
		
		double m = total;
		m = m / list.size();
		total = 0.0;
		for (Double d : list) {
			double avg = (d/10-min)/(max-min);
			//total += Math.pow((m - d/10), 2);
		    cases_detail.add(avg);
			total += avg ;
			//System.out.print(" (d-min)/(max-min) " +  (d-min)/(max-min) + "\n");
		}
		
		//total = Math.pow( total/list.size(), 0.5);
		//total = total/m;
		// AVG for a case
		total = total / list.size();
		
		System.out.print("total " + total + "\n");
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
