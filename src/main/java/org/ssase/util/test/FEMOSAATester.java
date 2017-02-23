package org.ssase.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ssase.Service;
import org.ssase.executor.Executor;
import org.ssase.network.Receiver;
import org.ssase.objective.Objective;
import org.ssase.objective.optimization.femosaa.moead.*;
import org.ssase.objective.optimization.femosaa.nsgaii.*;
import org.ssase.objective.optimization.femosaa.ibea.*;
import org.ssase.objective.optimization.femosaa.variability.fm.FeatureModel;
import org.ssase.planner.Planner;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.util.Repository;
import org.ssase.util.Ssascaling;

public class FEMOSAATester {

	public static final List<Objective> objectives = new ArrayList<Objective>();
	
	
	private static final boolean testFueatureModelOnly = false;
	private static String[] qosStrings = new String[] {
		"Response Time",
		"Energy"
	};
	
	public static void main(String[] a){
		System.out.print(System.getProperty("os.name"));
//		File f = new File("Objectives");
//		if (f.exists()) {
//
//			for (String s : qosStrings) {
//				try {
//					FileInputStream fileIn = new FileInputStream("Objectives/"
//							+ s + ".ser");
//					ObjectInputStream in = new ObjectInputStream(fileIn);
//					objectives.add((Objective) in.readObject());
//					
//					in.close();
//					fileIn.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			System.out.print("Successfully read objective funtions\n");
//		}

		Ssascaling.activate();
		
//		
//		if (objectives.size() == 0) {
//			if (!f.exists())
//				f.mkdir();
//
//			for (Objective obj : Repository.getAllObjectives()) {
//				try {
//					FileOutputStream fileOut = new FileOutputStream(
//							"Objectives/"+obj.getName()+".ser");
//					ObjectOutputStream out = new ObjectOutputStream(fileOut);
//					out.writeObject(obj);
//					
//					out.close();
//					fileOut.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			System.out.print("Successfully write objective funtions\n");
//		}
		
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
	
		
		System.out.print("=============== MOEAD_STMwithKAndDRegion ===============\n");
		MOEAD_STMwithKAndDRegion moead = new MOEAD_STMwithKAndDRegion();
		moead.addObjectives(Repository.getAllObjectives());
		moead.optimize();
	
		System.out.print("=============== MOEAD_STMwithDRegion ===============\n");
		MOEAD_STMwithDRegion moeadd = new MOEAD_STMwithDRegion();
		moeadd.addObjectives(Repository.getAllObjectives());
		moeadd.optimize();
		
		
		
		System.out.print("=============== NSGAIIwithKAndDRegion ===============\n");
		NSGAIIwithKAndDRegion nsgaii = new NSGAIIwithKAndDRegion();
		nsgaii.addObjectives(Repository.getAllObjectives());
		nsgaii.optimize();
	
		System.out.print("=============== NSGAIIwithDRegion ===============\n");
		NSGAIIwithDRegion nsgaiid = new NSGAIIwithDRegion();
		nsgaiid.addObjectives(Repository.getAllObjectives());
		nsgaiid.optimize();
		
		
		
		System.out.print("=============== IBEAwithKAndDRegion ===============\n");
		IBEAwithKAndDRegion ibea = new IBEAwithKAndDRegion();
		ibea.addObjectives(Repository.getAllObjectives());
		ibea.optimize();
		
		System.out.print("=============== IBEAwithDRegion ===============\n");
		IBEAwithDRegion ibead = new IBEAwithDRegion();
		ibead.addObjectives(Repository.getAllObjectives());
		ibead.optimize();
		
		
		System.out.print("=============== MOEAD_STMRegion ===============\n");
		MOEAD_STMRegion moeadnothing = new MOEAD_STMRegion();
		moeadnothing.addObjectives(Repository.getAllObjectives());
		moeadnothing.optimize();
		System.out.print("=============== MOEAD_STMwithKRegion ===============\n");
		MOEAD_STMwithKRegion moeadk = new MOEAD_STMwithKRegion();
		moeadk.addObjectives(Repository.getAllObjectives());
		moeadk.optimize();
		
		
		System.out.print("=============== NSGAIIRegion ===============\n");
		NSGAIIRegion nsgaiinothing = new NSGAIIRegion();
		nsgaiinothing.addObjectives(Repository.getAllObjectives());
		nsgaiinothing.optimize();
		System.out.print("=============== NSGAIIwithKRegion ===============\n");
		NSGAIIwithKRegion nsgaiik = new NSGAIIwithKRegion();
		nsgaiik.addObjectives(Repository.getAllObjectives());
		nsgaiik.optimize();
		
		
		System.out.print("=============== IBEARegion ===============\n");
		IBEARegion ibeanothing = new IBEARegion();
		ibeanothing.addObjectives(Repository.getAllObjectives());
		ibeanothing.optimize();
		System.out.print("=============== IBEAwithKRegion ===============\n");
		IBEAwithKRegion ibeak = new IBEAwithKRegion();
		ibeak.addObjectives(Repository.getAllObjectives());
		ibeak.optimize();
		
		/**
		 * (13.537660699698488,8.483574288492537E-6)
(15.986553727124798,1.2654748318810854E-6)
(15.986553727124798,1.2654748318810854E-6)
(17.670695371944845,8.965910796709995E-7)
(13.353115602898635,9.053853646009194E-6)
(13.319195354757735,0.0036457243950464365)
(18.77062092484283,1.0497964578136243E-6)
(17.916346177470345,1.2335020616116466E-6)
(25.190025990250703,4.823396762623822E-7)
(25.190025990250703,4.823396762623822E-7)
(25.190025990250703,4.823396762623822E-7)
(20.760943667054093,1.7282253544966693E-6)
(20.760943667054093,1.7282253544966693E-6)
(15.039767009677627,0.2119825549958772)
(15.039767009677627,0.2119825549958772)
(15.039767009677627,0.2119825549958772)
(15.039767009677627,0.2119825549958772)
(15.039767009677627,0.2119825549958772)
(21.5962571278097,5.959586100400517E-6)
(16.683665184954066,0.009829280200325852)
(27.614060870088682,1.0920824945862378E-6)
(28.13425977864761,1.5986968858017295E-6)
(16.72548096077389,0.27259556403874646)
		 */
	}
}
