package org.ssase.dataset;

public class EWMAChartDM {

    private static final long serialVersionUID = -3518369648142099719L;

    //private static final int DDM_MINNUMINST = 30;
//    public IntOption minNumInstancesOption = new IntOption(
//            "minNumInstances",
//            'n',
//            "The minimum number of instances before permitting detecting change.",
//            30, 0, Integer.MAX_VALUE);
//
//    public FloatOption lambdaOption = new FloatOption("lambda", 'l',
//            "Lambda parameter of the EWMA Chart Method", 0.2, 0.0, Float.MAX_VALUE);

    private int min_sample = 1;
    private double m_n;

    private double m_sum;
    
    private double m_p;
    
    private double m_s;
    
    private double lambda;
    
    private double z_t;
    
    protected boolean isChangeDetected = false;
    protected boolean isInitialized = false;
    protected boolean isWarningZone = false;
    protected double estimation;
    protected double delay;
    
    
    private double sum;

    private double x_mean;

    private double alpha;

    private double delta;
    
    public EWMAChartDM() {
        resetLearning();
    }
    
    public boolean hasChange(){
    	return isChangeDetected;
    }

 
    public void resetLearning() {
        m_n = 1.0;
        m_sum = 0.0;
        m_p = 0.0;
        m_s = 0.0;
        z_t = 0.0;
        lambda = 0.2;// this.lambdaOption.getValue();
        
      
        x_mean = 0.0;
        sum = 0.0;
        delta = 0.005;
        alpha = 1 - 0.0001;
        lambda = 0.02;
    }

    
    //PageHinkleyDM
    public void input(double x) {
    	// It monitors the error rate
        if (this.isChangeDetected == true || this.isInitialized == false) {
            resetLearning();
            this.isInitialized = true;
        }

        x_mean = x_mean + (x - x_mean) / (double) m_n;
        sum = this.alpha * sum + (x - x_mean - this.delta);

        m_n++;

        // System.out.print(prediction + " " + m_n + " " + (m_p+m_s) + " ");
        this.estimation = x_mean;
        this.isChangeDetected = false;
        this.isWarningZone = false;
        this.delay = 0;

        if (m_n < 1) {
            return;
        }

        if (sum > this.lambda) {
            this.isChangeDetected = true;
        } 
    }
    
 
    public void input1(double prediction) {
        // prediction must be 1 or 0
        // It monitors the error rate
         if (this.isChangeDetected == true || this.isInitialized == false) {
            resetLearning();
            this.isInitialized = true;
        }

        m_sum += prediction;
        
        m_p = m_sum/m_n; // m_p + (prediction - m_p) / (double) (m_n+1);

        m_s = Math.sqrt(  m_p * (1.0 - m_p)* lambda * (1.0 - Math.pow(1.0 - lambda, 2.0 * m_n)) / (2.0 - lambda));

        m_n++;

        z_t += lambda * (prediction - z_t);

        //double L_t = 2.76 - 6.23 * m_p + 18.12 * Math.pow(m_p, 3) - 312.45 * Math.pow(m_p, 5) + 1002.18 * Math.pow(m_p, 7); //%1 FP
        double L_t = 3.97 - 6.56 * m_p + 48.73 * Math.pow(m_p, 3) - 330.13 * Math.pow(m_p, 5) + 848.18 * Math.pow(m_p, 7); //%1 FP
        //double L_t = 1.17 + 7.56 * m_p - 21.24 * Math.pow(m_p, 3) + 112.12 * Math.pow(m_p, 5) - 987.23 * Math.pow(m_p, 7); //%1 FP

        // System.out.print(prediction + " " + m_n + " " + (m_p+m_s) + " ");
        this.estimation = m_p;
        this.isChangeDetected = false;
        this.isWarningZone = false;
        this.delay = 0;

        if (m_n < min_sample) {
            return;
        }
            
        if (m_n > min_sample && z_t > m_p + L_t * m_s) {
            //System.out.println(m_p + ",D");
            this.isChangeDetected = true;
            //resetLearning();
        } else if (z_t > m_p + 0.5 *  L_t * m_s) {
            //System.out.println(m_p + ",W");
            this.isWarningZone = true;
        } else {
            this.isWarningZone = false;
            //System.out.println(m_p + ",N");
        }
    }

    
}
