package org.ssase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.IterativeKMeans;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.evaluation.AICScore;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.EuclideanDistance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.clustering.CustomIterativeKMeans;
import org.ssase.model.ann.EncogFeedForwardNeuralNetwork;
import org.ssase.model.ann.NeuralNetworkStructureSelector;
import org.ssase.model.selection.PrimitiveLearner;
import org.ssase.model.selection.StructureSelector;
import org.ssase.model.timeseries.ARMA;
import org.ssase.model.timeseries.ARMAStructureSelector;
import org.ssase.model.tree.RegressionTree;
import org.ssase.model.tree.RegressionTreeStructureSelector;
import org.ssase.observation.event.ModelChangeEvent;
import org.ssase.observation.listener.Listener;
import org.ssase.observation.listener.ModelListener;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.qos.QualityOfService;
import org.ssase.util.SSAScalingThreadPool;
import org.ssase.util.Tuple;
import org.ssase.util.Util;

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
public class Model {
	
	public static final boolean isEliminateZero = true;

	private final boolean isConsiderTimeSeriesOnLocalError = false;
	
	protected static final Logger logger = LoggerFactory
	.getLogger(Model.class);
	
	protected Set<Primitive> possibleInputs;
	
	// selectedPrimitivesMatrix
	protected List<Primitive> inputs = new ArrayList<Primitive>();
	protected QualityOfService output;
	// This is update in each interval.
	protected double outputMean = 0.0;
	protected double clusterOutputMean = 0.0;
	
	private ModelFunction[] functions;
	// Configuration array for each function.
	private double[][] functionConfig;
	private double[][] structureConfig;
	// aviod concurrent modeification of model functions.
	//private ModelFunction[] finalFunctions;
	private StructureSelector[] selectors;
	// Store clsuter results
	private Map<ModelFunction, ClusterPair[]> dataset = new HashMap<ModelFunction, ClusterPair[]>();
	
	private Map<ModelFunction, ClusterData[]> resultset = new HashMap<ModelFunction, ClusterData[]>();
	// This needs to be updated with new max value for nomalization.
	//private List<ClusterData>[] clusterData;
	//private double[] totalMape;
	// Error of the newly measured data for each function.
	private double[] lastMape;
	// Local modeling, trigger by threshold
	//private ModelFunction[][] localFunctions;
	
	private Set<ModelListener> listeners = new HashSet<ModelListener>();
	
	private String name;
	
	// Formatted data
	private double[][] formattedInput;
	private double[] formattedOutput1D;
	// a matrix of max value of x
	private double[] xMax;
	private double yMax;
	// The index of QoS which has a value of 0.
	private int invalidOuputCount = 0;
	// The difference between measurement frequency and modeling frequency.
	//private int difference = 0;
	
	// A lock for write of models when it is currently reading
	//private Boolean writeLock = false;
	// A lock for read of models when it is currently writing
	//private AtomicInteger readLock = new AtomicInteger(0);
	// A lock for concurrently modeling via different training techniques.
	private AtomicInteger concurrentModelLock = new AtomicInteger(0);
	// Only for testing
	private int startPoint = 100;
	// Threshold ************************************************
	//public final int ANN_DEFAULT_MAX_TRAINING_TIME_LIMIT = 5000; //ms
	//public final int ANN_DEFAULT_CHANGE_HIDDEN_NO = 3; 
	// Both RSS and RMAPE
	//public final double ANN_DEFAULT_BEST_ERROR_PERCENTAGE= 0.15;// 10%? this should be calculated by RMAPE % and RS/RV %
	
	//public final double ANN_DEFAULT_WOREST_ERROR_PERCENTAGE= 0.01; // 99% accuracy to prevent over-fitting

	// Used to trigger finding structure together with matrix change.
	// Also used to trigger sub modeling.
	public final double DEFAULT_PREDICTION_ERROR_PERCENTAGE= 0.5;
	// The max number of clueter
	//public final int DEFAULT_MAX_NUMBER_OF_CLUSTER= 50;
	
	// Threshold ************************************************
	
	
	PrimitiveLearner primitiveLearner = new PrimitiveLearner();
	
	@SuppressWarnings("unused")
	private Model() {
		
	}
	

	@Deprecated
	public Model (String name) {
		this.name = name;
		selectors = new StructureSelector[]{new NeuralNetworkStructureSelector(), 
				                            new ARMAStructureSelector(),
				                            new RegressionTreeStructureSelector()};
		functions = new ModelFunction[selectors.length];
		//finalFunctions = new ModelFunction[selectors.length];
	}
	@Deprecated
	public Model (String name, Set<Primitive> possibleInputs, QualityOfService output) {
		this.name = name;
		this.possibleInputs = possibleInputs;
		this.output = output;
		selectors = new StructureSelector[]{new NeuralNetworkStructureSelector(), 
				                            new ARMAStructureSelector(),
				                            new RegressionTreeStructureSelector()};

		this.functionConfig = new double[selectors.length][];
		this.structureConfig = new double[selectors.length][];
		functions = new ModelFunction[selectors.length];
		//clusterData = new List[selectors.length];
		//totalMape = new double[selectors.length];
		lastMape = new double[selectors.length];
		//finalFunctions = new ModelFunction[selectors.length];
	}
	
	public Model (String name, Set<Primitive> possibleInputs, QualityOfService output, 
			double[][] functionConfig,
	        double[][] structureConfig) {
		super();
		this.name = name;
		this.possibleInputs = possibleInputs;
		this.output = output;
		this.functionConfig = functionConfig;
		this.structureConfig = structureConfig;
		selectors = new StructureSelector[]{new NeuralNetworkStructureSelector(), 
				                            new ARMAStructureSelector(),
				                            new RegressionTreeStructureSelector()};
		functions = new ModelFunction[selectors.length];
		//clusterData = new List[selectors.length];
		//totalMape = new double[selectors.length];
		lastMape = new double[selectors.length];
		//finalFunctions = new ModelFunction[selectors.length];
	}
	
	public List<Primitive> getInputs() {
		return inputs;
	}


	public QualityOfService getOutput() {
		return output;
	}
	
	public void selectPrimititvesAndTrainModels(){
		// Set the priority of write thread higher than other read thread.
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		/******************synchronized(writeLock) {
			// Carry on when no other thread is currently reading
			while (readLock.get() != 0) {
				try {
					writeLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			writeLock = true;
		*/
			
			boolean isFeaturesChanged = false;
			// This set needs to be transfered into a sorted collection.
			Set<Primitive> newInputs = primitiveLearner.select(output, possibleInputs);
			//System.out.print("Actual Input " + newInputs.size() + "\n");
			dataset.clear();
			if (newInputs.size() != inputs.size()) {
				inputs.clear();

				inputs.addAll(newInputs);
				isFeaturesChanged = true;
			} else {
				
				for (Primitive p : newInputs) {
					if (!inputs.contains(p)) {
						inputs.clear();
						inputs.addAll(newInputs);
						isFeaturesChanged = true;
						break;
					}
				}
				
			}
		
			yMax = output.getMax();
			xMax = new double[inputs.size()];
			for (int i = 0; i < inputs.size();i++) {
				xMax[i] = inputs.get(i).getMax();
			}
			
			if ( inputs.size() == 0) {
				return;
			}
			
			// copy the data for local usage, we need to do this in order to
			// avoid any concurrent changes in the underlying primitive and QoS
			// data set (e.g., updated by the sensors)
			
			// Get the formatted data based on new input matrix.
			
			final Object[] object = decomposeOutput();
			final double[][] formattedOutput2D = (double[][])object[1];
			formattedInput = decomposeInputs();
			formattedOutput1D = (double[])object[0];
			
			// The size of training data when used for evaluating the learner.
			int length = (int)(formattedInput.length*0.7);
			final double[][] clusterInput = new double[length][];
			final double[][] clusterOutput2D = new double[length][];
			final double[] clusterOutput1D = new double[length];
			
			final double[][] testingInput = new double[formattedInput.length - length][];
			final double[] testingOutput = new double[formattedInput.length - length];
			outputMean = 0;
			for (int i = 0; i < formattedInput.length; i++) {
				if (i < length) {
					clusterInput[i] = formattedInput[i];
					clusterOutput2D[i] = formattedOutput2D[i];
					clusterOutput1D[i] = formattedOutput2D[i][0];
					clusterOutputMean += formattedOutput2D[i][0];
				} else {
					testingInput[i - length] = formattedInput[i];
					testingOutput[i - length] = formattedOutput2D[i][0];
				}
				outputMean += formattedOutput2D[i][0];
			}
			outputMean = outputMean/formattedInput.length;
			clusterOutputMean = clusterOutputMean/length;
						
			//System.out.print("Number of primitives " + inputs.size() + " \n");
			
			if (isFeaturesChanged) {
				//System.out.print("Strcuture changed \n");
				
				
				
				triggerStructureSelection(formattedInput, 
						formattedOutput2D,
						formattedOutput1D, 
						clusterInput, 
						clusterOutput2D,
						clusterOutput1D, 
						testingInput, 
						testingOutput);
				
				//******************writeLock = false;
				//******************writeLock.notifyAll();
				
				// Notify the region control when primitives selection result change.
				// At this level, the optimization algorithm would be also notified to
				// abort the optimization.
				for (ModelListener listener : listeners) {
					listener.updateWhenModelChange(new ModelChangeEvent(true, output));
				}
				
				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
				//System.out.print("Train finished\n");
				return;
			}
			
			// If the prediction of current model of the new data point
			// faild under a threshold, we trigger structure selection as well.
			
			
			// Set the number of count of modeling.
			synchronized(concurrentModelLock) {
				concurrentModelLock.set(functions.length);
			}
			//System.out.print("Structure not changed\n");
			//**********************************************
			for (int k = 0; k < functions.length; k++) {
				final int index = k;
				//=============== should be removed ============
				if (index == 200 || index == 100) {
					synchronized(concurrentModelLock) {
						concurrentModelLock.decrementAndGet();		
					    if (concurrentModelLock.get() == 0) {
					    	concurrentModelLock.notifyAll();
					    }
					}
					continue;
				}
				//=============== should be removed ============
				SSAScalingThreadPool.executeJob(new Runnable(){

					@Override
					public void run() {
					try {
						if (functions[index] != null) {
							   	    							
							
							if (DEFAULT_PREDICTION_ERROR_PERCENTAGE < lastMape[index]) {
								//System.out.print("Strcuture still changed \n");
								triggerStructureSelection(index, formattedInput, 
										formattedOutput2D,
										formattedOutput1D, 
										clusterInput, 
										clusterOutput2D,
										clusterOutput1D, 
										testingInput, 
										testingOutput);
							} else {
								trainModelFunction(index, formattedInput, 
										formattedOutput2D,
										formattedOutput1D, 
										clusterInput, 
										clusterOutput2D,
										clusterOutput1D, 
										testingInput, 
										testingOutput);
							}
						}
					}catch (RuntimeException e) {
							// Make sure the process can keep going and avoid deadlock.
							e.printStackTrace();
							synchronized(concurrentModelLock) {
								concurrentModelLock.decrementAndGet();								
							    if (concurrentModelLock.get() == 0) {
							    	concurrentModelLock.notifyAll();
							    }
							}
						}
						synchronized(concurrentModelLock) {
							concurrentModelLock.decrementAndGet();		
						    if (concurrentModelLock.get() == 0) {
						    	concurrentModelLock.notifyAll();
						    }
						}
					
					}
				});
			
			
			}		
		
			synchronized(concurrentModelLock) {
				while (concurrentModelLock.get() != 0) {
					try {
						concurrentModelLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			// Notify the optimization, for potential dynamic handling within the optimization
			for (ModelListener listener : listeners) {
				listener.updateWhenModelChange(new ModelChangeEvent(false, output));
			}
			
			//**********************************************
			//******************writeLock = false;
			//******************writeLock.notifyAll();
	
		//******************}
		// Set the priority back.
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		//System.arraycopy(functions, 0, finalFunctions, 0, functions.length);
		//System.out.print("Train finished\n");
	}
	
	/**
	 * To test the error of the current model against newly measured data.
	 * if it is worse than a threshold, then in the next training, it will need
	 * structure change.
	 */
	public void updateNewlyError (double[] xValue,double yValue) {
		/******************synchronized(writeLock) {
			while (writeLock) {
				try {
					writeLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			readLock.incrementAndGet();
			
		}*/
		
		if (inputs.size() != 0) {
			
			for (int k = 0; k < functions.length; k++) {			
				final int index = k;
				//=============== should be removed ============
				if (index == 200 || index == 100) {
					continue;
				}
				//=============== should be removed ============
				double predict = 0;
				double ideal = yValue;
				if (k == ModelFunction.ARMAX/* for ARMAX */) {
					double[] x = this.transferSlidingWindow(xValue, 1);
				
					predict = functions[k]
							.predict(x);
			

					//System.out.print(" ARMAX INPUT"
						//	+ Arrays.toString(x)
							//+ "\n");

				} else {
					predict = functions[k].predict(xValue);
					if (k == ModelFunction.ANN) {
						//System.out.print("ANN INPUT" + Arrays.toString(xValue)
							//	+ "\n");
					} else if (k == ModelFunction.RT) {
						//System.out.print("RT INPUT" + Arrays.toString(xValue)
							//	+ "\n");
					}
					
				}
				lastMape[k] = Math.abs(predict- ideal) / (ideal==0? 1 : ideal);
				String name = "ARMAX";
				if (k == ModelFunction.ANN) {
					name = "ANN";
				} else if (k == ModelFunction.RT) {
					name = "RT";
				}
				/*totalMape[k] = (totalMape[k]
						* (formattedOutput1D.length - 1 - startPoint) + Math.abs(predict- ideal) / ideal)
						/ (formattedOutput1D.length - startPoint);
				System.out.print((k == 0 ? "ANN" : "ARMAX")
						+ " total MAPE " + totalMape[k] + "\n");*/
				//System.out.print(name
					//	+ " MAPE of new data is "
						//+ (Math.abs(predict - ideal) / ideal) + "\n");
				//System.out.print(name
					//	+ " Internal actual " + predict
						//+ ", internal ideal " + ideal + "\n");
			}
			// Finished recording data for cluster

		}

		/******************synchronized(writeLock) {
		    readLock.decrementAndGet();		
		    if (readLock.get() == 0) {
		       writeLock.notifyAll();
		    }
		}*/
	}
	
	/**
	 * To test the error of the current model against newly measured data.
	 * if it is worse than a threshold, then in the next training, it will need
	 * structure change.
	 * 
	 * This is used when newly measured data is feed as batch.
	 */
	public void updateNewlyErrorWithReturn(double[] result){
		for (int k = 0; k < functions.length; k++) {
			this.lastMape[k] = result[k];
		}
			
		
	}
	/**
	 * To test the error of the current model against newly measured data.
	 * if it is worse than a threshold, then in the next training, it will need
	 * structure change.
	 * 
	 * This is used when newly measured data is feed as batch.
	 */
	public double[] updateNewlyErrorWithReturn (double[] xValue,double yValue) {
		
		double[] result = new double[functions.length];
		if (inputs.size() != 0) {
			
			for (int k = 0; k < functions.length; k++) {			
				//=============== should be removed ============
				if (k == 200 || k == 100) {
					continue;
				}
				//=============== should be removed ============
				double predict = 0;
				double ideal = yValue;
				if (k == ModelFunction.ARMAX/* for ARMAX */) {
					double[] x = this.transferSlidingWindow(xValue, 1);
				
					predict = functions[k]
							.predict(x);
			

					//System.out.print(" ARMAX INPUT"
						//	+ Arrays.toString(x)
							//+ "\n");

				} else {
					predict = functions[k].predict(xValue);
					if (k == ModelFunction.ANN) {
						//System.out.print("ANN INPUT" + Arrays.toString(xValue)
							//	+ "\n");
					} else if (k == ModelFunction.RT) {
						//System.out.print("RT INPUT" + Arrays.toString(xValue)
							//	+ "\n");
					}
					
				}
				result[k] = Math.abs(predict- ideal) / (ideal==0? 1 : ideal);
				String name = "ARMAX";
				if (k == ModelFunction.ANN) {
					name = "ANN";
				} else if (k == ModelFunction.RT) {
					name = "RT";
				}
				/*totalMape[k] = (totalMape[k]
						* (formattedOutput1D.length - 1 - startPoint) + Math.abs(predict- ideal) / ideal)
						/ (formattedOutput1D.length - startPoint);
				System.out.print((k == 0 ? "ANN" : "ARMAX")
						+ " total MAPE " + totalMape[k] + "\n");*/
				//System.out.print(name
					//	+ " MAPE of new data is "
					//	+ (Math.abs(predict - ideal) / ideal) + "\n");
				//System.out.print(name
					//	+ " Internal actual " + predict
					//	+ ", internal ideal " + ideal + "\n");
			}
			// Finished recording data for cluster

		}

		return result;
	}
	
	public double predict(double[] xValue, boolean isSU, double a, double b) {
		//System.out.print("Enter\n");
		/******************synchronized(writeLock) {
			while (writeLock) {
				try {
					writeLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			readLock.incrementAndGet();
			
		}*/
		
		//TODO add transfer for ARMAX
		ModelFunction function = selectFunction (xValue, isSU, a, b);
		double[] x = xValue;
		if (function instanceof ARMA) {
			x = this.transferSlidingWindow(xValue, ModelFunction.ARMAX);
		} else  if (function instanceof EncogFeedForwardNeuralNetwork) {
			//System.out.print(" PREDICTED ANN INPUT " + Arrays.toString(x) +  "\n");
		} else if (function instanceof RegressionTree) {
			//System.out.print(" PREDICTED RT INPUT " + Arrays.toString(x) +  "\n");
		}
		//System.out.print("%%%%%%%%%%%%%%%%%%%%%%%%%%%%% \n");
		//System.out.print("%%%%%%%%%%%%%%%%%%%%%%%%%%%%% " + selectors[ModelFunction.ARMAX].getOrder() +  "\n");
		//System.out.print("%%%%%%%%%%%%%%%%%%%%%%%%%%%%% \n");
		double result = function==null? 0 : function.predict(x);
		
		/******************synchronized(writeLock) {
		    readLock.decrementAndGet();		
		    if (readLock.get() == 0) {
		       writeLock.notifyAll();
		    }
		}*/
		
		return result*100;
	}
	
	public double predict(double[] xValue, int index) {
		/******************synchronized(writeLock) {
			while (writeLock) {
				try {
					writeLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			readLock.incrementAndGet();
		}*/
		
		double[] x = xValue;
		if (index == 1) {
			x = this.transferSlidingWindow(xValue, ModelFunction.ARMAX);
		}
	
		double result =  functions[index].predict(x);
		
		/******************synchronized(writeLock) {
		    readLock.decrementAndGet();	
		    if (readLock.get() == 0) {
		       writeLock.notifyAll();
		    }
		}*/
		
		return result*100; // change to 100% with integer
	}
	
	public double getYMax(){
		return yMax;
	}
	
	public double getXMax(int i){
		return xMax[i];
	}
	
	public void addListener (ModelListener listener) {
		// As long as it does not conflict with read, then it is fine
		// do not need to use read lock here.
		//******************synchronized(writeLock) {
			for (ModelListener inlistener : listeners) {
				if (listener.getClass().isInstance(inlistener) ) {
					listeners.remove(inlistener);
				}
			}
			
			listeners.add(listener);
	    //******************}
	}
	/**
	 * This can be changed to multithread if adaptive function is required
	 */
	private void triggerStructureSelection(final double[][] inputs, 
			final double[][] outputs2D,
			final double[] outputs1D,
			final double[][] clusterInput,
			final double[][] clusterOutput2D,
			final double[] clusterOutput1D,		
			final double[][] testingInput,
			final double[] testingOutput ){
		
		synchronized(concurrentModelLock) {
			concurrentModelLock.set(functions.length);
		}
		long time = System.currentTimeMillis();
		for (int k = 0; k < functions.length; k++) {
			final int index = k;
			//=============== should be removed ============
			if (index == 200 || index == 100) {
				synchronized(concurrentModelLock) {
					concurrentModelLock.decrementAndGet();	
					
				    if (concurrentModelLock.get() == 0) {
				    	concurrentModelLock.notifyAll();
				    }
				}
				continue;
			}
			//=============== should be removed ============
			SSAScalingThreadPool.executeJob(new Runnable(){

				@Override
				public void run() {
					try {
					switch (index) {
					case ModelFunction.ANN: {
						//System.out.print(functions[index] + " train start\n");
						/*functions[ModelFunction.ANN] = new EncogFeedForwardNeuralNetwork(
								inputs, outputs2D, outputMean,
								18, 0, true);*/
						selectors[ModelFunction.ANN].decideStructure(inputs, outputs2D, outputMean, functionConfig[ModelFunction.ANN], structureConfig[ModelFunction.ANN]);		
						functions[ModelFunction.ANN] = selectors[ModelFunction.ANN].getModelFunction();
					    while (functions[ModelFunction.ANN].getMAPE() == 1){
					    	functions[ModelFunction.ANN] = new EncogFeedForwardNeuralNetwork(
					    			inputs, outputs2D, outputMean,
									selectors[ModelFunction.ANN].getOrder(), 0, true, functionConfig[ModelFunction.ANN]);
					    }
						
						ClusterData[] cluster = null;
						// Start clustering
						do {
							ModelFunction ann = new EncogFeedForwardNeuralNetwork(
									clusterInput, clusterOutput2D, clusterOutputMean,
									selectors[ModelFunction.ANN].getOrder(), 0, true,  functionConfig[ModelFunction.ANN]);
							//cluster = cluster(testingInput, testingInput, testingOutput,
								//	ann);
							cluster = record(testingInput, testingInput, testingOutput,
									ann);
						} while (cluster == null);
						resultset.put(functions[ModelFunction.ANN], cluster);
						//dataset.put(functions[ModelFunction.ANN], cluster);
						//System.out.print(functions[index] + " train finished\n");
						break;
					}
					case ModelFunction.ARMAX: {
						//System.out.print(functions[index] + " train start\n");
						selectors[ModelFunction.ARMAX].decideStructure(inputs, outputs1D, structureConfig[ModelFunction.ARMAX]);		
						functions[ModelFunction.ARMAX] = selectors[ModelFunction.ARMAX].getModelFunction();
						
						// Start clustering
						Object[] object = selectors[ModelFunction.ARMAX].doDataSeparation(clusterInput,
								clusterOutput1D);
						ModelFunction armax = new ARMA((double[][])object[0], (double[])object[1]);
						object = selectors[ModelFunction.ARMAX].doDataSeparation(testingInput,
								testingOutput);
						int order =  selectors[ModelFunction.ARMAX].getOrder();
						
						double[][] originalInputs = new double[testingInput.length - order][];
						System.arraycopy(testingInput, order, originalInputs, 0, testingInput.length - order);
						resultset.put(functions[ModelFunction.ARMAX], record(originalInputs, (double[][])object[0], (double[])object[1], armax));
						
						//dataset.put(functions[ModelFunction.ARMAX], cluster(originalInputs, (double[][])object[0], (double[])object[1], armax));
						//System.out.print(functions[index] + " train finished\n");
						break;
					}
					case ModelFunction.RT:{
						
						//System.out.print(functions[index] + " train start\n");
						selectors[ModelFunction.RT].decideStructure(inputs, outputs1D, structureConfig[ModelFunction.RT]);
						
						functions[ModelFunction.RT] = selectors[ModelFunction.RT]
						                       					.getModelFunction();

						RegressionTree tree = new RegressionTree(clusterInput,
								clusterOutput1D);
						
						ClusterData[] cluster = record(testingInput, testingInput, testingOutput,
								tree);
						resultset.put(functions[ModelFunction.RT], cluster);
						//System.out.print(functions[index] + " train finished\n");
						break;
					}
					}
				
					synchronized(concurrentModelLock) {
						concurrentModelLock.decrementAndGet();	
						
					    if (concurrentModelLock.get() == 0) {
					    	concurrentModelLock.notifyAll();
					    }
					}
				}catch (RuntimeException e) {
					// Make sure the process can keep going and avoid deadlock.
					e.printStackTrace();
					synchronized(concurrentModelLock) {
						concurrentModelLock.decrementAndGet();	
						
					    if (concurrentModelLock.get() == 0) {
					    	concurrentModelLock.notifyAll();
					    }
					}
				}
				} 
				
			});
			
		}
		
		synchronized(concurrentModelLock) {
			while (concurrentModelLock.get() != 0) {
				try {
					concurrentModelLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//System.out.print("******************************\n");
		//System.out.print("Time used for obtaining the model: " + (System.currentTimeMillis() - time) + "\n");
		//System.out.print("******************************\n");
		//System.out.print("concurrentModelLock notified\n");
		
	}
	
	private void triggerStructureSelection(int index, 
			double[][] inputs, 
			double[][] outputs2D,
			double[] outputs1D,
			double[][] clusterInput,
			double[][] clusterOutput2D,
			double[] clusterOutput1D,		
			double[][] testingInput,
			double[] testingOutput){
		switch (index) {
		case ModelFunction.ANN: {
			//System.out.print(functions[index] + " train start\n");
			/*functions[ModelFunction.ANN] = new EncogFeedForwardNeuralNetwork(
			inputs, outputs2D, outputMean,
			18, 0, true);*/
			selectors[ModelFunction.ANN].decideStructure(inputs,
					outputs2D, outputMean,  functionConfig[ModelFunction.ANN], structureConfig[ModelFunction.ANN]);
			functions[ModelFunction.ANN] = selectors[ModelFunction.ANN]
					.getModelFunction();
		    while (functions[ModelFunction.ANN].getMAPE() == 1){
		    	functions[ModelFunction.ANN] = new EncogFeedForwardNeuralNetwork(
		    			inputs, outputs2D, outputMean,
						selectors[ModelFunction.ANN].getOrder(), 0, true, functionConfig[ModelFunction.ANN]);
		    }
			
			ClusterData[] cluster = null;
			// Start clustering
			do {
				ModelFunction ann = new EncogFeedForwardNeuralNetwork(
						clusterInput, clusterOutput2D, clusterOutputMean,
						selectors[ModelFunction.ANN].getOrder(), 0, true, functionConfig[ModelFunction.ANN]);
				//cluster = cluster(testingInput, testingInput, testingOutput,
					//	ann);
				cluster = record(testingInput, testingInput, testingOutput,
						ann);
			} while (cluster == null);
			resultset.put(functions[ModelFunction.ANN], cluster);
			//dataset.put(functions[ModelFunction.ANN], cluster);
			break;
		}
		case ModelFunction.ARMAX: {
			//System.out.print(functions[index] + " train start\n");
			selectors[ModelFunction.ARMAX].decideStructure(inputs, outputs1D,structureConfig[ModelFunction.ARMAX]);
			functions[ModelFunction.ARMAX] = selectors[ModelFunction.ARMAX]
					.getModelFunction();
			// Start clustering
			Object[] object = selectors[ModelFunction.ARMAX].doDataSeparation(clusterInput,
					clusterOutput1D);
			ModelFunction armax = new ARMA((double[][])object[0], (double[])object[1]);
			object = selectors[ModelFunction.ARMAX].doDataSeparation(testingInput,
					testingOutput);
			
			int order =  selectors[ModelFunction.ARMAX].getOrder();
			
			double[][] originalInputs = new double[testingInput.length - order][];
			System.arraycopy(testingInput, order, originalInputs, 0, testingInput.length - order);
			resultset.put(functions[ModelFunction.ARMAX], record(originalInputs, (double[][])object[0], (double[])object[1], armax));
			
			//dataset.put(functions[ModelFunction.ARMAX], cluster(originalInputs, (double[][])object[0], (double[])object[1], armax));
			//System.out.print(functions[index] + " train finished\n");
			break;
		}
		case ModelFunction.RT:{
			
			//System.out.print(functions[index] + " train start\n");
			selectors[ModelFunction.RT].decideStructure(inputs, outputs1D,  structureConfig[ModelFunction.RT]);
			
			functions[ModelFunction.RT] = selectors[ModelFunction.RT]
			                       					.getModelFunction();

			RegressionTree tree = new RegressionTree(clusterInput,
					clusterOutput1D);
			
			ClusterData[] cluster = record(testingInput, testingInput, testingOutput,
					tree);
			resultset.put(functions[ModelFunction.RT], cluster);
			//System.out.print(functions[index] + " train finished\n");
			break;
		}

		}
	}
	/**
	 * This can be changed to multithread if adaptive function is required
	 */
	private void trainModelFunction(){		
		//functions[ModelFunction.ANN] = new EncogFeedForwardNeuralNetwork(decomposeInputs(), decomposeOutput(), output.getMean(),selectors[ModelFunction.ANN].getOrder(),0, true);	
		//functions[ModelFunction.ARMAX] = new ARMA(decomposeInputs(), output.getArray());
				
	}
	
	private void trainModelFunction(int index, 
			double[][] inputs, 
			double[][] outputs2D, 
			double[] outputs1D,
			double[][] clusterInput,
			double[][] clusterOutput2D,
			double[] clusterOutput1D,		
			double[][] testingInput,
			double[] testingOutput){	
		switch (index) {
		case ModelFunction.ANN: {
			//System.out.print(functions[index] + " train start\n");
			/*functions[ModelFunction.ANN] = new EncogFeedForwardNeuralNetwork(
			inputs, outputs2D, outputMean,
			18, 0, true);*/
			do {
			functions[ModelFunction.ANN] = new EncogFeedForwardNeuralNetwork(
					inputs, outputs2D, outputMean,
					selectors[ModelFunction.ANN].getOrder(), 0, true, functionConfig[ModelFunction.ANN]);
			} while (functions[ModelFunction.ANN].getMAPE() == 1);
			ClusterData[] cluster = null;
			// Start clustering
			do {
				ModelFunction ann = new EncogFeedForwardNeuralNetwork(
						clusterInput, clusterOutput2D, clusterOutputMean,
						selectors[ModelFunction.ANN].getOrder(), 0, true, functionConfig[ModelFunction.ANN]);
				//cluster = cluster(testingInput, testingInput, testingOutput,
					//	ann);
				cluster = record(testingInput, testingInput, testingOutput,
						ann);
			} while (cluster == null);
			resultset.put(functions[ModelFunction.ANN], cluster);
			//dataset.put(functions[ModelFunction.ANN], cluster);
			//System.out.print(functions[index] + " train finished\n");
			break;
		}
		case ModelFunction.ARMAX: {
			//System.out.print(functions[index] + " train start\n");
			Object[] object = selectors[ModelFunction.ARMAX].doDataSeparation(inputs,
					outputs1D);
			functions[ModelFunction.ARMAX] = new ARMA((double[][])object[0], (double[])object[1]);
			
			// Start clustering
			object = selectors[ModelFunction.ARMAX].doDataSeparation(clusterInput,
					clusterOutput1D);
			ModelFunction armax = new ARMA((double[][])object[0], (double[])object[1]);
			object = selectors[ModelFunction.ARMAX].doDataSeparation(testingInput,
					testingOutput);
			
			int order =  selectors[ModelFunction.ARMAX].getOrder();
			
			double[][] originalInputs = new double[testingInput.length - order][];
			System.arraycopy(testingInput, order, originalInputs, 0, testingInput.length - order);
			
			resultset.put(functions[ModelFunction.ARMAX], record(originalInputs, (double[][])object[0], (double[])object[1], armax));
			
			//dataset.put(functions[ModelFunction.ARMAX], cluster(originalInputs, (double[][])object[0], (double[])object[1], armax));
			//System.out.print(functions[index] + " train finished\n");
			break;
		}
		case ModelFunction.RT: {
			//System.out.print(functions[index] + " train start\n");
			
			
			functions[ModelFunction.RT] = new RegressionTree(inputs,
					outputs1D);
			
			RegressionTree tree = new RegressionTree(clusterInput,
					clusterOutput1D);
			
			ClusterData[] cluster = record(testingInput, testingInput, testingOutput,
					tree);
			resultset.put(functions[ModelFunction.RT], cluster);
		
			//System.out.print(functions[index] + " train finished\n");
			break;
		}
		}
	}
	
	
	
	private ClusterData[] record (double[][] originalInputs /*original inputs before transfer e.g., for ARMAX*/, 
			double[][] inputs, double[] output, ModelFunction function) {
		
		if (isConsiderTimeSeriesOnLocalError && !originalInputs.equals(inputs)) {
			originalInputs = inputs;
		}
		
		boolean isInvalidModelFunction = true;
		double previous = 0;
		ClusterData[] data = new ClusterData[output.length];
		//Map map = new HashMap();
		for (int i = 0;i < output.length;i++){
			double mape = Util.calculateMAPE(output[i],  function.predict(inputs[i]));
			data[i] = new ClusterData(originalInputs[i], output[i], mape);
			
			if (previous != 0 && previous != mape) {
				isInvalidModelFunction = false;
			}
			
			previous = mape;
		}
		
		if (isInvalidModelFunction) {
			//System.out.print("Oops, getting a model function that always produces the " +
				//	"same result, re-train it for clustering...\n");
			return null;
		}
		
		return data;
	}
	
	private ClusterPair[] cluster (double[][] originalInputs /*original inputs before transfer e.g., for ARMAX*/, 
			double[][] inputs, double[] output, ModelFunction function) {
		Dataset ds = new DefaultDataset();
		boolean isInvalidModelFunction = true;
		double previous = 0;
		//Map map = new HashMap();
		for (int i = 0;i < output.length;i++){
			double mape = Math.abs(output[i] - function.predict(inputs[i]))/output[i];
			ds.add(new DenseInstance(new double[]{mape}));
			
			if (previous != 0 && previous != mape) {
				isInvalidModelFunction = false;
			}
			
			previous = mape;
		}
		
		if (isInvalidModelFunction) {
			//System.out.print("Oops, getting a model function that always produces the " +
				//	"same result, re-train it for clustering...\n");
			return null;
		}
		
		Clusterer km = new KMeans(ds.size(),1,new EuclideanDistance());
			//new IterativeKMeans(1,ds.size() > DEFAULT_MAX_NUMBER_OF_CLUSTER? DEFAULT_MAX_NUMBER_OF_CLUSTER : ds.size()/2,50,new EuclideanDistance(),new AICScore());
        //System.out.print("Start clustering...\n");
        Dataset[] clusters = km.cluster(ds);
        //System.out.print("Clustering finished\n");
        //System.out.print("Clustering size: " + clusters.length + "\n");
        ClusterPair[] pairs = new ClusterPair[clusters.length];
        List inputList = null;
        List outputList = null;
     
        // Maintain the order of time series data.
        for (int i = 0; i < clusters.length; i++) {
    	
    	
        	inputList = new ArrayList(); 
            outputList = new ArrayList(); 
    		for (int j = 0;j < output.length;j++){
    			
    			if (inputList.size() == clusters[i].size()) {
    				break;
    			}
    			double mape = Math.abs(output[j] - function.predict(inputs[j]))/output[j];
    			for (Instance instance : clusters[i]) {
        			
        			if (mape == instance.get(0)) {
        				inputList.add(originalInputs[j]);
        				outputList.add(output[j]);
        				break;
        			}
    			}
    		}
        	
        	// mape, inputs, outputs
        	pairs[i] = new ClusterPair(clusters[i], inputList, outputList);
        }
        	
        //System.out.print("Exit clustering\n");
        return pairs;
	}
	
	private ModelFunction selectFunction (double[] xValue, boolean isSU, double a, double b) {
				
		ModelFunction selected = null;
		ClusterData bestPair = null;
		double bestMape = 0; 
		double averageMape = 0.0;
		double[] copy = xValue;
		boolean isSUcopy = isSU;
		for (int k = 0; k < functions.length; k++){
			//=============== should be removed ============
			if (k ==200 || k==100) {
				continue;
			}
			//=============== should be removed ============
			ClusterData[] paris = resultset.get(functions[k]);
			
			if (isConsiderTimeSeriesOnLocalError) {
				
				if (k ==  ModelFunction.ARMAX) {
					xValue = this.transferSlidingWindow(xValue, ModelFunction.ARMAX);
					isSU = false;
				} else {
					xValue = copy;
					isSU = isSUcopy;
				}
			
			}
			
			if (paris == null) {
				//logger.error(functions[k] + " has no cluster data under " + output.getName());
				continue;
			}
			
			double bestED = 0;
			double coresspondingMape = 0;
			ClusterData coresspondingPair = null;
			for (ClusterData pair : paris) {
		        		if (bestED == 0) {
		        			bestED = calculateEuclideanDistance(xValue, pair.inputs, isSU);
		        			coresspondingMape = pair.mape;
	        				coresspondingPair = pair;
		        		} else {
		        			double ED = calculateEuclideanDistance(xValue, pair.inputs, isSU);
		        			if (ED < bestED) {
		        				bestED = ED;
		        				coresspondingMape = pair.mape;
		        				coresspondingPair = pair;
		        			}
		        		
		                }
		        		averageMape += pair.mape;
			}
			averageMape = averageMape/paris.length;
		
			if ((a * coresspondingMape + b * averageMape) < bestMape || bestMape == 0){
				bestMape = a*coresspondingMape + b*averageMape;
				selected = functions[k];
				bestPair = coresspondingPair;
			}
			
			//System.out.print((isSU?"SU" : "nonSU")+a+b+ " Funtion: " + functions[k] + " with MAPE " + a*coresspondingMape + ", average MAPE " + b*averageMape + "\n");
		}
		
		   //logger.info("Selected: " + selected + " with MAPE " + bestMape);
		//System.out.print((isSU?"SU" : "nonSU")+a+b+ " Selected: " + selected + " with MAPE " + bestMape + "\n");
		// Only trigger sub modeling if the expected MAPE lower than the threshold.
		if (bestMape > DEFAULT_PREDICTION_ERROR_PERCENTAGE) {
			//return trainSubModelFunction (bestPair, selected);
		}
		
		return selected;
	}
	
	/*private ModelFunction selectFunction (double[] xValue) {
		// For first time, return the one with lowest training MAPE
				
		ModelFunction selected = null;
		ClusterPair bestPair = null;
		double bestMape = 0;
		for (int k = 0; k < functions.length; k++){
			ClusterPair[] paris = dataset.get(functions[k]);
			
			double bestED = 0;
			double coresspondingMape = 0;
			ClusterPair coresspondingPair = null;
			for (ClusterPair pair : paris) {
		       for (int i = 0; i < pair.inputs.size(); i++){
		        		if (bestED == 0) {
		        			bestED = calculateEuclideanDistance(xValue, (double[])pair.inputs.get(i));
		        			coresspondingMape = pair.average;
	        				coresspondingPair = pair;
		        		} else {
		        			double ED = calculateEuclideanDistance(xValue, (double[])pair.inputs.get(i));
		        			if (ED < bestED) {
		        				bestED = ED;
		        				coresspondingMape = pair.average;
		        				coresspondingPair = pair;
		        			}
		        		}
		       }
			}
			double a = 1;
			double b = 1;
			if ((a * coresspondingMape + b * totalMape[k]) < bestMape || bestMape == 0){
				bestMape = a*coresspondingMape + b*totalMape[k];
				selected = functions[k];
				bestPair = coresspondingPair;
			}
			
			System.out.print("Funtion: " + functions[k] + " with MAPE " + a*coresspondingMape + ", total MAPE " + b*totalMape[k] + "\n");
		}
		
		//logger.info("Selected: " + selected + " with MAPE " + bestMape);
		System.out.print("Selected: " + selected + " with MAPE " + bestMape + "\n");
		// Only trigger sub modeling if the expected MAPE lower than the threshold.
		if (bestMape > DEFAULT_PREDICTION_ERROR_PERCENTAGE) {
			//return trainSubModelFunction (bestPair, selected);
		}
		
		return selected;
	}*/
	
	private double calculateEuclideanDistance(double[] input, double[] another, boolean isSU) {
		// The two array needs to be as the same dimension.
		double result = 0;
		for (int i = 0; i < input.length; i++) {
			// Multiply the weight of SU value.
			if (isSU)
			  result += primitiveLearner.getValue(inputs.get(i)) * Math.pow((input[i] - another[i]), 2);
			else
			  result += Math.pow( (input[i] - another[i]), 2);
		}
		
		return Math.sqrt(result);
	}
	
	private double calculateManhattanDistance(double[] input, double[] another, boolean isSU) {
		// The two array needs to be as the same dimension.
		double result = 0;
		for (int i = 0; i < input.length; i++) {
			// Multiply the weight of SU value.
			if (isSU)
			  result += primitiveLearner.getValue(inputs.get(i)) * Math.abs(input[i] - another[i]);
			else
			  result += Math.abs(input[i] - another[i]);
		}
		// was Math.sqrt(result);
		return result;
	}

	private ModelFunction trainSubModelFunction (ClusterPair pair, ModelFunction funciton) {
		double [][] inputs = new double[pair.inputs.size()][];
		double [][] outputs2D = null;
		
		ModelFunction model = null;
		
		if (funciton instanceof EncogFeedForwardNeuralNetwork) {
			outputs2D = new double[pair.inputs.size()][];
			double mean = 0;
			for (int i = 0; i < pair.inputs.size(); i++) {
				inputs[i] = (double[])pair.inputs.get(i);
				outputs2D[i] = new double[]{(Double)pair.outputs.get(i)};
				mean += (Double)pair.outputs.get(i);
			}
			mean = mean/pair.inputs.size();
			model = new EncogFeedForwardNeuralNetwork(
					    inputs, outputs2D, mean,
						// Use the current number of hidden neuron.
						selectors[ModelFunction.ANN].getOrder(), 0, true, functionConfig[ModelFunction.ANN]);
		} else 	if (funciton instanceof ARMA) {
			
		}
		return model;
		
	}
	
	private double[][] decomposeInputs(){
		
		double[][] x = new double[inputs.get(0).getArray().length - invalidOuputCount - 1][inputs.size()];
		
		
		
		for (int i = 0; i < inputs.size(); i++) {
			int index = 0;
			for (int k = 1; k < inputs.get(i).getArray().length; k++) {
				// For EP, there should be always EP(t-1), which is one interval before.
				if (inputs.get(i) instanceof EnvironmentalPrimitive) {
					// Only put inputs if the corresponding output is non-zero.
					if (!isEliminateZero || output.getArray()[k] != 0) {
					    x[index][i] =  inputs.get(i).getArray()[k - 1]/100;
					  //  System.out.print(inputs.get(i).getName() + x[index][i]+" EP ********** \n");
					    index++;
					}
				} else {
					if (!isEliminateZero || output.getArray()[k] != 0) {
					    x[index][i] =  inputs.get(i).getArray()[k]/100;
					   // System.out.print(inputs.get(i).getAlias()+ " : " +  k + " CP ********** \n");
					   // System.out.print(inputs.get(i).getName() + x[index][i]+" CP ********** \n");
					    index++;
					}
				}
			}
		
		}
		return x;
	}
	
	/**
	 * Call this one before decomposeInput.
	 * @return
	 */
	private Object[] decomposeOutput(){
		invalidOuputCount = 0;
		
		if (isEliminateZero) {
			// Do not allow to model 0 output.
			for (int i = 1; i < output.getArray().length; i++)  {
				if (output.getArray()[i] == 0) {
					invalidOuputCount++;
				}
				
			}
		}
		
		double[][] y2 = new double[output.getArray().length - invalidOuputCount - 1][];
		double[] y1 = new double[output.getArray().length - invalidOuputCount - 1];
		
		int k = 0;
		for (int i = 1; i < output.getArray().length; i++) {
			
			
			// Get rid of the first data
			if (!isEliminateZero || output.getArray()[i] != 0) {
				y2[k] = new double[]{output.getArray()[i]/100};				
				y1[k] = output.getArray()[i]/100;
				k++;
			}
			
		}
		return new Object[]{y1,y2};
	}
	/**
	 * Used when making prediction for a set of inputs whose output is not
	 * known (at least not know by the existing models)
	 * @param givenInputs
	 * @param index (usually just the ARMAX learner)
	 * @return
	 */
	private double[] transferSlidingWindow(double[] givenInputs, int index) {
		
		double[][] x = new double[formattedInput.length + 1][];
		double[] y = new double[formattedOutput1D.length + 1];
		System.arraycopy(formattedInput, 0, x, 0, formattedInput.length);
		System.arraycopy(formattedOutput1D, 0, y, 0, formattedOutput1D.length);
		
		x[formattedInput.length] = givenInputs;
		// Fake, as it is unknown.
		y[formattedOutput1D.length] = 0;
		
		Object[] object = selectors[index].doDataSeparation(x,y);
		
		int length = ((double[][])object[0]).length - 1;
		//System.out.print(" PREDICTED ARMAX INPUT " + Arrays.toString(((double[][])object[0])[length]) +  "\n");
		return ((double[][])object[0])[length];
		
	}
	
	public Primitive get (int i){
		return inputs.get(i);
	}
	
	public int getSize (){
		return inputs.size();
	}
	
	public int countFunction(){
		return functions.length;
	}
	
	/*public void updatePossiblePrimitives(){
		for (Primitive p : possibleInputs)  {
			p.addValue();
		}
	}*/
	
	private class ClusterPair{
		
		@SuppressWarnings("rawtypes")
		private List inputs;
		private List outputs;
		private double average = 0;
		@SuppressWarnings({ "unused", "rawtypes" })
		public ClusterPair(Dataset set, List inputs, List outputs) {
			super();
			this.inputs = inputs;
			this.outputs = outputs;
			for (int i = 0; i < set.size(); i++) {
				average += set.get(i).get(0);
			}
			
			average = average/set.size();
		}
		
		
	}
	
	private class ClusterData{
		private double[] inputs;
		private double output;
		private double mape;
		public ClusterData(double[] inputs, double output, double mape) {
			super();
			this.inputs = inputs;
			this.output = output;
			this.mape = mape;
		}
		
		
	}

}
