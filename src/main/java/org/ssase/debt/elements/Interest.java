package org.ssase.debt.elements;

import java.util.List;

import org.ssase.objective.QualityOfService;

public class Interest implements DebtEelment{

	
	private List<QualityOfService> qos;
	
	public Interest (List<QualityOfService> qos) {
		this.qos = qos;
	}

	@Override
	public void setMonetaryUnit(double unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getMonetaryUtility() {
		double result = 0.0;
		for (QualityOfService q : qos) {
			result += q.getMonetaryUtility(true);
		}
		return result;
	}

	@Override
	public double getMonetaryUtility(double cost) {
		// TODO Auto-generated method stub
		return 0;
	}
}
