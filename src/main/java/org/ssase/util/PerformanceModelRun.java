package org.ssase.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.ssase.model.onoff.OnOffModel;

public class PerformanceModelRun {

	private static final int runs = 10;
	public static int QoSIndex = 1;
	private String[] qosStrings = new String[] {
		"Response Time",
		"Energy"
	};
	private  String prefix = //"/home/tao/sas-init/";
		//"/Users/tao/research/projects/ssase-core/ssase/experiments-data/imbalance/";
		"/home/tao/workload-pattern-result/";
	
	public void automaticTest(CallBack call){
		
		File f1 = new File(prefix);
		// read-write-2
		for (File f2 : f1.listFiles()) {
			if(f2.getName().contains("completed_results") || 
					f2.getName().contains("completed_results")) {
				continue;
			}
			// increase
			for (File f3 : f2.listFiles()) {
				
				for (int i = 0; i < runs; i++) {
					
					call.call();
					String type = OnOffModel.selected.toString()+"-LR";
					StepByStepHistoryLoader loader = new StepByStepHistoryLoader();
					
					String data = loader.run(prefix+"/"+f2.getName()+"/"+f3.getName()+"/", 121, 
							loader.countRow(prefix+"/"+f2.getName()+"/"+f3.getName()+"/sas/rubis_software/"+qosStrings[QoSIndex]+".rtf"), 0);
					String path = prefix+"/completed_results/"+f2.getName()+"/"+f3.getName() +"/" + qosStrings[QoSIndex] + "/"
					+ (OnOffModel.isOnline? "on" : "off") + type+ "/run"+i+"_data.rtf";
					
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
				}
				
				
			}
			
		}
	}
	
	
	
	public interface CallBack{
		
		public void call();
	}
}
