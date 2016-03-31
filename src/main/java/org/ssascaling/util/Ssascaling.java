package org.ssascaling.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.ssascaling.Service;
import org.ssascaling.actuator.MaxThreadActuator;
import org.ssascaling.actuator.linux.CPUCapActuator;
import org.ssascaling.actuator.linux.MemoryActuator;
import org.ssascaling.executor.Executor;
import org.ssascaling.executor.VM;
import org.ssascaling.network.Receiver;
import org.ssascaling.objective.Cost;
import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.EnvironmentalPrimitive;
import org.ssascaling.primitive.HardwareControlPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.primitive.SoftwareControlPrimitive;
import org.ssascaling.primitive.Type;
import org.ssascaling.qos.QualityOfService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Ssascaling {
	
	public static void main (String[] arg) {
		
		if (arg == null || arg.length == 0) {
			activate();
			return;
		}
		
		if ("0".equals(arg[0])) {
			activate ();
		} else if ("1".equals(arg[0])) {
			testExecution();
		} else if ("2".equals(arg[0])) {
			activateSensors("192.168.0.101");
			activateSensors("192.168.0.102");
			activateSensors("192.168.0.103");
		} else {
			testExecution();
		}
		
	}
	
	public static boolean activateSensors(String dest) {

		HttpURLConnection conn = null;
		// Initialization section:
		// Try to open a socket on port 25
		// Try to open input and output streams
		try {
			URL url = new URL("http://" + dest + ":8080"
					+ "/rubis_servlets/servlet/ConfigServlet?data=1");

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			System.out.print(dest + " : " + conn.getResponseCode() + "\n");

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: hostname");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}

		return true;
	}


	
	public static void testExecution(){
		ControlPrimitive threadOfService1 = new SoftwareControlPrimitive(null, "jeos-edu.rice.rubis.servlets.SearchItemsByCategory", false, null, null, 0,0,1, 0.7,0.1,2, 80);
		threadOfService1.setType(Type.maxThread);
		//threadOfService1.setAlias("test.service1");
		threadOfService1.setActuator(new MaxThreadActuator(null));
		ControlPrimitive threadOfService2 = new SoftwareControlPrimitive(null, "jeos-edu.rice.rubis.servlets.BrowseCategories", false, null, null, 0,0,1, 0.7,0.1,2, 80);
		threadOfService2.setType(Type.maxThread);
		//threadOfService2.setAlias("test.service2");
		threadOfService2.setActuator(new MaxThreadActuator(null));
		
		HardwareControlPrimitive jeosCPU = new HardwareControlPrimitive(null, "jeos", true, null, null, 0,0,1, 0.9,0.1,5, 80);
		jeosCPU.setType(Type.CPU);
		//jeosCPU.setAlias("jeos");
		((ControlPrimitive)jeosCPU).setProvision(30);
		jeosCPU.setActuator(new CPUCapActuator());
		jeosCPU.setHardware(true);
		HardwareControlPrimitive jeosMemory = new HardwareControlPrimitive(null, "jeos", true, null, null, 0,0,5, 0.9,0.1,100, 300);
		jeosMemory.setType(Type.Memory);
		//jeosMemory.setAlias("jeos");
		((ControlPrimitive)jeosMemory).setProvision(128);
		jeosMemory.setActuator(new MemoryActuator());
		jeosMemory.setHardware(true);
		
		HardwareControlPrimitive kittyCPU = new HardwareControlPrimitive(null, "kitty", true, null, null, 0,0,1, 0.9,0.1,5, 80);
		kittyCPU.setType(Type.CPU);
		//kittyCPU.setAlias("kitty");
		((ControlPrimitive)kittyCPU).setProvision(30);
		kittyCPU.setActuator(new CPUCapActuator());
		kittyCPU.setHardware(true);
		HardwareControlPrimitive kittyMemory = new HardwareControlPrimitive(null, "kitty", true, null, null, 0,0,5, 0.9,0.1,100, 300);
		kittyMemory.setType(Type.Memory);
		//kittyMemory.setAlias("kitty");
		((ControlPrimitive)kittyMemory).setProvision(128);
		kittyMemory.setActuator(new MemoryActuator());
		kittyMemory.setHardware(true);
		
		HardwareControlPrimitive mikuCPU = new HardwareControlPrimitive(null, "miku", true, null, null, 0,0,1, 0.9,0.1,5, 80);
		mikuCPU.setType(Type.CPU);
		//mikuCPU.setAlias("miku");
		((ControlPrimitive)mikuCPU).setProvision(30);
		mikuCPU.setActuator(new CPUCapActuator());
		mikuCPU.setHardware(true);
		HardwareControlPrimitive mikuMemory = new HardwareControlPrimitive(null, "miku", true, null, null, 0,0,5, 0.9,0.1,100, 300);
		mikuMemory.setType(Type.Memory);
		//mikuMemory.setAlias("miku");
		((ControlPrimitive)mikuMemory).setProvision(128);
		mikuMemory.setActuator(new MemoryActuator());
		mikuMemory.setHardware(true);
		
		Executor.init(new HardwareControlPrimitive[]{jeosCPU, jeosMemory, kittyCPU, kittyMemory, mikuCPU, mikuMemory});
		Executor.print();
		
		System.out.print("-------------- \n");
		LinkedHashMap<ControlPrimitive, Double> decisions = new LinkedHashMap<ControlPrimitive, Double>();
		
		decisions.put(jeosCPU, 120.5);
		decisions.put(jeosMemory, 334.6);
		
		decisions.put(kittyCPU, 79.9);
		decisions.put(kittyMemory, 185.4);
		
		decisions.put(mikuCPU, 21.4);
		decisions.put(mikuMemory, 274.9);
		
		
		decisions.put(threadOfService1, 54.0);
		decisions.put(threadOfService2, 27.0);
		//decisions.put(kittyCPU, 79.9);
		//decisions.put(kittyMemory, 185.4);
		long time = System.currentTimeMillis();
		Executor.execute(decisions);
		Executor.print();
		
		System.out.print("Time -------------- " +(System.currentTimeMillis() - time)+"\n");	
		System.out.print("-------------- \n");
		
	}
	
	public static void activate () {
		
		try {
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbFactory.newDocumentBuilder();
			Document doc = builder.parse(Ssascaling.class.getClassLoader().getResourceAsStream("dom0.xml"));
			
			doc.getDocumentElement().normalize();
			
			NodeList vmNodes = doc.getElementsByTagName("vm");
			
			for (int i = 0; i < vmNodes.getLength(); i++) {
				
				Node node = vmNodes.item(i);
				String vmName = node.getAttributes().getNamedItem("id").getNodeValue();
			
				NodeList insideVmNodes = node.getChildNodes();
				// Order from hardware CP then to software CP.
				List<Double> hardwarePrices = new ArrayList<Double>();
				List<Double> sharedSoftwarePrices = new ArrayList<Double>();
				for (int j = 0; j < insideVmNodes.getLength(); j++) {
					
					
					
					
					if ("hardwareControlPrimitive".equals(insideVmNodes.item(j).getNodeName())){
						NodeList hardwareCPs = insideVmNodes.item(j).getChildNodes();
						List<HardwareControlPrimitive> list = new ArrayList<HardwareControlPrimitive>();
						for (int k = 0; k < hardwareCPs.getLength(); k++) {
							
							if (Node.ELEMENT_NODE == hardwareCPs.item(k).getNodeType()) {
								HardwareControlPrimitive cp = new HardwareControlPrimitive(
										hardwareCPs.item(k).getAttributes().getNamedItem("name").getNodeValue(), 
										vmName, 
										true, 
										Type.getTypeByString(hardwareCPs.item(k).getAttributes().getNamedItem("name").getNodeValue()),
										Type.getActuatorByString(hardwareCPs.item(k).getAttributes().getNamedItem("name").getNodeValue()),
										Double.parseDouble(hardwareCPs.item(k).getAttributes().getNamedItem("provision").getNodeValue()), 
										Double.parseDouble(hardwareCPs.item(k).getAttributes().getNamedItem("constraint").getNodeValue()), 
										Integer.parseInt(hardwareCPs.item(k).getAttributes().getNamedItem("differences").getNodeValue()), 
										Double.parseDouble(hardwareCPs.item(k).getAttributes().getNamedItem("pre_to_max").getNodeValue()),
										Double.parseDouble(hardwareCPs.item(k).getAttributes().getNamedItem("pre_of_max").getNodeValue()),
										Double.parseDouble(hardwareCPs.item(k).getAttributes().getNamedItem("min").getNodeValue()),
										Double.parseDouble(hardwareCPs.item(k).getAttributes().getNamedItem("max").getNodeValue()));
							
								hardwarePrices.add(Double.parseDouble(hardwareCPs.item(k).getAttributes().getNamedItem("price_per_unit").getNodeValue()));
								list.add(cp);
								
							//System.out.print(hardwareCPs.item(k).getAttributes().getNamedItem("price_per_unit").getNodeValue()+ "\n");
						    }
						}
						
						Repository.setVM(vmName, new VM(vmName, list.toArray(new HardwareControlPrimitive[list.size()]) ));
					}
					
					if ("softwareControlPrimitive".equals(insideVmNodes.item(j).getNodeName())){
						NodeList softwareCPs = insideVmNodes.item(j).getChildNodes();
						List<SoftwareControlPrimitive> list = new ArrayList<SoftwareControlPrimitive>();
						for (int k = 0; k < softwareCPs.getLength(); k++) {
							
							if (Node.ELEMENT_NODE == softwareCPs.item(k).getNodeType()) {
								SoftwareControlPrimitive cp = new SoftwareControlPrimitive(
										softwareCPs.item(k).getAttributes().getNamedItem("name").getNodeValue(), 
										vmName, 
										true, 
										Type.getTypeByString(softwareCPs.item(k).getAttributes().getNamedItem("name").getNodeValue()),
										Type.getActuatorByString(softwareCPs.item(k).getAttributes().getNamedItem("name").getNodeValue()),
										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("provision").getNodeValue()), 
										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("constraint").getNodeValue()), 
										Integer.parseInt(softwareCPs.item(k).getAttributes().getNamedItem("differences").getNodeValue()), 
										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("pre_to_max").getNodeValue()),
										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("pre_of_max").getNodeValue()),
										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("min").getNodeValue()),
										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("max").getNodeValue()));
							
								sharedSoftwarePrices.add(Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("price_per_unit").getNodeValue()));
								list.add(cp);
								
							//System.out.print(hardwareCPs.item(k).getAttributes().getNamedItem("price_per_unit").getNodeValue()+ "\n");
						    }
						}
						
						Repository.getVM(vmName).setSharedSoftwareControlPrimitives(list);
					}
					
					
					
					
					
	                if ("service".equals(insideVmNodes.item(j).getNodeName())){
	                	
	                	
	                	
	                	NodeList insideService = insideVmNodes.item(j).getChildNodes();
	                	String serviceName = insideVmNodes.item(j).getAttributes().getNamedItem("name").getNodeValue();
	                	
	                	Map<String, Objective> objectives =new HashMap<String, Objective>(); /*The objective here is with a null Model instance*/
	    				Map<String, Primitive> primitives = new HashMap<String, Primitive>();
	    				
	    				
	    				
	    				List<SoftwareControlPrimitive> softs = new ArrayList<SoftwareControlPrimitive>();
	    				List<Double> softwarePrices = new ArrayList<Double>();
	    				
	    				for (int l = 0; l < insideService.getLength(); l++) {
	    					//System.out.print(insideService.item(l).getNodeName()+ "\n");
	    					if ("softwareControlPrimitive".equals(insideService.item(l).getNodeName())){
	    						NodeList softwareCPs = insideService.item(l).getChildNodes();
	    						
	    						for (int k = 0; k < softwareCPs.getLength(); k++) {
	    							
	    							if (Node.ELEMENT_NODE == softwareCPs.item(k).getNodeType()) {
	    								SoftwareControlPrimitive cp = new SoftwareControlPrimitive(
	    										softwareCPs.item(k).getAttributes().getNamedItem("name").getNodeValue(), 
	    										vmName+"-"+serviceName, 
	    										false, 
	    										Type.getTypeByString(softwareCPs.item(k).getAttributes().getNamedItem("name").getNodeValue()),
	    										Type.getActuatorByString(softwareCPs.item(k).getAttributes().getNamedItem("name").getNodeValue()),
	    										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("provision").getNodeValue()), 
	    										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("constraint").getNodeValue()), 
	    										Integer.parseInt(softwareCPs.item(k).getAttributes().getNamedItem("differences").getNodeValue()), 
	    										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("pre_to_max").getNodeValue()),
	    										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("pre_of_max").getNodeValue()),
	    										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("min").getNodeValue()),
	    										Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("max").getNodeValue()));
	    							 
	    								softwarePrices.add(Double.parseDouble(softwareCPs.item(k).getAttributes().getNamedItem("price_per_unit").getNodeValue()));
	    								primitives.put(softwareCPs.item(k).getAttributes().getNamedItem("name").getNodeValue(), cp);
	    								softs.add(cp);
	    								System.out.print(cp.getName() + " min: " + cp.getValueVector()[0] + ", max: " + cp.getValueVector()[cp.getValueVector().length-1] + "\n");
	    							 }
	    						}
	    						
	    					}
	    					
	    					
	    					
	    					if ("environmentalPrimitive".equals(insideService.item(l).getNodeName())){
	    						NodeList eps = insideService.item(l).getChildNodes();
	    						
	    						for (int k = 0; k < eps.getLength(); k++) {
	    							
	    							if (Node.ELEMENT_NODE == eps.item(k).getNodeType()) {
	    								String name = eps.item(k).getAttributes().getNamedItem("name").getNodeValue();
	    								if(eps.item(k).getAttributes().getNamedItem("alias").getNodeValue() != null) {
	    									name  = name + "-" + eps.item(k).getAttributes().getNamedItem("alias").getNodeValue();
	    								}
	    								EnvironmentalPrimitive ep = new EnvironmentalPrimitive(
	    										vmName+"-"+serviceName,
	    										name, 
	    										Type.getTypeByString(eps.item(k).getAttributes().getNamedItem("name").getNodeValue()));
	    							
	    								
	    								primitives.put(name, ep);
	    							 }
	    						}
	    						
	    					}
	    					
	    					if ("QoS".equals(insideService.item(l).getNodeName())){
	    						NodeList qoss = insideService.item(l).getChildNodes();
	    						
	    						for (int k = 0; k < qoss.getLength(); k++) {
	    							
	    							
	    							if (Node.ELEMENT_NODE == qoss.item(k).getNodeType()) {
	    								
	    								QualityOfService qos = new QualityOfService(vmName+"-"+serviceName+"-"+qoss.item(k).getAttributes().getNamedItem("name").getNodeValue(), 
	    										Double.parseDouble(qoss.item(k).getAttributes().getNamedItem("constraint").getNodeValue()), 
	    										"true".equals(qoss.item(k).getAttributes().getNamedItem("is_min").getNodeValue()),
	    										Double.parseDouble(qoss.item(k).getAttributes().getNamedItem("pre_to_change").getNodeValue()));
	    								
	    								if (qoss.item(k).getAttributes().getNamedItem("ep") != null) {
	    								    qos.setEP((EnvironmentalPrimitive)primitives.get(qoss.item(k).getAttributes().getNamedItem("ep").getNodeValue()));
	    								}
	    								
	    								objectives.put(qoss.item(k).getAttributes().getNamedItem("name").getNodeValue(), qos);
	    								Repository.setQoS(qos);
	    							 }
	    						}
	    						
	    					}
	    					
	    					
	    					if ("Cost".equals(insideService.item(l).getNodeName())){
	    						// Cost objectives
		    					List<Primitive> results = new ArrayList<Primitive>();
		    					
		    					
		    					results.addAll(Repository.getVM(vmName)
		    							.getAllHardwarePrimitives());
		    					results.addAll(Repository.getVM(vmName)
		    							.getAllSharedSoftwarePrimitives());
		    					results.addAll(softs);
		    					
		    					
		    					double[] p = new double[hardwarePrices.size() + softwarePrices.size() + sharedSoftwarePrices.size()];
		    					for (int n = 0; n < hardwarePrices.size(); n++) {
		    						p[n] = hardwarePrices.get(n);
		    					}
		    					
		    					for (int n = 0; n < sharedSoftwarePrices.size(); n++) {
		    						p[n + hardwarePrices.size()] = sharedSoftwarePrices.get(n);
		    					}
		    					
		    					for (int n = 0; n < softwarePrices.size(); n++) {
		    						p[n + sharedSoftwarePrices.size() + hardwarePrices.size()] = softwarePrices.get(n);
		    					}
		    					
		    					
		    					
		    					Cost cost = new Cost(vmName+"-"+serviceName+"-cost",
		    							results, 
		    							p, 
		    							Double.parseDouble(insideService.item(l).getAttributes().getNamedItem("budget").getNodeValue()));
		    					objectives.put("Cost", cost);
		    					Repository.setCost(cost); 
		    					
		    					
		    					System.out.print("Number of CP in cost " + results.size() + "\n");
		    					for (int m = 0; m < results.size() ; m++) {
		    						System.out.print(p[m]+ " : " + results.get(m).getName() + " : "+ results.get(m) + "\n");
		    					}  
		    					
	    					}
	    					
	    					
	    					
	    				}
	    				
	    				
	    				Service service = new Service(vmName, serviceName, objectives, primitives);
	    				Repository.setService(vmName+"-"+serviceName, service);
	    				System.out.print("Number of obj in service " + service.getObjectives().size() + "\n");
						
					}
					
	                
	                
					//System.out.print(insideVmNodes.item(j).getNodeType()+ "\n");
				}
				
				
				
				
			}
			
		} catch (Exception e) { 
			e.printStackTrace();
		}
		
		
		
		 System.out.print("***** total qos " + Repository.getQoSSet().size() + "\n");
			
			/**
			 * Setting the possible relevant primitives and the direct primitives.
			 * 
			 ***/
			for (VM vm : Repository.getAllVMs()) {
				
				String vmName = vm.getID();
				
				for (Service s : Repository.getAllServices()) {
					

					String serviceName = s.getName();
					
					// Possible primitives
					
					Service service = Repository.getService(vmName+"-"+serviceName);
					
					if (service == null) {
						continue;
					}
					

					if (!service.isHasObjectiveToModel()) {
						continue;
					}
					
					
					for (Service subS : Repository.getAllServices()) {
						String subServiceName = subS.getName();
						
						if (Repository.getService(vmName+"-"+subServiceName) == null) {
							continue;
						} 
						
						service.addPossiblePrimitive(Repository.getService(vmName+"-"+subServiceName).getPrimitives() );
					}
					
				

					for (VM subVm : Repository.getAllVMs()) {

							Set<Primitive> ps = new HashSet<Primitive>();
							ps.addAll(subVm
									.getAllHardwarePrimitives());
							ps.addAll(subVm
									.getAllSharedSoftwarePrimitives());
							service.addPossiblePrimitive(ps);
					}
					
					
					//System.out.print(service.getObjectives().size() +  " Number of possible relevant primitives " + service.getPossiblePrimitives().size()  + "\n");
					
					   
					
				}
			}
			
		
			
			
			// Set direct primitives.
			for (Service s : Repository.getAllServices() ) {
				
				if (!s.isHasObjectiveToModel()) {
					continue;
				}
				// This could include both qos and cost objective, even though we do not
				// use the cost objectives at all.
				for (Objective obj : s.getObjectives()) {
					
					// we need cost objective as this is also a collection of all objectives.
					
					
					    // Software CP and EP
						for (Primitive p : s.getPrimitives()) {
							Repository.setDirectForAnObjective(obj, p);
							//System.out.print(p.getAlias() + "\n");
						}
						
						// Hardware CP
						for (Primitive p : Repository.getVM(s.getVMID()).getAllHardwarePrimitives()) {
							Repository.setDirectForAnObjective(obj, p);
							//System.out.print(p.getAlias() + "\n");
						}
						
						for (Primitive p : Repository.getVM(s.getVMID()).getAllSharedSoftwarePrimitives()) {
							Repository.setDirectForAnObjective(obj, p);
							//System.out.print(p.getAlias() + "\n");
						}
						
					
					System.out.print("Number of direct primitives " + Repository.countDirectForAnObjective(obj)  + "\n");
				}
				
				s.print();
				s.initializeModelForQoS();
			}
			
			Executor.init(3);
			//new HistoryLoader().run();
					
			new Receiver().receive();
			
			
	}
}
