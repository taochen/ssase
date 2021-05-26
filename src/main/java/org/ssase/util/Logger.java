package org.ssase.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ssase.Interval;
import org.ssase.Interval.ValuePair;
import org.ssase.objective.QualityOfService;
import org.ssase.region.Region;

public class Logger { 

	public static String prefix = //"/home/"+System.getProperty("user.name")+"/monitor/";
		//"/Users/"+System.getProperty("user.name")+"/research/monitor/sas-soa/final/";
		"/Users/"+System.getProperty("user.name")+"/research/monitor/ws-soa/sas/";
	private static long executionCount = 0;
	 
	
	public static void logMonitoredData (List<ValuePair> list,  Map<String, BufferedWriter> writers,  Map<String, 
			List<Double>> values, String path, long previousNumberOfSample ) throws Exception{
		BufferedWriter bw = null;
		File file = null;
		for (Interval.ValuePair vp : list) {
			if (!writers.containsKey(vp.getName())) {
				if(!(file = new File(prefix + path + "/")).exists()){
					file.mkdir();
				} 
				if(!(new File(prefix + path  + "/" + vp.getName() + ".rtf")).exists()){
					writers.put(vp.getName(), new BufferedWriter(new FileWriter(
							prefix + path  + "/" + vp.getName() + ".rtf" , true)));
					
					bw = writers.get(vp.getName());
					for (int i = 0; i < previousNumberOfSample; i++) {
						// Insert 0 to the precceding values.
						bw.write(String.valueOf(0));
						bw.newLine();
						//System.out.print(vp.getName() + " write\n");
					}
				} else {
					writers.put(vp.getName(), new BufferedWriter(new FileWriter(
							prefix + path +  "/" + vp.getName() + ".rtf" , true)));
					
				}
			}
			bw = writers.get(vp.getName());
			bw.write(String.valueOf(vp.getValue()));
			bw.newLine();
			
			if (!values.containsKey(vp.getName())) {
				values.put(vp.getName(), new ArrayList<Double>());
			}
			
			values.get(vp.getName()).add(vp.getValue());
		}
	}
	
	/**
	 * Synchronization should have been ensured in the MAPE loop.
	 * @param VM_ID
	 * @param data
	 */
	public static synchronized void logExecutionData(String VM_ID, StringBuilder data, long executionCount ){
		File file = null;
		if(!(file = new File(prefix
				+ VM_ID + "/")).exists()){
			file.mkdir();
		} 
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(prefix
					+ VM_ID + "/Executions.rtf", true));

			if (executionCount > 0) {
				bw.newLine();
				//System.out.print("-----------" + executionCount + "-----------" + "\n");
				bw.write("-----------" + executionCount + "-----------");
				bw.newLine();
			}
			//System.out.print(data.toString() + "\n");
			bw.write(data.toString());
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void logOptimizationTime(String VM_ID, String data){
		
		if(VM_ID == null) VM_ID = "sas";
		if(QualityOfService.isDelegate()) VM_ID = VM_ID + "/" + Region.selected;
		File file = null;
		if(!(file = new File(prefix
				+ VM_ID + "/")).exists()){
			file.mkdirs();
		} 
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(prefix
					+ VM_ID + "/Execution-time.rtf", true));

			
			//System.out.print(data.toString() + "\n");
			bw.write(data + "\n");
			bw.close();
		} catch (Exception e) {
			System.out.print("exeution time is : " + data + "\n");
			e.printStackTrace();
		}
	}
	
    public static synchronized void logDependencyEnforcement(String VM_ID, String data){
		
		if(VM_ID == null) VM_ID = "sas";
		if(QualityOfService.isDelegate()) VM_ID = VM_ID + "/" + Region.selected;
		File file = null;
		if(!(file = new File(prefix
				+ VM_ID + "/")).exists()){
			file.mkdirs();
		} 
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(prefix
					+ VM_ID + "/Dependency.rtf", true));

			
			//System.out.print(data.toString() + "\n");
			bw.write(data + "\n");
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
  public static synchronized void logDependencyEnforcementFinal(String VM_ID, String data){
		
		if(VM_ID == null) VM_ID = "sas";
		
		File file = null;
		if(!(file = new File(prefix
				+ VM_ID + "/")).exists()){
			file.mkdir();
		} 
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(prefix
					+ VM_ID + "/Dependency-final.rtf", true));

			
			//System.out.print(data.toString() + "\n");
			bw.write(data + "\n");
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    
	public static synchronized long getExecutionCount(){
		executionCount++;
		return executionCount;
	}
	
}
