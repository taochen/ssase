package org.ssase.model.sam;

public class ModelFunctionConfigGenerator {
	
	private static double[] otherFunctionANN = {
		6000, //15000
		5,
		0.15,
		0.01
	};
	
	private static double[] throughputFunctionANN = {
		6000,
		5,
		0.25,
		0.01
	};
	
	/**
	 * We assume the identical configuration first
	 */
    private static double[][] functionResult = {
		/*ANN*/new double[]{},
		/*ARMAX*/new double[]{},
		/*RT*/new double[]{}
	};
    
    
    private static double[][] structureResult = {
    	/*ANN*/new double[]{3, 9,/*30*/ 0.35},
    	/*ARMAX*/new double[]{1,3},
    	/*RT*/new double[]{}
	};
	

	public static double[][] getFunctionConfiguration(String name){
		double[][] local = functionResult;
		
		if ("Throughput".equals(name)) {
			local[ModelFunction.ANN] = throughputFunctionANN;
		} else {
			local[ModelFunction.ANN] = otherFunctionANN;
		}
		
		return local;
	}
	
	public static double[][] getStructureConfiguration(String name){
		return structureResult;
	}
}
