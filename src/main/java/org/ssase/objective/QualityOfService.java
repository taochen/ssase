package org.ssase.objective;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
 
import org.ssase.model.Delegate;
import org.ssase.model.DelegateModel;
import org.ssase.model.Model;
import org.ssase.model.ModelingType;
import org.ssase.model.sam.SAMModel;
import org.ssase.observation.listener.Listener;
import org.ssase.observation.listener.ModelListener;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.util.Timer;
import org.ssase.util.Tuple;


@SuppressWarnings("rawtypes")
/**
 * Note that primitive selection can contain 0, but QoS function training may be better
 * to eliminate 0 values.
 * 
 * This can refer to all non functional quality attributes, including cost, even though there is a class named Cost.
 */
public class QualityOfService implements Objective, Comparable{

	// 0 to 1, this exclude any 0 value
	protected double mean;

	// 0 to 100
	protected double[] array;
	// The max numerical value
	protected double max = 0.0;
	// The min numerical value
	//protected double min = 0;
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
	// This will be set by historyLoader externally.
	public static int leastNumberOfSample; //340,88
	// Some QoS e.g., throughput's constraints need to rely on some EP.
	private EnvironmentalPrimitive ep;
	
	// Used for testing.
	private boolean isReallyTrain = true;
	
	// The SLA panelty in money.
	private double unit = 0.0;
	
	// The new values that has not yet being updated. This is raw value.
	protected double[] pendingValues = null;
	protected int samplingCounter = 0;
	protected int addingCounter = 0;
	protected Timer timer = new Timer();
	private static ModelingType selected;
	
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
	
	public QualityOfService (String name, double constraint, boolean isMin, double changeP, double unit) {
		super();
		this.name = name;
		array = new double[0];
		this.constraint = constraint;
		this.isMin = isMin;
		this.changeP = changeP;
		this.unit = unit;
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
	
	public static boolean isDelegate() {
		return selected == ModelingType.DELEGATE;
	}

	public static void setSelectedModelingType(String type) {
        if(type == null) throw new RuntimeException("No proper ModelingType found!");
		
		type = type.trim();
		
		if("sam".equals(type)) {
			selected = ModelingType.SAM;
		} else if("delegate".equals(type)) {
			selected = ModelingType.DELEGATE;
		} 
		System.out.print(selected+ "*****\n");
		
		if(selected == null) throw new RuntimeException("Can not find modeling method for type " + type);	
	}
	
	/**
	 * Need to call this to init model after the QoS instance has been created.
	 * 
	 * Also can be used to re-create model when possible inputs change.
	 * @param possibleInputs
	 */
	public void buildModel(Set<Primitive> possibleInputs, double[][] functionConfig,
	        double[][] structureConfig){
		if(ModelingType.SAM == selected) {
			model = new SAMModel(name, possibleInputs, this, functionConfig, structureConfig);
		} else if(ModelingType.DELEGATE == selected) {
			model = new DelegateModel(name, possibleInputs, this);
		}
	}
	
	/**
	 * This is mutually exclusive with the other prepareToAddValue
	 * @param value
	 */
	public synchronized void prepareToAddValue(double value) {
		// If the previous value has not been added.
		if (addingCounter != samplingCounter) {
			//System.out.print(addingCounter + " : " + samplingCounter + "***** queuing values! *********\n");
			if (pendingValues == null) {
				pendingValues = new double[]{this.value, value};
			} else {

				double[] newArray = new double[pendingValues.length + 1];
				System.arraycopy(pendingValues, 0, newArray, 0, pendingValues.length);
				newArray[newArray.length - 1] = value;
				pendingValues = newArray;
			}
			
		} else {
			pendingValues = new double[]{value};
		}
		
		samplingCounter++;
	}

	
	public boolean isValid(){
		return !Model.isEliminateZero ||  value != 0;
	}
	
	public synchronized void doAddValue(long samples){
		
		//System.out.print(samples + " samples it have ******\n");
		//System.out.print(pendingValues.length + " total pendingValues it have ******\n");
		
		int no = (int)(samples - array.length);
		//System.out.print(no + " the number it needs to add ******\n");
		if (no == 1) {
			values = null;
		} else {
			values = new double[no];
			System.arraycopy(pendingValues, 0, values, 0, no);
		}
		value = pendingValues[pendingValues.length - 1];
				
		
		if (values != null) {
			addValues(values);
		} else {
			addValue(value);
		}
		
		
		if (no != pendingValues.length) {
			double[] newValues = new double[pendingValues.length-no];
			System.arraycopy(pendingValues, no, newValues, 0, newValues.length);
			pendingValues = newValues;
		} else {
			pendingValues = null;
		}
		
		
		
	}
	


	public synchronized void resetValues(){
		// Only reset the array, as 'value' would be used
		// as the current state of this QoS.
		// Call this only after addValue has been invoked.
		values = null;
	}
	/**
	 * This is the final stage for updating the model when new data is available
	 * @param value
	 */
	public boolean doTraining () {
	
		// Add some data first in case there is too less data samples.
		if (!isReallyTrain || array.length < leastNumberOfSample) {
			return false;
		}
		
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
		
			
		}
		
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		return true;
	}

	
	/**
	 * This should not be use with doTraining, they are alternative.
	 * @return
	 */
	public boolean doUpdate () {
		
		// Add some data first in case there is too less data samples.
		if (!isReallyTrain || array.length < leastNumberOfSample) {
			return false;
		}
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		synchronized(writeLock) {
			while (writeLock.get() != 0) {
				try {
					writeLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
			}
			
			// For determining if there is need to trigger structure change
			// on the next training.
			updateNewlyError();
			
			// For updating the heuristics of local and global errors.
			updateGlobalAndLocalErrorHeuristics();
		
		
			
		}
		
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		return true;
	}

	
	public double[] getArray() {
		return array;
	}
	
	/*public double[] getFilteredArray(){
		int size = 0;
		double[] result;
		for (double d : array) {
			if (d != 0) {
				size++;
			}
		}
		result = new double[size];
		for (int i = 0; i < array.length;i++) {
			if (array[i] != 0) {
				result[i] = array[i]; 
			}
		}
		
	}*/

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
		if (model == null || !isValid() || model.getInputs().size() == 0) {
			return;
		}
		
		
		if (values != null) {
			if ( values[0] == 0) {
				return;
			}
			//for (int k = 0; k < values.length; k++) {
			// Only fouces on the 'next' interval data.
			int k = 0;
			double[] xValue = new double[model.getSize()];
			for (int i = 0; i < xValue.length; i++) {
				
				double factor = model.get(i).getMax() == model.getXMax(i)? 1/100 : model.get(i).getMax()/100*model.getXMax(i);
				
				if (model.get(i) instanceof EnvironmentalPrimitive) {
					xValue[i] = model.get(i).getArray()[model.get(i).getArray().length - 
					                                    values.length + k - 1]*factor;
				} else {
				
				xValue[i] = model.get(i).getArray()[model.get(i).getArray().length - 
				                                    values.length + k]*factor;
				}
			}
			
			calculateRelativeImportance(xValue,  values[k]*100/model.getYMax());
			//}
		// If there is only single newly measured data sample.	
		} else {
			double[] xValue = new double[model.getSize()];
			for (int i = 0; i < xValue.length; i++) {
				xValue[i] = model.get(i).getValue()/model.getXMax(i);
			}
			
			calculateRelativeImportance(xValue, value*100/model.getYMax());
		}
		
		
		
	}
	
	/**
	 * Only use for testing the MAPE/SMAPE of newly collected data against the model.
	 * @param index
	 * @return
	 */
	public Tuple<Double, Double> testNewData (int index){
		double[] xValue = new double[model.getSize()];
		for (int i = 0; i < xValue.length; i++) {
			xValue[i] = model.get(i).getValue()/model.getXMax(i);
		}
		
		
		double result = 0;
		if (index == 0) {
			result = model.predict(xValue, 0);
		} else if (index == 1) {
			result = model.predict(xValue, 1);
		} else if (index == 2) {
			result = model.predict(xValue, 2);
		} else {
			result = model.predict(xValue, true, a, b);
		}
		
		return new Tuple<Double, Double>(value, result*model.getYMax()/100);
	}
	
	/**
	 * Calculate the importance of local and global errors.
	 */
	private void calculateRelativeImportance(double[] xValue, double expectedY){
		double aMape = 0.0;
		double bMape = 0.0;
		
		double su = model.predict(xValue, true, a, b);
		double su10 = model.predict(xValue, true, 1, 0);
		double su01 = model.predict(xValue, true, 0, 1);
		
		//System.out.print("***** The prediction MAPE is " +  (Math.abs(su - expectedY )/(expectedY==0? 1 : expectedY))  +"*********\n");
		if ((aMape = Math.abs(su10 - expectedY )/(expectedY==0? 1 : expectedY)) <
				(bMape = Math.abs(su01 - expectedY)/(expectedY==0? 1 : expectedY))){
		    //System.out.print("aMape: " + aMape + ", bMape: " + bMape + "\n");
			a += bMape - aMape;
			
		} else if (aMape > bMape){
			//System.out.print("aMape: " + aMape + ", bMape: " + bMape + "\n");
			b += aMape - bMape;
		}
		
		
		//System.out.print("***** The new a is" + a + ", the new b is " + b +"*********\n");
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
		
		addingCounter++;
		
		mean = 0;
		int size = 0;
		for (double d : array) {
			if (!Model.isEliminateZero || d != 0){
				size ++;
			}
			mean +=d;
		}
		
		this.mean = mean/(size*100);
	}

	public synchronized void removeHistoreicalValues(int no) {
		double[] newArray = new double[array.length - no];
		System.arraycopy(array, no, newArray, 0, array.length);
		
		array = newArray;
		mean = 0;
		int size = 0;
		for (double d : array) {
			if (!Model.isEliminateZero || d != 0){
				size ++;
			}
			mean +=d;
		}
		
		this.mean = mean/(size*100);
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
	
	protected double predict(double[] xValue, boolean isLatestEP) {
		
		double reuslt = 0.0;
		synchronized(writeLock) {
			writeLock.incrementAndGet();
		}
		
		double[] x = new double[xValue.length];
		for (int i = 0; i < x.length; i++) {
			// get the latest EP here.
			if (isLatestEP && model.get(i) instanceof EnvironmentalPrimitive) {
			   x[i] = ((EnvironmentalPrimitive)model.get(i)).getLatest()/model.get(i).getMax();
			} else {
			   x[i] = ModelingType.DELEGATE == selected? xValue[i] : xValue[i]/model.get(i).getMax();
			}
		}
		
	
		reuslt = model.predict(x, true, a, b)*max/100;
		//reuslt = model.predict(x, 0)*max/100;
		/*if(name.equals("sas-rubis_software-Energy")) {
			reuslt = model.predict(x, 1)*max/100;
		} else {
			reuslt = model.predict(x, 1)*max/100;
		}*/
		
		
		synchronized(writeLock) {
			writeLock.decrementAndGet();
			if (writeLock.get() == 0) {
				writeLock.notifyAll();
			}
		}
		
		return reuslt;
	}

	public void addListener(Listener listener) {
		
		if (!(listener instanceof ModelListener)) {
			return;
		}
		
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
		if (model == null || !isValid() || model.getInputs().size() == 0) {
			return;
		}
		
		if (values != null) {
			double[] result = new double[model.countFunction()];
			double[] sub = null;
			int number = 0;
			for (int k = 0; k < values.length; k++) {
				
				if (values[k] == 0) {
					continue;
				}
				number++;
				double[] xValue = new double[model.getSize()];
				for (int i = 0; i < xValue.length; i++) {
					
					double factor = model.get(i).getMax() == model.getXMax(i)? 1/100 : model.get(i).getMax()/100*model.getXMax(i);
					
					if (model.get(i) instanceof EnvironmentalPrimitive) {
						xValue[i] = model.get(i).getArray()[model.get(i).getArray().length - 
						                                    values.length + k - 1]*factor;
					} else {
					
					xValue[i] = model.get(i).getArray()[model.get(i).getArray().length - 
					                                    values.length + k]*factor;
					}
				}
				
				
				sub = model.updateNewlyErrorWithReturn(xValue, values[k]/model.getYMax());
				for (int i = 0; i < sub.length; i++) {
					result[i] += sub[i];
				}
			}
			for (int i = 0; i < result.length; i++) {
				// Here, we use the average error of all batch data for each learning algorithmn. 
				result [i] = result[i] / number;
			}
			model.updateNewlyErrorWithReturn(result);
		// If there is only single newly measured data sample.	
		} else {
			double[] xValue = new double[model.getSize()];
			for (int i = 0; i < xValue.length; i++) {
				xValue[i] = model.get(i).getValue()/model.getXMax(i);
			}
			
			model.updateNewlyError(xValue, value/model.getYMax());
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
	
	@Override
	public double getCurrentPrediction() {
		double[] xValue = new double[model.getInputs().size()];
		for (int i = 0; i < xValue.length; i++) {			
			xValue[i] = model.getInputs().get(i).getProvision();		 
		}
		return predict(xValue);
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
		
		
		if (ep != null) {
			// If make no sense if the required throughput even larger than the
			// current workload.
			if (isMin ? constraint < ep.getLatest() : constraint > ep
					.getLatest()) {
				return true;
			}

		}
		
		return isMin? constraint >= predict(xValue) : constraint <= predict(xValue);
	}

	public double getMonetaryUtility(boolean isLatest) {
		int i = isLatest? 1 : 2;
		if (ep != null) {
			// If make no sense if the required throughput even larger than the
			// current workload.
			if (isMin ? constraint < ep.getLatest() : constraint > ep
					.getLatest()) {
				return 0;
			}

		}
		
		double v = array[array.length - i] * max / 100;
		
		return unit * (isMin? constraint - v : v - constraint);

	}
	
	public double getMonetaryUtility(boolean isLatest, double v) {
		int i = isLatest? 1 : 2;
		if (ep != null) {
			// If make no sense if the required throughput even larger than the
			// current workload.
			if (isMin ? constraint < ep.getLatest() : constraint > ep
					.getLatest()) {
				return 0;
			}

		}
		
		return unit * v;//(isMin? constraint - v : v - constraint);

	}


	public double getExtentOfViolation(boolean isLatest) {
		int i = isLatest? 1 : 2;
		if (ep != null) {
			// If make no sense if the required throughput even larger than the
			// current workload.
			if (isMin ? constraint < ep.getLatest() : constraint > ep
					.getLatest()) {
				return 0;
			}

		}
		
		/*
		 * 	
		double d = isMin? constraint - v : v - constraint;
		return  d / constraint;
		 * 
		 * */
		double v = array[array.length - i] * max / 100;
		
		return  (isMin? constraint - v : v - constraint);

	}
	
	@Override
	public boolean isBetter(double v1, double v2) {
		return (isMin()) ? v1 <= v2  : v1 >= v2;
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
			// If make no sense if the required throughput even larger than the
			// current workload.
			if (isMin ? constraint < ep.getLatest() : constraint > ep
					.getLatest()) {
				return false;
			}

		} 
		
		boolean result = isMin? constraint < value : constraint > value;
		if (result) {
			timer.increaseTimer();
		}
		
		return timer.isValidViolation();
	}
	
	public void setIsReallyTrain(boolean isReallyTrain){
		this. isReallyTrain = isReallyTrain;
	}
	
	public void setEP (EnvironmentalPrimitive ep) {
		this.ep = ep;
	}
	
	public EnvironmentalPrimitive getEP () {
		return ep;
	}
	
	public void setDelegate(Delegate delegate){
		if(model instanceof DelegateModel) {
			((DelegateModel)model).setDelegate(delegate);
			max = 1;// This is to eliminate the effect of max, as there might not be needed to normalize.
		}
	}
}
