package org.ssase.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import org.ssase.Interval;
import org.ssase.analyzer.Analyzer;
import org.ssase.debt.AdaptationDebtBroker;
import org.ssase.objective.QualityOfService;
import org.ssase.primitive.*;
public class DebtLoader {

	private String[] qosStrings = new String[] { "Response Time", "Energy" };

	private int startIndex = 121;
	// private int cap = 223;//88//340/*342*/;
	// private boolean finished = false;

	private final String prefix =  //"/home/tao/debt-init/";
	//"/Users/tao/research/projects/ssase-core/ssase/experiments-data/femosaa/results/femosaa/";
		"/Users/tao/research/projects/ssase-core/ssase/experiments-data/debt/init/";
	private Map<String, List<Double>> map = new HashMap<String, List<Double>>();

	
	public static void main(String[] args ){
		Ssascaling.activate();
		new DebtLoader().run();
	}
	
	public void run() {
		collectFromFiles("sas");

		AdaptationDebtBroker broker = Analyzer.getAdaptationDebtBroker();

		for (int i = 0; i < map.get(qosStrings[0]).size(); i++) {

			if (i == 0) {
				continue;
			}
			
			int k = 0;
			
			double unit = 0;
			double[] preQoS = new double[qosStrings.length];
			double[] currentQoS = new double[qosStrings.length];
			double[] prePrimitives = new double[broker.getPrimitive()
					.size()];
			
			for (Primitive p : broker.getPrimitive()) {

				
				prePrimitives[k] = map.get(p.getName()).get(i-1); 

				k++;
			}
			k = 0;
			for (QualityOfService s : broker.getQoSs()) {
				//System.out.print(map.get(s.getName().split("-")[s.getName().split("-").length-1]).size()+"\n");
				preQoS[k] = map.get(s.getName().split("-")[s.getName().split("-").length-1]).get(i-1);
				currentQoS[k] = map.get(s.getName().split("-")[s.getName().split("-").length-1]).get(i);
				k++;
			}
			
			unit = map.get("Execution-time").get(i-1);
			broker.preLoad(unit, preQoS, prePrimitives, currentQoS);


		}

		map.clear();
		
	}

	private void collectFromFiles(String VM_ID) {

		//Interval interval = new Interval(System.currentTimeMillis());
		// System.out.print("Start collect\n");
		// File root = new File(prefix +"adaptive/"+VM_ID+"/bak_3/");
		File root = new File(prefix + VM_ID + "/");

		List<String> qos = new ArrayList<String>();
		for (String s : qosStrings) {
			qos.add(s);
		}
		try {
			for (File file : root.listFiles()) {

				if (!file.isDirectory()) {

					if (file.getName().equals(".DS_Store")) {
						continue;
					}

					if (file.getName().equals("Executions.rtf")
							||
							// file.getName().equals("Execution-time.rtf") ||
							file.getName().equals("Dependency.rtf")
							|| file.getName().equals("Dependency-final.rtf")) {
						continue;
					}

					// System.out.print("Read " + file.getAbsolutePath() +
					// "\n");
					BufferedReader reader = new BufferedReader(new FileReader(
							file));
					String line = null;
					String name = null;
					// if ("CPU.rtf".equals(file.getName())) {
					// name = "CPU";
					// } else if ("Memory.rtf".equals(file.getName())) {
					// name = "Memory";
					// }

					name = file.getName().split("\\.")[0];
					
					 System.out.print("name " + name + "\n");
					int k = 0;
					int j = 0;
					while ((line = reader.readLine()) != null) {

						/*
						 * if (j <= 87) { j++; continue; }
						 */
						
						if("Execution-time".equals(name) && !"".equals(line.trim()) ) {
							if (!map.containsKey(name)) {
								map.put(name, new ArrayList<Double>());
							}

							map.get(name).add(Double.parseDouble(line));
						}else if (k >= startIndex && !"".equals(line.trim())) {

							if (!map.containsKey(name)) {
								map.put(name, new ArrayList<Double>());
							}

							map.get(name).add(Double.parseDouble(line));
							// System.out.print(name + " ***** "+line+"\n");

						}

						k++;
					}

					//System.out.print(name + " name " + map.get(name).size() + "\n");
					reader.close();

				} else {

					// Services
					for (File subFile : file.listFiles()) {

						String line = null;
						String name = null;

						boolean isY = false;

						name = subFile.getName().split("\\.rtf")[0];
						// System.out.print("name " + name + "\n");
						if (qos.contains(name)) {
							isY = true;
						}
						QualityOfService qosInstance = null;
						for (QualityOfService qosSubInstance : Repository
								.getQoSSet()) {
							if (name.equals(qosSubInstance.getName().split("-")[qosSubInstance
									.getName().split("-").length - 1])) {
								qosInstance = qosSubInstance;
								break;
							}
						}

						// System.out.print("Read " + subFile.getAbsolutePath()
						// + "\n");
						BufferedReader reader = new BufferedReader(
								new FileReader(subFile));
						int k = 0;
						int j = 0;
						while ((line = reader.readLine()) != null) {

							/*
							 * if (j <= 87) { j++; continue; }
							 */

							if (k >= startIndex && !"".equals(line.trim())) {

								if (isY) {
									if (!map.containsKey(name)) {
										map.put(name, new ArrayList<Double>());
									}

									double v = Double.parseDouble(line);
									v = (qosInstance.isMin() ? qosInstance
											.getConstraint() - v : v
											- qosInstance.getConstraint());

									map.get(name).add(v);
									// System.out.print(name +
								// " ***** "+v+"\n");
								} else {
									if (!map.containsKey(name)) {
										map.put(name, new ArrayList<Double>());
									}

									map.get(name).add(Double.parseDouble(line));
									// System.out.print(name +
									// " ***** "+line+"\n");
								}

							}

							k++;
						}

						reader.close();
					}
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
