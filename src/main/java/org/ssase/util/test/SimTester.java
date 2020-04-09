package org.ssase.util.test;


/**
 * for FROAS work
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.femosaa.core.EAConfigure;
import org.femosaa.util.Logger;
import org.ssase.ControlBus;
import org.ssase.Interval;
import org.ssase.Service;
import org.ssase.analyzer.Analyzer;
import org.ssase.executor.Executor;
import org.ssase.executor.VM;
import org.ssase.monitor.Monitor;
import org.ssase.network.Receiver;
import org.ssase.objective.Objective;
import org.ssase.objective.QualityOfService;
import org.ssase.objective.optimization.femosaa.moead.*;
import org.ssase.objective.optimization.femosaa.nsgaii.*;
import org.ssase.objective.optimization.femosaa.ibea.*;
import org.ssase.objective.optimization.femosaa.variability.fm.FeatureModel;
import org.ssase.planner.Planner;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.region.Region;
import org.ssase.requirement.froas.RequirementPrimitive;
import org.ssase.requirement.froas.RequirementProposition;
import org.ssase.sensor.Sensor;
import org.ssase.util.Repository;
import org.ssase.util.Ssascaling;
import org.ssase.util.Triple;

public class SimTester {

	public static final List<Objective> objectives = new ArrayList<Objective>();
	
	private static final String prefix = //"/home/tao/sas-init/";
		//"/Users/tao/research/projects/ssase-core/ssase/experiments-data/femosaa/results/femosaa/";
		"/Users/tao/research/experiments-data/fuzzy-requirement/sim/";
	public static int counterNo = 0;
	
	private static int readFileIndex = 0;
	
	private static int startIndex = 121;//121;
	private static int cap = 223;//88//340/*342*/;
	private static boolean finished = false;
	private static List<Interval> intervals;
	private static Map<String, List<Sensor>> sensors;
	private static final boolean testFueatureModelOnly = false;
	private static String[] qosStrings = new String[] {
		"Response Time",
		"Energy"
	};
	
	public static void main(String[] a){
		//new SimTester().run();
		//readSingleRunFile();
		String login = "\\' and password=' or 1=1";//"\\";
		System.out.print(login.replace("\\'", "'").replace("'", "''"));
		//System.out.print(login.replace("'", "''"));
	}
	
	private static void readSingleRunFile() {
		HashSet<String> set = new HashSet<String> ();
		
		//File file = new File("/Users/tao/research/monitor/ws-soa/sas/sas/p1/IBEA/SolutionSet.rtf");
		
		File file = new File("/Users/tao/research/monitor/ws-soa/sas/sas/p5/IBEA/E-30/SolutionSet.rtf");
		
		try {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while((line = reader.readLine()) != null) {
			
			if(line.startsWith("-----")) {
				continue;
			}
			double r =  Double.parseDouble(line.split(",")[0])*1000.0;
			
			String d = "(" + line.split(",")[1] + "," + String.valueOf(r) +")";
			
			set.add(d);
		}
		}catch (Exception e) {
			
		}
		
		for (String s : set) {
			System.out.print(s + "\n");
		}
	}
	
	public void run(){
		System.out.print(System.getProperty("os.name"));


		Ssascaling.activate();
		
		EAConfigure.getInstance().setupRUBiSSimConfiguration();
		//EAConfigure.getInstance().setupWSConfiguration();
		long samples = 122;
		//Ssascaling.main(new String[]{"0"});
		//QualityOfService.leastNumberOfSample = cap+1;//cap - startIndex;
		init();
		
	
		
		
		System.out.print("Start the pre-loading\n");
		long time = System.currentTimeMillis();
		readFileIndex = startIndex;
		
		List<ControlPrimitive> list = new ArrayList<ControlPrimitive>();
		
		ControlPrimitive Connection = null;
		ControlPrimitive maxThread = null;
		ControlPrimitive minSpareThreads = null;
		ControlPrimitive Compression = null;
		ControlPrimitive cacheMode = null;
		Set<ControlPrimitive> set = new HashSet<ControlPrimitive>();
		for(Service s : Repository.getAllServices()){
			
			for (Primitive p : s.getPossiblePrimitives()) {
//				
//				if(p instanceof ControlPrimitive && !list.contains(p)){
//					if(p.getName().equals("Connection")) {
//						Connection = (ControlPrimitive)p;
//					} else if(p.getName().equals("maxThread")) {
//						maxThread = (ControlPrimitive)p;
//					} else if(p.getName().equals("minSpareThreads")) {
//						minSpareThreads = (ControlPrimitive)p;
//					} else if(p.getName().equals("Compression")) {
//						Compression = (ControlPrimitive)p;
//					} else if(p.getName().equals("cacheMode")) {
//						cacheMode = (ControlPrimitive)p;
//					}else {
//						list.add((ControlPrimitive)p);
//					}
//				}
				
				if(p instanceof ControlPrimitive){
					set.add((ControlPrimitive)p);
				}
			}
			
		}
		
		
//		list.add(0, Connection);
//		list.add(1, minSpareThreads);
//		list.add(2, maxThread);
//		list.add(3, Compression);
//		list.add(cacheMode);

		
//		// ===============
//		list.addAll(set);
//		FeatureModel fm = new FeatureModel(list);
//		
//		List<FeatureModel> models = new ArrayList<FeatureModel>();
//		models.add(fm);
//		
//		FeatureModel.readFile(models);
//		
//		if(testFueatureModelOnly) return;
//		
//		for(Objective obj : Repository.getAllObjectives()) {
//			Repository.setSortedControlPrimitives(obj, fm);
//		}
//		
//		// ===============
		//Executor.execute(Planner.optimize(Repository.getAllObjectives().iterator().next(), UUID.randomUUID().toString()));
		//Planner.optimize(Repository.getAllObjectives().iterator().next(), UUID.randomUUID().toString());
	
	
		
		for (int i = 0; i < 100/*100 343*/; i ++) {
			
			//if(i != 98) {
			simulateSendAndReceive("sas");
			
//			if(i < 2) {
//				for (Service s : Repository.getAllServices() ) {
//					for (Primitive p : s.getPrimitives()) {
//						p.addValue(samples);
//					}
//				}
//				
//				for (VM v : Repository.getAllVMs()) {
//					for (Primitive p : v.getAllHardwarePrimitives()){
//						p.addValue(samples);
//					}
//					for (Primitive p : v.getAllSharedSoftwarePrimitives()){
//						p.addValue(samples);
//					}
//				}
//				
//				for (final QualityOfService qos : Repository.getQoSSet()) {
//					qos.doAddValue(samples);
//				}
//				samples++;
//				continue;
//			}
//			
			

			if (finished) {
				break;
			}
			
			
			

			
			
			//List<Objective> objectivesToBeOptimized = Analyzer.doAnalysis(samples);
			doAddValues(samples);
			doResetValues();
			
			
			samples++;
			
			ControlBus.getInstance().increaseCurrentSampleCount();
			Monitor.outputCurrentSample();
			//readFileIndex++;
			//continue;
			//}
		
			double dmin = 20;
			double dmax = 30;//30;
			for (int k = 0; k < 10;k++) {
			
			NSGAII("p1","",-1.0);
			NSGAII("p2","E-30",dmax);
			NSGAII("p3","E-30",dmax);
			NSGAII("p4","E-30",dmax);
			NSGAII("p5","E-30",dmax);
			NSGAII("p6","E-30",dmax);
			NSGAII("p7","E-30",dmax);
			
			IBEA("p1","",-1.0);
			IBEA("p2","E-30",dmax);
			IBEA("p3","E-30",dmax);
			IBEA("p4","E-30",dmax);
			IBEA("p5","E-30",dmax);
			IBEA("p6","E-30",dmax);
			IBEA("p7","E-30",dmax);
			}
			
//			System.out.print("=============== NSGAIIwithKAndDRegion ===============\n");
/*			NSGAII("p1","",-1.0);
			NSGAII("p2","E-10",dmin);
			NSGAII("p2","E-20",dmax);
			NSGAII("p3","E-10",dmin);
			NSGAII("p3","E-20",dmax);
			NSGAII("p4","E-10",dmin);
			NSGAII("p4","E-20",dmax);
			NSGAII("p5","E-10",dmin);
			NSGAII("p5","E-20",dmax);
			NSGAII("p6","E-10",dmin);
			NSGAII("p6","E-20",dmax);
			NSGAII("p7","E-10",dmin);
			NSGAII("p7","E-20",dmax);
			
			IBEA("p1","",-1.0);
			IBEA("p2","E-10",dmin);
			IBEA("p2","E-20",dmax);
			IBEA("p3","E-10",dmin);
			IBEA("p3","E-20",dmax);
			IBEA("p4","E-10",dmin);
			IBEA("p4","E-20",dmax);
			IBEA("p5","E-10",dmin);
			IBEA("p5","E-20",dmax);
			IBEA("p6","E-10",dmin);
			IBEA("p6","E-20",dmax);
			IBEA("p7","E-10",dmin);
			IBEA("p7","E-20",dmax);
			

*/		
			
			//****************
			readFileIndex++;
			//****************
		}

	}
	
	private static void NSGAII(String p, String e, double d){
		
		
		for (QualityOfService qos : Repository.getQoSSet()){
			if(qos.getName().equals("sas-rubis_software-Energy")) {
				System.out.print(p + " Change fuzzy proposition!\n");
				fitFuzzyReq(p, qos, d);
			}		
		}
		String o = Logger.prefix;
		Logger.prefix = o + "sas/"+p+"/NSGAII/"+e + "/";
		File f = null;
		if(!(f = new File(Logger.prefix )).exists()){
			f.mkdirs();
		} 
		
		NSGAIIwithKAndDRegion moea = new NSGAIIwithKAndDRegion();
		moea.addObjectives(Repository.getAllObjectives());		
		double[] r = getFitness(moea.optimize());
		Logger.prefix = o;
		logData("sas/"+p+"/NSGAII/"+e, "Response Time", String.valueOf(r[0]));
		logData("sas/"+p+"/NSGAII/"+e, "Energy", String.valueOf(r[1]));
	}
	
	
	private static void IBEA(String p, String e, double d){
		
		
		for (QualityOfService qos : Repository.getQoSSet()){
			if(qos.getName().equals("sas-rubis_software-Energy")) {
				fitFuzzyReq(p, qos, d);
			}		
		}
		
		String o = Logger.prefix;
		Logger.prefix = o + "sas/"+p+"/IBEA/"+e + "/";
		File f = null;
		if(!(f = new File(Logger.prefix )).exists()){
			f.mkdirs();
		} 
		
		IBEAwithKAndDRegion moea = new IBEAwithKAndDRegion();
		moea.addObjectives(Repository.getAllObjectives());		
		double[] r = getFitness(moea.optimize());
		Logger.prefix = o;
		logData("sas/"+p+"/IBEA/"+e, "Response Time", String.valueOf(r[0]));
		logData("sas/"+p+"/IBEA/"+e, "Energy", String.valueOf(r[1]));
	}
	
	
	
	private static void fitFuzzyReq(String req, QualityOfService qos, double d){
		if("p1".equals(req)) {
			Repository.setRequirementProposition(qos.getName(), 
					new RequirementProposition(RequirementPrimitive.AS_GOOD_AS_POSSIBLE));
		} else if("p2".equals(req)) {
			Repository.setRequirementProposition(qos.getName(), 
					new RequirementProposition(d, RequirementPrimitive.BETTER_THAN_d));
		} else if("p3".equals(req)) {
			Repository.setRequirementProposition(qos.getName(), 
					new RequirementProposition(d, RequirementPrimitive.AS_GOOD_AS_POSSIBLE_TO_d));
		} else if("p4".equals(req)) {
			Repository.setRequirementProposition(qos.getName(), 
					new RequirementProposition(d, RequirementPrimitive.AS_CLOSE_AS_POSSIBLE_TO_d));
		} else if("p5".equals(req)) {
			Repository.setRequirementProposition(qos.getName(), 
					new RequirementProposition(d, RequirementPrimitive.AS_FAR_AS_POSSIBLE_FROM_d));
		} else if("p6".equals(req)) {
			Repository.setRequirementProposition(qos.getName(), 
					new RequirementProposition(d, RequirementPrimitive.AS_GOOD_AS_POSSIBLE, 
							RequirementPrimitive.BETTER_THAN_d));
		} else if("p7".equals(req)) {
			Repository.setRequirementProposition(qos.getName(), 
					new RequirementProposition(d, RequirementPrimitive.AS_GOOD_AS_POSSIBLE, 
							RequirementPrimitive.AS_GOOD_AS_POSSIBLE_TO_d));
		}
	}
	
	private static double[] getFitness(LinkedHashMap<ControlPrimitive, Double> result){
		double[] r = new double[2];
		for (Objective obj : Repository.getAllObjectives()) {
			double[] xValue = new double[obj.getPrimitivesInput().size()];
			for (int i = 0; i < xValue.length; i++) {
				
				if (obj.getPrimitivesInput().get(i) instanceof ControlPrimitive) {
					xValue[i] = result.get(obj.getPrimitivesInput().get(i));
				} else {
					xValue[i] = obj.getPrimitivesInput().get(i).getProvision();
				}
				
				 
			}
			
			
			double adapt = obj.predict(xValue);
			
			if(obj.getName().equals("sas-rubis_software-Response Time")) {
				r[0] = adapt;
			} else {
				r[1] = adapt;
			}
			
			
			

		
			
			//System.out.print(obj.getName() + " current value: " + obj.getCurrentPrediction() + " - after adaptation: " + adapt + "\n");
			
		
		}
		
		//Seeder.posteriorObjetive(new double[]{1/r[0], r[1]});
		
		return r;
	}
	
	 private static synchronized void logData(String VM_ID, String qos, String data){
			
			if(VM_ID == null) VM_ID = "sas";
			if(QualityOfService.isDelegate()) VM_ID = VM_ID + "/" + Region.selected;
			File file = null;
			if(!(file = new File(Logger.prefix
					+ VM_ID + "/")).exists()){
				file.mkdirs();
			} 
			
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(Logger.prefix
						+ VM_ID + "/" + qos +  ".rtf", true));

				
				//System.out.print(data.toString() + "\n");
				bw.write(data + "\n");
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	
    private static void doAddValues(long samples){
		
		for (Service s : Repository.getAllServices() ) {
			for (Primitive p : s.getPrimitives()) {
				p.addValue(samples);
			}
		}
		
		for (VM v : Repository.getAllVMs()) {
			for (Primitive p : v.getAllHardwarePrimitives()){
				p.addValue(samples);
			}
			for (Primitive p : v.getAllSharedSoftwarePrimitives()){
				p.addValue(samples);
			}
		}
		
		for (final QualityOfService qos : Repository.getQoSSet()) {
			qos.doAddValue(samples);
		}
			
	}
    
	private static void doResetValues(){
		
		for (Service s : Repository.getAllServices() ) {
			for (Primitive p : s.getPrimitives()) {
				p.resetValues();
			}
		}
		
		for (VM v : Repository.getAllVMs()) {
			for (Primitive p : v.getAllHardwarePrimitives()){
				p.resetValues();
			}
			for (Primitive p : v.getAllSharedSoftwarePrimitives()){
				p.resetValues();
			}
		}
		
		for (final QualityOfService qos : Repository.getQoSSet()) {
			qos.resetValues();
		}
			
	}
	
	
	public  void simulateSendAndReceive(String VM_ID){
		intervals.add(collectFromFiles(VM_ID));
		
		if (finished) {
			return;
		}
		
		Monitor.write(1);
		
		StringBuilder data = new StringBuilder(VM_ID+"=1\n");
		for (Map.Entry<String, List<Sensor>> entry : sensors.entrySet()) {
			convert(intervals, entry.getKey(), data, VM_ID+"=1\n");
		}

		intervals.clear();
		
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data.toString().getBytes()));
		write(in);
		
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	   public  void init() {
			
	    	intervals = new LinkedList<Interval>();
			
			sensors = new HashMap<String, List<Sensor>> ();
			
			// This is temp implementation
			String[] services = new String[] {
				
					"rubis_software"
			};
			// Remember to uncomment this.
			Sensor cpu = new org.ssase.sensor.linux.CpuSensor();
			Sensor memory = new  org.ssase.sensor.linux.MemorySensor();
			
			List<Sensor> list = null;
			for (String service : services) {
				list = new ArrayList<Sensor>();
				sensors.put(service, list);
			}
			
			
		}
	   
		
	    private  Interval collectFromFiles (String VM_ID) {
			
			Interval interval = new Interval(System.currentTimeMillis());
			//System.out.print("Start collect\n");
			//File root = new File(prefix +"adaptive/"+VM_ID+"/bak_3/");
			File root = new File(prefix +VM_ID+"/");
			
			
//			Map<String, List<String>> services = new HashMap<String, List<String>>();
//			Map<String, List<String>> notModeledService = new HashMap<String, List<String>>();
	//	
//			
//			
//			services.put("jeos", new ArrayList<String>());
//			services.put("kitty", new ArrayList<String>());
//			services.put("miku", new ArrayList<String>());
//			
//			notModeledService.put("jeos", new ArrayList<String>());
//			notModeledService.put("kitty", new ArrayList<String>());
//			notModeledService.put("miku", new ArrayList<String>());
//			
//			
//			for (String s : notModeledServiceStrings) {
//				notModeledService.get("jeos").add(s);
//				notModeledService.get("kitty").add(s);
//				notModeledService.get("miku").add(s);
//				
//				/*services.get("jeos").add(s);
//				services.get("kitty").add(s);
//				services.get("miku").add(s);*/
//			}
//			
//			
//			services.get("jeos").add("edu.rice.rubis.servlets.SearchItemsByCategory");
//			services.get("jeos").add("edu.rice.rubis.servlets.BrowseCategories");
//			notModeledService.get("jeos").remove("edu.rice.rubis.servlets.SearchItemsByCategory");
//			notModeledService.get("jeos").remove("edu.rice.rubis.servlets.BrowseCategories");
//			
//			services.get("kitty").add("edu.rice.rubis.servlets.SearchItemsByRegion");
//			services.get("kitty").add("edu.rice.rubis.servlets.BrowseCategories");
//			notModeledService.get("kitty").remove("edu.rice.rubis.servlets.SearchItemsByRegion");
//			notModeledService.get("kitty").remove("edu.rice.rubis.servlets.BrowseCategories");
//			
//			services.get("miku").add("edu.rice.rubis.servlets.BrowseRegions");
//			services.get("miku").add("edu.rice.rubis.servlets.SearchItemsByCategory");
//			notModeledService.get("miku").remove("edu.rice.rubis.servlets.BrowseRegions");
//			notModeledService.get("miku").remove("edu.rice.rubis.servlets.SearchItemsByCategory");
			
			
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
					
					if (file.getName().equals("Executions.rtf") || 
							file.getName().equals("Execution-time.rtf")  || 
							file.getName().equals("Dependency.rtf") ||
							file.getName().equals("Dependency-final.rtf")) {
						continue;
					}
				
					
					//System.out.print("Read " + file.getAbsolutePath() + "\n");
						BufferedReader reader = new BufferedReader(new FileReader(file));
						String line = null;
						String name = null;
//						if ("CPU.rtf".equals(file.getName())) {
//							name = "CPU";
//						} else if ("Memory.rtf".equals(file.getName())) {
//							name = "Memory";
//						}
						
						name = file.getName().split("\\.")[0];
						//System.out.print("name " + name + "\n");
						int k = 0;
						int j = 0;
						while((line = reader.readLine()) != null) {
							

							/*if (j <= 87) {
								j++;
								continue;
							}*/
							
							
							if (k == readFileIndex) {
							
								
								interval.setVMX(new String[]{ name}, new double[]{Double.parseDouble(line)} );
							
								//System.out.print(name + " ***** "+line+"\n");
								
								break;
							}
							
							k++;
						}
						
						if (readFileIndex>k || readFileIndex==cap) {
							finished = true;
							reader.close();
							return null;
						}
						
						reader.close();
				
				} else {
					
					// Services
					for (File subFile : file.listFiles()) {
						
						String line = null;
						String name = null;
						
						boolean isY = false;
						
						name = subFile.getName().split("\\.rtf")[0];
						//System.out.print("name " + name + "\n");
						if(qos.contains(name)) {
							isY = true;
						}
//						// If the subfile is not workload and the service is not being modeled
//						if (notModeledService.get(VM_ID).contains(file.getName()) && !("Workload.rtf".equals(subFile.getName()))) {
//							continue;
//						}
//						
//						//System.out.print(file.getName()+"-"+subFile.getName()+"\n");
//						if ("Concurrency.rtf".equals(subFile.getName())) {
//							name = "Concurrency";
//							isY = false;
//						} else if ("Workload.rtf".equals(subFile.getName())) {
//							name = "Workload";
//							isY = false;
//						}else if ("Response Time.rtf".equals(subFile.getName())) {
//						
//							name = "Response Time";
//							isY = true;
//							if(!qos.contains(name)) {
//								continue;
//							}
//						}else if ("Throughput.rtf".equals(subFile.getName())) {
//							name = "Throughput";
//							isY = true;
//							if(!qos.contains(name)) {
//								continue;
//							}
//						}else if ("Availability.rtf".equals(subFile.getName())) {
//							name = "Availability";
//							isY = true;
//							if(!qos.contains(name)) {
//								continue;
//							}
//						}else if ("Reliability.rtf".equals(subFile.getName())) {
//							name = "Reliability";
//							isY = true;
//							if(!qos.contains(name)) {
//								continue;
//							}
//						} else {
//							continue;
//						}
						//System.out.print("Read " + subFile.getAbsolutePath() + "\n");
						BufferedReader reader = new BufferedReader(new FileReader(subFile));
						int k = 0;
						int j = 0;
						while((line = reader.readLine()) != null) {

							/*if (j <= 87) {
								j++;
								continue;
							}*/
							
							if (k == readFileIndex) {
//								if(name.startsWith("Workload")) {
//									System.out.print("***RT: " + Double.parseDouble(line) + "\n");
//								}
								if (isY) {
									
								  interval.setY(file.getName(), new String[]{ name}, new double[]{Double.parseDouble(line)} );
										
								} else {
								  interval.setX(file.getName(), new String[]{ name}, new double[]{Double.parseDouble(line)} );
								}
								break;
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
			
			return interval;
		}
		

		// Convert Interval instance to low level protocol data. . = service,  1 = X, 2 = Y, 3 = VM X
		private  void convert (List<Interval> intervals, String service,StringBuilder builder, String init){		
			boolean isRecordVMX = false;
			if (builder.toString().equals(init)) {
				isRecordVMX = true;
			}
			
			builder.append(".\n");
			builder.append(service + "\n");
			
			for (Interval interval : intervals) {
				if (interval.getXData(service) != null) {
					builder.append("1\n");
					for (Interval.ValuePair vp : interval.getXData(service)) {			
						builder.append(vp.getName() + "=" + String.valueOf(vp.getValue()) + "\n");
					}
				}
				
				if (interval.getYData(service) != null) {
					builder.append("2\n");
					for (Interval.ValuePair vp : interval.getYData(service)) {
						builder.append(vp.getName() + "=" + String.valueOf(vp.getValue()) + "\n");
					}
				}
				// Only record this under the first service
				if (isRecordVMX) {
					if (interval.getVMXData() != null) {
						builder.append("3\n");
						for (Interval.ValuePair vp : interval.getVMXData()) {
							builder.append(vp.getName() + "=" + String.valueOf(vp.getValue()) + "\n");
						}
					}
				}
			}
			
			isRecordVMX = false;
			
		}
		
		private  Triple<List<String>, Interval, String> convert (DataInputStream is){
			Interval interval = new Interval(System.currentTimeMillis());
			List<String> services = new ArrayList<String>();
			int type = 0;
			boolean isService = false;
			String currentService = null;
			String line = null;
			String VM_ID = null;
			try {
				VM_ID = is.readLine();
				while ((line = is.readLine()) != null) {
					//System.out.print(line + "\n");
					if (line.equals(".")) {
						isService = true;
						continue;
					} else if (line.equals("1")) {
						type = 1;
						continue;
					} else if (line.equals("2")) {
						// It is Y
						type = 2;
						continue;
					} else if (line.equals("3")) {
						// It is VM X
						type = 3;
						continue;
					}
					
					

					
					
					if (isService) {
						services.add(line);
						currentService = line;
						isService = false;
						continue;
					}
					
					// e.g., workload=34.5
					String[] args = line.split("="); 
					
					switch (type) {
					case 1:
						interval.setX(currentService, new String[]{args[0]}, new double[]{Double.parseDouble(args[1]) });
						break;
					case 2:
						interval.setY(currentService, new String[]{args[0]}, new double[]{Double.parseDouble(args[1]) });
						break;
					case 3:
						interval.setVMX(new String[]{args[0]}, new double[]{Double.parseDouble(args[1]) });
						break;
					}
					
					
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return new Triple<List<String>, Interval, String>(services, interval, VM_ID);
		}
		
		public  boolean write(DataInputStream is){
			
			Triple<List<String>, Interval, String> triple = convert(is);
			final String VM_ID = triple.getVal3().split("=")[0];
			writeVM(triple.getVal2(), VM_ID);
			for (String service : triple.getVal1()) {
				// key here is VM ID + '-' + service name
				write(triple.getVal2(), service, VM_ID);
			}
			

		
			
			return false;
		}
		
		
		private  void writeVM (Interval interval, String VM_ID) {
			//final Map<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
			// The newly measured values.
			final Map<String, List<Double>> values = new HashMap<String,  List<Double>>();
			BufferedWriter bw = null;
			File file = null;
			try {
				
				if ( interval.getVMXData() != null) {
				
				for (Interval.ValuePair vp : interval.getVMXData()) {
					
					
					if (!values.containsKey(vp.getName())) {
						values.put(vp.getName(), new ArrayList<Double>());
					}
					
					values.get(vp.getName()).add(vp.getValue());
				}
				}
				
					for (Map.Entry<String, List<Double>> entry : values.entrySet()) {
						
						double[] value = new double[entry.getValue().size()];
						for (int k = 0; k < value.length; k++) {
							value[k] = entry.getValue().get(k);
						}
						prepareToAddValueForHardwareCP(VM_ID, entry.getKey(), value);
					}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		

		private  void write(Interval interval, String service, String VM_ID) {

			//final Map<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
			// The newly measured values.
			final Map<String, List<Double>> xValues = new HashMap<String, List<Double>>();
			final Map<String, List<Double>> yValues = new HashMap<String, List<Double>>();
		
			try {

				if ( interval.getXData(service) != null) {
				for (Interval.ValuePair vp : interval.getXData(service)) {
					

					if (!xValues.containsKey(vp.getName())) {
						xValues.put(vp.getName(), new ArrayList<Double>());
					}

					xValues.get(vp.getName()).add(vp.getValue());
				}
				
				}

				// For Y
				if ( interval.getYData(service) != null) {
				for (Interval.ValuePair vp : interval.getYData(service)) {
					

					if (!yValues.containsKey(vp.getName())) {
						yValues.put(vp.getName(), new ArrayList<Double>());
					}

					yValues.get(vp.getName()).add(vp.getValue());
				}

				}
				for (Map.Entry<String, List<Double>> entry : xValues.entrySet()) {

					double[] value = new double[entry.getValue().size()];
					for (int k = 0; k < value.length; k++) {
						value[k] = entry.getValue().get(k);
					}
					prepareToAddValueForPrimitive(VM_ID, service, entry.getKey(), value);
				}

				for (Map.Entry<String, List<Double>> entry : yValues.entrySet()) {

					double[] value = new double[entry.getValue().size()];
					for (int k = 0; k < value.length; k++) {
						value[k] = entry.getValue().get(k);
					}
					prepareToAddValueForQoS(VM_ID, service, entry.getKey(), value);
				}

				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		private  void prepareToAddValueForHardwareCP(String VM_ID, String name, double... values){
			Repository.prepareToUpdateHardwareControlPrimitive(VM_ID, name, values);
		}
		
		private  void prepareToAddValueForQoS(String VM_ID, String service, String name, double... values) {
			Repository.prepareToAddValueForQoS(VM_ID+"-"+service, name, values);
		}
		
		private  void prepareToAddValueForPrimitive(String VM_ID, String service, String name, double... values){
			Repository.prepareToAddValueForPrimitive(VM_ID+"-"+service, name, values);
		}

	
}
