package org.ssase.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ssase.model.Model;
import org.ssase.objective.QualityOfService;
import org.ssase.observation.listener.ModelListener;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.util.Repository;

public class DelegateModel implements Model {

	private QualityOfService output;
	private Set<Primitive> set;
	private List<Primitive> list;
	private String name;

	private Delegate delegate;

	public DelegateModel(String name, Set<Primitive> set,
			QualityOfService output) {
		this.name = name;
		this.set = set;
		this.output = output;
		list = new ArrayList<Primitive>();
		list.addAll(set);
	}

	public void setDelegate(Delegate delegate) {
		this.delegate = delegate;
	}

	public List<Primitive> getInputs() {
		return list;
	}

	public QualityOfService getOutput() {
		// TODO Auto-generated method stub
		return output;
	}

	public void selectPrimititvesAndTrainModels() {
		

	}

	public void updateNewlyError(double[] xValue, double yValue) {
		// TODO Auto-generated method stub

	}

	public void updateNewlyErrorWithReturn(double[] result) {
		// TODO Auto-generated method stub

	}

	public double[] updateNewlyErrorWithReturn(double[] xValue, double yValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public double predict(double[] xValue, boolean isSU, double a, double b) {
		
		List<ControlPrimitive> cp = Repository.getSortedControlPrimitives(output);
		double[] newX = new double[xValue.length]; 
		for (int i = 0; i < newX.length; i++) {
			newX[i] = xValue[list.indexOf(cp.get(i))];
		}
		
		return delegate.predict(newX);
	}

	public double predict(double[] xValue, int index) {
		return delegate.predict(xValue);
	}

	public double getYMax() {
		// TODO Auto-generated method stub
		return 1;
	}

	public double getXMax(int i) {
		// TODO Auto-generated method stub
		return 1;
	}

	public void addListener(ModelListener listener) {
		// TODO Auto-generated method stub

	}

	public Primitive get(int i) {
		// TODO Auto-generated method stub
		return list.get(i);
	}

	public int getSize() {
		// TODO Auto-generated method stub
		return list.size();
	}

	public int countFunction() {
		// TODO Auto-generated method stub
		return 0;
	}

}
