package org.soa;
/**MOEAD
   33.82 16.37
   34.63 19.47
   36.25 41.51
   34.71 44.17
   19.7  81.96
 * 
 * NSGAII
 * 
 * 
 * 36.95 28.81
 * 40.43 38.99
 * 40.48 63.89
   36.3 71
 * 22.91 83
 * 
 * IBEA
 
 * 44.85 33.20
 * 44.04 43.5
 * 40.62 67.52
 * 36.93 69.7
 * 24.10 81.89
 * 
 * GP: 28.52 64.05
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import jmetal.util.JMException;

import org.femosaa.core.SASAlgorithmAdaptor;
import org.femosaa.core.SASSolution;
import org.femosaa.seed.Seeder;
import org.ssase.Service;
import org.ssase.objective.Objective;
import org.ssase.objective.QualityOfService;
import org.ssase.objective.optimization.bb.BranchAndBoundRegion;
import org.ssase.objective.optimization.femosaa.FEMOSAASolutionInstantiator;
import org.ssase.objective.optimization.femosaa.moead.*;
import org.ssase.objective.optimization.femosaa.nsgaii.*;
import org.ssase.objective.optimization.femosaa.ibea.*;
import org.ssase.objective.optimization.gp.GPRegion;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.region.OptimizationType;
import org.ssase.region.Region;
import org.ssase.util.Logger;
import org.ssase.util.Repository;
import org.ssase.util.Ssascaling;

public class Simulator {

	
	private static double[] std = new double[]{
		0.5598781683545676,
		0.5577668885764526,
		0.5560550466922252,
		0.5809903481804344,
		0.5557609833783113,
		0.5631040546927574,
		0.5671721812717974,
		0.5766828924709453,
		0.5754014716777731,
		0.5692605758674869,
		0.5704293017401422,
		0.5684793300168369,
		0.5699545355521076,
		0.5709104102942282,
		0.5779262774729406,
		0.5819186376436061,
		0.5831649836057103,
		0.5850040750657443,
		0.5882151340824664,
		0.6055284484486501,
		0.59822638865834,
		0.6266092641264538,
		0.6014554601526401,
		0.5852442142164849,
		0.593872306386467,
		0.6221926457450775,
		0.5972373768865806,
		0.5892310884289209,
		0.5891902887612805,
		0.5876254688957394,
		0.6062978870887651,
		0.5872744997642643,
		0.6074643819687763,
		0.5916490306891786,
		0.6022175078177188,
		0.5929133813546841,
		0.5954089758663231,
		0.5935352473245473,
		0.6175658520543852,
		0.5903672231887879,
		0.5948379331877979,
		0.6067661240518817,
		0.614347087762929,
		0.6130868237344695,
		0.6071120704454128,
		0.6146365786009473,
		0.6118323045204574,
		0.6109308191818994,
		0.6152851968400803,
		0.6396099587106816,
		0.643473325015516,
		0.6380659462414335,
		0.6432235091700665,
		0.6464737050439574,
		0.6364237905437392,
		0.6299163530862999,
		0.6574113701667911,
		0.6323743237406728,
		0.6164543870470915,
		0.6183014560768237,
		0.6461649056588066,
		0.6216241863115287,
		0.613709800348128,
		0.642942462908493,
		0.6413019400956,
		0.6591597857990978,
		0.6409199125240554,
		0.654635884945472,
		0.6385065589059731,
		0.6379693264332971,
		0.6286908164073939,
		0.631185420171705,
		0.6448295005960204,
		0.6681871669396009,
		0.6395190041886842,
		0.6417454711883992,
		0.6580075075101048,
		0.684244676693945,
		0.6831404348960315,
		0.6775542607090856,
		0.6844957084116058,
		0.6633908750118558,
		0.6708962312196841,
		0.6624243530555167,
		0.665672811618681,
		0.6844924990704713,
		0.658227844100732,
		0.685237380964268,
		0.68845407037546,
		0.6752052045221766,
		0.6785059074148766,
		0.6954651514864651,
		0.6679710845652012,
		0.6555291491680046,
		0.6551930770487524,
		0.6659558337755469,
		0.6407473857344709,
		0.6331674575829247,
		0.6625893707251804,
		0.6324681297904391,
		0.657699365974985,
		0.6323272929178849
		
	};
	
	
	public static void main(String[] arg) {
		main_test();
//		String o = "";
//		Random r  = new Random();
//		for(int i = 0; i < 10;i++) {
//			o += r.nextDouble() + ",";
//		}
//		System.out.print(o);
//		printSurface();
		
//		for(int i = 0; i < std.length;i++) {
//			System.out.print("("+(i+1)+","+std[i]+")\n");
//		}
		
		
//		
//		File file = new File("/Users/tao/research/analysis/knee/ZDT1.pf");
//		String s = "";
//		Set<String> set = new HashSet<String>();
//		Set<Double> set1 = new HashSet<Double>();
//		Set<Double> set2 = new HashSet<Double>();
//		try {
//		BufferedReader reader = new BufferedReader(new FileReader(file));
//		String line = null;
//		
//		while((line = reader.readLine()) != null) {
//			double d1 = Math.round(Double.parseDouble(line.split(" ")[0]) * 100.0)/100.0;
//			double d2 = Math.round(Double.parseDouble(line.split(" ")[1]) * 10.0)/10.0;
//			if(!set.contains("("+d1+","+d2+")\n") && !set1.contains(d1) && !set2.contains(d2)) {
//				set.add("("+d1+","+d2+")\n");
//				set1.add(d1);
//				set2.add(d2);
//				s += "("+d1+","+d2+")\n";
//			}
//			
//		}
//		}catch (Exception e) {
//			
//		}
//		
//		System.out.print(s);
	}
	
	private static void printSurface() {
		List<Double> tp = readFile("IBEA01/Throughput.rtf");
		List<Double> cost = readFile("IBEA01/Cost.rtf");
		
		for (int i = 0; i < std.length;i++) {
			System.out.print(cost.get(i)  + " " + std[i] + " " + (1/tp.get(i))   + "\n");
		}
	}
	
	private static List<Double> readFile(String path) {
		List<Double> list = new ArrayList<Double> ();
		
		
		File file = new File("/Users/tao/research/monitor/sas-soa/saved/"+path);
		
		try {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while((line = reader.readLine()) != null) {
			list.add(Double.parseDouble(line));
		}
		}catch (Exception e) {
			
		}
		
		return list;
	}

	private static void main_test() {
		
		Ssascaling.activate();
		Workflow workflow = new Workflow();

		List<AbstractService> as = workflow.all;
		List<ConcreteService> exist = new ArrayList<ConcreteService>();
		for (AbstractService a : as) {
			exist.addAll(a.getOption());
		}

		List<ConcreteService> repeat = new ArrayList<ConcreteService>();
		
		// A copy for comparison
		List<ConcreteService> repeat1 = new ArrayList<ConcreteService>();
		for (int i = 0; i < 102; i++) {
			int n = i;
			if (n >= exist.size()) {
				n = n % exist.size();
			}
			repeat.add(exist.get(n));
			repeat1.add(exist.get(n).clone());
		}

		List<ControlPrimitive> cp = new ArrayList<ControlPrimitive>();

		Set<ControlPrimitive> set = new HashSet<ControlPrimitive>();
		for (Service s : Repository.getAllServices()) {

			for (Primitive p : s.getPossiblePrimitives()) {
				if (p instanceof ControlPrimitive) {
					set.add((ControlPrimitive) p);
				}
			}

		}

		cp.addAll(set);

		// Assume all objectives have the same order and inputs
		// List<ControlPrimitive> cp =
		// Repository.getSortedControlPrimitives(Repository.getAllObjectives().iterator().next());
		Collections.sort(cp, new Comparator() {

			public int compare(Object arg0, Object arg1) {
				ControlPrimitive cp1 = (ControlPrimitive) arg0;
				ControlPrimitive cp2 = (ControlPrimitive) arg1;
				int value1 = Integer.parseInt(cp1.getName().substring(2));
				int value2 = Integer.parseInt(cp2.getName().substring(2));
				return value1 < value2 ? -1 : 1;
			}

		});
		//Region.selected = OptimizationType.FEMOSAA01 ;
		Ssascaling.loadFeatureModel(cp);

		compact(cp, "CS1", 0);
		compact(cp, "CS2", 1);
		compact(cp, "CS3", 2);
		compact(cp, "CS4", 3);
		compact(cp, "CS5", 4);

		for (ControlPrimitive p : cp) {
			System.out.print(p.getName() + "\n");
		}

		SOADelegate qos1 = new SOADelegate(0, workflow);
		SOADelegate qos2 = new SOADelegate(1, workflow);

		for (Objective obj : Repository.getAllObjectives()) {

			QualityOfService qos = (QualityOfService) obj;
			if (qos.getName().equals("sas-rubis_software-Throughput")) {
				qos.setDelegate(qos1);
			} else {
				qos.setDelegate(qos2);
			}

		}

		double[] features = null;
		SASAlgorithmAdaptor.isSeedSolution = false;
		int no = 0;
		for (ConcreteService cs : repeat) {
			
			
			
			org.femosaa.core.SASSolution.putDependencyChainBack();
			
//			features = new double[exist.size()*2];
//			for (int i = 0; i < exist.size(); i++ ){
//				int k = i*2;
//				features[k] = exist.get(i).getObjectiveValues()[0];
//				features[k+1] = exist.get(i).getObjectiveValues()[1];
//			}
//			
//			Seeder.priorFeatures(features);
//			Seeder.calculateRank(features);
			
//			testNSGAIIwithSeed1();
			
			//testMOEAD();
			//testNSGAII();
			testIBEA();
			//testMOEADwithInvalidity();
			//testNSGAIIwithInvalidity();
			//testIBEAwithInvalidity();
			
//			
//			Region.selected = OptimizationType.BB;
//			
//			
//			System.out
//			.print("=============== BBRegion ===============\n");
//			ExtendedBB moead01 = new ExtendedBB();
//			moead01.addObjectives(Repository.getAllObjectives());
//		
//			long time = System.currentTimeMillis();
//			double[] r = getFitness(moead01.optimize());
//			org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//			logData("sas", "Throughput", String.valueOf(r[0]));
//			logData("sas", "Cost", String.valueOf(r[1]));
	
			
//			Region.selected = OptimizationType.GP;
////			
////			
//			System.out
//			.print("=============== GPRegion ===============\n");
//			GPRegion moead01 = new GPRegion();
//			moead01.addObjectives(Repository.getAllObjectives());
//		
//			long time = System.currentTimeMillis();
//			double[] r = getFitness(moead01.optimize());
//			org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//			logData("sas", "Throughput", String.valueOf(r[0]));
//			logData("sas", "Cost", String.valueOf(r[1]));
			
			
//			Region.selected = OptimizationType.GP;
////		
////		
//		System.out
//		.print("=============== GPRegion ===============\n");
//		GPRegion moead01 = new GPRegion();
//		moead01.addObjectives(Repository.getAllObjectives());
//	
//		long time = System.currentTimeMillis();
//		double[] r = getFitness(moead01.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
			
//			
			
//			List<Double> tplist = workflow.getObjectiveValueForEachOption(0);
//			List<Double> ctlist = workflow.getObjectiveValueForEachOption(1);
//			
//			double std = (getStD(ctlist) + getStD(tplist))/2;
//			System.out.print(std + ",\n");
			
//			for (int i = 0; i < 50; i++) {
//				//int l = new java.util.Random().nextInt(repeat.size());
//				repeat.get((i+no < repeat.size()? i+no : (i+no)%repeat.size())).change();
//			}
			
			cs.change();

			no++;
		}

		
	
		
		
//		System.out.print("Final solution counts\n");
//		System.out.print("Valid ones 1:\n");
//		System.out.print(SASAlgorithmAdaptor.valid1);
//		System.out.print("Valid ones 2:\n");
//		System.out.print(SASAlgorithmAdaptor.valid2);
//		System.out.print("Invalid ones 1:\n");
//		System.out.print(SASAlgorithmAdaptor.invalid1);
//		System.out.print("Invalid ones 2:\n");
//		System.out.print(SASAlgorithmAdaptor.invalid2);
		
	}
	
	private static double getStD(List<Double> list){
		double mean = 0.0;
		for (Double d : list) {
			mean += d;
		}
		
		mean = mean / list.size();
		double std = 0.0;
		for (Double d : list) {
			std += Math.pow((d - mean),2);
		}
		
		std = std / list.size();
		std = Math.pow(std, 0.5);
		std = std / mean;
		
		return std;
	}
		
	
	private static void testMOEADwithInvalidity(){
		
		double[] r = null;
		Region.selected = OptimizationType.FEMOSAA;
		SASAlgorithmAdaptor.isPreserveInvalidSolution = false;
		System.out
				.print("=============== MOEAD_STMwithKAndDRegion ===============\n");
		MOEAD_STMwithKAndDRegion moead = new MOEAD_STMwithKAndDRegion();
		moead.addObjectives(Repository.getAllObjectives());
		long time = System.currentTimeMillis();
		r = getFitness(moead.optimize());
		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
		logData("sas", "Throughput", String.valueOf(r[0]));
		logData("sas", "Cost", String.valueOf(r[1]));
		
		
//		Region.selected = OptimizationType.FEMOSAA;
//		SASAlgorithmAdaptor.isPreserveInvalidSolution = true;
//		
//		System.out
//				.print("=============== MOEAD_STMwithKAndDRegion-Invalidity ===============\n");
//		MOEAD_STMwithKAndDRegion moead_in = new MOEAD_STMwithKAndDRegion();
//		moead_in.addObjectives(Repository.getAllObjectives());
//		time = System.currentTimeMillis();
//		r = getFitness(moead_in.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
	}
	
	private static void testNSGAIIwithSeed1(){
		
		
		double[] r = null;
		Region.selected = OptimizationType.NSGAIIkd;
		SASAlgorithmAdaptor.isPreserveInvalidSolution = false;
		Seeder.isRandom = true;
		
		System.out
				.print("=============== NSGAIIwithKAndDRegion ===============\n");
		NSGAIIwithKAndDRegion moead = new NSGAIIwithKAndDRegion();
		moead.addObjectives(Repository.getAllObjectives());
		long time = System.currentTimeMillis();
		r = getFitness(moead.optimize());
		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
		logCustomData("sas", "Throughput", String.valueOf(r[0]), "NSGAIIkd-seed1");
		logCustomData("sas", "Cost", String.valueOf(r[1]), "NSGAIIkd-seed1");
	}
	
	private static void testNSGAIIwithSeed(){
		
		double[] r = null;
		Region.selected = OptimizationType.NSGAIIkd;
		SASAlgorithmAdaptor.isPreserveInvalidSolution = false;
		Seeder.isRandom = false;
		
		System.out
				.print("=============== NSGAIIwithKAndDRegion ===============\n");
		NSGAIIwithKAndDRegion moead = new NSGAIIwithKAndDRegion();
		moead.addObjectives(Repository.getAllObjectives());
		long time = System.currentTimeMillis();
		r = getFitness(moead.optimize());
		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
		logCustomData("sas", "Throughput", String.valueOf(r[0]), "NSGAIIkd-seed");
		logCustomData("sas", "Cost", String.valueOf(r[1]), "NSGAIIkd-seed");
	}
	
	private static void testNSGAIIwithInvalidity(){
		
		double[] r = null;
		Region.selected = OptimizationType.NSGAIIkd;
		SASAlgorithmAdaptor.isPreserveInvalidSolution = false;
		
		System.out
				.print("=============== NSGAIIwithKAndDRegion ===============\n");
		NSGAIIwithKAndDRegion moead = new NSGAIIwithKAndDRegion();
		moead.addObjectives(Repository.getAllObjectives());
		long time = System.currentTimeMillis();
		r = getFitness(moead.optimize());
		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
		logData("sas", "Throughput", String.valueOf(r[0]));
		logData("sas", "Cost", String.valueOf(r[1]));

		
		
		
//		Region.selected = OptimizationType.NSGAIIkd;
//		SASAlgorithmAdaptor.isPreserveInvalidSolution = true;
//		
//		System.out
//				.print("=============== NSGAIIwithKAndDRegion-Invalidity ===============\n");
//		NSGAIIwithKAndDRegion moead_in = new NSGAIIwithKAndDRegion();
//		moead_in.addObjectives(Repository.getAllObjectives());
//		time = System.currentTimeMillis();
//		r = getFitness(moead_in.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
	}
	
	private static void testIBEAwithInvalidity(){
		
		double[] r = null;
		Region.selected = OptimizationType.IBEAkd;
		SASAlgorithmAdaptor.isPreserveInvalidSolution = true;
		System.out
				.print("=============== IBEAwithKAndDRegion ===============\n");
		IBEAwithKAndDRegion moead = new IBEAwithKAndDRegion();
		moead.addObjectives(Repository.getAllObjectives());
		long time = System.currentTimeMillis();
		r = getFitness(moead.optimize());
		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
		logData("sas", "Throughput", String.valueOf(r[0]));
		logData("sas", "Cost", String.valueOf(r[1]));
		
		
//		Region.selected = OptimizationType.IBEAkd;
//		SASAlgorithmAdaptor.isPreserveInvalidSolution = true;
//		System.out
//				.print("=============== IBEAwithKAndDRegion ===============\n");
//		IBEAwithKAndDRegion moeadkd = new IBEAwithKAndDRegion();
//		moeadkd.addObjectives(Repository.getAllObjectives());
//		time = System.currentTimeMillis();
//		r = getFitness(moeadkd.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
		
	}
	
	private static void testMOEAD(){
		double[] r = null;
//		Region.selected = OptimizationType.FEMOSAA;
//
//		System.out
//				.print("=============== MOEAD_STMwithKAndDRegion ===============\n");
//		MOEAD_STMwithKAndDRegion moead = new MOEAD_STMwithKAndDRegion();
//		moead.addObjectives(Repository.getAllObjectives());
//		long time = System.currentTimeMillis();
//		r = getFitness(moead.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
		
//		
//		Region.selected = OptimizationType.FEMOSAAd;
//
//		System.out
//				.print("=============== MOEAD_STMwithDRegion ===============\n");
//		MOEAD_STMwithDRegion moeadd = new MOEAD_STMwithDRegion();
//		moeadd.addObjectives(Repository.getAllObjectives());
//		time = System.currentTimeMillis();
//		r = getFitness(moeadd.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
//		
//		
//		
//		Region.selected = OptimizationType.FEMOSAAnothing;
//
//		System.out
//				.print("=============== MOEAD_STMRegion ===============\n");
//		MOEAD_STMRegion moeadnothing = new MOEAD_STMRegion();
//		moeadnothing.addObjectives(Repository.getAllObjectives());
//		time = System.currentTimeMillis();
//		r = getFitness(moeadnothing.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
//		
//		
//		Region.selected = OptimizationType.FEMOSAAk;
//
//		System.out
//				.print("=============== MOEAD_STMwithKRegion ===============\n");
//		MOEAD_STMwithKRegion moeadk = new MOEAD_STMwithKRegion();
//		moeadk.addObjectives(Repository.getAllObjectives());
//		time = System.currentTimeMillis();
//		r = getFitness(moeadk.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
//		
//
//		
		Region.selected = OptimizationType.FEMOSAA01;
		
		
		System.out
		.print("=============== MOEAD_STMwithZeroAndOneRegion ===============\n");
		MOEAD_STMwithZeroAndOneRegion moead01 = new MOEAD_STMwithZeroAndOneRegion();
		moead01.addObjectives(Repository.getAllObjectives());
	
		long time = System.currentTimeMillis();
		r = getFitness(moead01.optimize());
		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
		logData("sas", "Throughput", String.valueOf(r[0]));
		logData("sas", "Cost", String.valueOf(r[1]));
	}
	
	
	private static void testIBEA(){
		double[] r = null;
//		Region.selected = OptimizationType.IBEAkd;
//
//		System.out
//				.print("=============== IBEAwithKAndDRegion ===============\n");
//		IBEAwithKAndDRegion moead = new IBEAwithKAndDRegion();
//		moead.addObjectives(Repository.getAllObjectives());
//		long time = System.currentTimeMillis();
//		r = getFitness(moead.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
		
		
//		Region.selected = OptimizationType.IBEAd;
//
//		System.out
//				.print("=============== IBEAwithDRegion ===============\n");
//		IBEAwithDRegion moeadd = new IBEAwithDRegion();
//		moeadd.addObjectives(Repository.getAllObjectives());
//		time = System.currentTimeMillis();
//		r = getFitness(moeadd.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
//		
//		
//		
//		Region.selected = OptimizationType.IBEA;
//
//		System.out
//				.print("=============== IBEARegion ===============\n");
//		IBEARegion moeadnothing = new IBEARegion();
//		moeadnothing.addObjectives(Repository.getAllObjectives());
//		time = System.currentTimeMillis();
//		r = getFitness(moeadnothing.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
//		
//		
//		Region.selected = OptimizationType.IBEAk;
//
//		System.out
//				.print("=============== IBEAwithKRegion ===============\n");
//		IBEAwithKRegion moeadk = new IBEAwithKRegion();
//		moeadk.addObjectives(Repository.getAllObjectives());
//		time = System.currentTimeMillis();
//		r = getFitness(moeadk.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
//		
//
//		
		Region.selected = OptimizationType.IBEA01;
		
		
		System.out
		.print("=============== IBEAwithZeroAndOneRegion ===============\n");
		IBEAwithZeroAndOneRegion moead01 = new IBEAwithZeroAndOneRegion();
		moead01.addObjectives(Repository.getAllObjectives());
	
		long time = System.currentTimeMillis();
		r = getFitness(moead01.optimize());
		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
		logData("sas", "Throughput", String.valueOf(r[0]));
		logData("sas", "Cost", String.valueOf(r[1]));
	}
	
	
	
	private static void testNSGAII(){
		double[] r = null;
//		Region.selected = OptimizationType.NSGAIIkd;
//
//		System.out
//				.print("=============== NSGAIIwithKAndDRegion ===============\n");
//		NSGAIIwithKAndDRegion moead = new NSGAIIwithKAndDRegion();
//		moead.addObjectives(Repository.getAllObjectives());
//		long time = System.currentTimeMillis();
//		r = getFitness(moead.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
		
		
//		Region.selected = OptimizationType.NSGAIId;
//
//		System.out
//				.print("=============== NSGAIIwithDRegion ===============\n");
//		NSGAIIwithDRegion moeadd = new NSGAIIwithDRegion();
//		moeadd.addObjectives(Repository.getAllObjectives());
//		long time = System.currentTimeMillis();
//		r = getFitness(moeadd.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
//		
//		
//		
//		Region.selected = OptimizationType.NSGAII;
//
//		System.out
//				.print("=============== NSGAIIRegion ===============\n");
//		NSGAIIRegion moeadnothing = new NSGAIIRegion();
//		moeadnothing.addObjectives(Repository.getAllObjectives());
//		time = System.currentTimeMillis();
//		r = getFitness(moeadnothing.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
//		
//		
//		Region.selected = OptimizationType.NSGAIIk;
//
//		System.out
//				.print("=============== NSGAIIwithKRegion ===============\n");
//		NSGAIIwithKRegion moeadk = new NSGAIIwithKRegion();
//		moeadk.addObjectives(Repository.getAllObjectives());
//		time = System.currentTimeMillis();
//		r = getFitness(moeadk.optimize());
//		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
//		logData("sas", "Throughput", String.valueOf(r[0]));
//		logData("sas", "Cost", String.valueOf(r[1]));
//		
//
//		
		Region.selected = OptimizationType.NSGAII01;
		
		
		System.out
		.print("=============== NSGAIIwithZeroAndOneRegion ===============\n");
		NSGAIIwithZeroAndOneRegion moead01 = new NSGAIIwithZeroAndOneRegion();
		moead01.addObjectives(Repository.getAllObjectives());
	
		long time = System.currentTimeMillis();
		r = getFitness(moead01.optimize());
		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
		logData("sas", "Throughput", String.valueOf(r[0]));
		logData("sas", "Cost", String.valueOf(r[1]));
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
			
			if(obj.getName().equals("sas-rubis_software-Throughput")) {
				r[0] = 1/adapt;
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
	 
	 private static synchronized void logCustomData(String VM_ID, String qos, String data, String name){
			
			if(VM_ID == null) VM_ID = "sas";
			if(QualityOfService.isDelegate()) VM_ID = VM_ID + "/" + name;
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

	private static void compact(List<ControlPrimitive> cp, String name,
			int index) {
		List<Integer> l = new ArrayList<Integer>();
		for (int k = 0; k < cp.size(); k++) {

			if (cp.get(k).getName().startsWith(name)) {
				l.add(k);
			}

		}

		Workflow.map.put(Workflow.all.get(index),
				l.toArray(new Integer[Workflow.no_of_cs[index]]));
		Workflow.cs_map
				.put(Workflow.all.get(index),
						Workflow.all
								.get(index)
								.getOption()
								.toArray(
										new ConcreteService[Workflow.no_of_cs[index]]));
	}

	private static void test() {

		//
		// Workflow workflow = new Workflow();
		// for (int i = 0; i < 5; i++) {
		//
		// List<Integer> l = new ArrayList<Integer> ();
		// for (int j = 0; j < 10; j++) {
		// l.add((10*i) + j);
		// }
		//
		//
		//
		// Workflow.map.put(Workflow.all.get(i), l.toArray(new Integer[10]));
		// Workflow.cs_map.put(Workflow.all.get(i),Workflow.all.get(i).getOption().toArray(new
		// ConcreteService[10]));
		//
		// for (int j = 0; j < 10; j++) {
		// //System.out.print(Workflow.all.get(i).getOption().get(j)+"\n");
		// }
		//
		// }
		//
		// Random r = new Random();
		// double[] xValue = new double[50];
		// for (int i = 0; i < 50; i++) {
		// xValue[i] = r.nextInt(10);
		// }
		//
		// System.out.print("Throughput: "+workflow.getObjectiveValues(0,
		// xValue));
	}
}
