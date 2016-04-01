package org.ssase.objective;

import java.util.List;

import org.ssase.observation.listener.Listener;
import org.ssase.primitive.Primitive;

public class ScalarizedObjective implements Objective {

	private Objective[] objectives;
	private double[] weights;

	protected double constraint;
	
	
	protected boolean isMin;
	
	// To what extent is said the change on model is significant.
	protected double changeP;
	
	public ScalarizedObjective(Objective[] objectives, double[] weights) {
		super();
		this.objectives = objectives;
		this.weights = weights;
	}

	@Override
	public double[] getArray() {

		double[] results = new double[objectives[0].getArray().length];
		for (int i = 0; i < objectives[0].getArray().length; i++) {
			double sum = 0;
			for (int j = 0; j < objectives.length; j++) {
				sum += weights[j] * objectives[j].getArray()[i];
			}
			results[i] = sum;
		}
		return results;
	}

	@Override
	public boolean isQoS() {
		return false;
	}

	@Override
	public double predict(double[] xValue) {
		double sum = 0;
		for (int j = 0; j < objectives.length; j++) {
			sum += weights[j] * objectives[j].predict(xValue);
		}
		return sum;
	}

	@Override
	public boolean isMin() {
		// TODO Auto-generated method stub
		return isMin;
	}
	

	@Override
	public double getConstraint() {
		return constraint;
	}

	@Override
	public boolean isSatisfied(double[] xValue) {
		return isMin? constraint > predict(xValue) : constraint < predict(xValue);
	}
	
	public Objective[] getObjectives(){
		return objectives;
	}

	@Override
	public boolean isBetter(double v1, double v2) {
		return (isMin()) ? v1 < v2  : v1 > v2;
	}

	@Override
	public boolean isChangeSignificant(double v1, double v2) {
		return v1/v2 > changeP ;
	}
	
	public void addListener(Listener listener) {
		for (int j = 0; j < objectives.length; j++) {
			objectives[j].addListener(listener);
		}
	}

	@Override
	public Objective getMainObjective() {
		return this;
	}

	@Override
	public boolean isSensitiveToTheSamePrimitive(Objective another, List<Primitive> inputs ) {
	
		for (int j = 0; j < objectives.length; j++) {
			if (objectives[j].isSensitiveToTheSamePrimitive(another, inputs)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int countObjective() {
		return objectives.length;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public List<Primitive> getPrimitivesInput() {
		// TODO implement generated primitives.
		return null;
	}

	@Override
	public boolean isViolate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getCurrentPrediction() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMax() {
		// TODO Auto-generated method stub
		return 0;
	}

}
