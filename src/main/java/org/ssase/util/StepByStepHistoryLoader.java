package org.ssase.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import moa.classifiers.core.driftdetection.ADWIN;
import moa.classifiers.core.driftdetection.DDM;
import moa.options.FloatOption;
import moa.options.IntOption;
import moa.options.MultiChoiceOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.ControlBus;
import org.ssase.Interval;
import org.ssase.Service;
import org.ssase.executor.VM;
import org.ssase.monitor.Monitor;
import org.ssase.objective.QualityOfService;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.sensor.AvailabilitySensor;
import org.ssase.sensor.ReliabilitySensor;
import org.ssase.sensor.ResponseTimeSensor;
import org.ssase.sensor.Sensor;
import org.ssase.sensor.ServabilitySensor;
import org.ssase.sensor.ThroughputSensor;
import org.ssase.sensor.WorkloadSensor;
import org.ssase.sensor.control.ThreadSensor;
import org.ssase.util.Repository;
import org.ssase.util.Triple;


public class StepByStepHistoryLoader {
	
	protected static final Logger logger = LoggerFactory
	.getLogger(StepByStepHistoryLoader.class);
	
	private String[] qosStrings = new String[] {
		"Response Time",
		"Energy"
	};

	private  String[] notModeledServiceStrings = new String[] {
		
//		"edu.rice.rubis.servlets.SearchItemsByCategory",
//		"edu.rice.rubis.servlets.PutBid",
//		"edu.rice.rubis.servlets.StoreBid",
//		"edu.rice.rubis.servlets.BrowseCategories",
//		"edu.rice.rubis.servlets.RegisterUser",
//		
//		
//		"edu.rice.rubis.servlets.AboutMe",
//		"edu.rice.rubis.servlets.BrowseRegions",
//		"edu.rice.rubis.servlets.StoreBuyNow",
//		"edu.rice.rubis.servlets.BuyNow",
//		"edu.rice.rubis.servlets.BuyNowAuth",
//		"edu.rice.rubis.servlets.PutBidAuth",
//		"edu.rice.rubis.servlets.PutComment",
//		"edu.rice.rubis.servlets.PutCommentAuth",
//		"edu.rice.rubis.servlets.RegisterItem",
//		"edu.rice.rubis.servlets.SearchItemsByRegion",
//		"edu.rice.rubis.servlets.SellItemForm",
//		"edu.rice.rubis.servlets.StoreComment",
//		"edu.rice.rubis.servlets.ViewBidHistory",
//		"edu.rice.rubis.servlets.ViewItem",
//		"edu.rice.rubis.servlets.ViewUserInfo"
	};
	
	private  String prefix = //"/home/tao/sas-init/";
		"/Users/tao/research/projects/ssase-core/ssase/experiments-data/imbalance/";
	
	public  int counterNo = 0;
	
	private  int readFileIndex = 0;
	private int startIndex = 121;
	private  int cap = 101 - (startIndex - 121);//88//340/*342*/;

	private  boolean finished = false;
	private  List<Interval> intervals;
	private  Map<String, List<Sensor>> sensors;
	
	private  AtomicInteger counter = new AtomicInteger(0);
	private  boolean isGo = false;
	
	private int QoSIndex = 1;
	
	private double QoSValueRestriction = Double.MAX_VALUE;
	

	private List<String> resultsList = new ArrayList<String>();
	private List<Double> ideal = new ArrayList<Double>();
	private List<Double> predictedList = new ArrayList<Double>();
	private List<Double> mediam = new ArrayList<Double>();
	private Map<String, Double> nextMap = new HashMap<String, Double>();
	
	public static void main(String[] args) {
		new StepByStepHistoryLoader().run("",0 ,0, 0);
	}
	
	/**
	 * @param args
	 */
	public void run(String prefix,  int startIndex, int cap, int preRun) {
		this.startIndex = startIndex;
		this.cap = cap;
		this.prefix = prefix;
		//Ssascaling.main(new String[]{"0"});
		QualityOfService.leastNumberOfSample = 2;
		init();
		
		String testQoS = "sas-rubis_software-"+qosStrings[QoSIndex];
		
		if(!ControlBus.isTriggerQoSModeling) {
			// To avoid waiting.
			logger.debug("Notice that ControlBus.isTriggerQoSModeling has been set to false!");
			return;
		}
		
		QualityOfService targetQoS = null;	
		System.out.print("Start the pre-loading\n");
		long time = System.currentTimeMillis();
		readFileIndex = startIndex;
		int minus = 0;
		for (int i = 0; i < (cap+1)/*343*/; i ++) {
			
		
			
			System.out.print("The " + i + " run \n");
			if(!checkQoS("sas")) {
				readFileIndex++;
				minus++;
				System.out.print("Jump to next\n");
				continue;
			}
			simulateSendAndReceive("sas");
//			simulateSendAndReceive("jeos");
//			simulateSendAndReceive("kitty");
//			simulateSendAndReceive("miku");
		
			
			
			
			if (finished) {
				break;
			}
			
	for (final QualityOfService qos : Repository.getQoSSet()) {
				
				if(!qos.getName().equals(testQoS)){ 
					
					
					continue;
				}
				
			
				targetQoS = qos;
			
			if(i > QualityOfService.leastNumberOfSample) {
				System.out.print("QoS: " + qos.getValue() + "\n");
				double[] xValue = new double[qos.getPrimitivesInput().size()];
				int k = 0;
				//System.out.print("--------------start------------\n");
				for (Primitive p : qos.getPrimitivesInput()) {
					xValue[k] =  nextMap.containsKey(p.getName())? nextMap.get(p.getName()) : 0.0;
					//System.out.print(p.getName() + ":" + xValue[k] +"\n");
					k++;
				}
				//System.out.print("--------------end------------\n");
				double predicted = qos.predict(xValue);
				
				//System.out.print(qos.getName()+"*******\n");
				double idealV = nextMap.get(qos.getName().split("-")[qos.getName().split("-").length-1]);
				if(predicted == 0) {
					predicted = idealV;
				}
				ideal.add(idealV);
				predictedList.add(predicted);
				// actual value,predicted value
				String result = idealV+","+predicted + "\n";
				resultsList.add(result);
				logger.debug("Result: " + result);
				System.out.print("next: " +Math.abs((idealV-predicted)/(idealV+predicted)) + "\n");
			}
			
	}
			
			for (Service s : Repository.getAllServices() ) {
				for (Primitive p : s.getPrimitives()) {
					//System.out.print((i+preRun+1)+"\n");
					p.addValue(i-minus+preRun+1);
				}
			}
			
			for (VM v : Repository.getAllVMs()) {
				for (Primitive p : v.getAllHardwarePrimitives()){
					p.addValue(i-minus+preRun+1);
				}
				for (Primitive p : v.getAllSharedSoftwarePrimitives()){
					p.addValue(i-minus+preRun+1);
				}
			}
			
			for (final QualityOfService qos : Repository.getQoSSet()) {
				qos.doAddValue(i-minus+preRun+1);
			}
			
			ControlBus.getInstance().increaseCurrentSampleCount();
			Monitor.outputCurrentSample();
			
		
			
			
			
			
			for (final QualityOfService qos : Repository.getQoSSet()) {
				
				if(!qos.getName().equals(testQoS)){ 
					synchronized (counter){
						counter.incrementAndGet();
						if (counter.get() == Repository.getQoSSet().size()) {
							isGo = true;
							counter.notifyAll();
						}
					}
					
					continue;
				}
				
				
				
				System.out.print("Traning starts\n");
				
//				new Thread(new Runnable(){
//
//					@Override
//					public void run() {
//						
//						//QualityOfService qos = (QualityOfService)Repository.getService(name.split("=")[0]).getObjective(name.split("=")[1]);
//						train(qos);
//					}
//					
//				}).start();
				train(qos);
				
				
				
				System.out.print("Traning ends\n");
			}
			
			//train((QualityOfService)Repository.getService("jeos-"+Configurator.service).getObjective("Response Time"));
			
			synchronized(counter) {
					while (!isGo) {
						try {
							counter.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						
					}
					
					counter.set(0);
					isGo = false;
				
			}
			
		
			
			readFileIndex++;
			
		}
		
		time = System.currentTimeMillis() - time;
		//System.out.print("Number of QoS: " + Repository.getQoSSet().size() + " \n");
		
		intervals.clear();
		sensors.clear();
		intervals = null;
		sensors = null;
		ControlPrimitive.isPreLoad = false;
		
		double total = 0D;
		double gtotal = 1D;
		BigDecimal bd = new BigDecimal(1);
	
		double mean = 0D;
		int count = 0;
		for (int i = 0; i < ideal.size(); i++) {
			
			if(ideal.get(i) - predictedList.get(i) == 0) {
				//System.out.print(predictedList.get(i) + " Equal!!!\n");
			}
			
			System.out.print(Math.log(Math.abs(ideal.get(i) - predictedList.get(i)))+ "\n");
			//System.out.print(Math.log(Math.abs(ideal.get(i) - predictedList.get(i)))+ "\n");
			count = ideal.get(i) - predictedList.get(i) == 0? count : count + 1;
			
			double factor = 1;//ideal.get(i) + predictedList.get(i);
			total = total + Math.abs((ideal.get(i) - predictedList.get(i))/(factor));
			mean = mean + ideal.get(i);
			gtotal = ideal.get(i) - predictedList.get(i) == 0? gtotal : gtotal * Math.abs((ideal.get(i) - predictedList.get(i))/(factor));
			mediam.add(Math.abs((ideal.get(i) - predictedList.get(i))/(factor)));
			//bd = bd.multiply(new BigDecimal(Math.abs((ideal.get(i) - predictedList.get(i))/(ideal.get(i) + predictedList.get(i)))));
			//System.out.print("next: " +  Math.abs((ideal.get(i) - predictedList.get(i))/(ideal.get(i) + predictedList.get(i))) + " \n");
		}
		mean = mean/ideal.size();
		gtotal = Math.pow(gtotal, 1.0/count);
		total = total/ideal.size();
		//gmtotal = Math.pow(gmtotal, (1.0/ideal.size()));
		//bd.pow((int)(1.0/ideal.size()));
		Collections.sort(mediam);
		System.out.print("Average: " + total + " \n");
		System.out.print("G Average: " + gtotal + " \n");
		System.out.print("Me: " + mediam.get(mediam.size()/2) + " \n");
		
		HDDM_A_Test adwin = new HDDM_A_Test();
		//HDDM_W_Test adwin = new HDDM_W_Test();
		adwin.resetLearning();
		double std = 0D;
		double cd = 0D;
		for (int i = 0; i < ideal.size(); i++) {
			std = std + Math.pow(ideal.get(i) - mean, 2);
			double subCd = 0D;
			for (int j = (i-1); j > 0; j--) {
				subCd = subCd + Math.abs(ideal.get(i) - ideal.get(j));
			}
			
			if(i != 0) {
				subCd = subCd/i;
				cd = cd + subCd;
			}
			adwin.input(Math.abs(ideal.get(i) - predictedList.get(i)));
		}
		std = std/ideal.size();
		std = Math.pow(std, 0.5);
		System.out.print("STD: " + std + " \n");
		System.out.print("MEAN: " + mean + " \n");
		System.out.print("RSD: " + (std/mean) + " \n");
		//System.out.print("CD: " + (cd/ideal.size()) + " \n");
		calculateDrift(targetQoS);
		//double r = calculateRSD(targetQoS);
		//System.out.print("Avg RSD: " + ((r + (std/mean))/(targetQoS.getPrimitivesInput().size() + 1)) + " \n");
		for(double d : mediam){
			//System.out.print("next: " +d + "\n");
		}
		
		System.gc();
	
		
		
	}
	
	private double calculateRSD(QualityOfService qos){

		double result = 0D;
		for (Primitive p : qos.getPrimitivesInput()) {

			double mean = 0D;
			double std = 0D;
			
			for (int i = 0; i < p.getArray().length; i++) {
				mean = mean +p.getArray()[i]*p.getMax()/100;

			}
			
			mean = mean/p.getArray().length;
			
			for (int i = 0; i < p.getArray().length; i++) {
				std = std + Math.pow(p.getArray()[i]*p.getMax()/100 - mean, 2);

			}
			
			std = std/p.getArray().length;
			std = Math.pow(std, 0.5);
			
			//System.out.print(p.getName() + " RSD: " +  (std/mean) + " \n");
			result = result +  (std/mean) ;
		}
		
	
		return result;
	}
	
	
	private void calculateDrift(QualityOfService qos){

		double cd = 0D;
		
		for (Primitive p : qos.getPrimitivesInput()) {
		double cp = 0D;
		for (int i = 0; i < p.getArray().length; i++) {
			double subCd = 0D;
			for (int j = (i-1); j > 0; j--) {
				subCd = subCd + Math.abs(p.getArray()[i]*p.getMax()/100 - p.getArray()[j]*p.getMax()/100 );
			}
			
			if(i != 0) {
				subCd = subCd/i;
				cd = cd + subCd;
				cp = cp + subCd;
			}
			
			
		}
		//System.out.print(p.getName() + " CD: " +  cp + " \n");
		}
		
		double cp = 0D;
		for (int i = 0; i < qos.getArray().length; i++) {
			double subCd = 0D;
			for (int j = (i-1); j > 0; j--) {
				subCd = subCd + Math.abs(qos.getArray()[i]*qos.getMax()/100  - qos.getArray()[j]*qos.getMax()/100 );
			}
			
			if(i != 0) {
				subCd = subCd/i;
				cd = cd + subCd;
				cp = cp + subCd;
			}
			
			
		}
		System.out.print("qos CD: " +  cp + " \n");
		
		//System.out.print("CD: " + (cd/((qos.getPrimitivesInput().size() + 1)*qos.getArray().length)) + " \n");
	}
	
	
	private  void train (QualityOfService qos) {
		
	
		if(org.ssase.util.test.FEMOSAATester.objectives.size() == 0) {
		   qos.doTraining();
		}
		
		synchronized (counter){
			counter.incrementAndGet();
			if (counter.get() == Repository.getQoSSet().size()) {
				isGo = true;
				counter.notifyAll();
			}
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
		Sensor cpu;// = new org.closlaes.sensor.linux.CpuSensor();
		Sensor memory;// = new  org.closlaes.sensor.linux.MemorySensor();
		
		List<Sensor> list = null;
		for (String service : services) {
			list = new ArrayList<Sensor>();
//			list.add(new AvailabilitySensor());
//			list.add(new ResponseTimeSensor());
//			list.add(new ThroughputSensor());
//			list.add(new WorkloadSensor());
//			list.add(new ThreadSensor());
//			//list.add(cpu);
//			//list.add(memory);
//			list.add(new ReliabilitySensor());
//			list.add(new ServabilitySensor());
			sensors.put(service, list);
		}
		
		
	}
	
	private boolean checkQoS(String VM_ID) {

		// System.out.print("Start collect\n");
		// File root = new File(prefix +"adaptive/"+VM_ID+"/bak_3/");
		File f = new File(prefix + VM_ID + "/rubis_software/"
				+ qosStrings[QoSIndex]+".rtf");

		boolean r = true;

		try {

			String line = null;
			// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
			BufferedReader reader = new BufferedReader(new FileReader(f));
			int k = 0;
			int j = 0;
			while ((line = reader.readLine()) != null) {

				/*
				 * if (j <= 87) { j++; continue; }
				 */

				if (k == readFileIndex) {
					if (Double.parseDouble(line) > this.QoSValueRestriction) {
						System.out.print("Double.parseDouble(line) " + Double.parseDouble(line) + "\n");
						r = false;
					}
					break;
				}

				k++;
			}

			reader.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return r;
	}

    private  Interval collectFromFiles (String VM_ID) {
		
		Interval interval = new Interval(System.currentTimeMillis());
		//System.out.print("Start collect\n");
		//File root = new File(prefix +"adaptive/"+VM_ID+"/bak_3/");
		File root = new File(prefix +VM_ID+"/");
		
		
//		Map<String, List<String>> services = new HashMap<String, List<String>>();
//		Map<String, List<String>> notModeledService = new HashMap<String, List<String>>();
//	
//		
//		
//		services.put("jeos", new ArrayList<String>());
//		services.put("kitty", new ArrayList<String>());
//		services.put("miku", new ArrayList<String>());
//		
//		notModeledService.put("jeos", new ArrayList<String>());
//		notModeledService.put("kitty", new ArrayList<String>());
//		notModeledService.put("miku", new ArrayList<String>());
//		
//		
//		for (String s : notModeledServiceStrings) {
//			notModeledService.get("jeos").add(s);
//			notModeledService.get("kitty").add(s);
//			notModeledService.get("miku").add(s);
//			
//			/*services.get("jeos").add(s);
//			services.get("kitty").add(s);
//			services.get("miku").add(s);*/
//		}
//		
//		
//		services.get("jeos").add("edu.rice.rubis.servlets.SearchItemsByCategory");
//		services.get("jeos").add("edu.rice.rubis.servlets.BrowseCategories");
//		notModeledService.get("jeos").remove("edu.rice.rubis.servlets.SearchItemsByCategory");
//		notModeledService.get("jeos").remove("edu.rice.rubis.servlets.BrowseCategories");
//		
//		services.get("kitty").add("edu.rice.rubis.servlets.SearchItemsByRegion");
//		services.get("kitty").add("edu.rice.rubis.servlets.BrowseCategories");
//		notModeledService.get("kitty").remove("edu.rice.rubis.servlets.SearchItemsByRegion");
//		notModeledService.get("kitty").remove("edu.rice.rubis.servlets.BrowseCategories");
//		
//		services.get("miku").add("edu.rice.rubis.servlets.BrowseRegions");
//		services.get("miku").add("edu.rice.rubis.servlets.SearchItemsByCategory");
//		notModeledService.get("miku").remove("edu.rice.rubis.servlets.BrowseRegions");
//		notModeledService.get("miku").remove("edu.rice.rubis.servlets.SearchItemsByCategory");
		
		
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
//					if ("CPU.rtf".equals(file.getName())) {
//						name = "CPU";
//					} else if ("Memory.rtf".equals(file.getName())) {
//						name = "Memory";
//					}
					
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
//					// If the subfile is not workload and the service is not being modeled
//					if (notModeledService.get(VM_ID).contains(file.getName()) && !("Workload.rtf".equals(subFile.getName()))) {
//						continue;
//					}
//					
//					//System.out.print(file.getName()+"-"+subFile.getName()+"\n");
//					if ("Concurrency.rtf".equals(subFile.getName())) {
//						name = "Concurrency";
//						isY = false;
//					} else if ("Workload.rtf".equals(subFile.getName())) {
//						name = "Workload";
//						isY = false;
//					}else if ("Response Time.rtf".equals(subFile.getName())) {
//					
//						name = "Response Time";
//						isY = true;
//						if(!qos.contains(name)) {
//							continue;
//						}
//					}else if ("Throughput.rtf".equals(subFile.getName())) {
//						name = "Throughput";
//						isY = true;
//						if(!qos.contains(name)) {
//							continue;
//						}
//					}else if ("Availability.rtf".equals(subFile.getName())) {
//						name = "Availability";
//						isY = true;
//						if(!qos.contains(name)) {
//							continue;
//						}
//					}else if ("Reliability.rtf".equals(subFile.getName())) {
//						name = "Reliability";
//						isY = true;
//						if(!qos.contains(name)) {
//							continue;
//						}
//					} else {
//						continue;
//					}
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
//							if(name.startsWith("Workload")) {
//								System.out.print("***RT: " + Double.parseDouble(line) + "\n");
//							}
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
		nextMap.put(name, values[0]);
	}
	
	private  void prepareToAddValueForQoS(String VM_ID, String service, String name, double... values) {
		Repository.prepareToAddValueForQoS(VM_ID+"-"+service, name, values);
		nextMap.put(name, values[0]);
		//System.out.print(name+"\n");
	}
	
	private  void prepareToAddValueForPrimitive(String VM_ID, String service, String name, double... values){
		Repository.prepareToAddValueForPrimitive(VM_ID+"-"+service, name, values);
		nextMap.put(name, values[0]);
		//System.out.print(name+"\n");
	}
	
	public class HDDM_A_Test  {


	    /**
	     * Change was detected
	     */
	    protected boolean isChangeDetected;

	    /**
	     * Warning Zone: after a warning and before a change 
	     */
	    protected boolean isWarningZone;

	    /**
	     * Prediction for the next value based in previous seen values
	     */
	    protected double estimation;

	    /**
	     * Delay in detecting change
	     */
	    protected double delay;

	    /**
	     * The change detector has been initialized with the option values
	     */
	    protected boolean isInitialized;
		
		
	    public FloatOption driftConfidenceOption = new FloatOption("driftConfidence", 'd',
	            "Confidence to the drift",
	            0.001, 0, 1);
	    public FloatOption warningConfidenceOption = new FloatOption("warningConfidence", 'w',
	            "Confidence to the warning",
	            0.005, 0, 1);
	    public MultiChoiceOption oneSidedTestOption = new MultiChoiceOption(
	            "typeOfTest", 't',
	            "Monitors error increments and decrements (two-sided) or only increments (one-sided)", new String[]{
	        "One-sided", "Two-sided"}, new String[]{
	        "One-sided", "Two-sided"},
	            1);

	    protected int n_min = 0;
	    protected double c_min = 0;
	    protected int total_n = 0;
	    protected double total_c = 0;
	    protected int n_max = 0;
	    protected double c_max = 0;
	    protected double cEstimacion = 0;
	    protected int nEstimacion = 0;
	    protected double cumV = 0;
	    protected int count = 0;
	    public void input(double value) {
	        total_n++;
	        total_c += value;
	        if (n_min == 0) {
	            n_min = total_n;
	            c_min = total_c;
	        }
	        if (n_max == 0) {
	            n_max = total_n;
	            c_max = total_c;
	        }

	        double cota = Math.sqrt(1.0 / (2 * n_min) * Math.log(1.0 / driftConfidenceOption.getValue())),
	                cota1 = Math.sqrt(1.0 / (2 * total_n) * Math.log(1.0 / driftConfidenceOption.getValue()));
	        if (c_min / n_min + cota >= total_c / total_n + cota1) {
	            c_min = total_c;
	            n_min = total_n;
	        }

	        cota = Math.sqrt(1.0 / (2 * n_max) * Math.log(1.0 / driftConfidenceOption.getValue()));
	        if (c_max / n_max - cota <= total_c / total_n - cota1) {
	            c_max = total_c;
	            n_max = total_n;
	        }
	        if (meanIncr(c_min, n_min, total_c, total_n, driftConfidenceOption.getValue())) {
	            nEstimacion = total_n - n_min;
	            cEstimacion = total_c - c_min;
	            n_min = n_max = total_n = 0;
	            c_min = c_max = total_c = 0;
	            this.isChangeDetected = true;
	            this.isWarningZone = false;
	        } else if (meanIncr(c_min, n_min, total_c, total_n, warningConfidenceOption.getValue())) {
	            this.isChangeDetected = false;
	            this.isWarningZone = true;
	        } else {
	            this.isChangeDetected = false;
	            this.isWarningZone = false;
	        }
	        if (this.oneSidedTestOption.getChosenIndex() == 1 
	                && meanDecr(c_max, n_max, total_c, total_n)) {
	            nEstimacion = total_n - n_max;
	            cEstimacion = total_c - c_max;
	            n_min = n_max = total_n = 0;
	            c_min = c_max = total_c = 0;
	        }
	        updateEstimations();
	        
	        //System.out.print("isChangeDetected " + isChangeDetected + " isWarningZone " + isWarningZone + "\n");
	        if(isChangeDetected || isWarningZone ) {
	        	cumV = cumV + value;
	        	count++;
	        	System.out.print(count + ". " + value + " drift" + " cumV " + cumV + "\n");
	        }
	    }

	    private boolean meanIncr(double c_min, int n_min, double total_c, int total_n, double confianzaCambio) {
	        if (n_min == total_n) {
	            return false;
	        }
	        double m = (double) (total_n - n_min) / n_min * (1.0 / total_n);
	        double cota = Math.sqrt(m / 2 * Math.log(2.0 / confianzaCambio));
	        return total_c / total_n - c_min / n_min >= cota;
	    }

	    private boolean meanDecr(double c_max, int n_max, double total_c, int total_n) {
	        if (n_max == total_n) {
	            return false;
	        }
	        double m = (double) (total_n - n_max) / n_max * (1.0 / total_n);
	        double cota = Math.sqrt(m / 2 * Math.log(2.0 / driftConfidenceOption.getValue()));
	        return c_max / n_max - total_c / total_n >= cota;
	    }

	  
	    protected void updateEstimations() {
	        if (this.total_n >= this.nEstimacion) {
	            this.cEstimacion = this.nEstimacion = 0;
	            this.estimation = this.total_c / this.total_n;
	            this.delay = this.total_n;
	        } else {
	            this.estimation = this.cEstimacion / this.nEstimacion;
	            this.delay = this.nEstimacion;
	        }
	    }
	    
	    public void resetLearning() {
	    	  this.isChangeDetected = false;
	          this.isWarningZone = false;
	          this.estimation = 0.0;
	          this.delay = 0.0;
	          this.isInitialized = false;
	          
	          
	          
	           n_min = 0;
	           c_min = 0;
	           total_n = 0;
	           total_c = 0;
	           n_max = 0;
	           c_max = 0;
	           cEstimacion = 0;
	           nEstimacion = 0;
	    }

	}

	public class HDDM_W_Test  {
		
		 /**
	     * Change was detected
	     */
	    protected boolean isChangeDetected;

	    /**
	     * Warning Zone: after a warning and before a change 
	     */
	    protected boolean isWarningZone;

	    /**
	     * Prediction for the next value based in previous seen values
	     */
	    protected double estimation;

	    /**
	     * Delay in detecting change
	     */
	    protected double delay;

	    /**
	     * The change detector has been initialized with the option values
	     */
	    protected boolean isInitialized;

	    protected static final long serialVersionUID = 1L;
	    public FloatOption driftConfidenceOption = new FloatOption("driftConfidence", 'd',
	            "Confidence to the drift",
	            0.001, 0, 1);
	    public FloatOption warningConfidenceOption = new FloatOption("warningConfidence", 'w',
	            "Confidence to the warning",
	            0.005, 0, 1);
	    public FloatOption lambdaOption = new FloatOption("lambda",
	            'm', "Controls how much weight is given to more recent data compared to older data. Smaller values mean less weight given to recent data.",
	            0.050, 0, 1);
	    public MultiChoiceOption oneSidedTestOption = new MultiChoiceOption(
	            "typeOfTest", 't',
	            "Monitors error increments and decrements (two-sided) or only increments (one-sided)", new String[]{
	        "One-sided", "Two-sided"}, new String[]{
	        "One-sided", "Two-sided"},
	            0);
	    

	    public class SampleInfo {

	        private static final long serialVersionUID = 1L;
	        public double EWMA_Estimator;
	        public double independentBoundedConditionSum;

	        public SampleInfo() {
	            this.EWMA_Estimator = -1.0;
	        }
	    }
	    private SampleInfo sample1_IncrMonitoring,
	            sample2_IncrMonitoring,
	            sample1_DecrMonitoring,
	            sample2_DecrMonitoring,
	            total;
	    protected double incrCutPoint, decrCutPoint;
	    protected double lambda;
	    protected double warningConfidence;
	    protected double driftConfidence;
	    protected boolean oneSidedTest;
	    protected int width;

	    public HDDM_W_Test() {
	        resetLearning();
	    }

	
	    public void resetLearning() {
	    	 this.isChangeDetected = false;
	          this.isWarningZone = false;
	          this.estimation = 0.0;
	          this.delay = 0.0;
	          this.isInitialized = false;
	          
	        this.total = new SampleInfo();
	        this.sample1_DecrMonitoring = new SampleInfo();
	        this.sample1_IncrMonitoring = new SampleInfo();
	        this.sample2_DecrMonitoring = new SampleInfo();
	        this.sample2_IncrMonitoring = new SampleInfo();
	        this.incrCutPoint = Double.MAX_VALUE;
	        this.decrCutPoint = Double.MIN_VALUE;
	        this.lambda = this.lambdaOption.getValue();
	        this.driftConfidence = this.driftConfidenceOption.getValue();
	        this.warningConfidence = this.warningConfidenceOption.getValue();
	        this.oneSidedTest = this.oneSidedTestOption.getChosenIndex() == 0;
	        this.width = 0;
	        this.delay = 0;
	    }

	    public void input(boolean prediction) {
	        double value = prediction == false ? 1.0 : 0.0;
	        input(value);
	    }

	 
	    public void input(double value) {
	        double auxDecayRate = 1.0 - lambda;
	        this.width++;
	        if (total.EWMA_Estimator < 0) {
	            total.EWMA_Estimator = value;
	            total.independentBoundedConditionSum = 1;
	        } else {
	            total.EWMA_Estimator = lambda * value + auxDecayRate * total.EWMA_Estimator;
	            total.independentBoundedConditionSum = lambda * lambda + auxDecayRate * auxDecayRate * total.independentBoundedConditionSum;
	        }
	        updateIncrStatistics(value, driftConfidence);
	        if (monitorMeanIncr(value, driftConfidence)) {
	            resetLearning();
	            this.isChangeDetected = true;
	            this.isWarningZone = false;
	        } else if (monitorMeanIncr(value, warningConfidence)) {
	            this.isChangeDetected = false;
	            this.isWarningZone = true;
	        } else {
	            this.isChangeDetected = false;
	            this.isWarningZone = false;
	        }
	        updateDecrStatistics(value, driftConfidence);
	        if (!oneSidedTest && monitorMeanDecr(value, driftConfidence)) {
	            resetLearning();
	        }
	        this.estimation = this.total.EWMA_Estimator;
	        if(isChangeDetected || isWarningZone) {
	        	System.out.print(value + " drift\n");
	        }
	    }

	    protected boolean monitorMeanDecr(double valor, double confidence) {
	        return detectMeanIncrement(sample2_DecrMonitoring, sample1_DecrMonitoring, confidence);
	    }
	    
	    public boolean detectMeanIncrement(SampleInfo sample1, SampleInfo sample2, double confidence) {
	        if (sample1.EWMA_Estimator < 0 || sample2.EWMA_Estimator < 0) {
	            return false;
	        }
	        double bound = Math.sqrt((sample1.independentBoundedConditionSum + sample2.independentBoundedConditionSum) * Math.log(1 / confidence) / 2);
	        return sample2.EWMA_Estimator - sample1.EWMA_Estimator > bound;
	    }
	    
	    void updateIncrStatistics(double valor, double confidence) {
	            double auxDecay = 1.0 - lambda;
	            double bound = Math.sqrt(total.independentBoundedConditionSum * Math.log(1.0 / driftConfidence) / 2);

	            if (total.EWMA_Estimator + bound < incrCutPoint) {
	                incrCutPoint = total.EWMA_Estimator + bound;
	                sample1_IncrMonitoring.EWMA_Estimator = total.EWMA_Estimator;
	                sample1_IncrMonitoring.independentBoundedConditionSum = total.independentBoundedConditionSum;
	                sample2_IncrMonitoring = new SampleInfo();
	                this.delay = 0;
	            } else {
	                this.delay++;
	                if (sample2_IncrMonitoring.EWMA_Estimator < 0) {
	                    sample2_IncrMonitoring.EWMA_Estimator = valor;
	                    sample2_IncrMonitoring.independentBoundedConditionSum = 1;
	                } else {
	                    sample2_IncrMonitoring.EWMA_Estimator = lambda * valor + auxDecay * sample2_IncrMonitoring.EWMA_Estimator;
	                    sample2_IncrMonitoring.independentBoundedConditionSum = lambda * lambda + auxDecay * auxDecay * sample2_IncrMonitoring.independentBoundedConditionSum;
	                }
	            }
	        
	    }

	    protected boolean monitorMeanIncr(double valor, double confidence) {
	        return detectMeanIncrement(sample1_IncrMonitoring, sample2_IncrMonitoring, confidence);
	    }
	    
	    void updateDecrStatistics(double valor, double confidence) {
	            double auxDecay = 1.0 - lambda;
	            double epsilon = Math.sqrt(total.independentBoundedConditionSum * Math.log(1.0 / driftConfidence) / 2);

	            if (total.EWMA_Estimator - epsilon > decrCutPoint) {
	                decrCutPoint = total.EWMA_Estimator - epsilon;
	                sample1_DecrMonitoring.EWMA_Estimator = total.EWMA_Estimator;
	                sample1_DecrMonitoring.independentBoundedConditionSum = total.independentBoundedConditionSum;
	                sample2_DecrMonitoring = new SampleInfo();
	            } else {
	                if (sample2_DecrMonitoring.EWMA_Estimator < 0) {
	                    sample2_DecrMonitoring.EWMA_Estimator = valor;
	                    sample2_DecrMonitoring.independentBoundedConditionSum = 1;
	                } else {
	                    sample2_DecrMonitoring.EWMA_Estimator = lambda * valor + auxDecay * sample2_DecrMonitoring.EWMA_Estimator;
	                    sample2_DecrMonitoring.independentBoundedConditionSum = lambda * lambda + auxDecay * auxDecay * sample2_DecrMonitoring.independentBoundedConditionSum;
	                }
	            }
	    }

	}


}
