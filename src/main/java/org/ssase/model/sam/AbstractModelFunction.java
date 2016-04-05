package org.ssase.model.sam;

import org.ssase.objective.QualityOfService;

public abstract class AbstractModelFunction implements ModelFunction {
	


	@Override
	public double predict(double[] xValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getResidualSumOfSquares() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSMAPE() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public double getMAPE() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRSquares() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double calculateEachSMAPE(double ideal, double actual){
		
		if (ideal == 0) {
			return actual; 
		}
		
		if ((actual > 0 && ideal < 0) || (actual < 0 && ideal > 0)) {
			return Math.abs((ideal + actual) / (ideal - actual));
		} else {
			return Math.abs((ideal - actual) / (ideal + actual));
		}
	}
	
	public double calculateEachMAPE(double ideal, double actual){	
		
		if (ideal == 0) {
			return actual; 
		}
		
		return ideal==0? 0 : Math.abs(ideal - actual)/ideal;
	}
}
