package org.soa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import jmetal.core.Variable;
import jmetal.encodings.variable.Int;
import jmetal.util.JMException;
import java.util.Comparator;
import org.femosaa.core.SASSolution;
import org.ssase.objective.Objective;
import org.ssase.objective.optimization.bb.BranchAndBoundRegion;
import org.ssase.objective.optimization.femosaa.FEMOSAASolution;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.primitive.SoftwareControlPrimitive;
import org.ssase.region.Region;
import org.ssase.util.Repository;

/**
 * This is just for SOA case
 * @author tao
 *
 */
public class ExtendedBB extends Region{
	protected static final long EXECUTION_TIME = 40000;

	@Override
	public LinkedHashMap<ControlPrimitive, Double> optimize() {
		LinkedHashMap<ControlPrimitive, Double> current = new LinkedHashMap<ControlPrimitive, Double>();

		synchronized (lock) {
			while (waitingUpdateCounter != 0) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			isLocked = true;

			List<ControlPrimitive> tempList = Repository
					.getSortedControlPrimitives(objectives.get(0));

			List<ControlPrimitive> list = new ArrayList<ControlPrimitive>();
			list.addAll(tempList);
			
			addRemoveFeatureFor01Representation(list);
			sortForDependency(list);
			
			
			
			
			Map<Objective, Double> map = getBasis();

			double[] currentDecision = new double[list.size()];

			Node best = null;
			Double[] bestResult = null;

			for (Objective obj : objectives) {
				for (Primitive p : obj.getPrimitivesInput()) {
					if (p instanceof ControlPrimitive) {
						current.put((ControlPrimitive) p,
								(double) p.getProvision());
						currentDecision[list.indexOf((ControlPrimitive) p)] = (double) p
								.getProvision();
					}
				}
			}
		
			
			for (ControlPrimitive cp : list) {
				if (!current.containsKey(cp)) {
					current.put(cp, (double) cp.getProvision());
					currentDecision[list.indexOf(cp)] = cp.getProvision();
				}
			}

			LinkedBlockingQueue<Node> q = new LinkedBlockingQueue<Node>();

//			for (double d : list.get(0).getValueVector()) {
//				q.offer(new Node(0, d, currentDecision));
//			}
			
			for (int i = list.get(0).getValueVector().length-1; i >= 0 ; i--) {
				q.offer(new Node(0, list.get(0).getValueVector()[i], currentDecision));
			}

			long start = System.currentTimeMillis();
			int count = 0;
			do {

				// To avoid an extrmely long runtime
				if ((System.currentTimeMillis() - start) > EXECUTION_TIME) {
					break;
				}

				count++;
				// System.out.print("Run " + count + "\n");

				Node node = q.poll();
				Double[] v = doWeightSum(list, node.decision, map);
				
				if (bestResult == null || v[0] > bestResult[0]) {
					best = node;
					bestResult = v;
				}

				if (node.index + 1 < list.size()) {
//					for (double d : list.get(node.index + 1).getValueVector()) {
//						q.offer(new Node(node.index + 1, d, node.decision));
//					}
					for (int i = list.get(0).getValueVector().length-1; i >= 0 ; i--) {
						q.offer(new Node(node.index + 1, list.get(0).getValueVector()[i], node.decision));
					}
				}

			} while (!q.isEmpty());

			q.clear();

			for (int i = 0; i < best.decision.length; i++) {
				current.put(list.get(i), best.decision[i]);
			}

			for (ControlPrimitive cp : list) {
				if (!tempList.contains(cp)) {
					current.remove(cp);
				}
			}

			list = tempList;

			// Starting the dependency check and log

			double[][] optionalVariables = new double[list.size()][];
			for (int i = 0; i < optionalVariables.length; i++) {
				optionalVariables[i] = list.get(i).getValueVector();
			}

			// This is a static method
			SASSolution.init(optionalVariables);
			SASSolution.clearAndStoreForValidationOnly();
			
			
			FEMOSAASolution dummy = new FEMOSAASolution();
			dummy.init(objectives, null);
			Variable[] variables = new Variable[list.size()];
			for (int i = 0; i < list.size(); i++) {
				variables[i] = new Int(0,
						list.get(i).getValueVector().length - 1);
			}

			dummy.setDecisionVariables(variables);

			for (int i = 0; i < list.size(); i++) {

				double v = current.get(list.get(i));
				double value = 0;

				for (int j = 0; j < list.get(i).getValueVector().length; j++) {
					if (list.get(i).getValueVector()[j] == v) {
						value = j;
						break;
					}
				}

				//System.out.print(list.get(i).getName() + ", v=" + v + ", index=" + value +"\n");
				try {
					dummy.getDecisionVariables()[i].setValue(value);
				} catch (JMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			Region.logDependencyForFinalSolution(dummy);

			if (!dummy.isSolutionValid()) {
				try {
					dummy.correctDependency();
					current.clear();
					for (int i = 0; i < list.size(); i++) {
						

						current.put(list.get(i),
								list.get(i).getValueVector()[ (int)dummy.getDecisionVariables()[i].getValue()]);
					}

					System.out
							.print("The final result does not satisfy all dependency, thus correct it\n");

				} catch (JMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			print(current);
			isLocked = false;
			lock.notifyAll();
		}
		System.out
				.print("================= Finish optimization ! =================\n");

		return current;
	}

	private Double[] doWeightSum(List<ControlPrimitive> list,
			double[] decision, Map<Objective, Double> map) {
		double result = 0;
		double satisfied = 1;
		double[] xValue;
		double w = 1.0 / objectives.size();
		for (Objective obj : objectives) {
			xValue = new double[obj.getPrimitivesInput().size()];
			// System.out.print(obj.getPrimitivesInput().size()+"\n");
			for (int i = 0; i < obj.getPrimitivesInput().size(); i++) {
				if (obj.getPrimitivesInput().get(i) instanceof ControlPrimitive) {
					xValue[i] = decision[list.indexOf(obj.getPrimitivesInput()
							.get(i))];
				} else {
					xValue[i] = ((EnvironmentalPrimitive) obj
							.getPrimitivesInput().get(i)).getLatest();
				}
			}

			if (!obj.isSatisfied(xValue)) {
				// System.out.print(obj.getName() + " is not satisfied " +
				// obj.predict(xValue) + "\n");
				// throw new RuntimeException();
				satisfied = -1;
			}
			
			result = obj.isMin() ? result - w
					* (obj.predict(xValue) / (1 + map.get(obj))) : result + w
					* (obj.predict(xValue) / (1 + map.get(obj)));
		}
		
		return new Double[] { result, satisfied };
	}

	private Map<Objective, Double> getBasis() throws RuntimeException {
		Map<Objective, Double> map = new HashMap<Objective, Double>();
		double[] xValue;
		for (Objective obj : objectives) {
			xValue = new double[obj.getPrimitivesInput().size()];
			// System.out.print(obj.getPrimitivesInput().size()+"\n");
			for (int i = 0; i < obj.getPrimitivesInput().size(); i++) {
				if (obj.getPrimitivesInput().get(i) instanceof ControlPrimitive) {
					xValue[i] = obj.getPrimitivesInput().get(i).getProvision();
				} else {
					xValue[i] = ((EnvironmentalPrimitive) obj
							.getPrimitivesInput().get(i)).getLatest();
				}
			}

			map.put(obj, obj.predict(xValue));
		}

		return map;
	}

	protected double[] getValueVector(Node node, List<ControlPrimitive> list) {
		//System.out.print("count " + list.get(node.index).getName() +"\n");
		if (list.get(node.index).getName().equals("CS12")) {
			double[] r = null;
			for (int i = 0; i < node.index; i++) {

				if (list.get(i).getName().equals("CS11")
						&& node.decision[i] != 0.0) {
					return new double[] { 0.0 };
				}
				if (r != null) {
					return r;
				}
			}
		}

		if (list.get(node.index).getName().equals("CS15")) {
			double[] r = null;
			for (int i = 0; i < node.index; i++) {

				if (list.get(i).getName().equals("CS11")
						&& node.decision[i] != 0.0) {
					return new double[] { 0.0 };
				}
				if (r != null) {
					return r;
				}
			}
		}
		
		if (list.get(node.index).getName().equals("CS22")) {
			double[] r = null;
			for (int i = 0; i < node.index; i++) {

				if (list.get(i).getName().equals("CS13")
						&& node.decision[i] == 0.0) {
					return new double[] { 0.0 };
				}
				if (r != null) {
					return r;
				}
			}
		}
		
		

		if (list.get(node.index).getName().equals("CS14")) {
			
			double[] r = null;
			int count = 0;
			for (int i = 0; i < node.index; i++) {

				if (list.get(i).getName().equals("CS11")
						&& node.decision[i] == 0.0) {
					count++;
				}
				
				if (list.get(i).getName().equals("CS12")
						&& node.decision[i] == 0.0) {
					count++;
				}
				
				if (list.get(i).getName().equals("CS13")
						&& node.decision[i] == 0.0) {
					count++;
				}
				
				if (list.get(i).getName().equals("CS15")
						&& node.decision[i] == 0.0) {
					count++;
				}
			
			}
			
			
			if(count == 4) {
				return new double[] { 1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0 };
			}
		}
		
		
		if (list.get(node.index).getName().equals("CS24")) {
			double[] r = null;
			int count = 0;
			for (int i = 0; i < node.index; i++) {

				if (list.get(i).getName().equals("CS21")
						&& node.decision[i] == 0.0) {
					count++;
				}
				
				if (list.get(i).getName().equals("CS22")
						&& node.decision[i] == 0.0) {
					count++;
				}
				
				if (list.get(i).getName().equals("CS23")
						&& node.decision[i] == 0.0) {
					count++;
				}
			
			}
			
			if(count == 3) {
				return new double[] { 1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0 };
			}
		}
		
		
		if (list.get(node.index).getName().equals("CS34")) {
			double[] r = null;
			int count = 0;
			for (int i = 0; i < node.index; i++) {

				if (list.get(i).getName().equals("CS31")
						&& node.decision[i] == 0.0) {
					count++;
				}
				
				if (list.get(i).getName().equals("CS32")
						&& node.decision[i] == 0.0) {
					count++;
				}
				
				if (list.get(i).getName().equals("CS33")
						&& node.decision[i] == 0.0) {
					count++;
				}
			
			}
			
			if(count == 3) {
				return new double[] { 1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0 };
			}
		}
		
		
		
		
		if (list.get(node.index).getName().equals("CS42")) {
			double[] r = null;
			int count = 0;
			for (int i = 0; i < node.index; i++) {

				if (list.get(i).getName().equals("CS41")
						&& node.decision[i] == 0.0) {
					count++;
				}
			
			}
			
			if(count == 1) {
				return new double[] { 1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0 };
			}
		}
		
		
		
		if (list.get(node.index).getName().equals("CS53")) {
			double[] r = null;
			int count = 0;
			for (int i = 0; i < node.index; i++) {

				if (list.get(i).getName().equals("CS51")
						&& node.decision[i] == 0.0) {
					count++;
				}
				
				if (list.get(i).getName().equals("CS52")
						&& node.decision[i] == 0.0) {
					count++;
				}
				
			
			}
			
			if(count == 2) {
				return new double[] { 1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0 };
			}
		}

		return list.get(node.index).getValueVector();
	}

	private void sortForDependency(List<ControlPrimitive> list) {
		
//		Collections.sort(list, new Comparator(){
//
//			public int compare(Object o1, Object o2) {
//				//System.out.print(((ControlPrimitive)o1).getName() + "*****\n");
//				int i1 = Integer.parseInt(((ControlPrimitive)o1).getName().substring(2, 3));
//				int i2 = Integer.parseInt(((ControlPrimitive)o2).getName().substring(2, 3));
//				return i1 < i2? -1 : 1;
//			}
//			
//		});
//		
//		ControlPrimitive cp3 = list.get(3);
//		list.remove(3);
//		list.add(4, cp3);
//		
//		System.out.print("after sorted\n");
//		for (ControlPrimitive cp : list) {
//			System.out.print(cp.getName() + "\n");
//		}
		
		Collections.shuffle(list);
		
	}

	private void addRemoveFeatureFor01Representation(List<ControlPrimitive> list) {
//		SoftwareControlPrimitive c = null;
//
//		c = new SoftwareControlPrimitive("cache", "sas", false, null, null, 1,
//				1, 1, 1, 1, 1, 1, true);
//		c.setValueVector(new double[] { 0, 1 });
//		c.setProvision(1);
//		list.add(c);
//
//		c = new SoftwareControlPrimitive("cache_config", "sas", false, null,
//				null, 1, 1, 1, 1, 1, 1, 1, true);
//		c.setValueVector(new double[] { 1 });
//		c.setProvision(1);
//		list.add(c);
//
//		c = new SoftwareControlPrimitive("thread_pool", "sas", false, null,
//				null, 1, 1, 1, 1, 1, 1, 1, true);
//		c.setValueVector(new double[] { 1 });
//		c.setProvision(1);
//		list.add(c);
//
//		c = new SoftwareControlPrimitive("connection_pool", "sas", false, null,
//				null, 1, 1, 1, 1, 1, 1, 1, true);
//		c.setValueVector(new double[] { 1 });
//		c.setProvision(1);
//		list.add(c);
//
//		c = new SoftwareControlPrimitive("database", "sas", false, null, null,
//				1, 1, 1, 1, 1, 1, 1, true);
//		c.setValueVector(new double[] { 1 });
//		c.setProvision(1);
//		list.add(c);
//
//		c = new SoftwareControlPrimitive("database", "sas", false, null, null,
//				1, 1, 1, 1, 1, 1, 1, true);
//		c.setValueVector(new double[] { 1 });
//		c.setProvision(1);
//		list.add(c);
	}

	protected class Node {

		protected int index;
		protected double[] decision;

		public Node(int index, double value, double[] given) {
			this.index = index;
			decision = new double[given.length];
			for (int i = 0; i < given.length; i++) {
				decision[i] = given[i];
			}

			decision[index] = value;

		}

	}
}
