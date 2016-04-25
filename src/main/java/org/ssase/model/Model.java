package org.ssase.model;


import java.io.Serializable;
import java.util.List;

import org.ssase.objective.QualityOfService;
import org.ssase.observation.listener.ModelListener;
import org.ssase.primitive.Primitive;

/**
 * Note that the measured value are updated via the Repository. But this
 * model would aware of change as long as they using the same reference of primitives.
 * 
 * The values are assumed to be normalized (in the QoS class) and plain in their time series order.
 * Both CP(t) and EP(t) are placed in the right index in the array as well.
 * 
 * The formatting function here would automatically shift EP(t) to EP(t-1), which
 * matches with CP(t) and QoS(t).
 * 
 * In addition, it also filter 0 value of the QoS and eliminate the corresponding primitives
 * as well.
 * @author tao
 *
 */
public interface Model extends Serializable{
	
	// Eliminate cases where the objective has zero value.
	public static final boolean isEliminateZero = true;

	
	public List<Primitive> getInputs();

	public QualityOfService getOutput();
	
	public void selectPrimititvesAndTrainModels();
	
	/**
	 * To test the error of the current model against newly measured data.
	 * if it is worse than a threshold, then in the next training, it will need
	 * structure change.
	 */
	public void updateNewlyError (double[] xValue,double yValue);
	
	/**
	 * To test the error of the current model against newly measured data.
	 * if it is worse than a threshold, then in the next training, it will need
	 * structure change.
	 * 
	 * This is used when newly measured data is feed as batch.
	 */
	public void updateNewlyErrorWithReturn(double[] result);
	/**
	 * To test the error of the current model against newly measured data.
	 * if it is worse than a threshold, then in the next training, it will need
	 * structure change.
	 * 
	 * This is used when newly measured data is feed as batch.
	 */
	public double[] updateNewlyErrorWithReturn (double[] xValue,double yValue);
	
	public double predict(double[] xValue, boolean isSU, double a, double b);
	
	
	public double predict(double[] xValue, int index);
	
	public double getYMax();
	
	public double getXMax(int i);
	
	public void addListener (ModelListener listener);
	
	
	public Primitive get (int i);
	
	public int getSize ();
	
	public int countFunction();


}
