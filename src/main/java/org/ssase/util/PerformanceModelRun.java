package org.ssase.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.ssase.model.onoff.OnOffModel;
import org.ssase.objective.QualityOfService;

public class PerformanceModelRun {

	private static final int runs = 10;
	public static int QoSIndex = 0;
	private String[] qosStrings = new String[] {
		"Response Time",
		"Energy"
	};
	public static String time = "";
	public static String nona_time = "";
	private  String prefix = //"/home/tao/sas-init/";
		//"/Users/tao/research/projects/ssase-core/ssase/experiments-data/imbalance/";
		"/home/tao/workload-pattern-result/";
	private static String modelType= "onlr";
	private static String subModelType= "";

	private static String esem_type= "";
	private static Class esem_class= null;
	
	public void automaticTest(CallBack call){
		for (int i = 0; i <  org.ssase.dataset.ModelingSimulator.model_types.length; i++) {
			modelType = org.ssase.dataset.ModelingSimulator.model_types[i];
			subModelType = org.ssase.dataset.ModelingSimulator.sub_model_types[i];
			OnOffModel.sub_model_types = org.ssase.dataset.ModelingSimulator.sub_model_types[i];
			esem_type = org.ssase.dataset.ModelingSimulator.esem_types[i];
			esem_class = org.ssase.dataset.ModelingSimulator.esem_classes[i];
			innerAutomaticTest(call);
		}
	}
	
	public void innerAutomaticTest(CallBack call){
		QualityOfService.setSelectedModelingType(modelType);
		File f1 = new File(prefix);
		// read-write-2
		for (File f2 : f1.listFiles()) {
			if(f2.getName().contains("completed_results") || 
					f2.getName().contains("completed_results")) {
				continue;
			}
			// increase
			for (File f3 : f2.listFiles()) {
				System.gc();
				for (int i = 0; i < runs; i++) {
					org.ssase.dataset.ModelingSimulator.time="";
					org.ssase.dataset.ModelingSimulator.nona_time="";
					time = "";
					nona_time = "";
					call.call();
					String type = OnOffModel.selected.toString() + (subModelType.equals("")? "" : "-" + subModelType);
					OnOffModel.ensemble = esem_type;
					OnOffModel.ensembleClass = esem_class;
					StepByStepHistoryLoader loader = new StepByStepHistoryLoader();
					
					String data = loader.run(prefix+"/"+f2.getName()+"/"+f3.getName()+"/", 121, 
							loader.countRow(prefix+"/"+f2.getName()+"/"+f3.getName()+"/sas/rubis_software/"+qosStrings[QoSIndex]+".rtf"), 0);
					String path = prefix+"/completed_results/"+f2.getName()+"/"+f3.getName() +"/" + qosStrings[QoSIndex] + "/"
					+ (OnOffModel.isOnline? "on" : "off") + type+ "/run"+i+"_data.rtf";
					
					String timePath = prefix+"/completed_results/"+f2.getName()+"/"+f3.getName() +"/" + qosStrings[QoSIndex] + "/"
					+ (OnOffModel.isOnline? "on" : "off") + type+ "/run"+i+"_time.rtf";
					
					
					String nanoTimePath = prefix+"/completed_results/"+f2.getName()+"/"+f3.getName() +"/" + qosStrings[QoSIndex] + "/"
					+ (OnOffModel.isOnline? "on" : "off") + type+ "/run"+i+"_nano_time.rtf";
					
					File file = new File(prefix+"/completed_results/"+f2.getName()+"/"+f3.getName()+"/"
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
	}
	
	
	
	public interface CallBack{
		
		public void call();
	}
}
