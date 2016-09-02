package org.ssase.debt;

import org.ssase.debt.elements.Interest;
import org.ssase.debt.elements.Principal;

public class AdaptationDebtBroker {

	
	private double noAdaptationUtility = Double.NaN;
	// 0 = adapt, 1 = no adapt
	private int latestJudgement = Integer.MIN_VALUE;
	

	
	/**
	 * Only trigger this when is has been decided to adapt.
	 * 
	 * Before adaptation, if trigger
	 */
	public void doPriorDebtAnalysis(){
		noAdaptationUtility = new Interest().getMonetaryUtility();
	}
	
	/**
	 * 
	 * Just after adaptation
	 */
	public void doPosteriorDebtAnalysis(double unit, double cost){
		
		if(noAdaptationUtility == Double.NaN) return;
		
		double adaptationUtility = new Interest().getMonetaryUtility();
		Principal p = new Principal();
		p.setMonetaryUnit(unit);
		adaptationUtility = adaptationUtility - p.getMonetaryUtility(cost);
		latestJudgement = adaptationUtility > noAdaptationUtility? 0 : 1;
		
		noAdaptationUtility = Double.NaN;
	}
	
	
	public int getExpertDebtJudgement(){
		int r = latestJudgement;
		// clean it immediately
		latestJudgement = Integer.MIN_VALUE;
		return r;
	}
		
}
