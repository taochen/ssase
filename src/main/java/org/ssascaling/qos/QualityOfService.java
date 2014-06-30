package org.ssascaling.qos;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.ssascaling.Model;
import org.ssascaling.objective.Objective;
import org.ssascaling.objective.ScalarizedObjective;
import org.ssascaling.observation.listener.Listener;
import org.ssascaling.observation.listener.ModelListener;
import org.ssascaling.primitive.EnvironmentalPrimitive;
import org.ssascaling.primitive.Primitive;


@SuppressWarnings("rawtypes")
public class QualityOfService implements Objective, Comparable{

	// 0 to 1
	protected double mean;

	// 0 to 100
	protected double[] array;
	// The max numerical value
	protected double max = 0;
	
	protected String name;
	
	
	protected Model model;
	
	protected double constraint;
	// If the objective is to minimize
	protected boolean isMin;
	
	// The new value for next update
	protected double value = -1;
	
	// The new values for next update
	protected double[] values = null;
	
	// To what extent (precentage) is said the change on model is significant
	// in case of non-critical changes of model.
	protected double changeP;
	
	// The heuristics of local error.
	private double a = 0.1;
	// The heuristics of global error.
	private double b = 0.1;
	
	// If this is being part of a aggregative objective, then
	// return that aggregative one, return itself otherwise.
	private ScalarizedObjective so;
	private AtomicInteger writeLock = new AtomicInteger(0);
	

	// Least number of samples received before triggering training.
	private int leastNumberOfSample = 10;
	// Some QoS e.g., throughput's constraints need to rely on some EP.
	private EnvironmentalPrimitive ep;
	
	// Used for testing.
	private boolean isReallyTrain = true;
	
	protected QualityOfService(){
		
	}
	
	public QualityOfService (String name, double constraint, boolean isMin, double changeP) {
		super();
		this.name = name;
		array = new double[0];
		this.constraint = constraint;
		this.isMin = isMin;
		this.changeP = changeP;
	}

	@Deprecated
	public QualityOfService (double[] array, String name) {
		super();
		this.array = array;
		this.name = name;
		for (int i = 0; i < this.array.length; i++) {
			if (max < this.array[i]) {
				max = this.array[i];
			}
		}
	
		for (int i = 0; i < this.array.length; i++) {
			this.array[i] = this.array[i]*100/max;
		}
	}

	@Deprecated
	public QualityOfService (double[] array) {
		super();
		this.array = array;
		for (int i = 0; i < this.array.length; i++) {
			if (max < this.array[i]) {
				max = this.array[i];
			}
		}
	
		for (int i = 0; i < this.array.length; i++) {
			this.array[i] = this.array[i]*100/max;
		}
	}

	/**
	 * Need to call this to init model after the QoS instance has been created.
	 * 
	 * Also can be used to re-create model when possible inputs change.
	 * @param possibleInputs
	 */
	public void buildModel(Set<Primitive> possibleInputs, double[][] functionConfig,
	        double[][] structureConfig){
		model = new Model(name, possibleInputs, this, functionConfig, structureConfig);
	}
	
	/**
	 * This is mutually exclusive with the other prepareToAddValue
	 * @param value
	 */
	public void prepareToAddValue(double value) {
		this.value = value;
	}
	
	/**
	 * This is mutually exclusive with the other prepareToAddValue
	 * @param value
	 */
	public void prepareToAddValue(double[] values) {
		this.values = values;
		this.value  = values [ values.length - 1];
	}

	
	public void doAddValue(){
		
		if (values != null) {
			addValues(values);
		} else {
			addValue(value);
		}
	}
	/**
	 * This is the final stage for updating the model when new data is available
	 * @param value
	 */
	public void doTraining () {
		
		// Add some data first in case there is too less data samples.
		if (!isReallyTrain || array.length < leastNumberOfSample) {
			return;
		}
		System.out.print("***** doing training *********\n");
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		synchronized(writeLock) {
			while (writeLock.get() != 0) {
				try {
					writeLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
			}
			
			// Actually update the new values to primitives
			// Update primitives first, QoS is updated the latest.
			// We need to make sure that 'prepareToAddValue' has already been invoked when
			// we use this function here.
			//model.updatePossiblePrimitives();
			
			
			// For determining if there is need to trigger structure change
			// on the next training.
			updateNewlyError();
			
			// For updating the heuristics of local and global errors.
			updateGlobalAndLocalErrorHeuristics();
		
			
			
			model.selectPrimititvesAndTrainModels();
		
			// Only reset the array, as 'value' would be used
			// as the current state of this QoS.
			values = null;
		}
		
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
	}

	public double[] getArray() {
		return array;
	}

	private void addValues(double[] values) {
		for (double v : values) {
			addValue(v);
		}
	}
	
	/**
	 * This is perform after training is done.
	 * This has to be called with 'doTraining' function
	 */
	public void updateGlobalAndLocalErrorHeuristics(){
		
		// Do nothing for the first run.
		if (model.getInputs().size() == 0) {
			return;
		}
		
		
		if (values != null) {
			for (int k = 0; k < values.length; k++) {
				double[] xValue = new double[model.getSize()];
				for (int i = 0; i < xValue.length; i++) {
					xValue[i] = model.get(i).getArray()[model.get(i).getArray().length - 
					                                    values.length + k]/model.get(i).getMax();
				}
				
				calculateRelativeImportance(xValue,  values[k]*100/max);
			}
		// If there is only single newly measured data sample.	
		} else {
			double[] xValue = new double[model.getSize()];
			for (int i = 0; i < xValue.length; i++) {
				xValue[i] = model.get(i).getValue()/model.get(i).getMax();
			}
			
			calculateRelativeImportance(xValue, value*100/max);
		}
		
		
		
	}
	
	/**
	 * Calculate the importance of local and global errors.
	 */
	private void calculateRelativeImportance(double[] xValue, double expectedY){
		double aMape = 0.0;
		double bMape = 0.0;
		System.out.print("***** The prediction MAPE is " +  (Math.abs(model.predict(xValue, true, a, b) - expectedY )/(expectedY))  +"*********\n");
		if ((aMape = Math.abs(model.predict(xValue, true, 1, 0) - expectedY )/(expectedY)) <
				(bMape = Math.abs(model.predict(xValue, true, 0, 1) - expectedY)/(expectedY))){
		    System.out.print("aMape: " + aMape + ", bMape: " + bMape + "\n");
			a += bMape - aMape;
			
		} else if (aMape > bMape){
			System.out.print("aMape: " + aMape + ", bMape: " + bMape + "\n");
			b += aMape - bMape;
		}
		
		
		System.out.print("***** The new a is" + a + ", the new b is " + b +"*********\n");
	}
	
	// TODO change it to private
	public void addValue(double value) {
		if (value > max) {
			double[] newArray = new double[array.length + 1];
			for (int i = 0; i < newArray.length-1; i++) {
				newArray[i] = array[i]*max/value;
			}
			
			newArray[array.length] = 100; /*this means the new value of precentage*/
			
			max = value;
			array = newArray;
		} else {
			
			double[] newArray = new double[array.length + 1];
			System.arraycopy(array, 0, newArray, 0, array.length);
			
			newArray[array.length] =  max == 0? 0 : value*100/max;
			
			array = newArray;
			
		}
		
		mean = 0;
		for (double d : array) {
			mean +=d;
		}
		
		this.mean = mean/(array.length*100);
	}

	public void removeHistoreicalValues(int no) {
		double[] newArray = new double[array.length - no];
		System.arraycopy(array, no, newArray, 0, array.length);
		
		array = newArray;
		mean = 0;
		for (double d : array) {
			mean +=d;
		}
		
		this.mean = mean/array.length;
	}
	
	public double getMean(){
		return mean;
	}
	
	public double getMax(){
		return max;
	}


	@Override
	public int compareTo(Object arg0) {
		return this.hashCode();
	}


	@Override
	public boolean isQoS() {
		return true;
	}

	/**
	 * Always predict the one that unseen based on newest data.
	 * With this, we can have the EP slot as null.
	 */
	@Override
	public double predict(double[] xValue) {
		return predict(xValue, true);
	}
	
	/**
	 * Predict any known data sample.
	 * @param xValue
	 * @return
	 */
	public double arbitaryPredict(double[] xValue) {
		return predict(xValue, false);
	}
	
	private double predict(double[] xValue, boolean isLatestEP) {
		
		double reuslt = 0.0;
		synchronized(writeLock) {
			writeLock.incrementAndGet();
		}
		
		double[] x = new double[xValue.length];
		for (int i = 0; i < x.length; i++) {
			// get the latest EP here.
			if (isLatestEP && model.get(i) instanceof EnvironmentalPrimitive) {
			   x[i] = model.get(i).getProvision()/model.get(i).getMax();
			} else {
			   x[i] = xValue[i]/model.get(i).getMax();
			}
		}
		
	
		reuslt = model.predict(xValue, true, a, b)*max/100;
		
		
		synchronized(writeLock) {
			writeLock.decrementAndGet();
			if (writeLock.get() == 0) {
				writeLock.notifyAll();
			}
		}
		
		return reuslt;
	}

	public void addListener(Listener listener) {
		synchronized(writeLock) {
			writeLock.incrementAndGet();
		}
		
		model.addListener((ModelListener)listener);
		
		synchronized(writeLock) {
			writeLock.decrementAndGet();
			if (writeLock.get() == 0) {
				writeLock.notifyAll();
			}
		}
		
	}
	
	
	/**
	 * To test the error of the current model against newly measured data.
	 * if it is worse than a threshold, then in the next training, it will need
	 * structure change.
	 * 
	 * This should be called before the training (before selectPrimititvesAndTrainModels)
	 * 
	 * This has to be called with 'doTraining' function
	 */
	public void updateNewlyError() {
		/*synchronized(writeLock) {
			writeLock.incrementAndGet();
		}*/
		
		// Do nothing for the first run.
		if (model.getInputs().size() == 0) {
			return;
		}
		
		if (values != null) {
			double[] result = new double[model.countFunction()];
			double[] sub = null;
			for (int k = 0; k < values.length; k++) {
				double[] xValue = new double[model.getSize()];
				for (int i = 0; i < xValue.length; i++) {
					xValue[i] = model.get(i).getArray()[model.get(i).getArray().length - 
					                                    values.length + k]/model.get(i).getMax();
				}
				
				
				sub = model.updateNewlyErrorWithReturn(xValue, values[k]/max);
				for (int i = 0; i < sub.length; i++) {
					result[i] += sub[i];
				}
			}
			for (int i = 0; i < result.length; i++) {
				// Here, we use the average error of all batch data for each learning algorithmn. 
				result [i] = result[i] / values.length;
			}
			model.updateNewlyErrorWithReturn(result);
		// If there is only single newly measured data sample.	
		} else {
			double[] xValue = new double[model.getSize()];
			for (int i = 0; i < xValue.length; i++) {
				xValue[i] = model.get(i).getValue()/model.get(i).getMax();
			}
			
			model.updateNewlyError(xValue, value/max);
		}
		
		
		
		
		
		/*synchronized(writeLock) {
			writeLock.decrementAndGet();
			if (writeLock.get() == 0) {
				writeLock.notifyAll();
			}
		}*/
	}
	
	public boolean isSensitiveToTheSamePrimitive(Objective another, List<Primitive> anotherInputs ) {
		
		boolean result = false;
		synchronized(writeLock) {
			writeLock.incrementAndGet();
		}
		
		
		/**
		 * Will need to double sync for both of the two compared objectives.
		 */
		if (another != null) {
			result = another.isSensitiveToTheSamePrimitive(null, model.getInputs());		
		} else {
			final List<Primitive> inputs = model.getInputs();
			for (Primitive p : inputs) {
				
				if (result) {
					break;
				}
				
				
				for (Primitive subP : anotherInputs) {
					if(subP.equals(p)) {
						result = true;
						break;
					}
				}
				
			}
		}
		
		synchronized(writeLock) {
			writeLock.decrementAndGet();
			if (writeLock.get() == 0) {
				writeLock.notifyAll();
			}
		}
		
		return result;
	}
	
	public void addListener (ModelListener listener) {
		synchronized(writeLock) {
			model.addListener(listener);
		}
	}

	@Override
	public boolean isMin() {
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


	@Override
	public boolean isBetter(double v1, double v2) {
		return (isMin()) ? v1 < v2  : v1 > v2;
	}


	@Override
	public boolean isChangeSignificant(double v1, double v2) {
		return v1/v2 > changeP ;
	}


	@Override
	public Objective getMainObjective() {
		return so == null? this : so;
	}


	@Override
	public int countObjective() {
		return 1;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public List<Primitive> getPrimitivesInput() {
		
		return model.getInputs();
	}

	@Override
	public boolean isViolate() {
		
		if (ep != null) {
		// If make no sense if the required throughput even larger than the current workload.
		if (isMin? constraint < ep.getLatest() : constraint > ep.getLatest()) {
			return false;
		}
		
		}
		
		return isMin? constraint < value : constraint > value;
	}
	
	public void setIsReallyTrain(boolean isReallyTrain){
		this. isReallyTrain = isReallyTrain;
	}
	
	public void setEP (EnvironmentalPrimitive ep) {
		this.ep = ep;
	}
	
}
