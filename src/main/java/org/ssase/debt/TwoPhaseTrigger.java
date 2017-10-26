package org.ssase.debt;

import java.util.ArrayList;
import java.util.List;

import org.ssase.objective.QualityOfService;
import org.ssase.primitive.Primitive;

public class TwoPhaseTrigger {

	List<TwoPhasePrediction> list = new ArrayList<TwoPhasePrediction>();
	
	private static TwoPhaseTrigger instance;
	public static TwoPhaseTrigger getInstance(List<QualityOfService> qos){
		if(instance == null) {
			instance = new TwoPhaseTrigger(qos);
		}
		
		return instance;
	}
	
	public TwoPhaseTrigger(List<QualityOfService> qos){
		for (QualityOfService q : qos) {
			list.add(new TwoPhasePrediction(q));
		}
	}
	
	public void preTraining(){
		for (TwoPhasePrediction tpp : list) {
			tpp.preTraining();
		}
	}
	
	public void doTraining(){
		for (TwoPhasePrediction tpp : list) {
			tpp.trainOnInstance();
		}
	}
	
	public boolean isTrigger(){
		for (TwoPhasePrediction tpp : list) {
			if(tpp.predict() == 0) {
				return true;
			}
		}
		
		return false;
	}
}
