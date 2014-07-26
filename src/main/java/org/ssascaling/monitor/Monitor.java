
package org.ssascaling.monitor;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.ssascaling.Interval;
import org.ssascaling.sensor.Sensor;
import org.ssascaling.util.Repository;
import org.ssascaling.util.Triple;

public class Monitor {
	
	// We currently does not care about the maximum data recorded
	private static final long MAX_DATA_RECORD = 10000;
	// For sync the timing of sample interval steps.
	//private static final Object lock = new Object();
	// The number of sample per VM * total number of VM.
	private static long numberOfSample = 0;
	private static long previousNumberOfSample = 0;
	// Inlcude all QoS, CP and EP, we can only trigger MAPE once all associated data has been senced.
	// Should equals to the number of VM if need 1 sample to trigger
	private static long totalNumberOfSenceToTriggerModeling = 3;
	private static long totalNumberOfVM = 3;
	private static long numberOfSenceToTriggerModeling = 0;
	public static final String prefix = //"/home/tao/monitor/";
		"/Users/tao/research/monitor/test/";
	
	// Called by updatePrimitivesAndQoSFromFiles in Analyzer class, so do not need
	// to consider sync as it has been ensured in ControlBus class.
	public static long getNumberOfNewSamples(){
		return totalNumberOfSenceToTriggerModeling/totalNumberOfVM;
	}
	
	public synchronized static void updateNumberOfSenceToTriggerModeling(int number){		
		 totalNumberOfSenceToTriggerModeling = number;
		
	}
	
	
	/**
	 * 
	 * @param is
	 * @return If trigger analyzer
	 */
	public synchronized static long write(DataInputStream is) {

		Triple<List<String>, Interval, String> triple = convert(is);

		previousNumberOfSample = numberOfSample;
		numberOfSample += Integer.parseInt(triple.getVal3().split("=")[1]);

		System.out.print("Total sample is : " + numberOfSample + "\n");
		//System.out.print("Previous sample is : " + previousNumberOfSample
				//+ "\n");
		final String VM_ID = triple.getVal3().split("=")[0];
		writeVM(triple.getVal2(), VM_ID);
		for (String service : triple.getVal1()) {
			// key here is VM ID + '-' + service name
			write(triple.getVal2(), service, VM_ID);
		}

		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		numberOfSenceToTriggerModeling++;
		if (numberOfSenceToTriggerModeling == totalNumberOfSenceToTriggerModeling) {
			numberOfSenceToTriggerModeling = 0;

			return numberOfSample / totalNumberOfVM;
		}
		
		

		return 0;
	}
	
	
	private static void writeVM (Interval interval, String VM_ID) {
		final Map<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
		// The newly measured values.
		final Map<String, List<Double>> values = new HashMap<String,  List<Double>>();
		BufferedWriter bw = null;
		File file = null;
		try {
			
			
			
				// For VM X
				for (Interval.ValuePair vp : interval.getVMXData()) {
					/*if (!writers.containsKey(vp.getName())) {
						if(!(file = new File(prefix + VM_ID + "/")).exists()){
							file.mkdir();
						} 
						if(!(new File(prefix + VM_ID  + "/" + vp.getName() + ".rtf")).exists()){
							writers.put(vp.getName(), new BufferedWriter(new FileWriter(
									prefix + VM_ID  + "/" + vp.getName() + ".rtf" , true)));
							
							bw = writers.get(vp.getName());
							for (int i = 0; i < previousNumberOfSample; i++) {
								// Insert 0 to the precceding values.
								bw.write(String.valueOf(0));
								bw.newLine();
								System.out.print(vp.getName() + " write\n");
							}
						} else {
							writers.put(vp.getName(), new BufferedWriter(new FileWriter(
									prefix + VM_ID +  "/" + vp.getName() + ".rtf" , true)));
							
						}
					}
					bw = writers.get(vp.getName());
					bw.write(String.valueOf(vp.getValue()));
					bw.newLine();*/
					
					if (!values.containsKey(vp.getName())) {
						values.put(vp.getName(), new ArrayList<Double>());
					}
					
					values.get(vp.getName()).add(vp.getValue());
				}
			
				for (Map.Entry<String, List<Double>> entry : values.entrySet()) {
					
					double[] value = new double[entry.getValue().size()];
					for (int k = 0; k < value.length; k++) {
						value[k] = entry.getValue().get(k);
					}
					prepareToAddValueForHardwareCP(VM_ID, entry.getKey(), value);
				}
			
			
			    for (Map.Entry<String, BufferedWriter> writer : writers.entrySet()) {
				    writer.getValue().close();
			    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private static void write(Interval interval, String service, String VM_ID) {

		final Map<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
		// The newly measured values.
		final Map<String, List<Double>> xValues = new HashMap<String, List<Double>>();
		final Map<String, List<Double>> yValues = new HashMap<String, List<Double>>();
		BufferedWriter bw = null;
		File file = null;
		try {

			// For X
			for (Interval.ValuePair vp : interval.getXData(service)) {
				/*if (!writers.containsKey(vp.getName())) {
					if (!(file = new File(prefix + VM_ID + "/" + service + "/"))
							.exists()) {
						file.mkdir();
					}

					if (!(new File(prefix + VM_ID + "/" + service + "/"
							+ vp.getName() + ".rtf")).exists()) {
						writers.put(vp.getName(), new BufferedWriter(
								new FileWriter(prefix + VM_ID + "/" + service
										+ "/" + vp.getName() + ".rtf", true)));

						bw = writers.get(vp.getName());
						for (int i = 0; i < previousNumberOfSample; i++) {
							// Insert 0 to the precceding values.
							bw.write(String.valueOf(0));
							bw.newLine();
							System.out.print(vp.getName() + " write\n");
						}
					} else {
						writers.put(vp.getName(), new BufferedWriter(
								new FileWriter(prefix + VM_ID + "/" + service
										+ "/" + vp.getName() + ".rtf", true)));

					}
				}
				bw = writers.get(vp.getName());
				bw.write(String.valueOf(vp.getValue()));
				bw.newLine();*/

				if (!xValues.containsKey(vp.getName())) {
					xValues.put(vp.getName(), new ArrayList<Double>());
				}

				xValues.get(vp.getName()).add(vp.getValue());
			}

			// For Y
			for (Interval.ValuePair vp : interval.getYData(service)) {
				/*if (!writers.containsKey(vp.getName())) {
					if (!(file = new File(prefix + VM_ID + "/" + service + "/"))
							.exists()) {
						file.mkdir();
					}
					if (!(new File(prefix + VM_ID + "/" + service + "/"
							+ vp.getName() + ".rtf")).exists()) {
						writers.put(vp.getName(), new BufferedWriter(
								new FileWriter(prefix + VM_ID + "/" + service
										+ "/" + vp.getName() + ".rtf", true)));

						bw = writers.get(vp.getName());
						for (int i = 0; i < previousNumberOfSample; i++) {
							// Insert 0 to the precceding values.
							bw.write(String.valueOf(0));
							bw.newLine();
							System.out.print(vp.getName() + " write\n");
						}
					} else {
						writers.put(vp.getName(), new BufferedWriter(
								new FileWriter(prefix + VM_ID + "/" + service
										+ "/" + vp.getName() + ".rtf", true)));

					}
				}
				bw = writers.get(vp.getName());
				bw.write(String.valueOf(vp.getValue()));
				bw.newLine();*/

				if (!yValues.containsKey(vp.getName())) {
					yValues.put(vp.getName(), new ArrayList<Double>());
				}

				yValues.get(vp.getName()).add(vp.getValue());
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

			for (Map.Entry<String, BufferedWriter> writer : writers.entrySet()) {
				writer.getValue().close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void prepareToAddValueForHardwareCP(String VM_ID, String name, double... values){
		Repository.prepareToUpdateHardwareControlPrimitive(VM_ID, name, values);
	}
	
	private static void prepareToAddValueForQoS(String VM_ID, String service, String name, double... values) {
		Repository.prepareToAddValueForQoS(VM_ID+"-"+service, name, values);
	}
	
	private static void prepareToAddValueForPrimitive(String VM_ID, String service, String name, double... values){
		Repository.prepareToAddValueForPrimitive(VM_ID+"-"+service, name, values);
	}

	// Convert low level protocol data to Interval instance
	private static Triple<List<String>, Interval, String> convert (DataInputStream is){
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

	

}

