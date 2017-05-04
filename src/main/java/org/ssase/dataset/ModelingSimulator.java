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

	public static int QoSIndex = 1;
	private String[] qosStrings = new String[] {
		"Response Time",
		"Energy"
	};
	
	private  String prefix = "/Users/tao/research/projects/ssase-core/ssase/experiments-data/on-off/";
	
	public static void main(String[] a) {
		
	}
	

	public void automaticTest(CallBack call){
		
		File f1 = new File(prefix);
		// read-write-2
		for (File f2 : f1.listFiles()) {
			if(f2.getName().contains("completed_results") || 
					f2.getName().contains("completed_results")) {
				continue;
			}
			// increase

				
				for (int i = 0; i < 10; i++) {
					
					call.call();
					String type = OnOffModel.selected.toString();//+"-KNN";
				
					ModelingSimulator s = new ModelingSimulator();
					
					String data = s.run(prefix+"/"+f2.getName()+"/");
					String path = prefix+"/completed_results/"+f2.getName()+"/" + qosStrings[QoSIndex] + "/"
					+ (OnOffModel.isOnline? "on" : "off") + type+ "/run"+i+"_data.rtf";
					
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
				}
				
				
			
			
		}
	}
	
	
	public String run(String f) {
		List<QualityOfService> qoss = new ArrayList<QualityOfService>();
		List<SoftwareControlPrimitive> cps = new ArrayList<SoftwareControlPrimitive>();
		List<EnvironmentalPrimitive> eps = new ArrayList<EnvironmentalPrimitive>();

		this.readQoS(f, qoss, "Execution");
		this.readCP(f, cps, "CPU");
		this.readCP(f, cps, "Memory");
		this.readEP(f, eps, "Time");
		
		Set<Primitive> ps = new HashSet<Primitive>();
		ps.addAll(cps);
		ps.addAll(eps);

		for (QualityOfService q : qoss) {

			q.buildModel(ps, null, null);

		}
		QualityOfService.leastNumberOfSample = 2;
		int cap = 0;

		String testQoS = qosStrings[QoSIndex] ;

		String data = "";
		for (int t = 0; t < (cap + 1)/* 343 */; t++) {

			for (final QualityOfService qos : Repository.getQoSSet()) {

				if (!qos.getName().equals(testQoS)) {

					continue;
				}

			

				if (t > QualityOfService.leastNumberOfSample) {
					System.out.print("QoS: " + qos.getValue() + "\n");
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
					double predicted = qos.predict(xValue);

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
					System.out.print("next: "
							+ Math.abs((idealV - predicted)
									/ (idealV + predicted)) + "\n");
				}

			}
			
			
			for (final QualityOfService qos : Repository.getQoSSet()) {

				if (!qos.getName().equals(testQoS)) {

					continue;
				}
				
				qos.doTraining();
			}
			

			List<Double> qos_value = new ArrayList<Double>();
			List<Double> cps_value = new ArrayList<Double>();
			List<Double> eps_value = new ArrayList<Double>();
			
			this.update(f, qos_value, t, "Execution");
			this.update(f, cps_value, t, "CPU");
			this.update(f, cps_value, t, "Memory");
			this.update(f, eps_value, t, "Time");

			for (int i = 0; i < qoss.size(); i++) {
				qoss.get(i).prepareToAddValue(qos_value.get(i));
				nextMap.put(qoss.get(i).getName(), qos_value.get(i));
			}

			for (int i = 0; i < cps.size(); i++) {
				cps.get(i).prepareToAddValue(cps_value.get(i));
				nextMap.put(cps.get(i).getName(), cps_value.get(i));
			}

			for (int i = 0; i < eps.size(); i++) {
				eps.get(i).prepareToAddValue(eps_value.get(i));
				nextMap.put(eps.get(i).getName(), eps_value.get(i));
			}
		}
		
		
		
		for (int i = 0; i < ideal.size(); i++) {
			data = data + Math.abs(ideal.get(i) - predictedList.get(i))+ "\n";
		}

		
		return data;
	}

	private QualityOfService createQoS(String name, boolean isMin) {
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
	
	
	private void update(String f, List<Double> list, int t, String start){
		File file = new File(f);
		for (File f1 : file.listFiles()) {
			
			
			if(!f1.getName().startsWith(start)){
				continue;
			}
			
			try {

				int k = 0;
				String line = null;
				// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
				BufferedReader reader = new BufferedReader(new FileReader(f1));
				while ((line = reader.readLine()) != null) {

					if(t == k) {
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
		
	
		
	}
	
	private void readQoS(String f, List<QualityOfService> qoss, String start){
		File file = new File(f);
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
