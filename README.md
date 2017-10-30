SSASE (including SAM, RCA, FEMOSAA and MOACO)
==========

This is the main repository for Search-based Self-Adaptive Software Engine (SSASE), an adaptation engine framework that exploits the advances of search-based software engineering techniques to tackle modelings, architecting and decision making problems for self-adaptive software systems (SAS), especially at their runtime.

Currently it encapsulates the following sub-frameworks/components, some of which are alternative:

- - - -

## 1. SAM for QoS/performance modeling in SAS
---------------


### SAM: Self-Adaptive Modeling

This is a framework that enables self-adaptive feature selection and selection of learning algorithms to model the correlation of control features, environments, interference to Quality of Service (QoS) attributes (e.g., response time). More details can be found in the following publications:

 > * T. Chen and R. Bahsoon. Self-Adaptive and Online QoS Modeling for Cloud-Based Software Services, IEEE Transactions on Software Engineering, vol. 43, no. 5, 2017.

 > * T. Chen, R. Bahsoon and X. Yao. Online QoS Modeling in the Cloud: A Hybrid and Adaptive Multi-Learners Approach. The 7th IEEE/ACM International Conference on Utility and Cloud Computing (UCC2014), London, UK. 2014.

 > * T. Chen and R. Bahsoon. Self-Adaptive and Sensitivity-Aware QoS Modelling for the Cloud. The 8th International Symposium on Software Engineering for Adaptive and Self-Managing Systems, SEAMS in conjunction with the 35th International Conference on Software Engineering (ICSE), San Francisco, CA, 2013.



Source code directory:
  * [src/main/java/org/ssase/model/](https://github.com/taochen/ssase/tree/master/src/main/java/org/ssase/model)

- - - -

## 2. RCA for architecting SAS
---------------


### RCA : Region Controlled Architecture

This is a symbiotic component that intelligently partitions the architecture of self-adaptive software with respect to objective-dependency, which is determined by the inputs of QoS model.  More details can be found in the following publications:

 > * T. Chen and R. Bahsoon. Symbiotic and Sensitivity-Aware Architecture for Globally-Optimal Benefit in Self-Adaptive Cloud. The 9th International Symposium on Software Engineering for Adaptive and Self-Managing Systems, SEAMS in conjunction with the 36th International Conference on Software Engineering (ICSE), India, 2014.

 > * T. Chen, R. Bahsoon and G. Theodoropoulos. A Decentralized Architecture for Dynamic QoS Optimization in Cloud-based DDDAS. 2013 International Conference on Computational Science, Procedia of Computer Science, Elsevier Science, 2013.

Source code directory:
  * [src/main/java/org/ssase/region/](https://github.com/taochen/ssase/tree/master/src/main/java/org/ssase/region)

- - - -

## 3. DLDA for determining when to adapt the SAS
---------------


### DLDA: Debt Learning Driven Adaptation 

Debt Learning Driven Adaptation (DLDA) is an automated framework that determines when and whether to adapt the SAS at runtime. DLDA leverages the temporal adaptation debt, a temporal notion derived from the technical debt metaphor in so ware engineering, to quantify the time-varying real money that the SAS carries in relation to its performance. We designed a temporal net debt driven labeling to label and correlate whether it is economically healthier to adapt the SAS (or not) in a circumstance, a er which an online learning classifier learns the correlation through the labeled samples, and then predicts whether to adapt under the unforeseen circumstances.

To ensure flexibility, we designed DLDA as an independent framework that is compatible with different online learning classifiers (e.g., MLP, SVM) and planners (e.g., FEMOSAA, PLATO and FUSION) for adaptation, in which DLDA also learns the effectiveness of a planner on net debt under different circumstances.  us, the planning serves as a black box and DLDA can easily work with different planners.

Source code directory:
   * [src/main/java/org/ssase/debt/](https://github.com/taochen/ssase/tree/master/src/main/java/org/ssase/debt)

Experiment results:
   * [experiments-data/dlda/](https://github.com/taochen/ssase/tree/master/experiments-data/dlda)

- - - -

## 4. FEMOSAA and MOACO-CD for decision making and optimization in SAS
---------------


### FEMOSAA: Feature Guided and Knee Driven Multi-Objective Optimization for Self-Adaptive Software at Runtime 

This is a novel framework that automatically synergizes the feature model and Multi-Objective Evolutionary Algorithm (MOEA) to optimize SAS’s conflicting QoS objectives at runtime. At design time, FEMOSAA automatically transposes the design of SAS, which is expressed as a feature model, to the chromosome representation and the reproduction operators (mutation and crossover) in MOEA. At runtime, the feature model serves as the domain knowledge to guide the search, providing a larger chance to find better solutions. FEMOSAA contains a new method to search for the knee solutions, which can achieve balanced trade-off. It does not cater for QoS interference, however. This work is currently being submitted for publication.

FEMOSAA supports any MOEAs, currently, it is implemented and integrated with MOEA/D-STM, NSGA-II and IBEA. Other MOEAs can be easily work with FEMOSAA. The source code of studied MOEAs, our feature dependency aware mutation/crossover operators and knee selection method can be found at [here](https://github.com/JerryI00/Software-Adaptive-System). (Note that to build and use FEMOSAA, users are advised to download and build the source code of MOEAs with dependency aware operators and knee selection first.)

Source code directory:
   * [src/main/java/org/ssase/objective/optimization/femosaa/](https://github.com/taochen/ssase/tree/master/src/main/java/org/ssase/objective/optimization/femosaa)

Experiment results:
   * [experiments-data/femosaa/results/](https://github.com/taochen/ssase/tree/master/experiments-data/femosaa/results)

### MOACO-CD: Self-Adaptive and Interference-Aware Multi-Objective Ant Colony Optimization for Decision Making in Self-Adaptive Software 

This is a component that exploits multi-objective ant colony algorithm to optimise adaptation decisions for self-adaptive software system at runtime. It particularly considers QoS interference caused by multi-tenants and virtualized environment, e.g. cloud computing. The approach leverage nash dominance, a popular economic principle, to find well-compromised/knee trade-off decisions. More details can be found in the following publications:

  > * T. Chen and R. Bahsoon. Self-Adaptive Trade-off Decision Making for Autoscaling Cloud-Based Services, IEEE Transactions on Services Computing, vol. 10, no. 4, 2017.

Source code directory:
   * [src/main/java/org/ssase/objective/optimization/moaco/](https://github.com/taochen/ssase/tree/master/src/main/java/org/ssase/objective/optimization/moaco)

- - - -

For all experiments in the published papers, we have used the following benchmarks and workload trace:

 * RUBiS - we have extended the original RUBiS by installing various sensors, source code can be found at [adaptable-software/rubis/](https://github.com/taochen/ssase/tree/master/adaptable-software/rubis)


 * FIFA98 workload trace - we have extracted and compressed the trace to meet our demand, the files and parsing scripts can be found at http://ita.ee.lbl.gov/html/contrib/WorldCup.html and [fifa98/](https://github.com/taochen/ssase/tree/master/fifa98) respectively.


Note that all the sub-framework/components are controlled centrally in the [ControlBus.java](https://github.com/taochen/ssase/blob/master/src/main/java/org/ssase/ControlBus.java) class. One can switch on/off/replace the alternative via changing the configurations in [dom0.properties](https://github.com/taochen/ssase/blob/master/src/main/resources/dom0.properties).

The SSASE repository contains a pom.xml for Maven build, but the Ant build file has not yet been completed. Potential users are advised to use Maven for building the project.

To use this framework, the only necessary configurations are the files under [src/main/resources/](https://github.com/taochen/ssase/tree/master/src/main/resources). Those files are explained as below:

 * _dom0.properties_ specifies the port/ip address of the adaptable software. It should be placed with the main framework as an adaptation engine.

 * _dom0.xml_ specifies the control features/primitives, environmental factors and QoS objectives that the users wish to control/manage. It should be placed with the main framework as an adaptation engine.

 * _domU.properties_ specifies the port/ip address of the adaptation engine. It should be placed with the adaptable software.

 * _domU.xml_ specifies the various sensors that are placed with the adaptable software. It should be placed with the main framework as an adaptation engine. It should be placed with the adaptable software. In the future, we will refactor them to be more flexible similar to the installation of sensors.

 * _feature_model.xml_ specifies the control features and their dependency. This will be used by our FEMOSAA framework to perform feature guided multi-objective optimization. We currently only support XML representation of the feature model, other formats, e.g., CNF, will be implemented shortly.

We are also in the process to add more exampled scenarios for using SSASE framework. We will update here once they have been completed.

- - - -

SSASE, including SAM, RCA, DLDA, FEMOSAA and MOACO, can be setup and run via the following steps:

1. Download/fork/clone the repository to your local codebase.
2. Implement the sensors for the QoS attributes, control features and environmental factors in your domain of self-adaptive software by extending the [Sensor.java](https://github.com/taochen/ssase/blob/master/src/main/java/org/ssase/sensor/Sensor.java) interface. We have already provided implementation for the most commonly considered dimension in [src/main/java/org/ssase/sensor](https://github.com/taochen/ssase/tree/master/src/main/java/org/ssase/sensor).
3. Deploy and plug the sensors in the adaptable software, also, triggering their initialisation and the monitoring, see for example, [StimulusListener.java](https://github.com/taochen/ssase/blob/master/adaptable-software/rubis/src/main/java/edu/rice/rubis/servlets/StimulusListener.java) and [ContextListener.java](https://github.com/taochen/ssase/blob/master/adaptable-software/rubis/src/main/java/edu/rice/rubis/servlets/ContextListener.java)
4. Implement the necessary actuators by extending on the [Actuator.java](https://github.com/taochen/ssase/blob/master/src/main/java/org/ssase/actuator/Actuator.java). Again, some popular ones have been provided in [src/main/java/org/ssase/actuator](https://github.com/taochen/ssase/tree/master/src/main/java/org/ssase/actuator). Note that some actuations can only be triggered within the adaptable software, in those cases, trigger the [ActuationReceiver.java](https://github.com/taochen/ssase/blob/master/src/main/java/org/ssase/actuator/ActuationReceiver.java#L42) where appropriate.
5. Properly configure the files mentioned above.
6. Build and deploy SSASE (run with the main function in [Ssascaling.java](https://github.com/taochen/ssase/blob/master/src/main/java/org/ssase/util/Ssascaling.java)) along with the adaptable software. Note that we currently do not provide sub-build for each sub-framework explicitly. To use only one sub-framework (e.g., FEMOSAA), one can change the configurations in [dom0.properties](https://github.com/taochen/ssase/blob/master/src/main/resources/dom0.properties).