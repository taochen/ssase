package org.ssase.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ssase.model.onoff.OnOffModel;
import org.ssase.objective.QualityOfService;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.primitive.SoftwareControlPrimitive;
import org.ssase.primitive.Type;
import org.ssase.util.Repository;
import org.ssase.util.StepByStepHistoryLoader;
import org.ssase.util.PerformanceModelRun.CallBack;

public class ModelingSimulator {
	
	private List<String> resultsList = new ArrayList<String>();
	private List<Double> ideal = new ArrayList<Double>();
	private List<Double> predictedList = new ArrayList<Double>();
	private List<Double> mediam = new ArrayList<Double>();
	private Map<String, Double> nextMap = new HashMap<String, Double>();

	private static String[] model_types = new String[]{
		"onlr",
		"offlr",
		"onks",
		"offks",
		"onsvm",
		"offsvm",
		"onknn",
		"offknn",
		"onmlp",
		"offmlp",
		"onbag",
		"offbag",
		"onboost",
		"offboost",
		"onbag",
		"offbag",
		"onboost",
		"offboost",
		"onbag",
		"offbag",
		"onboost",
		"offboost"
		
		
	};
	
	private static String[] sub_model_types = new String[]{
		"",
		"",
		"",
		"",
		"",
		"",
		"",
		"",
		"",
		"",
		"LR",
		"LR",
		"LR",
		"LR",
		"KNN",
		"KNN",
		"KNN",
		"KNN",
		"KS",
		"KS",
		"KS",
		"KS"
		
		
	};
	
	private static String modelType= "onlr";
	private static String subModelType= "";
	public static int QoSIndex = 2;
	private static String[] qosStrings = new String[] {
		"Response Time",
		"Energy",
		"Execution.txt",
		"rtdata.rtf",
		"tpdata.rtf"
	};
	
	
	private String[] qosName = new String[]{
			"Execution"
			//"rtdata.rtf",
			//"tpdata.rtf"
	};
	
	
	private String[] cpName = new String[]{
			"CPU",
			"Memory"
			//"Control"
	};
	
	
	private String[] epName = new String[]{
			"Time-parsed"
			//"Workload"
	};
	
	public static String time = "";
	public static String nona_time = "";
	private static String prefix = "/home/tao/on-off/amazon-ec2/dataset";
	// "/home/tao/on-off/amazon-ec2/dataset";
	//"/Users/tao/research/projects/ssase-core/ssase/experiments-data/on-off/amazon-ec2/dataset";
	// "/Users/tao/research/projects/ssase-core/ssase/experiments-data/on-off/wsdream/processed/dataset1"
	public static void main(String[] a) {
		
		for (int i = 0; i < model_types.length; i++) {
			modelType = model_types[i];
			subModelType = sub_model_types[i];
			automaticTest();
		}
		
		
	}
	
	

	public static void automaticTest(){
		QualityOfService.setSelectedModelingType(modelType);
		File f1 = new File(prefix);
		// read-write-2
		for (File f2 : f1.listFiles()) {
			if(f2.getName().contains("completed_results") || 
					f2.getName().contains("completed_results")) {
				continue;
			}
			// increase

				
				for (int i = 0; i < 10; i++) {
					time = "";
					nona_time = "";
					Repository.clear();
					//call.call();
					String type = OnOffModel.selected.toString() + subModelType;//+"-KNN";
				
					ModelingSimulator s = new ModelingSimulator();
					
					if(f2.getName().equals(".DS_Store")) {
						continue;
					}
					
					String data = s.run(prefix+"/"+f2.getName()+"/", f2.getName());
					String path = prefix+"/completed_results/"+f2.getName()+"/" + qosStrings[QoSIndex] + "/"
					+ (OnOffModel.isOnline? "on" : "off") + type+ "/run"+i+"_data.rtf";
					
					String timePath = prefix+"/completed_results/"+f2.getName()+"/" + qosStrings[QoSIndex] + "/"
					+ (OnOffModel.isOnline? "on" : "off") + type+ "/run"+i+"_time.rtf";
					
					
					String nanoTimePath = prefix+"/completed_results/"+f2.getName()+"/" + qosStrings[QoSIndex] + "/"
					+ (OnOffModel.isOnline? "on" : "off") + type+ "/run"+i+"_nano_time.rtf";
					
					File file = new File(prefix+"/completed_results/"+f2.getName()+"/"
							+ qosStrings[QoSIndex] + "/" + (OnOffModel.isOnline? "on" : "off") + type + "/");
					if(!file.exists()) {
						file.mkdirs();
					}
					
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(path, true));

						
						//System.out.print(data.toString() + "\n");
						bw.write(data);
						bw.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(timePath, true));

						
						//System.out.print(data.toString() + "\n");
						bw.write(time);
						bw.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(nanoTimePath, true));

						
						//System.out.print(data.toString() + "\n");
						bw.write(nona_time);
						bw.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				
			
			
		}
	}
	
	
	
	public int getLength(String n){
		int cap = 0;
		try {

			
			String line = "";
			// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
			BufferedReader reader = new BufferedReader(new FileReader(prefix+ "/"+ n +"/"+qosStrings[QoSIndex]));
			while ((line = reader.readLine()) != null) {

				cap++;
				
			}

			reader.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cap;
	}
	
	public String run(String f, String n) {
		List<QualityOfService> qoss = new ArrayList<QualityOfService>();
		List<SoftwareControlPrimitive> cps = new ArrayList<SoftwareControlPrimitive>();
		List<EnvironmentalPrimitive> eps = new ArrayList<EnvironmentalPrimitive>();
		
//		List<Double> qos_value = new ArrayList<Double>();
//		List<Double> cps_value = new ArrayList<Double>();
//		List<Double> eps_value = new ArrayList<Double>();
		
		
		Map<String, QualityOfService> qosMap = new HashMap<String, QualityOfService>();
		Map<String, SoftwareControlPrimitive> cpMap = new HashMap<String, SoftwareControlPrimitive>();
		Map<String, EnvironmentalPrimitive> epMap = new HashMap<String, EnvironmentalPrimitive>();
		
		
		for (String q : qosName) {
			this.readQoS(f, qoss, q);
		}

		
	
		
		
		for (String q : cpName) {
			this.readCP(f, cps, q);
		}
		
		for (String q : epName) {
			this.readEP(f, eps, q);
		}

		
		//this.readCP(f, cps, "CPU");
		//this.readCP(f, cps, "Memory");
		//this.readEP(f, eps, "Time-parsed");
		
		
		for (SoftwareControlPrimitive q : cps) {
			cpMap.put(q.getName(), q);
		}
		
		
		for (EnvironmentalPrimitive q : eps) {
			epMap.put(q.getName(), q);
		}
		
		
		for (QualityOfService q : qoss) {
			Repository.setQoS(q);
			
			
			for (SoftwareControlPrimitive cp : cps) {
				Repository.setDirectForAnObjective(q, cp);
			}
			
			
			for (EnvironmentalPrimitive ep : eps) {
				Repository.setDirectForAnObjective(q, ep);
			}
			
			
			qosMap.put(q.getName(), q);
		}
		
		
		
		Set<Primitive> ps = new HashSet<Primitive>();
		ps.addAll(cps);
		ps.addAll(eps);
		
		for (QualityOfService q : qoss) {

			q.buildModel(ps, null, null);

		}
		QualityOfService.leastNumberOfSample = 5;
		int cap = getLength(n);

		String testQoS = qosStrings[QoSIndex] ;
		//System.out.print("Modeling for QoS cap: " + cap+ "\n");
		//System.out.print("Modeling for QoS: " + Repository.getQoSSet().size()+ "\n");
		String data = "";
		for (int t = 0; t < cap/* 343 */; t++) {

			for (final QualityOfService qos : Repository.getQoSSet()) {
				
				if (!qos.getName().equals(testQoS)) {

					continue;
				}

				//System.out.print("Modeling for QoS: " + qos.getName()+ "\n");

				if (t > QualityOfService.leastNumberOfSample) {
					System.out.print("t " + t + " QoS: " + qos.getValue() + "\n");
					double[] xValue = new double[qos.getPrimitivesInput()
							.size()];
					int k = 0;
					// System.out.print("--------------start------------\n");
					for (Primitive p : qos.getPrimitivesInput()) {
						xValue[k] = nextMap.containsKey(p.getName()) ? nextMap
								.get(p.getName()) : 0.0;
						// System.out.print(p.getName() + ":" + xValue[k]
						// +"\n");
						k++;
					}
					// System.out.print("--------------end------------\n");
					double predicted = 0.0;
					try {
					 predicted = qos.predict(xValue);
					} catch (Throwable th) {
						th.printStackTrace();
						continue;
					}

					// System.out.print(qos.getName()+"*******\n");
					double idealV = nextMap.get(qos.getName());
					if (predicted == 0) {
						predicted = idealV;
					}
					ideal.add(idealV);
					predictedList.add(predicted);
					// actual value,predicted value
					String result = "Ideal: " + idealV + ", Predicted: "
							+ predicted + "\n";
					resultsList.add(result);
					System.out.print(result);
					System.out.print("next: "
							+ Math.abs((idealV - predicted)
									/ (idealV + predicted)) + "\n");
				}

			}
			
			
			for (String q : qosName) {
				this.update(f, qosMap, t, q);
			}
			
			
			for (String q : cpName) {
				this.update(f, cpMap, t, q);
			}
			
			for (String q : epName) {
				this.update(f, epMap, t, q);
			}

			
//			this.update(f, qosMap, t, "Execution");
//			this.update(f, cpMap, t, "CPU");
//			this.update(f, cpMap, t, "Memory");
//			this.update(f, epMap, t, "Time-parsed");

			
			for (SoftwareControlPrimitive q : cps) {
				q.addValue(t+1);
			}
			
			
			for (EnvironmentalPrimitive q : eps) {
				q.addValue(t+1);
			}
			
			for (QualityOfService q : qoss) {
				q.doAddValue(t+1);
			}
			
			
			for (final QualityOfService qos : Repository.getQoSSet()) {

				if (!qos.getName().equals(testQoS)) {

					continue;
				}
				try {
				qos.doTraining();
				} catch (Throwable th) {
					th.printStackTrace();
					continue;
				}
			}
			

		
			
		}
		
		
		
		for (int i = 0; i < ideal.size(); i++) {
			data = data + Math.abs(ideal.get(i) - predictedList.get(i))+ "\n";
		}

		
		return data;
	}

	private QualityOfService createQoS(String name, boolean isMin) {
		//System.out.print("******"+name+"\n");
		QualityOfService qos = new QualityOfService(name, 0, isMin, 0.9);

		return qos;
	}

	private SoftwareControlPrimitive createCP(String name) {
		SoftwareControlPrimitive cp = new SoftwareControlPrimitive(name, name,
				false, null, null, 10, 100, 1, 0.5, 0.5, 10,
				100, false);

		return cp;
	}

	private EnvironmentalPrimitive createEP(String name) {
		EnvironmentalPrimitive ep = new EnvironmentalPrimitive(name, name, null);

		return ep;
	}
	
	
	private void update(String f, Map map, int t, String start){
		File file = new File(f);
		for (File f1 : file.listFiles()) {
			
			
			if(!f1.getName().startsWith(start)){
				continue;
			}
			//System.out.print("f1" +f1.getName()+ "\n");
			double value = -1;
			double nex_v = -1;
			try {

				int k = 0;
				String line = null;
				// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
				BufferedReader reader = new BufferedReader(new FileReader(f1));
				while ((line = reader.readLine()) != null) {

					if(t == k) {
						value = Double.parseDouble(line);
						//break;
					}
					
					if((t + 1) == k) {
						nex_v = Double.parseDouble(line);
						break;
					}
					
						 k++;
				}

				reader.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
			if(map.get(f1.getName()) instanceof QualityOfService) {
				//System.out.print("next value " + value + "\n");
				((QualityOfService)map.get(f1.getName())).prepareToAddValue(value);
			} else {
				((Primitive)map.get(f1.getName())).prepareToAddValue(value);
			}
			
			
			nextMap.put(f1.getName(), nex_v);
		}
		
	
		
	}
	
	private void readQoS(String f, List<QualityOfService> qoss, String start){
		File file = new File(f);
		System.out.print(f + "\n");
		
		for (File f1 : file.listFiles()) {
			
			
			if(!f1.getName().startsWith(start)){
				continue;
			}
			
			qoss.add(createQoS(f1.getName(),true));
		}
		
	
		
	}
	
	private void readCP(String f, List<SoftwareControlPrimitive> cps, String start){
		File file = new File(f);
		for (File f1 : file.listFiles()) {
			
			
			if(!f1.getName().startsWith(start)){
				continue;
			}
			
			cps.add(createCP(f1.getName()));
		}
		
	
		
	}
	
	private void readEP(String f, List<EnvironmentalPrimitive> eps, String start){
		File file = new File(f);
		for (File f1 : file.listFiles()) {
			
			
			if(!f1.getName().startsWith(start)){
				continue;
			}
			
			eps.add(createEP(f1.getName()));
		}
		
	
		
	}
}
