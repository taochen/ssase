package org.ssase.model.onoff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import moa.core.Measurement;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.meta.AdditiveRegression;
import weka.classifiers.rules.ZeroR;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.UnassignedClassException;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

public class ExtendedOnlineBoost extends moa.classifiers.AbstractClassifier {
	/** for serialization */
	static final long serialVersionUID = -2368937577670527151L;

	/**
	 * ArrayList for storing the generated base classifiers. Note: we are hiding
	 * the variable from IteratedSingleClassifierEnhancer
	 */
	protected ArrayList<moa.classifiers.Classifier> m_Classifiers;

	/**
	 * Shrinkage (Learning rate). Default = no shrinkage.
	 */
	protected double m_shrinkage = 0.01;//1.0

	/** The model for the mean */
	protected ZeroR m_zeroR;

	/** whether we have suitable data or nor (if not, ZeroR model is used) */
	protected boolean m_SuitableData = true;

	/** The working data */
	protected Instances m_Data;

	/** The sum of squared errors */
	protected double m_SSE;

	/** The improvement in squared error */
	protected double m_Diff;
	protected double sum = 0.0;
	protected int m_NumIterations = 100;

	public moa.classifiers.Classifier[] learners = null;

	/**
	 * Returns a string describing this attribute evaluator
	 * 
	 * @return a description of the evaluator suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return " Meta classifier that enhances the performance of a regression "
				+ "base classifier. Each iteration fits a model to the residuals left "
				+ "by the classifier on the previous iteration. Prediction is "
				+ "accomplished by adding the predictions of each classifier. "
				+ "Reducing the shrinkage (learning rate) parameter helps prevent "
				+ "overfitting and has a smoothing effect but increases the learning "
				+ "time.\n\n"
				+ "For more information see:\n\n"
				+ getTechnicalInformation().toString();
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed
	 * information about the technical background of this class, e.g., paper
	 * reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;

		result = new TechnicalInformation(Type.TECHREPORT);
		result.setValue(Field.AUTHOR, "J.H. Friedman");
		result.setValue(Field.YEAR, "1999");
		result.setValue(Field.TITLE, "Stochastic Gradient Boosting");
		result.setValue(Field.INSTITUTION, "Stanford University");
		result.setValue(Field.PS,
				"http://www-stat.stanford.edu/~jhf/ftp/stobst.ps");

		return result;
	}

	/**
	 * Default constructor specifying DecisionStump as the classifier
	 */
	public ExtendedOnlineBoost( moa.classifiers.Classifier[] learners) {
		this.learners = learners;
		m_NumIterations = learners.length;
	}

	/**
	 * String describing default classifier.
	 * 
	 * @return the default classifier classname
	 */
	protected String defaultClassifierString() {

		return "weka.classifiers.trees.DecisionStump";
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String shrinkageTipText() {
		return "Shrinkage rate. Smaller values help prevent overfitting and "
				+ "have a smoothing effect (but increase learning time). "
				+ "Default = 1.0, ie. no shrinkage.";
	}

	/**
	 * Set the shrinkage parameter
	 * 
	 * @param l
	 *            the shrinkage rate.
	 */
	public void setShrinkage(double l) {
		m_shrinkage = l;
	}

	/**
	 * Get the shrinkage rate.
	 * 
	 * @return the value of the learning rate
	 */
	public double getShrinkage() {
		return m_shrinkage;
	}

	/**
	 * Method used to build the classifier.
	 */
	public void buildClassifier(Instances data) throws Exception {

		// // Initialize classifier
		// initializeClassifier(data);
		//
		// // For the given number of iterations
		// while (next()) {};
		//
		// // Clean up
		// done();
	}

	/**
	 * Initialize classifier.
	 * 
	 * @param data
	 *            the training data
	 * @throws Exception
	 *             if the classifier could not be initialized successfully
	 */
	public void initializeClassifier(Instances data) throws Exception {

		// can classifier handle the data?
		// getCapabilities().testWithFail(data);

		// remove instances with missing class

		m_Data.deleteWithMissingClass();
		
		// Add the model for the mean first
		m_zeroR = new ZeroR();
		m_zeroR.buildClassifier(m_Data);

		// only class? -> use only ZeroR model
		if (m_Data.numAttributes() == 1) {
			System.err
					.println("Cannot build model (only class attribute present in data!), "
							+ "using ZeroR model instead!");
			m_SuitableData = false;
			return;
		} else {
			m_SuitableData = true;
		}

		// Initialize list of classifiers and data
		m_Classifiers = new ArrayList<moa.classifiers.Classifier>(
				learners.length);
		for (int i = 0;i < learners.length;i++) {
			m_Classifiers.add(learners[i]);
		}
		
		
		m_Data = residualReplace(m_Data, m_zeroR, false);
		
		// Calculate sum of squared errors
		m_SSE = 0;
		m_Diff = Double.MAX_VALUE;
		for (int i = 0; i < m_Data.numInstances(); i++) {
			m_SSE += m_Data.instance(i).weight()
					* m_Data.instance(i).classValue()
					* m_Data.instance(i).classValue();
		}

	}

	/**
	 * Perform another iteration.
	 */
	public boolean next(int k) throws Exception {

		if ((!m_SuitableData) || (k >= m_NumIterations)
				) {
			return false;
		}
		//System.out.print(m_Data.instance(m_Data.size() - 1).classValue()+"toAdd************\n");
		//System.out.print(k+"\n");
		m_Classifiers.get(k)
				.trainOnInstance(m_Data.instance(m_Data.size() - 1));
		//System.out.print(m_Data.instance(m_Data.size() - 1).classValue()+"toAdd************\n");
		m_Data = residualReplace(m_Data,
				m_Classifiers.get(k), true);
		sum = 0;
		for (int i = 0; i < m_Data.numInstances(); i++) {
			sum += m_Data.instance(i).weight()
					* m_Data.instance(i).classValue()
					* m_Data.instance(i).classValue();
		}

		m_Diff = m_SSE - sum;
		m_SSE = sum;

		return true;
	}

	/**
	 * Clean up.
	 */


	/**
	 * Replace the class values of the instances from the current iteration with
	 * residuals ater predicting with the supplied classifier.
	 * 
	 * @param data
	 *            the instances to predict
	 * @param c
	 *            the classifier to use
	 * @param useShrinkage
	 *            whether shrinkage is to be applied to the model's output
	 * @return a new set of instances with class values replaced by residuals
	 * @throws Exception
	 *             if something goes wrong
	 */
	private Instances residualReplace(Instances data,
			moa.classifiers.Classifier c, boolean useShrinkage)
			throws Exception {
		double pred, residual;
		Instances newInst = new Instances(data);

		for (int i = 0; i < newInst.numInstances(); i++) {
			pred = c.getVotesForInstance(newInst.instance(i))[0];
			if (Utils.isMissingValue(pred)) {
				throw new UnassignedClassException(
						"AdditiveRegression: base learner predicted missing value.");
			}
			if (useShrinkage) {
				pred *= getShrinkage();
			}
			residual = newInst.instance(i).classValue() - pred;
			newInst.instance(i).setClassValue(residual);
		}
		// System.err.print(newInst);
		return newInst;
	}

	private Instances residualReplace(Instances data,
			weka.classifiers.Classifier c, boolean useShrinkage)
			throws Exception {
		double pred, residual;
		Instances newInst = new Instances(data);

		for (int i = 0; i < newInst.numInstances(); i++) {
			pred = c.classifyInstance(newInst.instance(i));
			if (Utils.isMissingValue(pred)) {
				throw new UnassignedClassException(
						"AdditiveRegression: base learner predicted missing value.");
			}
			if (useShrinkage) {
				pred *= getShrinkage();
			}
			residual = newInst.instance(i).classValue() - pred;
			newInst.instance(i).setClassValue(residual);
		}
		// System.err.print(newInst);
		return newInst;
	}

	
	
	@Override
	public boolean isRandomizable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double[] getVotesForInstance(Instance inst) {
		double prediction = m_zeroR.classifyInstance(inst);

	    // default model?
	    if (!m_SuitableData) {
	      return new double[]{prediction};
	    }
	   
	    for (moa.classifiers.Classifier classifier : m_Classifiers) {
	      double toAdd = classifier.getVotesForInstance(inst)[0];
	      //System.out.print(classifier+"toAdd************\n");
	      if (Utils.isMissingValue(toAdd)) {
	        throw new UnassignedClassException("AdditiveRegression: base learner predicted missing value.");
	      }
	      toAdd *= getShrinkage();
	      prediction += toAdd;
	    }

	    return new double[]{prediction};
	  
	}

	@Override
	public void resetLearningImpl() {
		// TODO Auto-generated method stub

	}

	@Override
	public void trainOnInstanceImpl(Instance inst) {
		m_Data = getInstances(inst);
		//System.out.print(m_Data.instance(m_Data.size() - 1).classValue()+"toAdd************\n");
		try {
			if (m_Classifiers == null) {
				//System.out.print(this.hashCode() +"toAdd************\n");
				initializeClassifier(m_Data);
				//System.out.print(m_Classifiers.hashCode() +"toAdd************\n");
			}

			for (int i = 0; i < m_NumIterations; i++) {
				next(i);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Initialize classifier

		// Clean up
		// done();

	}

	@Override
	protected Measurement[] getModelMeasurementsImpl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getModelDescription(StringBuilder out, int indent) {
		// TODO Auto-generated method stub

	}

	public Instances getInstances(Instance inst) {
		Instances insts;
		FastVector atts = new FastVector();
		for (int i = 0; i < inst.numAttributes(); i++) {
			atts.addElement(inst.attribute(i));
		}
		insts = new Instances("CurrentTrain", atts, 0);
		insts.add(inst);
		insts.setClassIndex(inst.numAttributes() - 1);
		
		return insts;
	}
}
