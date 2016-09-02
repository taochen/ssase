package org.ssase.debt.elements;

public class Principal implements DebtEelment{
	
	private double unit = 0.0;;
	@Override
	public void setMonetaryUnit(double unit) {
		this.unit = unit;
		
	}
	/**
	 * 
	 * @param cost This can be e.g., energy or simply decision time etc.
	 * @return
	 */
	@Override
	public double getMonetaryUtility(double cost) {
		return cost * unit;
	}
	@Override
	public double getMonetaryUtility() {
		// TODO Auto-generated method stub
		return 0;
	}
}
