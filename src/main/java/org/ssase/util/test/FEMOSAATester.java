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
		//Set<ControlPrimitive> set = new HashSet<ControlPrimitive>();
		for(Service s : Repository.getAllServices()){
			
			for (Primitive p : s.getPossiblePrimitives()) {
				
				if(p instanceof ControlPrimitive && !list.contains(p)){
					if(p.getName().equals("Connection")) {
						Connection = (ControlPrimitive)p;
					} else if(p.getName().equals("maxThread")) {
						maxThread = (ControlPrimitive)p;
					} else if(p.getName().equals("minSpareThreads")) {
						minSpareThreads = (ControlPrimitive)p;
					} else if(p.getName().equals("Compression")) {
						Compression = (ControlPrimitive)p;
					} else if(p.getName().equals("cacheMode")) {
						cacheMode = (ControlPrimitive)p;
					}else {
						list.add((ControlPrimitive)p);
					}
				}
			}
			
		}
		
		
		list.add(0, Connection);
		list.add(1, minSpareThreads);
		list.add(2, maxThread);
		list.add(3, Compression);
		list.add(cacheMode);
		
		//list.addAll(set);
		FeatureModel fm = new FeatureModel(list);
		
		List<FeatureModel> models = new ArrayList<FeatureModel>();
		models.add(fm);
		
		FeatureModel.readFile(models);
		
		if(testFueatureModelOnly) return;
		
		for(Objective obj : Repository.getAllObjectives()) {
			Repository.setSortedControlPrimitives(obj, fm);
		}
		
		
		Executor.execute(Planner.optimize(Repository.getAllObjectives().iterator().next(), UUID.randomUUID().toString()));
	}
}
