package org.ssase.objective.optimization.femosaa.variability.fm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jmetal.problems.SASSolution;
import jmetal.problems.VarEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.SoftwareControlPrimitive;
import org.ssase.region.OptimizationType;
import org.ssase.region.Region;
import org.ssase.util.Ssascaling;
import org.ssase.util.Tuple;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FeatureModel {

	protected static final Logger logger = LoggerFactory
	.getLogger(FeatureModel.class);
	
	private List<ControlPrimitive> list;
	// This is the breaches of tree after grow-prune,
	// Its order should be the same as List<ControlPrimitive> list.
	private List<Branch> chromosome = new ArrayList<Branch>();
	
	// Key = dependent, Value = the list of different layers of main variable = the VarEntity at each layer 
	//private Map<Branch, List<List<VarEntity>>> tempMap;
	

	private Branch root = null;
	
	
	// Key = cp, value = lower, upper
	Map<ControlPrimitive, Tuple<Integer, Integer>> zeroAndOneTupleMap; 
	
	List<Integer[]> zeroAndOneOptionalValueList;
	
	
	public Map<ControlPrimitive, Tuple<Integer, Integer>> getZeroAndOneTupleMap() {
		if(Region.selected != OptimizationType.FEMOSAA01) {
			throw new RuntimeException("Only 0/1 representation needs this function");
		}
		return zeroAndOneTupleMap;
	}

	public List<Integer[]> getZeroAndOneOptionalValueList() {
		if(Region.selected != OptimizationType.FEMOSAA01) {
			throw new RuntimeException("Only 0/1 representation needs this function");
		}
		return zeroAndOneOptionalValueList;
	}

	public static void readFile(List<FeatureModel> models) {
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = dbFactory.newDocumentBuilder();

			Document doc = builder.parse(FeatureModel.class.getClassLoader()
					.getResourceAsStream("feature_model.xml"));

			doc.getDocumentElement().normalize();

			NodeList modelNodes = doc.getElementsByTagName("model");
			
			Map<String, Branch> map = new LinkedHashMap<String, Branch>();
			Map<String, CrossDependency> crossDependencyMap = new LinkedHashMap<String, CrossDependency>();
			
			for (int i = 0; i < modelNodes.getLength(); i++) {
				
				FeatureModel model = models.get(i);
				
				Node node = modelNodes.item(i);
				

				// This is the grow step
				grow(model, node, map, crossDependencyMap, null, false);
				
				// This is the prune step			
				prune(map, crossDependencyMap, model);
				
				
				// This is the step that create dependency chain and the network for valid values.
				model.configureDependencyChain(map);
				
			
				//model.checkClosedLoop();
				
				// Use 0/1 translation
				if(Region.selected == OptimizationType.FEMOSAA01) {
					
					model.zeroAndOneTupleMap = new HashMap<ControlPrimitive, Tuple<Integer, Integer>>();
					model.zeroAndOneOptionalValueList = new ArrayList<Integer[]>();
					
					
					normalParse(model, model.root, model.zeroAndOneTupleMap, model.zeroAndOneOptionalValueList);
					//parseGeneFor01(model, model.root, model.zeroAndOneTupleMap, model.zeroAndOneOptionalValueList);
				} 
				
				model.release();
				map.clear();
				crossDependencyMap.clear();
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	 private static void parseGeneFor01(FeatureModel model, Branch branch, Map<ControlPrimitive, Tuple<Integer, Integer>> tupleMap, List<Integer[]> list){
			
		
				Tuple<Integer, Integer> tuple = new Tuple<Integer, Integer>(list.size(), list.size() + branch.getRange().length - 1);
				
				for(int i = 0; i < branch.getRange().length; i++) {
					list.add(new Integer[]{0,1});
				}
				
				ControlPrimitive p = null;
				for (ControlPrimitive sub : model.list) {
					if(sub.getName().equals(branch.getName())){
						p = sub;
						break;
					}
				}
				
				if(p == null) {
					throw new RuntimeException(branch.getName() + " has no CP");
				}
				
				tupleMap.put(p, tuple);
		
			
		}
	
   private static void normalParse(FeatureModel model, Branch branch, Map<ControlPrimitive, Tuple<Integer, Integer>> tupleMap, List<Integer[]> list){
		
		if(branch.isNumeric() && !branch.isRoot()) {
			Tuple<Integer, Integer> tuple = new Tuple<Integer, Integer>(list.size(), list.size() + branch.getRange().length - 1);
			
			for(int i = 0; i < branch.getRange().length; i++) {
				list.add(new Integer[]{0,1});
			}
			
			ControlPrimitive p = null;
			for (ControlPrimitive sub : model.list) {
				if(sub.getName().equals(branch.getName())){
					p = sub;
					break;
				}
			}
			
			if(p == null) {
				p = new SoftwareControlPrimitive(null); //dummy
			}
			
			tupleMap.put(p, tuple);
			
		} else if(!branch.isRoot()){
			
			if (branch.getRange() !=null && branch.getRange().length != 0) {
				Tuple<Integer, Integer> tuple = new Tuple<Integer, Integer>(list.size(), list.size() + branch.getRange().length - 1);
				
				for(int i = 0; i < branch.getRange().length; i++) {
					list.add(new Integer[]{0,1});
				}
				
				ControlPrimitive p = null;
				for (ControlPrimitive sub : model.list) {
					if(sub.getName().equals(branch.getName())){
						p = sub;
						break;
					}
				}
				
				if(p == null) {
					p = new SoftwareControlPrimitive(null); //dummy
				}
				
				tupleMap.put(p, tuple);
			} else {
				// Only have switch on/off, including the mandatory ones
				Tuple<Integer, Integer> tuple = new Tuple<Integer, Integer>(list.size(), list.size() + 1);
				
				for(int i = 0; i < 1; i++) {
					list.add(new Integer[]{0,1});
				}
				
				ControlPrimitive p = null;
				for (ControlPrimitive sub : model.list) {
					if(sub.getName().equals(branch.getName())){
						p = sub;
						break;
					}
				}
				
				if(p == null) {
					p = new SoftwareControlPrimitive(null); //dummy
				}
				
				tupleMap.put(p, tuple);
			}
			
		}
		
	
		for(Branch b : branch.getNormalGroup()) {
			normalParse(model, b,  tupleMap,list);
		}
		for(Branch b : branch.getORGroup()) {
			normalParse(model,  b,  tupleMap,list);
		}
		for(Branch b : branch.getXORGroup()) {
			normalParse(model,  b,  tupleMap,list);
		}
	}
	
	private static Branch grow(FeatureModel model, Node node, Map<String, Branch> map, Map<String, CrossDependency> crossDependencyMap, String currentGroup, boolean isAddOff){
		
		Branch branch  = null;
		
		if (node.getNodeName().equals("feature")) {

			if ("numeric".equals(node.getAttributes().getNamedItem("type")
					.getNodeValue())) {
				String range = node.getAttributes().getNamedItem("range")
						.getNodeValue();
				double min = Double.parseDouble(range.split(" ")[0]);
				double max = Double.parseDouble(range.split(" ")[1]);
				// Grow 0
				branch = new Branch(node.getAttributes().getNamedItem("name")
						.getNodeValue(), node.getAttributes().getNamedItem(
						"optional") == null ? "false" : node.getAttributes()
						.getNamedItem("optional").getNodeValue(), node
						.getAttributes().getNamedItem("type").getNodeValue(),
						min, max, Double.parseDouble(node.getAttributes()
								.getNamedItem("gap").getNodeValue()), node.getAttributes().getNamedItem(
								"switchoff") == null ? "false" : node.getAttributes()
								.getNamedItem("switchoff").getNodeValue());

			} else {

				branch = new Branch(node.getAttributes().getNamedItem("name")
						.getNodeValue(), node.getAttributes().getNamedItem(
						"optional") == null ? "false" : node.getAttributes()
						.getNamedItem("optional").getNodeValue(), node
						.getAttributes().getNamedItem("type").getNodeValue());

			}

		} else if (node.getNodeName().equals("model")) {
			branch = new Branch();
			model.root = branch;
		}
		
		map.put(branch.getName(), branch);
//		if("cache_config".equals(branch.getName())) {
//			branch.isPossibleToBecomeGene();
//		}
		// Grow 1
		if("or".equals(currentGroup) && !branch.isNumeric() && 
				(node.getChildNodes() == null || node.getChildNodes().getLength() == 0)) {
			branch.enableSwitchOff();
			branch.enableSwitchOn();
		}
		
	
		// Grow 2
		if(currentGroup == null && !branch.isNumeric() && 
				(node.getChildNodes() == null || node.getChildNodes().getLength() == 0)) {		
			branch.enableSwitchOn();
		}
		
		// Grow 3
		if(isAddOff && (branch.isNumeric() || 
				(node.getChildNodes() != null && node.getChildNodes().getLength() != 0))) {
			branch.enableSwitchOff();
		}
		
		// Grow 4
		if(("or".equals(currentGroup) || "alt".equals(currentGroup)) && (branch.isNumeric() || 
				(node.getChildNodes() != null && node.getChildNodes().getLength() != 0))){
			branch.enableSwitchOff();
		}
		
		
		
		if(node.getChildNodes() == null || node.getChildNodes().getLength() == 0) {		
			return branch;
		}
		
		
		
		
		NodeList insideNodes = node.getChildNodes();
		
		for (int j = 0; j < insideNodes.getLength(); j++) {
			if ("cross-dependency".equals(insideNodes.item(j).getNodeName())){
				// This might be null here, but we can reset it in the prune function.
				Branch b = map.get(insideNodes.item(j).getAttributes().getNamedItem("main").getNodeValue());
				
				
				CrossDependency d = new CrossDependency(b, 
						branch, 
						insideNodes.item(j).getAttributes().getNamedItem("type").getNodeValue());
				
				crossDependencyMap.put(insideNodes.item(j).getAttributes().getNamedItem("main").getNodeValue(), d);
				
				
				if(insideNodes.item(j).getAttributes().getNamedItem("operator") != null) {
					d.setOperator(insideNodes.item(j).getAttributes().getNamedItem("operator").getNodeValue());
				}
				
				if(insideNodes.item(j).getAttributes().getNamedItem("main_range") != null) {
					String range = insideNodes.item(j).getAttributes().getNamedItem("main_range").getNodeValue();
					
					List<Double> list = new ArrayList<Double>();
					String[] individual = range.split(" ");
					for( String s : individual) {
						if(s.contains("-")) {
							String gap = insideNodes.item(j).getAttributes().getNamedItem("main_gap").getNodeValue();
							double[] r = getRange(Double.parseDouble(s.split("-")[0]), Double.parseDouble(s.split("-")[1]), 
									Double.parseDouble(gap));
							for (double dou : r) {
								list.add(dou);
							}
							
							
						} else {
							list.add(Double.parseDouble(s));
						}
						
						
					}
					
					Collections.sort(list);
					
					d.setMainRange(list);
				}
				
				
				if(insideNodes.item(j).getAttributes().getNamedItem("dependent_range") != null) {
		            String range = insideNodes.item(j).getAttributes().getNamedItem("dependent_range").getNodeValue();
					
		        	List<Double> list = new ArrayList<Double>();
					String[] individual = range.split(" ");
					for( String s : individual) {
						if(s.contains("-")) {
							String gap = insideNodes.item(j).getAttributes().getNamedItem("dependent_gap").getNodeValue();
							double[] r = getRange(Double.parseDouble(s.split("-")[0]), Double.parseDouble(s.split("-")[1]), 
									Double.parseDouble(gap));
							for (double dou : r) {
								list.add(dou);
							}
							
							
						} else {
							list.add(Double.parseDouble(s));
						}
						
						
					}
					
					Collections.sort(list);
					
					d.setDependentRange(list);
				}
				
	            if(insideNodes.item(j).getAttributes().getNamedItem("translate") != null) {
					d.setTranslation(insideNodes.item(j).getAttributes().getNamedItem("translate").getNodeValue());
				}
	            
	            branch.addCrossDependency(d);
			}
			
            if ("feature".equals(insideNodes.item(j).getNodeName())){
            	Branch b = grow(model, insideNodes.item(j), map, crossDependencyMap, null, 
            			(node.getAttributes().getNamedItem("optional") != null 
            			&& "true".equals(node.getAttributes().getNamedItem("optional").getNodeValue())));
            	if(b != null) {
            		branch.addNormal(b);
            		b.setParent(branch);
            	}
			}
            
            if ("alt".equals(insideNodes.item(j).getNodeName())){
            	for(int i = 0; i < insideNodes.item(j).getChildNodes().getLength(); i ++) {
            		if(insideNodes.item(j).getChildNodes().item(i).getNodeName().equals("#text")) {
            			continue;
            		}
            		Branch b = grow(model, insideNodes.item(j).getChildNodes().item(i), map, crossDependencyMap, "alt", true);
            		if(b != null) {
                		branch.addXOR(b);
                		b.setParent(branch);
                	}
            	}
            
			}
            
            if ("or".equals(insideNodes.item(j).getNodeName())){
            	for(int i = 0; i < insideNodes.item(j).getChildNodes().getLength(); i ++) {
            		if(insideNodes.item(j).getChildNodes().item(i).getNodeName().equals("#text")) {
            			continue;
            		}
            		Branch b = grow(model, insideNodes.item(j).getChildNodes().item(i), map, crossDependencyMap, "or", true);
            		if(b != null) {
                		branch.addOR(b);
                		b.setParent(branch);
                	}
            	}
			}
		}
		
	
		return branch;
	}
	
	
	private static void prune(Map<String, Branch> map, Map<String, CrossDependency> crossDependencyMap, FeatureModel model){
		// Set main in the cross tree dependency in case the main is placed after the cross-dependency entry.
		// This should also belong to grow 5.
		for(Map.Entry<String, CrossDependency> entry : crossDependencyMap.entrySet()) {
			entry.getValue().setMainIfNotPresent(map.get(entry.getKey()));
			// Grow 5
//			if(entry.getValue().isRequiredRelation()) {
//				map.get(entry.getKey()).enableSwitchOn();
//			}
		}
		
		// Switch the cross tree dependency and remove features with less than 2 ALT children.
		for(Map.Entry<String, Branch> entry : map.entrySet()) {
			// Ensure that required cross dependency's main can be switch off.
			//entry.getValue().validateRequiredAndSwitchOff();
//			if("cache_config".equals(entry.getValue().getName())) {
//				entry.getValue().isPossibleToBecomeGene();
//			}
			if(entry.getValue().isPossibleToBecomeGene()) {
				model.chromosome.add(entry.getValue());
			} 
		}
		
	
	}
	
	
	private static void sort(FeatureModel model){
		for (Branch b : model.chromosome) {
			b.insertRange();
		}
		
		
		List<Branch> list = new ArrayList<Branch>();
		for (ControlPrimitive p : model.list) {
			boolean isHas = false;
			for (Branch b : model.chromosome) {
				if(b.getName().equals(p.getName())) {
					isHas = true;
					if (!b.isRangeCorrect(p.getValueVector())) {
						throw new RuntimeException("The range of "  + p.getName() + " is not consistent with the setup in dom0.xml");
					}
					list.add(b);
					break;
				}
			}
			
			if(!isHas)
			  throw new RuntimeException(p.getName() + " is inconsistent with the setup in dom0.xml and feature_model.xml");
			
		}
		
		model.chromosome = list;
	}

	public void configureDependencyChain(Map<String, Branch> allMap){
		// Key = dependent variable, Value = main variable, the range matrix.
		//Map<Branch, Map<Branch, Integer[][]>> map = new HashMap<Branch, Map<Branch, Integer[][]>>();
		
		//----------------Analyze the dependency----------------
		
		for(Map.Entry<String, Branch> entry : allMap.entrySet()) {
			entry.getValue().inheritCrossDepedencyToChildrenOrParent(chromosome);
		}
		
		// Parse the in-branch dependency.
	    root.generateInBranchOptionalDependency(chromosome);
	    root.generateInBranchMandatoryDependency(chromosome);
	    root.generateInBranchXORDependency(chromosome);
	    root.generateInBranchORDependency(chromosome);
	    sort(this);
	    
	    for (Branch b : chromosome) {
	    	b.mergeIntersectableDependency();
	    }
	    

		
		for(Branch b : chromosome) {
			logger.debug("\n");
    	    b.debug();
		}
	
		//----------------Analyze the dependency----------------
		
		Map<Integer, VarEntity[]> dependencyMap = SASSolution.getDependencyMap();
		
		
		for (Branch b : chromosome) {
			 List<Map<Branch, Integer[][]>> list = b.getMainBranchesOfThisBranch();
			 
			 if(list == null) {
				 continue;
			 }
			 
			 List<VarEntity[]> varList = new ArrayList<VarEntity[]>();
			 
			 for (int i = 0; i < list.size();i++) {
				 // Key = dependent, Value = the list of different layers of main variable = the VarEntity at each layer 
				 Map<Branch, List<List<VarEntity>>> tempMap = new LinkedHashMap<Branch, List<List<VarEntity>>>();
				 Map<Branch, Integer[][]> map = list.get(i);
				 VarEntity[] ve = null;
				 if(i == 0) {
					 
					 if(map.size() == 0) {
						 continue;
					 }
					
					 if(logger.isDebugEnabled()) {
						 String r = "Do intersection for " + b.getName() + " in the following order: ";
						 
						 for(Map.Entry<Branch, Integer[][]> entry : list.get(i).entrySet()) {
							 r = r + entry.getKey().getName() + " ";
						 }
						 
						 logger.debug(r);
					 }
					 
					 List<Map.Entry<Branch, Integer[][]>> itr = new ArrayList<Map.Entry<Branch, Integer[][]>>();
					 for (Map.Entry<Branch, Integer[][]> e :  map.entrySet()) {
						 itr.add(e);
					 }
					 
					 ve = setIntersectionEntity(tempMap, b, itr.get(0), itr,
								null, 0);
				 } else {
					 
					 if(map.size() == 0) {
						 continue;
					 }
					 
					 if(logger.isDebugEnabled()) {
						 String r = "Do union for " + b.getName() + " in the following order: ";
						 
						 for(Map.Entry<Branch, Integer[][]> entry : list.get(i).entrySet()) {
							 r = r + entry.getKey().getName() + " ";
						 }
						 
						 logger.debug(r);
					 }
					 
					 List<Map.Entry<Branch, Integer[][]>> itr = new ArrayList<Map.Entry<Branch, Integer[][]>>();
					 for (Map.Entry<Branch, Integer[][]> e :  map.entrySet()) {
						 itr.add(e);
					 }
					 
					 ve = setUnionEntity(tempMap, b, itr.get(0), itr,
								null, 0);
				 }
				 varList.add(ve);
				 tempMap.clear();
			 }
			 
			 VarEntity[] finalOne = varList.get(0);
			 
			 for (int i = 1; i < varList.size();i++) {
				
				 
				 
				 
				 List<Integer> layerIndexA = new ArrayList<Integer>();
				 List<Integer> layerIndexB = new ArrayList<Integer>();
				 
				 finalOne[0].getMainVariablesByDependentVariable(layerIndexA);
				 varList.get(i)[0].getMainVariablesByDependentVariable(layerIndexB);
				 
				 if(logger.isDebugEnabled()) {
					 String r = "Do group intersection for " + b.getName() + " in the following order: \n";
					 r = r + "set1=";
					 for(Integer in : layerIndexA) {
						 r = r + chromosome.get(in).getName() + " ";
					 }
					 
					 
					 r = r + "\nset2=";
					 
					 for(Integer in : layerIndexB) {
						 r = r + chromosome.get(in).getName() + " ";
					 }
					 
					 logger.debug(r);
				 }
				 
				 insertMissingVarEntityFromBtoA(b, finalOne, layerIndexA, varList.get(i), layerIndexB);
			 }
			// if(finalOne != null)
			 dependencyMap.put(chromosome.indexOf(b), finalOne);
			 
		}
		
		
	
		
		
//		for (Map.Entry<Branch, Map<Branch, Integer[][]>> entry : map.entrySet()) {
//			Iterator<Map.Entry<Branch, Integer[][]>> itr = entry.getValue().entrySet().iterator();
//			
//			if(!itr.hasNext()) continue;
//			
//			dependencyMap.put(chromosome.indexOf(entry.getKey()), setEntity(entry.getKey(), itr.next(), itr,
//					null, -1));
//			
//		}
		
	
		
		
		for (int i = 0; i < list.size(); i ++) {
			logger.debug(list.get(i).getName() + ", index of " + i);
		}
		
		logger.debug("-----------Dependency Chain-----------");
		for (Map.Entry<Integer, VarEntity[]> entry : dependencyMap.entrySet()) {
			logger.debug(chromosome.get(entry.getKey()).getName() + ", index of " + entry.getKey());
			
			VarEntity[] array = entry.getValue();
			do{
				VarEntity ve = array[0];
			    logger.debug("Main features: " + chromosome.get(ve.getVarIndex()).getName() + ", index of " + ve.getVarIndex());
			    array = ve.getNext();
			} while(array != null);
		
			
			
			for(int i = 0; i < entry.getValue().length ; i++) {
				printVariableTree(list.get(entry.getKey()), 
						entry.getValue()[i],
						chromosome.get(entry.getValue()[i].getVarIndex()).getName() + "-" + i + "(" + chromosome.get(entry.getValue()[i].getVarIndex()).getRange()[i] + ")");
			}
			
			
		}
		logger.debug("-----------Dependency Chain-----------");
		
	}
	
	private void release(){
		chromosome.clear();
		chromosome = null;
		root = null;
	}
	
	private void insertMissingVarEntityFromBtoA(Branch key, VarEntity[] veA, List<Integer> layerIndexA, VarEntity[] veB, List<Integer> layerIndexB){
		
		List<Integer> toAdd = new ArrayList<Integer>();
		
		for (Integer i : layerIndexB) {
			if(!layerIndexA.contains(i)) {
				toAdd.add(i);
			}
		}
		
		for(Integer i : toAdd){
			insert(veA, i);
		}
		
		// Branch, current selected value
		Map<Branch, Integer> values = new HashMap<Branch, Integer>();
		for (Integer i : layerIndexB) {
			values.put(chromosome.get(i), -1);
		}
		
		intersect(key, veA, veB, values, layerIndexB);
	}
	
	public static double[] getRange (double minProvision, double maxProvision, double a){
		int max = (int)Math.ceil(maxProvision);
		int min = (int)Math.ceil(minProvision);
		
		
		
		int length = (int) (1 + (max - min)/a);
//		if (length % a != 0) {
//			length += 1;
//		}
		double[] valueVector = valueVector = new double[length]; 
			double value = min - a;
			for (int i = 0; i < valueVector.length; i++) {
				if (value + a >= max) {
					value = max;
				} else {
					value = value + a;
				}
				
				
				valueVector[i] = value;
			}
			
		
		
		
		
		
		return valueVector;
	}
	
	private void intersect(Branch key, VarEntity[] veA, VarEntity[] veB, Map<Branch, Integer> values, List<Integer> layerIndexB) {
		for (int i = 0; i < veA.length; i++) {
			if(values.containsKey(chromosome.get(veA[i].getVarIndex()))) {
				values.put(chromosome.get(veA[i].getVarIndex()), i);
			}
			
			if(veA[i].getNext() ==null) {
				VarEntity[] enArray = veB;
				VarEntity en = null;
				for(int j = 0 ; j < layerIndexB.size(); j++) {
					int index = values.get(chromosome.get(layerIndexB.get(j)));
					en = enArray[index];
					enArray = en.getNext();
				}
				
				veA[i].replace(getIntersection(veA[i].getOptionalValues(), en.getOptionalValues(), key, null, veB));
				
			} else {
				intersect(key, veA[i].getNext(), veB, values, layerIndexB);
			}
		}
	}
	
	private void insert(VarEntity[] ve, int indexToAdd) {
		for (VarEntity e : ve) {
			if(e.getNext() ==null) {
				VarEntity[] newVe = new VarEntity[chromosome.get(indexToAdd).getRange().length];
				for (int i = 0; i < newVe.length; i++) {
					newVe[i] = new VarEntity(indexToAdd, e.getOptionalValues(), null);
				}
				e.extend(newVe);
			} else {
				insert(e.getNext(), indexToAdd);
			}
		}
	}
	
	public void checkClosedLoop(){
		for(Branch b : chromosome) {
			if(b.isHasClosedLoopWithNumericDependency(b)) {
				logger.warn(b.getName() + " is in a closed loop which involve numeric dependency, please double check and try to break the loop otherwise it might cause deadlock in the mutation operation");
			}
		}
	}
	
	private void printVariableTree(ControlPrimitive dependenct, VarEntity ve, String log){
//		 if(!dependenct.getName().equals("cacheMode")) {
//			 return;
//		 }
		
		
		if(ve.getNext() != null) {
			for(int i = 0; i < ve.getNext().length ; i++) {
				String temp = log;
				printVariableTree(dependenct, ve.getNext()[i], temp + ", " + chromosome.get(ve.getNext()[i].getVarIndex()).getName() + "-" + i + "(" + chromosome.get(ve.getNext()[i].getVarIndex()).getRange()[i] + ")");
			}
			
		} else {
			Integer[] values = ve.getOptionalValues();
			log = log + "=[";
			int count = 0;
			for (int i : values) {
				if(count != 0) {
					log = log + ", " ;
				}
				log = log + i + "(" + dependenct.getValueVector()[i] + ")";
				count++;
			}
			log = log + "]" + "size=" + values.length;
			logger.debug(log);
		}
	}
	
	public FeatureModel(List<ControlPrimitive> list){
		this.list = list;
	}
	
	/**
	 *
	 * @return
	 */
	public List<ControlPrimitive> getSortedControlPrimitives () {
		return list;
	}
	
	/**
	 * 
	 * @param entry
	 * @param itr
	 * @param values
	 * @return
	 */
	private VarEntity[] setIntersectionEntity(Map<Branch, List<List<VarEntity>>> tempMap, Branch branch, Map.Entry<Branch, Integer[][]> entry,
			List<Map.Entry<Branch, Integer[][]>> itr, Integer[] values, int layer) {
		layer++;
		VarEntity[] ve = new VarEntity[entry.getValue().length];
		List<VarEntity> l = new ArrayList<VarEntity>();

//		if (!tempMap.containsKey(branch)) {
//			tempMap.put(branch, new ArrayList<List<VarEntity>>());
//				
//		}
//		
//		if(layer == tempMap.get(branch).size()) {
//			tempMap.get(branch).add(layer, l);	
//		}
		
		if (layer < itr.size()) {
			Map.Entry<Branch, Integer[][]> next = itr.get(layer);
			for (int i = 0; i < entry.getValue().length; i++) {

				VarEntity[] v = setIntersectionEntity(tempMap, branch, next, itr,
						getIntersection(values, entry.getValue()[i], branch, entry.getKey(), null), layer);
				VarEntity temp = null;
				if (tempMap.containsKey(branch)
						&& (temp = isSameRange(tempMap.get(branch).get(layer),
								v)) != null) {
					ve[i] = temp;
				} else {

					ve[i] = new VarEntity(chromosome.indexOf(entry.getKey()),
							null, v);
					l.add(ve[i]);
				}
			}
		} else {

			for (int i = 0; i < entry.getValue().length; i++) {

				Integer[] v = getIntersection(values, entry.getValue()[i], branch, entry.getKey(), null);
				VarEntity temp = null;
				if (tempMap.containsKey(branch)
						&& (temp = isSameRange(tempMap.get(branch).get(layer),
								v)) != null) {
					ve[i] = temp;
				} else {

					ve[i] = new VarEntity(chromosome.indexOf(entry.getKey()),
							v, null);
					l.add(ve[i]);
				}

			}

		}

		// This should not occur.
//		if(!tempMap.get(branch).get(layer).equals(l)) {
//			tempMap.get(branch).get(layer).addAll(l);
//		}
//		
		
		return ve;
	}
	
	
	private VarEntity[] setUnionEntity(Map<Branch, List<List<VarEntity>>> tempMap, Branch branch, Map.Entry<Branch, Integer[][]> entry,
			List<Map.Entry<Branch, Integer[][]>> itr, Integer[] values, int layer) {
		layer++;
		VarEntity[] ve = new VarEntity[entry.getValue().length];
		List<VarEntity> l = new ArrayList<VarEntity>();

//		if (!tempMap.containsKey(branch)) {
//			tempMap.put(branch, new ArrayList<List<VarEntity>>());
//				
//		}
//		
//		if(layer == tempMap.get(branch).size()) {
//			tempMap.get(branch).add(layer, l);	
//		}
		
		if (layer < itr.size()) {
			Map.Entry<Branch, Integer[][]> next = itr.get(layer);
			for (int i = 0; i < entry.getValue().length; i++) {

				VarEntity[] v = setUnionEntity(tempMap, branch, next, itr,
						getUnion(values, entry.getValue()[i]), layer);
				VarEntity temp = null;
				if (tempMap.containsKey(branch)
						&& (temp = isSameRange(tempMap.get(branch).get(layer),
								v)) != null) {
					ve[i] = temp;
				} else {

					ve[i] = new VarEntity(chromosome.indexOf(entry.getKey()),
							null, v);
					l.add(ve[i]);
				}
			}
		} else {

			for (int i = 0; i < entry.getValue().length; i++) {

				Integer[] v = getUnion(values, entry.getValue()[i]);
				VarEntity temp = null;
				if (tempMap.containsKey(branch)
						&& (temp = isSameRange(tempMap.get(branch).get(layer),
								v)) != null) {
					ve[i] = temp;
				} else {

					ve[i] = new VarEntity(chromosome.indexOf(entry.getKey()),
							v, null);
					l.add(ve[i]);
				}

			}

		}

//		// This should not occur.
//		if(!tempMap.get(branch).get(layer).equals(l)) {
//			tempMap.get(branch).get(layer).addAll(l);
//		}
		
		
		return ve;
	}
	
	
	private Integer[] getIntersection(Integer[] a, Integer[] b, Branch key, Branch added, VarEntity[] entities) {
		
		if(a == null) return b;
		if(b == null) return a;
		
		Set<Integer> set = new HashSet<Integer>();
		List<Integer> inter = new ArrayList<Integer>();
		for(Integer i : a) {
			set.add(i);
		}
		
		for(Integer i : b) {
			if(set.contains(i)) {
				inter.add(i);
			}
		}
		
		// Resolve extreme null cases of at-least-one-exist
		if(inter.size() == 0) { 
			String name = "";
			boolean isHasNumeric = false;
			if(added != null) {
				name = added.getName() + " ";
			} else {
				List<Integer> l = new ArrayList<Integer>();
				
				for (Integer i : entities[0].getMainVariablesByDependentVariable(l)) {
					if(key.isMainOfNumericDependency(chromosome.get(i))) {
						isHasNumeric = true;
					}
					name = name + chromosome.get(i).getName() + " ";
				}
			}
			
//			if(isHasNumeric) {
//				throw new RuntimeException("When perform intersection for " + key.getName() + ", adding " + name + "causing empty set. This is possibly a design error, as intersection leads to empty set while the dependency chain involve numeric dependency, please double check and revist if necessary");	
//			}
			
			if((a.length == 1 && a[0] == 0 && key.isCanSwitchOff()) ||
					(b.length == 1 && b[0] == 0 && key.isCanSwitchOff())) {
				logger.warn("When perform intersection for " + key.getName() + ", adding " + name + "causing empty set, we try fix 0 to represent the feature can only be disable under such secnario");
				
				inter.add(0);
				// This should not occur for correct FM.
			} else if(key.isCanSwitchOff()) {
				
				logger.warn("When perform intersection for " + key.getName() + ", adding " + name + "causing empty set, we try fix 0 to represent the feature can only be disable under such secnario"
						+ ", notice that both end can have more optional values than switch off");
				
				inter.add(0);
			} else {
				throw new RuntimeException("When perform intersection for " + key.getName() + ", adding " + name + "causing empty set. This is possibly a design error, as intersection leads to empty set while the depedent features cannot be switch off?");
			}
		}
		
		// Might be this is not necessary.
		Collections.sort(inter);
		
		return inter.toArray(new Integer[inter.size()]);
	}
	
	private Integer[] getUnion(Integer[] a, Integer[] b) {
		
		if(a == null) return b;
		if(b == null) return a;
		
		List<Integer> list = new ArrayList<Integer>();
		for(Integer i : a) {
			if(!list.contains(i)) {
				list.add(i);
			}
		}
		
		for(Integer i : b) {
			if(!list.contains(i)) {
				list.add(i);
				}		
		}
		
		// Might be this is not necessary.
		Collections.sort(list);
		
		return list.toArray(new Integer[list.size()]);
	}
	
	
	private VarEntity isSameRange(List<VarEntity> a, Integer[] b) {
		for(VarEntity v : a) {
			boolean r = isSameRange(v.getOptionalValues(), b);
			if(r) return v;
		}
		
		return null;
	}
	
	private VarEntity isSameRange(List<VarEntity> a, VarEntity[] b) {
		for(VarEntity v : a) {
			boolean r = isSameRange(v.getNext(), b);
			if(r) return v;
		}
		
		return null;
	}
	
	
	private boolean isSameRange(Integer[] a, Integer[] b) {
		if(a.length != b.length) return false;
		
		for (int i = 0; i < a.length; i++) {
			if(a[i] != b[i]) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isSameRange(VarEntity[] a, VarEntity[] b) {
		if(a.length != b.length) return false;
		
		for (int i = 0; i < a.length; i++) {
			if(!a[i].equals(b[i])) {
				return false;
			}
		}
		
		return true;
	}

}
