package org.ssase.model.iapm;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import moa.streams.ArffFileStream;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;

public class DataGenerator {

  /**
   * @param args
   */
  int numClasses;
  int numInstances;
  double[][] classPercentage;
  double decayfactor = 0.99;
  
  DataGenerator(Instances data){
    numClasses = data.numClasses();
    numInstances = data.numInstances();
    classPercentage = new double[data.numInstances()][numClasses];
  }
  
  public static void main(String[] args) throws Exception {
    // TODO Auto-generated method stub
    String path = "E:\\syw\\My Files\\IJCAI16\\Data\\Real\\";
    String fname = "tweet_7class";
    String fext = ".arff";
    DataSource source = new DataSource(path + fname +fext);
    Instances data = source.getDataSet();
    if (data.classIndex() == -1)
      data.setClassIndex(data.numAttributes() - 1);
    
    DataGenerator generator = new DataGenerator(data);
    
    //ONLY For static scenarios: 
    //randomize original data so each class has a uniform distribution in the data stream 
    //generator.saveData(generator.randomizeData(data), path+fname+fext);
    
    
    //Convert arff file to csv file (for the use in matlab)
    String file = path+fname+".arff";
    String file2 = path+fname+".csv";
    DataSource sourceArff = new DataSource(file);
    Instances dataArff = sourceArff.getDataSet();
    CSVSaver saver = new CSVSaver();
    saver.setInstances(dataArff);
    saver.setFile(new File(file2));
    saver.writeBatch();
    
    /*
    //display data class imbalance status at real time
    ArffFileStream data2 = new ArffFileStream(path + fname +fext,-1);
    data2.prepareForUse();
    int realLabel;
    int numSamples_Total = 0; 
    while(data2.hasMoreInstances()){
      Instance trainInst = data2.nextInstance();
      realLabel = (int)trainInst.classValue();
      numSamples_Total ++;
      generator.updateClassPercentage(realLabel, numSamples_Total, generator.decayfactor);
      for(int i = 0; i < generator.numClasses; i++){
	System.out.print(generator.classPercentage[numSamples_Total-1][i] + "\t");
      }
      System.out.println();
    }*/
    
    /*
    //delete some classes of data from the original data set
    int[] deleteclass = {3,4,5,6,7,10,11};
    Instances smalldata = generator.deleteClasses(data, deleteclass);
    ArffSaver saver = new ArffSaver();
    saver.setInstances(smalldata);
    saver.setFile(new File("E:\\syw\\My Files\\IJCAI16\\Data\\Real\\tweet_class_10000.arff"));
    saver.writeBatch();*/
  }
  
  public Instances deleteClasses(Instances data, int[] classval){
    Instances smalldata = new Instances(data);
    for(int i = data.numInstances()-1; i>=0; i--){
      Instance inst = data.instance(i);
      for(int j = 0; j < classval.length; j++){
	if(inst.classValue()==classval[j]){
	  smalldata.delete(i);
	  break;
	}
      }      
    }
    return smalldata;
  }

  /**Split examples for each class, and return.*/
  public Instances[] instancesPerClass(Instances data){
    Instances[] perClassIns = new Instances[numClasses];//store instances in each class;
    double classIndex;
    //initialization
    for(int c = 0; c < numClasses; c++){
      perClassIns[c] = new Instances(data,0);
    }
    
    for(int i = 0; i < numInstances; i++){
      classIndex = data.instance(i).classValue();
      perClassIns[(int)classIndex].add(data.instance(i));
    }    
    return perClassIns;
  }
  
  /**calculate the size percentage of each class in the whole data set
   * @param perClassIns examples in each class*/
  public double[] percentagePerClass(Instances[] perClassIns){
    double[] classPercentage = new double[numClasses];//store the percentage of this class in the whole data set;
    for(int c = 0; c < numClasses; c++){
      classPercentage[c] = (double)perClassIns[c].numInstances()/numInstances;
    }
    return classPercentage;
  }
  
  public Instances randomizeData(Instances data){
    Instances randomdata = new Instances(data);
    Random seed = new Random(System.currentTimeMillis());
    randomdata.randomize(seed);
    return randomdata;
  }
  
  public void saveData(Instances data, String path) throws IOException{
    ArffSaver saver = new ArffSaver();
    saver.setInstances(data);
    saver.setFile(new File(path));
    saver.writeBatch();
  }
  
  /**update percentage of classes at each time step with time decay*/
  public void updateClassPercentage(int realLabel, int numSamplesTotal, double sizedecayfactor){
    if(numSamplesTotal >1){
	for(int t = 0; t < numClasses; t++){
	  if(t==realLabel)
	    classPercentage[numSamplesTotal-1][t] = classPercentage[numSamplesTotal-2][t]*sizedecayfactor+(1-sizedecayfactor);
	  else
	    classPercentage[numSamplesTotal-1][t] = classPercentage[numSamplesTotal-2][t]*sizedecayfactor;
	}
    }
    else{
	classPercentage[numSamplesTotal-1][realLabel] = 1;
    }
  }
  
  
  /*public Instances generateData(){
    Instances newData = new Instances(perClassIns[0],0);
    //randomize data in each class
    for(int c = 0; c < numClasses; c++){
      Random seed = new Random(System.currentTimeMillis());
      perClassIns[c].randomize(seed);
    }
    //sort the classPercentage in an ascending order and return the index of the corresponding classPercentage
    int[] sortedLabels = Utils.sort(classPercentage);
    //calculate the accumulated percentage based on the ascending classPercentage. 
    //E.g. class1 percentage=0.1, class2 size=0.4, class3 size=0.5, then accumulatedPercentage = {0.1,0.5,1}
    double[] accumulatedPercentage = new double[numClasses];
    for(int c = 0; c < numClasses; c++){
      if(c==0)
	accumulatedPercentage[c] = classPercentage[sortedLabels[0]];
      else
	accumulatedPercentage[c] = classPercentage[sortedLabels[c]]+accumulatedPercentage[c-1];
    }
    //Pick example iteratively based on the generated probability number
    for(int i = 0; i < numInstances; i++){
      Instance newInstance;
      Random seed = new Random(System.currentTimeMillis());
      double prob = seed.nextDouble();
      for(int c = 0; c < numClasses; c++){
	if(prob <= accumulatedPercentage[0]){
	  newInstance = perClassIns[sortedLabels[0]].instance(index);
	}
	if(prob <= accumulatedPercentage[c] && prob > accumulatedPercentage[c-1])
      }
    }
    return newData;
  }*/
}
