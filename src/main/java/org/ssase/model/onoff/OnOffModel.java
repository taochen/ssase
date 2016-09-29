package org.ssase.model.onoff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import moa.evaluation.ClassificationPerformanceEvaluator;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.IterativeMultiKMeans;
import net.sf.javaml.clustering.evaluation.AICScore;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.distance.EuclideanDistance;

import org.ssase.model.Model;
import org.ssase.model.iapm.OnlineMultilayerPerceptron;
import org.ssase.model.iapm.OzaBag;
import org.ssase.model.iapm.SampleCluster;
import org.ssase.model.selection.PrimitiveLearner;
import org.ssase.objective.QualityOfService;
import org.ssase.observation.event.ModelChangeEvent;
import org.ssase.observation.listener.ModelListener;
import org.ssase.primitive.Primitive;
import org.ssase.util.Util;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.core.WekaPackageManager;

/**
 * 
 * If we should use the random workload? no
 * 
 * If we should normalize data before use? yes, should normalize
 * 
 * If we should use sigmoid function at the last layer of MLP? yes
 * 
 * MLP max normalization - online vs offline: A:2.560280877150219   ,M:0.07298985336509639  vs A:  ,M: 
 * LR max normalization - online vs offline: A:2.3586459625144047,M:0.4468200268164165 vs A:2.7431588876480415,M:0.6151308622151234
 * KNN max normalization - online vs offline: A:1.8770617913706749  ,M:0.037196640316205565  vs A:1.7871255828878725 ,M:0.01183026960878282  
 * K* max normalization - online vs offline: A:1.785316196870693   ,M:0.017009592527057707  vs A:1.7892980782552972 ,M:0.020379959940132773 
 * SVM max normalization - online vs offline: A:1.9394570485354783   ,M:0.13296058389895804  vs A:4.751551357753054 ,M:3.170053054988548 
 * 
 * @author tao
 * 
 */
public class OnOffModel implements Model {
	/**
	 * @param args
	 * @throws Exception
	 */

	public static LearningType selected = LearningType.MLP;

	private moa.classifiers.AbstractClassifier onlineModel = null;

	private weka.classifiers.AbstractClassifier offlineModel = null;

	private String name;

	protected Set<Primitive> possibleInputs;

	protected List<Primitive> inputs = new ArrayList<Primitive>();
	protected Map<Primitive,Double> old_inputs = new HashMap<Primitive,Double> ();
	protected QualityOfService output;

	public static boolean isOnline = false;
	public static boolean isRetrainForChangedMax = true;
	public boolean isMaxChange = false;
	private double[] xMax;
	private double yMax;

	private PrimitiveLearner primitiveLearner = new PrimitiveLearner();
	private Set<ModelListener> listeners = new HashSet<ModelListener>();

	// private Dataset ds = new DefaultDataset();
	private Instances dataRaw = null;

	private int changeRestart = 0;
	private ArrayList<Attribute> attrs = new ArrayList<Attribute>();

	public OnOffModel(String name, Set<Primitive> possibleInputs,
			QualityOfService output) {
		super();
		this.name = name;
		this.possibleInputs = possibleInputs;
		this.output = output;
	}

	public void selectPrimititvesAndTrainModels() {
		boolean isFeaturesChanged = false;
		//
		// output.expend();
		// for (Primitive p : possibleInputs) {
		// if (p instanceof ControlPrimitive) {
		// ((ControlPrimitive)p).expend();
		//
		// } else {
		// ((EnvironmentalPrimitive)p).expend();
		// }
		// }
		//
		isMaxChange = false;
		// This set needs to be transfered into a sorted collection.
		Set<Primitive> newInputs = primitiveLearner.select(output,
				possibleInputs);

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
		System.out.print("Number of inputs: " + inputs.size() + "\n");

		// for (Primitive p : inputs) {
		// System.out.print(p.getName() + "\n");
		// }

		if (inputs.size() == 0) {
			return;
		}

		if (yMax != output.getMax()) {
			isMaxChange = true;
		}

		yMax = output.getMax();
		
	
		
		
		if(old_inputs.size() != inputs.size()) {
			//System.out.print(old_inputs.size() + " : " + inputs.size() +"size\n");
			isMaxChange = true;
		} else {
			for(Primitive p : inputs) {
				if(!old_inputs.containsKey(p)) {
					isMaxChange = true;
				} else {
					if(old_inputs.get(p) != p.getMax()) {
						isMaxChange = true;
						//System.out.print(old_inputs.get(p) + " : " + p.getMax() +"\n");
					}
				}
			}
		}
		
		old_inputs.clear();
		for(Primitive p : inputs) {
			old_inputs.put(p, p.getMax());
		}
		
		xMax = new double[inputs.size()];
		
		for (int i = 0; i < inputs.size(); i++) {
			xMax[i] = inputs.get(i).getMax();
		}

		if (isRetrainForChangedMax && isMaxChange) {
			if (isOnline) {
				onlineModel = null;
				isMaxChange = true;
				attrs = new ArrayList<Attribute>();

				int i = 0;
				for (Primitive p : inputs) {
					attrs.add(new Attribute(p.getName(), i));
					i++;
				}

				attrs.add(new Attribute(output.getName(), i));

				//System.out.print(attrs.get(attrs.size() - 1).type()
						//+ "***********type!!!\n");
				dataRaw = new Instances("data_instances", attrs, 0);
				dataRaw.setClassIndex(dataRaw.numAttributes() - 1);
			}
		}
		
		// Reset everything
		if (isFeaturesChanged) {
			// TODO might be use the recorded historical data together?
			onlineModel = null;
			// Copy the new instances

			attrs = new ArrayList<Attribute>();

			int i = 0;
			for (Primitive p : inputs) {
				attrs.add(new Attribute(p.getName(), i));
				i++;
			}

			attrs.add(new Attribute(output.getName(), i));

			System.out.print(attrs.get(attrs.size() - 1).type()
					+ "***********type!!!\n");
			dataRaw = new Instances("data_instances", attrs, 0);
			dataRaw.setClassIndex(dataRaw.numAttributes() - 1);
			// Notify the region control when primitives selection result
			// change.
			// At this level, the optimization algorithm would be also notified
			// to
			// abort the optimization.
			for (ModelListener listener : listeners) {
				listener.updateWhenModelChange(new ModelChangeEvent(true,
						output));
			}

		}

		if (dataRaw == null) {

			int i = 0;

			for (Primitive p : inputs) {
				attrs.add(new Attribute(p.getName(), i));
				i++;
			}

			attrs.add(new Attribute(output.getName(), i));

			dataRaw = new Instances("data_instances", attrs, 0);
		}

		try {

			System.out.print(dataRaw.size()+"before size**************\n");
			if (dataRaw.size() == 0) {
				
				if(isOnline) {
					onlineModel = null;
					changeRestart++;
					System.out.print("**************isMaxChange!!**************\n");
					for (int i = 0; i < output.getArray().length; i++) {
						run(convertIntoWekaInstance(i));
					}
				} else {
					changeRestart++;
					for (int i = 0; i < output.getArray().length-1; i++) {
						convertIntoWekaInstance(i);
					}
					run(convertIntoWekaInstance());
				}
				

			} else {
				run(convertIntoWekaInstance());
			}
			System.out.print(dataRaw.size()+"after size**************\n");
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.print("Change " + changeRestart + "\n");
	}

	private Instance convertIntoWekaInstance() {

		final Instance trainInst = new DenseInstance(inputs.size() + 1);

		for (int i = 0; i < inputs.size(); i++) {
			trainInst.setValue(
					attrs.get(i),
					Model.isNormalizeModelingData ? inputs.get(i).getValue()
							/ inputs.get(i).getMax() : Util.sigmoid(inputs.get(
							i).getValue()));
		}

		trainInst.setValue(
				attrs.get(attrs.size() - 1),
				Model.isNormalizeModelingData ? output.getValue()
						/ output.getMax() : Util.sigmoid(output.getValue()));

		dataRaw.add(trainInst);
		trainInst.setDataset(dataRaw);
		dataRaw.setClassIndex(trainInst.numAttributes() - 1);

		return trainInst;
	}

	private Instance convertIntoWekaInstance(int k) {

		final Instance trainInst = new DenseInstance(inputs.size() + 1);

		for (int i = 0; i < inputs.size(); i++) {
			trainInst
					.setValue(
							attrs.get(i),
							Model.isNormalizeModelingData ? inputs.get(i)
									.getArray()[k] / 100 : Util.sigmoid(inputs
									.get(i).getArray()[k]
									* inputs.get(i).getMax() / 100));
		}

		trainInst.setValue(
				attrs.get(attrs.size() - 1),
				Model.isNormalizeModelingData ? output.getArray()[k] / 100
						: Util.sigmoid(output.getArray()[k] * output.getMax()
								/ 100));

		dataRaw.add(trainInst);
		trainInst.setDataset(dataRaw);
		dataRaw.setClassIndex(trainInst.numAttributes() - 1);

		return trainInst;
	}

	private void run(Instance trainInst/* use only one instance per time step */)
			throws Exception {

		if (isOnline) {

			if (selected == LearningType.MLP) {
				if (onlineModel == null) {
					onlineModel = new OnlineMultilayerPerceptron();
					((OnlineMultilayerPerceptron) onlineModel)
							.setValidationSetSize(0);
					((OnlineMultilayerPerceptron) onlineModel)
							.setTrainingTime(5000);
					((OnlineMultilayerPerceptron) onlineModel).setNormalizeNumericClass(false);
					((OnlineMultilayerPerceptron) onlineModel).setNormalizeAttributes(false);
					((OnlineMultilayerPerceptron) onlineModel)
							.initMLP(trainInst);
				}

			} else if (selected == LearningType.LR) {
				if (onlineModel == null) {
					onlineModel = initializeWEKAClassifier("weka.classifiers.lazy.LWL");
					// onlineModel.prepareForUse();
				}
			} else if (selected == LearningType.KNN) {
				if (onlineModel == null) {
					onlineModel = initializeWEKAClassifier("weka.classifiers.lazy.IBk");
					// onlineModel.prepareForUse();
				}
			} else if (selected == LearningType.KS) {
				if (onlineModel == null) {
					onlineModel = initializeWEKAClassifier("weka.classifiers.lazy.KStar");
				}
			} else if (selected == LearningType.DT) {
				if (onlineModel == null) {
					onlineModel = new moa.classifiers.trees.HoeffdingTree();
					onlineModel.prepareForUse();
				}
			} else if (selected == LearningType.SVM) {
				if (onlineModel == null) {
					onlineModel = new moa.classifiers.functions.SGD();
					onlineModel.prepareForUse();
				}
			}

			onlineModel.trainOnInstance(trainInst);
		} else {

			if (selected == LearningType.MLP) {

				offlineModel = new MultilayerPerceptron();
				((MultilayerPerceptron) offlineModel).setValidationSetSize(0);
				((MultilayerPerceptron) offlineModel).setTrainingTime(5000);

			} else if (selected == LearningType.LR) {
				offlineModel = new weka.classifiers.lazy.LWL();
			} else if (selected == LearningType.KNN) {
				offlineModel = new weka.classifiers.lazy.IBk();
			} else if (selected == LearningType.KS) {
				offlineModel = new weka.classifiers.lazy.KStar();
			} else if (selected == LearningType.DT) {
				offlineModel = new weka.classifiers.functions.SMOreg();//new ExtendedBagging();// new
														// weka.classifiers.trees.M5P();
				
//				for (int i = 0; i < dataRaw.size();i++) {
//					System.out.print(dataRaw.instance(i).classValue() + "class\n");
//				}
				
				
			} else if (selected == LearningType.SVM) {
				offlineModel = new ExtendedSGD();
				//offlineModel = new weka.classifiers.functions.SMOreg();
				//((weka.classifiers.functions.SMOreg)offlineModel).setFilterType(new SelectedTag(2, weka.classifiers.functions.SMOreg.TAGS_FILTER));
			}

			offlineModel.buildClassifier(dataRaw);
		}

	}

	private moa.classifiers.AbstractClassifier initializeWEKAClassifier(
			String name) {

		CustomWEKAClassifier weka = new CustomWEKAClassifier();
		try {
			weka.createWekaClassifier(name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return weka;
	}

	@Override
	public List<Primitive> getInputs() {
		// TODO Auto-generated method stub
		return inputs;
	}

	@Override
	public QualityOfService getOutput() {
		// TODO Auto-generated method stub
		return output;
	}

	@Override
	public void updateNewlyError(double[] xValue, double yValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateNewlyErrorWithReturn(double[] result) {
		// TODO Auto-generated method stub

	}

	@Override
	public double[] updateNewlyErrorWithReturn(double[] xValue, double yValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double predict(double[] xValue, boolean isSU, double a, double b) {
		return predict(xValue, -1);
	}

	@Override
	public double predict(double[] xValue, int index) {
		final Instance trainInst = new DenseInstance(xValue.length + 1);

		for (int i = 0; i < xValue.length; i++) {
			trainInst.setValue(attrs.get(i), xValue[i]);
		}
		// This should not do anything, just some learner need it for setting
		// range.
		trainInst.setValue(attrs.get(xValue.length), 0.0);
		trainInst.setDataset(dataRaw);
		double[] p = null;
		if(offlineModel == null && onlineModel == null && dataRaw.size() > 0) {
			return 0;
		}
		try {
			p = isOnline ? onlineModel.getVotesForInstance(trainInst)
					: offlineModel.distributionForInstance(trainInst);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.print("size*********" + p.length + "\n");
		for (double d : p) {
			System.out.print("vote*********" + d + "\n");
		}

		return Math.abs(p[0] * 100);
	}

	@Override
	public double getYMax() {
		return yMax;
	}

	@Override
	public double getXMax(int i) {
		return xMax[i];
	}

	@Override
	public void addListener(ModelListener listener) {
		for (ModelListener inlistener : listeners) {
			if (listener.getClass().isInstance(inlistener)) {
				listeners.remove(inlistener);
			}
		}

		listeners.add(listener);

	}

	public Primitive get(int i) {
		return inputs.get(i);
	}

	public int getSize() {
		return inputs.size();
	}

	public int countFunction() {
		return 1;
	}

	public enum LearningType {
		MLP, LR, KNN, KS, DT, SVM
	}
}
