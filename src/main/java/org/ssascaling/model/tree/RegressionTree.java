package org.ssascaling.model.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ssascaling.AbstractModelFunction;

import com.google.common.collect.Sets;

import quickdt.Attributes;
import quickdt.Instance;
import quickdt.Node;
import quickdt.TreeBuilder;

public class RegressionTree extends AbstractModelFunction {

	private double SMAPE = 0.0;
	private double MAPE = 0.0;
	
	private Node tree;
	
	public RegressionTree (double[][] inputs, double[] output) {
		final Set<Instance> instances = Sets.newHashSet();
		final List<Attributes> attributes = new ArrayList<Attributes>();
		for (int j = 0; j < inputs.length; j++) {
			final Attributes a = new Attributes();
			for (int k = 0; k < inputs[j].length; k++) {
				a.put(String.valueOf(k), inputs[j][k]);
			}
			attributes.add(a);
			instances.add(a.classification(output[j]));
		}

		TreeBuilder treeBuilder = new TreeBuilder();
		tree = treeBuilder.buildTree(instances);
		
			
		for (int j = 0; j < attributes.size(); j++) {
			double actual = (Double)tree.getLeaf(attributes.get(j)).classification;
			MAPE += calculateEachMAPE(output[j], actual);
			SMAPE += calculateEachMAPE(output[j], actual);
		}
	}
	
	@Override
	public double predict(double[] xValue) {

		final Attributes a = new Attributes();
		for (int k = 0; k < xValue.length; k ++) {
			a.put(String.valueOf(k), xValue[k]);
		}
		
		return (Double)tree.getLeaf(a).classification;
	}

	@Override
	public double getResidualSumOfSquares() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSMAPE() {
		return SMAPE;
	}

	@Override
	public double getMAPE() {
		return MAPE;
	}

	@Override
	public double getRSquares() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getSampleSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
