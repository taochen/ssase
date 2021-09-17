package org.ssase.objective.optimization.flash;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.femosaa.core.EAConfigure;
import org.femosaa.core.SASSolutionInstantiator;
import org.femosaa.seed.Seeder;
import org.ssase.objective.Objective;
import org.ssase.objective.optimization.rs.RSRegion;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.region.OptimizationType;
import org.ssase.region.Region;
import org.ssase.util.Repository;

import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import weka.core.Instances;
import weka.classifiers.trees.REPTree;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

public class Flash extends Algorithm {
	
	private SASSolutionInstantiator factory = null;
	private Seeder seeder = null;
	private Instances[] datasets;
	
	public static double[] weights = new double[0];
	
	public static REPTree[] models;

	public Flash(Problem problem) {
		super(problem);
	} // IBEA 
	
	/**
  	 * Constructor
  	 * @param problem Problem to solve
  	 */
	public Flash(Problem problem, SASSolutionInstantiator factory) {
		super(problem);
        this.factory = factory;
	}
	@Override
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		
		List<Objective> o = new ArrayList<Objective>();
		Set<Objective> objs = Repository.getAllObjectives();
		
		for (Objective ob : objs) {
			if ("sas-rubis_software-P1".equals(ob.getName())) {
				o.add(ob);
			} 
		}
		
		for (Objective ob : objs) {
			if ("sas-rubis_software-P2".equals(ob.getName())) {
				o.add(ob);
			} 
		}
		
		
		weights = (double[])getInputParameter("weights");
		
		SolutionSet solutionSet = null;
		int max_measurement = 50;
		int measurement = 0;
		if(getInputParameter("seeds") != null) {
			solutionSet = (SolutionSet)getInputParameter("seeds");
			measurement += factory.record(solutionSet);
		}
		
		int obj = solutionSet.get(0).numberOfObjectives();
		datasets = new Instances[obj];
		models = new REPTree[obj];
		
		//weka.classifiers.trees.REPTree;
		
		//Instances inst = new Instances();
		
		for (int k = 0; k < obj; k++) {
					
			
			ArrayList<Attribute> attrs = new ArrayList<Attribute>();
			Instances dataset = new Instances("data_instances", attrs, 0);
			dataset.setClassIndex(dataset.numAttributes() - 1);
			
			for (int i = 0; i < solutionSet.size(); i++) {
				Instance trainInst = new DenseInstance(dataset.numAttributes());
				
				for (int j = 0; j < dataset.numAttributes() - 1; j++) {
					//(FEMOSAASolution) solutionSet.get(i)
					
					//trainInst.setValue(j, ));
				}
				
				trainInst.setValue(dataset.numAttributes() - 1, solutionSet.get(i).getObjective(k));
				trainInst.setDataset(dataset);
				dataset.add(trainInst);
			}
			
			models[k] = new REPTree();
			try {
				models[k].buildClassifier(dataset);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public Solution random (List<Objective> o) {
		Region.selected = OptimizationType.RS;

		RSRegion moead = new RSRegion(weights);
		moead.addObjectives(o);
		long time = System.currentTimeMillis();
		return moead.raw_optimize();		
		
	}
	
	public void evaluate(Solution s) throws Exception {
		final Instance inst = new DenseInstance(s.getDecisionVariables().length + 1);
		for (int i = 0; i < s.getDecisionVariables().length; i++) {
			inst.setValue(i, s.getDecisionVariables()[i].getValue());
		}

		for (int i = 0; i < s.numberOfObjectives(); i++) {
			s.setObjective(i, models[i].distributionForInstance(inst)[0]);
		}
	}

}
