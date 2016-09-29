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
		"/Users/tao/research/projects/ssase-core/ssase/experiments-data/imbalance/fifa98/";
	
	public  int counterNo = 0;
	
	private  int readFileIndex = 0;
	private int startIndex = 121;
	private  int cap = 101 - (startIndex - 121);//88//340/*342*/;

	private  boolean finished = false;
	private  List<Interval> intervals;
	private  Map<String, List<Sensor>> sensors;
	
	private  AtomicInteger counter = new AtomicInteger(0);
	private  boolean isGo = false;
	
	
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
		QualityOfService.leastNumberOfSample = 5;
		init();
		
		String testQoS = "sas-rubis_software-"+qosStrings[0];
		
		if(!ControlBus.isTriggerQoSModeling) {
			// To avoid waiting.
			logger.debug("Notice that ControlBus.isTriggerQoSModeling has been set to false!");
			return;
		}
		
		
		System.out.print("Start the pre-loading\n");
		long time = System.currentTimeMillis();
		readFileIndex = startIndex;
		for (int i = 0; i < (cap+1)/*343*/; i ++) {
			
		
			
			System.out.print("The " + i + " run \n");
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
				
			
			
			
			if(i > QualityOfService.leastNumberOfSample) {
				System.out.print("QoS: " + qos.getValue() + "\n");
				double[] xValue = new double[qos.getPrimitivesInput().size()];
				int k = 0;
				for (Primitive p : qos.getPrimitivesInput()) {
					xValue[k] =  nextMap.containsKey(p.getName())? nextMap.get(p.getName()) : 0;
					//System.out.print(p.getName() + ":" + xValue[k] +"\n");
					k++;
				}
			
				double predicted = qos.predict(xValue);
				//System.out.print(qos.getName()+"*******\n");
				double idealV = nextMap.get(qos.getName().split("-")[qos.getName().split("-").length-1]);
				
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
					//System.out.print(p.getName()+"\n");
					p.addValue(i+preRun+1);
				}
			}
			
			for (VM v : Repository.getAllVMs()) {
				for (Primitive p : v.getAllHardwarePrimitives()){
					p.addValue(i+preRun+1);
				}
				for (Primitive p : v.getAllSharedSoftwarePrimitives()){
					p.addValue(i+preRun+1);
				}
			}
			
			for (final QualityOfService qos : Repository.getQoSSet()) {
				qos.doAddValue(i+preRun+1);
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
				new Thread(new Runnable(){

					@Override
					public void run() {
						
						//QualityOfService qos = (QualityOfService)Repository.getService(name.split("=")[0]).getObjective(name.split("=")[1]);
						train(qos);
					}
					
				}).start();
				
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
		System.out.print("Number of QoS: " + Repository.getQoSSet().size() + " \n");
		System.out.print("Used time for training: " + time + " ms \n");
		System.out.print("Average used time for training: " + (time/Repository.getQoSSet().size()) + " ms \n");
		
		intervals.clear();
		sensors.clear();
		intervals = null;
		sensors = null;
		ControlPrimitive.isPreLoad = false;
		
		double total = 0D;
		BigDecimal bd = new BigDecimal(1);
		for (int i = 0; i < ideal.size(); i++) {
			total += Math.abs((ideal.get(i) - predictedList.get(i))/(1));
			mediam.add(Math.abs((ideal.get(i) - predictedList.get(i))/(1)));
			//bd = bd.multiply(new BigDecimal(Math.abs((ideal.get(i) - predictedList.get(i))/(ideal.get(i) + predictedList.get(i)))));
			//System.out.print("next: " +  Math.abs((ideal.get(i) - predictedList.get(i))/(ideal.get(i) + predictedList.get(i))) + " \n");
		}
		
		total /= ideal.size();
		//gmtotal = Math.pow(gmtotal, (1.0/ideal.size()));
		//bd.pow((int)(1.0/ideal.size()));
		Collections.sort(mediam);
		System.out.print("Average: " + total + " \n");
		System.out.print("Me: " + mediam.get(mediam.size()/2) + " \n");
		
		for(double d : mediam){
			//System.out.print("next: " +d + "\n");
		}
		
		System.gc();
	
		
		
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

}
