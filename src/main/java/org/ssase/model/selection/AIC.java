package org.ssase.model.selection;

import org.ssase.model.sam.ModelFunction;
import org.ssase.model.sam.ml.ann.NeuralNetwork;
import org.ssase.objective.QualityOfService;

public class AIC  {

	

	/*@Override
	public double evaluate(ModelFunction model, boolean isUseTimeSeries) {
		int k = isUseTimeSeries? model.getNumOfInput()*2 + 2 : model.getNumOfInput();
		int degree = 0;
		if (model instanceof NeuralNetwork) {
			for (int i = 0; i < model.getDegree().length; i++) {
				
				if((i+1) >  model.getDegree().length -1) {
					break;
				}
				
				degree += model.getDegree()[i] * model.getDegree()[i+1];
			}
			
			k = k * model.getDegree()[0] + degree +  model.getDegree()[model.getDegree().length - 1];
		}
		return estimate(model.getResidualSumOfSquares(), model.getSampleSize(), k);
	}
	*/
	
	public static void main (String[] a){
		System.out.print(estimate(235.20430083138467,350,3));
	}
	
	private static double estimate (double rss, long n, int k) {
		if (n/k < 40) {
			return n * Math.log(rss/n) + 2*k + (2*k*(k+1))/(n-k-1);
		} else {
			return n * Math.log(rss/n) + 2*k;
		}
	}

}
