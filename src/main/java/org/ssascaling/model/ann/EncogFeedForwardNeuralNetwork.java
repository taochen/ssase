package org.ssascaling.model.ann;

//import org.encog.engine.network.activation.ActivationElliott;
import org.encog.engine.network.activation.*;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.train.strategy.RequiredImprovementStrategy;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
//import org.encog.neural.networks.training.strategy.RequiredImprovementStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssascaling.AbstractModelFunction;
public class EncogFeedForwardNeuralNetwork extends AbstractModelFunction implements NeuralNetwork {
	protected static final Logger logger = LoggerFactory
	.getLogger(EncogFeedForwardNeuralNetwork.class);

	private double RSS = 0.0;
	// Average error rate
	private double SMAPE = 0.0;
	private double MAPE = 0.0;
	private double R = 0.0;

	private double meanIdeal = 0.0;//10.890123529411767
	// Currently consists of RS and MAPE on fitting data
	private double bestQuality[];
	// private double worestMPE[];
	private long sample = 0;
	
	// Statefull parameters
	private BasicNetwork bestEverNetwork;
	private double bestEverError[];
	private int currentNoOfHiddenNode = -1;
	
	double[][] px;
	double[][] py;
	
	
	private int DEFAULT_MAX_TRAINING_TIME_LIMIT = 
		NeuralNetwork.DEFAULT_MAX_TRAINING_TIME_LIMIT;
	
	private int DEFAULT_SELECTION_LIMIT = 
		NeuralNetwork.DEFAULT_SELECTION_LIMIT;
	
	private double DEFAULT_BEST_ERROR_PERCENTAGE = 
		NeuralNetwork.DEFAULT_BEST_ERROR_PERCENTAGE;
	
	private double DEFAULT_WOREST_ERROR_PERCENTAGE = 
		NeuralNetwork.DEFAULT_WOREST_ERROR_PERCENTAGE;
	
	
	@Deprecated
	public EncogFeedForwardNeuralNetwork(double[][] x, double[][] y, double[][] px, double[][] py,double meanIdeal, int activation) {
		this.meanIdeal = meanIdeal;
		this.px = px;
		this.py = py;
		this.bestQuality = new double[]{NeuralNetwork.DEFAULT_BEST_ERROR_PERCENTAGE, NeuralNetwork.DEFAULT_BEST_ERROR_PERCENTAGE};
		//this.worestMPE = new double[]{NeuralNetwork.DEFAULT_WOREST_ERROR_PERCENTAGE,NeuralNetwork.DEFAULT_WOREST_ERROR_PERCENTAGE};
		try {
			this.trainOnModelStructureChange(x, y, ActivationSigmoid.class, null, null);
		} catch (InstantiationException e) {
			logger.error("Can not create new activation instance", e);
		} catch (IllegalAccessException e) {
			logger.error("Can not create new activation instance", e);
		}
	}
	
	@Deprecated
	public EncogFeedForwardNeuralNetwork(double[][] x, double[][] y, double meanIdeal, int activation) {
		this.meanIdeal = meanIdeal;
		this.bestQuality = new double[]{NeuralNetwork.DEFAULT_BEST_ERROR_PERCENTAGE, NeuralNetwork.DEFAULT_BEST_ERROR_PERCENTAGE};
		//this.worestMPE = new double[]{NeuralNetwork.DEFAULT_WOREST_ERROR_PERCENTAGE,NeuralNetwork.DEFAULT_WOREST_ERROR_PERCENTAGE};
		try {
			this.train(x, y, ActivationSigmoid.class);
		} catch (InstantiationException e) {
			logger.error("Can not create new activation instance", e);
		} catch (IllegalAccessException e) {
			logger.error("Can not create new activation instance", e);
		}
	}
	
	@Deprecated
	public EncogFeedForwardNeuralNetwork(double[][] x, double[][] y, double meanIdeal,int hidden, int activation, boolean timeConstraint) {
		this.meanIdeal = meanIdeal;
		this.bestQuality = new double[]{NeuralNetwork.DEFAULT_BEST_ERROR_PERCENTAGE, NeuralNetwork.DEFAULT_BEST_ERROR_PERCENTAGE,  NeuralNetwork.DEFAULT_WOREST_ERROR_PERCENTAGE};
		currentNoOfHiddenNode = hidden;
		//this.worestMPE = new double[]{NeuralNetwork.DEFAULT_WOREST_ERROR_PERCENTAGE,NeuralNetwork.DEFAULT_WOREST_ERROR_PERCENTAGE};
		try {
			this.trainOnFixedHiddenNode(x, y, ActivationSigmoid.class,hidden, timeConstraint, null, null);
		} catch (InstantiationException e) {
			logger.error("Can not create new activation instance", e);
		} catch (IllegalAccessException e) {
			logger.error("Can not create new activation instance", e);
		}
	}

	public EncogFeedForwardNeuralNetwork(
			double[][] x, 
			double[][] y,
			double meanIdeal, 
			int hidden, 
			int activation, 
			boolean timeConstraint,
			double[] config) {
		
		this.meanIdeal = meanIdeal;
		if (config != null && config.length != 0) {
			this.DEFAULT_MAX_TRAINING_TIME_LIMIT = (int)config[0];
			this.DEFAULT_SELECTION_LIMIT = (int)config[1];
			this.DEFAULT_BEST_ERROR_PERCENTAGE = config[2];
			this.DEFAULT_WOREST_ERROR_PERCENTAGE = config[3];
		}
		/*****
		System.out.print("DEFAULT_MAX_TRAINING_TIME_LIMIT " + DEFAULT_MAX_TRAINING_TIME_LIMIT + "\n");
		System.out.print("DEFAULT_SELECTION_LIMIT " + DEFAULT_SELECTION_LIMIT + "\n");
		System.out.print("DEFAULT_BEST_ERROR_PERCENTAGE " + DEFAULT_BEST_ERROR_PERCENTAGE + "\n");
		System.out.print("DEFAULT_WOREST_ERROR_PERCENTAGE " + DEFAULT_WOREST_ERROR_PERCENTAGE + "\n");
		
		System.out.print("Y data start ********** \n");
		for (double[] d : y) {
			System.out.print(d[0] + "\n");
		}
		System.out.print("Y data end ********** \n");
		
		System.out.print("X data start ********** \n");
		for (double[] d : x) {
			String s = "";
			for (double dd : d) {
				s += ", " + dd;
			}
			System.out.print(s + "\n");
		}
		System.out.print("X data end ********** \n");
		***/
		this.bestQuality = new double[] {
				DEFAULT_BEST_ERROR_PERCENTAGE,
				DEFAULT_BEST_ERROR_PERCENTAGE,
				DEFAULT_WOREST_ERROR_PERCENTAGE };
		currentNoOfHiddenNode = hidden;
		// this.worestMPE = new
		// double[]{NeuralNetwork.DEFAULT_WOREST_ERROR_PERCENTAGE,NeuralNetwork.DEFAULT_WOREST_ERROR_PERCENTAGE};
		try {
			this.trainOnFixedHiddenNode(x, y, ActivationSigmoid.class, hidden,
					timeConstraint, null, null);
		} catch (InstantiationException e) {
			logger.error("Can not create new activation instance", e);
		} catch (IllegalAccessException e) {
			logger.error("Can not create new activation instance", e);
		}
	}

	@Deprecated
	public void train(double[][] x, double[][] y,  int activation) {
		bestEverNetwork = null;
		bestEverError = null;
		try {
			this.train(x, y, ActivationSigmoid.class);
		} catch (InstantiationException e) {
			logger.error("Can not create new activation instance", e);
		} catch (IllegalAccessException e) {
			logger.error("Can not create new activation instance", e);
		}
	}
	
	public void train(double[][] x, double[][] y,  int activation, int hidden) {
		bestEverNetwork = null;
		bestEverError = null;
		try {
			this.trainOnFixedHiddenNode(x, y, ActivationSigmoid.class, hidden, false,null ,null);
		} catch (InstantiationException e) {
			logger.error("Can not create new activation instance", e);
		} catch (IllegalAccessException e) {
			logger.error("Can not create new activation instance", e);
		}
	}
	
	@Override
	public double predict(double[] xValue) {
		// Only one output therefore return the 0 index value
		return bestEverNetwork.compute(new BasicNeuralData(xValue)).getData(0);
	}

	@Override
	public double getResidualSumOfSquares() {
		// TODO Auto-generated method stub
		return RSS;
	}
	
	public double getSMAPE(){
		return SMAPE;
	}
	
	public double[] getQuality(){
		return bestQuality;
	}
	
	public double getMAPE() {
		return MAPE;
	}
	
	public double getRSquares(){
		return R;
	}
	

	public int getNumberOfHidden (){
		return currentNoOfHiddenNode;
	}
	
	@Override
	public long getSampleSize() {
		// TODO Auto-generated method stub
		return sample;
	}
	private BasicNetwork buildNetwork(NeuralDataSet trainingSet, int hidden, int size, Class<?> activation) throws InstantiationException, IllegalAccessException {
		BasicNetwork network = new BasicNetwork();
		this.currentNoOfHiddenNode = hidden;
		network.addLayer(new BasicLayer((ActivationFunction)activation.newInstance(), true, size));
		
		network.addLayer(new BasicLayer((ActivationFunction)activation.newInstance(), true, hidden));
		
		network.addLayer(new BasicLayer((ActivationFunction)activation.newInstance(), true, 1));
		network.getStructure().finalizeStructure();
		network.reset();
		
	
		return network;
	}
	
	private Train getTrain (NeuralDataSet trainingSet, BasicNetwork network) {

		//final Train train =
			//new ManhattanPropagation(network, trainingSet,
			//0.001);
		
		// Train the neural network, we use resilient propagation
		final ResilientPropagation train = new ResilientPropagation(network, trainingSet);
		train.setThreadCount(0);
		// Reset if improve is less than 1% over 5 cycles
		train.addStrategy(new RequiredImprovementStrategy(DEFAULT_SELECTION_LIMIT));
		
		
		return train;
	}
	

	private void trainOnModelStructureChange(double[][] x, double[][] y, Class<?> activation, BasicNetwork network, Train train) throws InstantiationException, IllegalAccessException {
		
        NeuralDataSet trainingSet = new BasicNeuralDataSet(x, y);
		
        if (network == null && train == null) {
        	network = buildNetwork(trainingSet, DEFAULT_CHANGE_HIDDEN_NO, x[0].length, activation);
    		train = getTrain(trainingSet, network);
        }
		
		int epoch = 1;
		long time = System.currentTimeMillis();
		double error[];
		double[] previousNetworkBestError = null;	
		do {
			train.iteration();
			error = getError(trainingSet, network); 
			logger.debug("Epoch #" + epoch + " Error:" + print(error));
			epoch++;
			if (isBetter(error,bestQuality) && 
					(bestEverError == null || 0.2 <= betterPercentage(error,bestEverError))) {
				bestEverNetwork = (BasicNetwork)network.clone();
				bestEverError = error;
				logger.debug("Epoch #" + epoch + " Best Ever Error:" + print(error));
				
                previousNetworkBestError = error;
				network = buildNetwork(trainingSet, currentNoOfHiddenNode + DEFAULT_CHANGE_HIDDEN_NO, x[0].length, activation);
				train = getTrain(trainingSet, network);
				time = System.currentTimeMillis();
				continue;
			}		
			
			// Change on hidden layer, retrain a new ANN
			if(System.currentTimeMillis() - time > DEFAULT_STRUCTURE_SELECTION_TIME_LIMIT ) {
				if (previousNetworkBestError != null && previousNetworkBestError.equals(bestEverError)) {					
					currentNoOfHiddenNode -= DEFAULT_CHANGE_HIDDEN_NO;					
					break;
				}
				
				previousNetworkBestError = error;
				
				network = buildNetwork(trainingSet, currentNoOfHiddenNode + DEFAULT_CHANGE_HIDDEN_NO, x[0].length, activation);
				train = getTrain(trainingSet, network);
				time = System.currentTimeMillis();
			}
			
		} while (true);
		

		logger.debug("Training Time: "+ (System.currentTimeMillis() - time));
		logger.debug("Current number of hidden node: "+ currentNoOfHiddenNode);
		calculateError(trainingSet);
	}
	
	private void train(double[][] x, double[][] y, Class<?> activation) throws InstantiationException, IllegalAccessException {
				
		bestEverNetwork = null;
		bestEverError = null;
		
		NeuralDataSet trainingSet = new BasicNeuralDataSet(x, y);
		// If this is a new model
		if (currentNoOfHiddenNode == -1) {
			currentNoOfHiddenNode = DEFAULT_CHANGE_HIDDEN_NO;
		}
		
		BasicNetwork network = buildNetwork(trainingSet, currentNoOfHiddenNode, x[0].length, activation);
		Train train = getTrain(trainingSet, network);
		int epoch = 1;
		long time = System.currentTimeMillis();
		double error[];
		
			
		do {
			train.iteration();
			error = getError(trainingSet, network); 
			logger.debug("Epoch #" + epoch + " Error:" + print(error));
			epoch++;
			if (isBetter(error,bestQuality) && 
					(bestEverError == null || isBetter(error,bestEverError))) {
				bestEverNetwork = (BasicNetwork)network.clone();
				bestEverError = error;
				logger.debug("Epoch #" + epoch + " Best Ever Error:" + print(error));
			}		
			
			// Change on hidden layer, retrain a new ANN
			if(System.currentTimeMillis() - time > DEFAULT_STRUCTURE_SELECTION_TIME_LIMIT) {
		
				network = buildNetwork(trainingSet,  currentNoOfHiddenNode + DEFAULT_CHANGE_HIDDEN_NO, x[0].length, activation);
				train = getTrain(trainingSet, network);
				time = System.currentTimeMillis();
			}
			
		} while (!isBetter(error,bestQuality));
	
		
		logger.debug("Training Time: "+ (System.currentTimeMillis() - time));
		logger.debug("Current number of hidden node: "+ currentNoOfHiddenNode);
		calculateError(trainingSet);
	}
	
	private void trainOnFixedHiddenNode(double[][] x, double[][] y, Class<?> activation, int hidden, boolean timeConstraint, BasicNetwork network, Train train) throws InstantiationException, IllegalAccessException {
		
		bestEverNetwork = null;
		bestEverError = null;
		
		NeuralDataSet trainingSet = new BasicNeuralDataSet(x, y);
		
		if (network == null && train == null) {
		    network = buildNetwork(trainingSet, hidden, x[0].length, activation);
			train = getTrain(trainingSet, network);
		}
		
		int epoch = 1;
		long time = System.currentTimeMillis();
		double error[];
		
			
		do {
			train.iteration();
			error = getError(trainingSet, network); 
			logger.debug("Epoch #" + epoch + ", Error:" + print(error));
			epoch++;
			if (bestEverError == null || isBetter(error,bestEverError)) {
				bestEverNetwork = (BasicNetwork) network.clone();
				bestEverError = error;
				logger.debug("Epoch #" + epoch + ", find better error:" + print(error));
			}		
			
			// Retrain as this is not model structure selection, therefore there always be a valid model
			/*if (!timeConstraint && !isBetter(error,bestQuality) && 
					(System.currentTimeMillis() - time) > NeuralNetwork.DEADLOCK_DETECTION_THRESHOLD){
				trainOnFixedHiddenNode(x, y, activation, hidden, timeConstraint);
				return;
			}*/
			
		} while (!isBetter(bestEverError,bestQuality) && (!timeConstraint || (timeConstraint &&
				(System.currentTimeMillis() - time) < DEFAULT_MAX_TRAINING_TIME_LIMIT)));
	
		if (!isBetter(bestEverError,bestQuality)) {
			logger.info("[Bad training error] at Epoch #" + epoch + ", Best Ever Error:" + print(bestEverError));
		} else {
			logger.info("[Good training error] at Epoch #" + epoch + ", Best Ever Error:" + print(bestEverError));
		}
		
		
		
		logger.debug("Training Time: "+ (System.currentTimeMillis() - time));
		logger.debug("Current number of hidden node: "+ hidden);
		if(!calculateError(trainingSet)){
			logger.error("Getting invalid ntwork, therefore doing retraining.");
			this.trainOnFixedHiddenNode(x, y, activation, hidden, timeConstraint, network, train);
		}
	}
	
	private boolean isBetter (double[] first, double[] second) {
		
		if (second == bestQuality) {
			// May lead to over-fitting
			if (first[first.length == 1? 0 : 1] <= second[2]) {
				return false;	
			}
		}
		
		for (int i = 0;i < first.length; i++) {			
			if(first[i] > second[i]) {						
					return false;				
			}
		}
		return true;
	}
	
	private double betterPercentage (double[] first, double[] second) {
		double total = 0.0;
		for (int i = 0;i < first.length; i++) {			
			if(first[i] > second[i]) {
				return 0.0;				
			} else {
				total += 1 - first[i]/second[i];
			}
		}
		return (double)total/first.length;
	}
	
	private String print(double[] error) {
		return error.length > 1?"RS/RV: " + error[0] + ", MAPE: " + error[1] :
			"MAPE: " + error[0];
	}
	
	private double[] getError(NeuralDataSet trainingSet, BasicNetwork network){
		double total = 0.0;
		double size = 0;
		double RSS = 0.0;
		double Ry = 0.0;
		for (MLDataPair pair : trainingSet) {

			final MLData output = network.compute(pair.getInput());
			if (Double.isNaN(output.getData(0))) {
				throw new RuntimeException("There is a NaN! may be something wrong with the data conversion");
			}
			double result = (Double.isNaN(output.getData(0)))? pair.getIdeal().getData(0) : output.getData(0); 
			//System.out.print("Result ********** " +result+"\n");
			total += calculateEachMAPE(pair.getIdeal().getData(0),
					result);

			double difference = pair.getIdeal().getData(0) - result;
			RSS += Math.pow(difference, 2);
			Ry += Math.pow(pair.getIdeal().getData(0) - meanIdeal, 2);
			size++;
		}
	
		return new double[]{Ry==0?RSS:RSS/Ry, 
				total/size};
	}
	
	private double[] getPredictError(BasicNetwork network){
		double total = 0.0;
		double size = 0;
		double value = 0;
	
		for(int i = 0; i < px.length; i++ ) {
			value = network.compute(new BasicNeuralData(px[i])).getData(0);
			total += calculateEachSMAPE(py[i][0], value);
			size++;
		}
		
		return new double[]{total/size};
	}
	
	private boolean calculateError (NeuralDataSet trainingSet){
		logger.debug("Neural Network Results:");
		double RSS = 0.0;
		double Ry = 0.0;
		double SMAPE = 0.0;
		double MAPE = 0.0;
		int no = 0;
		double all = 0.0;
		double sum = 0;
		sample = ((BasicNeuralDataSet)trainingSet).getRecordCount();
		
		for(MLDataPair pair: trainingSet ) {
		   final MLData output = bestEverNetwork.compute(pair.getInput());
		   double result = (Double.NaN == output.getData(0))? pair.getIdeal().getData(0) : output.getData(0); 
		   sum += result;
		   double difference = pair.getIdeal().getData(0) - result;
		   RSS += Math.pow(difference, 2);
		   all += Math.abs(difference);
		 
			   
		   SMAPE += calculateEachSMAPE(pair.getIdeal().getData(0), result);

		   MAPE += calculateEachMAPE(pair.getIdeal().getData(0), result);
		   Ry += Math.pow(pair.getIdeal().getData(0) - meanIdeal, 2);
		   logger.debug("Actual=" + output.getData(0) + ", Ideal=" + pair.getIdeal().getData(0));
		   no++;
		}
		this.RSS = RSS;
		//System.out.print("Ry: " + Ry + "\n");
		this.R = 1-(Ry==0? RSS : RSS/Ry);
		this.SMAPE = SMAPE/no;
		this.MAPE = MAPE/no;
		System.out.print(no+"MSE: " + bestEverNetwork.calculateError(trainingSet) + "\n");
		return sum != 0;
	}


}
