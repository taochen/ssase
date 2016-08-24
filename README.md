SSASE (including SAM, FEMOSAA, RCA and MOACO)
==========

This is the main repository for Search-based Self-Adaptive Software Engine (SSASE), an adaptation engine framework that exploits the advances of search-based software engineering techniques to tackle modelings, architecting and decision making problems for self-adaptive software systems, especially at runtime.

Currently it encapsulate the following sub-frameworks/components:

###SAM: Self-Adaptive Modeling###

This is a framework that enables self-adaptive feature selection and selection of learning algorithms to model the correlation between control features, environments, interference to Quality of Service (QoS) attributes (e.g., response time). More details can be found in the following publications:

 > * T. Chen and R. Bahsoon , Self-Adaptive and Online QoS Modeling for Cloud-Based Software Services. IEEE Transactions on Software Engineering, to appear.

 > * T. Chen, R. Bahsoon and X. Yao. Online QoS Modeling in the Cloud: A Hybrid and Adaptive Multi-Learners Approach. The 7th IEEE/ACM International Conference on Utility and Cloud Computing (UCC2014), London, UK. 2014.

 > * T. Chen and R. Bahsoon. Self-Adaptive and Sensitivity-Aware QoS Modelling for the Cloud. The 8th International Symposium on Software Engineering for Adaptive and Self-Managing Systems, SEAMS in conjunction with the 35th International Conference on Software Engineering (ICSE), San Francisco, CA, 2013.



Source code directory:
  * src/main/java/org/ssase/model/

 ###RCA : Region Controlled Architecture (for cloud environment only)###

This is a component that intelligent divide the architecture with respect to objective-dependency, which is determined by the inputs of QoS model.  More details can be found in the following publications:

 > * T. Chen and R. Bahsoon. Symbiotic and Sensitivity-Aware Architecture for Globally-Optimal Benefit in Self-Adaptive Cloud. The 9th International Symposium on Software Engineering for Adaptive and Self-Managing Systems, SEAMS in conjunction with the 36th International Conference on Software Engineering (ICSE), India, 2014.

 > * T. Chen, R. Bahsoon and G. Theodoropoulos. A Decentralized Architecture for Dynamic QoS Optimization in Cloud-based DDDAS. 2013 International Conference on Computational Science, Procedia of Computer Science, Elsevier Science, 2013.

Source code directory:
  * src/main/java/org/ssase/region/



 ###FEMOSAA: Feature Guided and Knee Driven Multi-Objective Optimization for Self-Adaptive Software at Runtime### 

This is a novel framework that automatically synergizes the feature model and Multi-Objective Evolutionary Algorithm (MOEA), to optimize SAS’s conflicting QoS objectives at runtime. At design time, FEMOSAA automatically transposes the design of SAS, which is expressed as a feature model, to the chromosome representation and the reproduction operators (mutation and crossover) in MOEA. At runtime, the feature model serves as the domain knowledge to guide the search, providing more chances to find better solutions. FEMOSAA contains a new method to search for the knee solutions, which can achieve balanced trade-off. This work is currently being submitted for publication.

Source code directory:
   * src/main/java/org/ssase/objective/optimization/femosaa/

 ###MOACO: Self-Adaptive and Interference-Aware Multi-Objective Ant Colony Optimisation for Decision Making in Self-Adaptive Software### 

This is a component that exploits multi-objective ant colony algorithm to optimise adaptation decisions for self-adaptive software system at runtime. It particularly considers QoS interference caused by multi-tenants and virtualized environment, e.g. cloud computing. The approach leverage nash dominance, a popular economic principle, to find well-compromised/knee trade-off decisions. More details can be found in the following publications:

  > * T. Chen and R. Bahsoon , Self-Adaptive Trade-off Decision Making for Autoscaling Cloud-Based Services. IEEE Transactions on Services Computing, 2015, doi:10.1109/TSC.2015.2499770.

Source code directory:
   * src/main/java/org/ssase/objective/optimization/moaco/


For all experiments in the published papers, we have used the following benchmarks and workload trace:

 * RUBiS - we have extended the original RUBiS by installing various sensors, source code can be found at adaptable-software/rubis/


 * FIFA98 workload trace - we have extracted and compressed the trace to meet our demand, the files and parsing scripts can be found at http://ita.ee.lbl.gov/html/contrib/WorldCup.html and fifa98/ respectively.


Note that all the sub-framework/components can be switch off/on in the ControlBus.java class. We are in the progress to implement more seamless and flexible to control those components.

The SSASE repository contains a pom.xml for Maven build, but the Ant build file has not yet been completed. Potential users are advised to use Maven for building the project.

To use this framework, the only necessary configurations are under src/main/resources/. The files are explained as below:

 * dom0.properties specifies the port/ip address of the adaptable software. It should be placed with the main framework as an adaptation engine.

 * dom0.xml specifies the control features/primitives, environmental factors and QoS objectives that the users wish to control/manage. It should be placed with the main framework as an adaptation engine.

 * domU.properties specifies the port/ip address of the adaptation engine. It should be placed with the adaptable software.

 * domU.xml specifies the various sensors that are placed with the adaptable software. It should be placed with the main framework as an adaptation engine. It should be placed with the adaptable software. In the future, we will refactor them to be more flexible similar to the installation of sensors.

 * feature_model.xml specifies the control features and their dependency. This will be used by our FEMOSAA framework to perform feature guided multi-objective optimisation. We currently only support XML, other formats, e.g., CNF, will be implemented shortly.

We are also in the process to add more exampled scenarios for using SSASE framework. We will update here once they have been completed.