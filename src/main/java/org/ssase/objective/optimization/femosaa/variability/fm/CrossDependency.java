package org.ssase.objective.optimization.femosaa.variability.fm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossDependency implements Dependency {

	
	protected static final Logger logger = LoggerFactory
	.getLogger(CrossDependency.class);

	protected Branch main;
	protected Branch dependent;

	protected String type;
	protected String operator;
	protected String translation;
	
	protected double mainMin = -1;
	protected double mainMax = -1;
	
	protected double dependentMin = -1;
	protected double dependentMax = -1;
	
	// This can occur for required, excluded and range with no mainMax and mainMin.
	// Used when leaf move up
	protected int mainAltValue = -1;
	
	// This can occur for required and excluded.
	// Used when leaf move up
	protected int dependentAltValue = -1;
	
	// This is only used when this dependency is a result of merge.
	// And this only occur for cross dependency.
	protected Integer[][] mergeResult;
	protected List<Dependency> mergedFrom;
	
	// This is the other branches that share OR group.
	private List<Branch> ORGroup = new ArrayList<Branch>();
	
	public CrossDependency(Branch main, Branch dependent, String type) {
		super();
		this.main = main;
		this.dependent = dependent;
		this.type = type;
	}

	public Dependency copy(Branch main, Branch dependent){
		CrossDependency copy = new CrossDependency(main, dependent, type);
		copy.translation = translation;
		copy.operator = operator;
		copy.mainMin = mainMin;
		copy.mainMax = mainMax;
		copy.dependentMin = dependentMin;
		copy.dependentMax = dependentMax;
		copy.mainAltValue = mainAltValue;
		copy.dependentAltValue = dependentAltValue;
		copy.mergeResult = mergeResult;
		copy.mergedFrom = mergedFrom;
		copy.ORGroup = ORGroup;
		return copy;
	}
	
	public String getType(){
		return type;
	}
	
	public void setOperator(String operator){
		this.operator = operator;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public Map<Branch, Integer[][]> getRangeBasedonMainVariableInOrGroup() {
		
		if (!"at-least-one-required".equals(type) && ORGroup.size() != 0) {
			throw new RuntimeException("Type " + type + " is wrong, as it has ORGroup but with wrong type");
		}
		
		if (!"at-least-one-required".equals(type))
			return null;
		
		if(ORGroup == null || ORGroup.size() == 0) {
			return null;
		}

		Map<Branch, Integer[][]> map = new HashMap<Branch, Integer[][]>();
	
		if ("at-least-one-required".equals(type)){
		
			List<Integer> list = new ArrayList<Integer>();
			List<Integer> when_zero = new ArrayList<Integer>();
			
			 if ("range".equals(operator)) {
				
				 for (int j = 0; j < dependent.getRange().length; j++) {
						if(dependentMin <= translate(dependent.getRange()[j]) && 
								dependentMax >= translate(dependent.getRange()[j])) {
						
						} else {
							when_zero.add(j);
						}
		
						
						list.add(j);
					}
			 } else {
				 
				 if(dependentAltValue == -1) {
					 when_zero.add(0);
				 } else {
					 for (int j = 0; j < dependent.getRange().length; j++) {

						 if(j != dependentAltValue) {
						  when_zero.add(j);
						 }

						}
				 }
				 
				 for (int j = 0; j < dependent.getRange().length; j++) {

						list.add(j);

					}
				 
					
					
			 }
			
			 for (Branch main : ORGroup) {

					Integer[][] result = new Integer[main.getRange().length][];

					for (int i = 0; i < main.getRange().length; i++) {
						result[i] = i == 0 ? when_zero.toArray(new Integer[when_zero.size()]) : list.toArray(new Integer[list.size()]);
					}

					map.put(main, result);
				}
		
		
		}
//		} else if ("range".equals(operator)){
//		
//			List<Integer> list = new ArrayList<Integer>();
//
//			for (int j = 0; j < dependent.getRange().length; j++) {
//				if(dependentMin <= translate(dependent.getRange()[j]) && 
//						dependentMax >= translate(dependent.getRange()[j])) {
//				list.add(j);
//				}
//
//			}
//			
//			for (Branch main : ORGroup) {
//
//				Integer[][] result = new Integer[main.getRange().length][];
//
//				for (int i = 0; i < main.getRange().length; i++) {
//					result[i] = i == 0 ? new Integer[0] : list.toArray(new Integer[list.size()]);
//				}
//
//				map.put(main, result);
//			}
//		
//		}
//		} else 	if ("excluded".equals(operator)) {
//			
//			
//			List<Integer> list = new ArrayList<Integer>();
//
//			for (int j = 0; j < dependent.getRange().length; j++) {
//
//				list.add(j);
//
//			}
//			
//			for (Branch main : ORGroup) {
//				Integer[][] result = new Integer[main.getRange().length][];
//
//				for (int i = 0; i < main.getRange().length; i++) {
//					result[i] = i == 0 ?  list.toArray(new Integer[list.size()]) : new Integer[0];
//				}
//
//				map.put(main, result);
//			}
//		}
		
		return map;
	}
	
//	public boolean isRequiredRelation(){
//		return "required".equals(operator) || "excluded".equals(operator) || ("range".equals(operator) && mainMin == -1 && mainMax == -1);
//	}

	/**
	 * double[the index of main variable][the coressponding array of value for dependent variable]
	 * @param main
	 * @param dependent
	 * @return
	 */
	public Integer[][] getRangeBasedonMainVariable() {
		if(mergeResult != null) {
			return mergeResult;
		}
		
		
		Integer[][] result = null;
		
//		if(main.getRange() == null) {
//			result = new Integer[main.getRange().length][];
//		}
		
		result = new Integer[main.getRange().length][];
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < main.getRange().length; i++) {
			list.clear();
			for (int j = 0; j < dependent.getRange().length; j++) {
				if ("required".equals(type)) {
					if ("less_or_equal".equals(operator)
							&& translate(dependent.getRange()[j]) <= main
									.getRange()[i]) {
						list.add(j);
					} else if ("less".equals(operator)
							&& translate(dependent.getRange()[j]) < main
									.getRange()[i]) {
						list.add(j);
					} else if ("greater_or_equal".equals(operator)
							&& translate(dependent.getRange()[j]) >= main
									.getRange()[i]) {
						list.add(j);
					} else if ("greater".equals(operator)
							&& translate(dependent.getRange()[j]) > main
									.getRange()[i]) {
						list.add(j);
					} else if ("equal".equals(operator)
							&& translate(dependent.getRange()[j]) == main
									.getRange()[i]) {
						list.add(j);
					} else if ("range".equals(operator)) {

						if (mainMin < 0 && mainMax < 0 && dependentMin > 0 && dependentMax > 0) {
							// If this case, the dependent values fall in to the
							// range only
							// when the main variable is selected. 0 means
							// deselected.

							if (mainAltValue != -1) {
								if (i == mainAltValue) {
									if (dependentMin <= dependent.getRange()[j]
											&& dependentMax >= dependent
													.getRange()[j]) {
										list.add(j);
									}
								} else {
									list.add(j);
								}
							} else {

								if (i != 0) {
									if (dependentMin <= dependent.getRange()[j]
											&& dependentMax >= dependent
													.getRange()[j]) {
										list.add(j);
									}
								} else {
									list.add(j);
								}
							}

						} else if (mainMin > 0 && mainMax > 0 && dependentMin < 0 && dependentMax < 0) {
							
							if (dependentAltValue != -1) {
								if (mainMin <= main.getRange()[i]
																&& mainMax >= main
																		.getRange()[i]) {
								if (j == dependentAltValue) {
									
										list.add(j);
									
								} 
								}else {
									list.add(j);
								}
							} else {
								if (mainMin <= main.getRange()[i]
																&& mainMax >= main
																		.getRange()[i]) {
								if (j != 0) {
								
										list.add(j);
									
								} 
							}else {
								list.add(j);
							}
							}

							
						} else {
							// If this case, the dependent values fall in to the
							// range only
							// when the main variable is with the given range. 0
							// means deselected.
							if (mainMax <= main.getRange()[i]
									&& mainMin >= main.getRange()[i]) {
								if (dependentMin <= dependent.getRange()[j]
										&& dependentMax >= dependent.getRange()[j]) {
									list.add(j);
								}
							} else {
								list.add(j);
							}
						}
					} else if (operator == null) {
						// categorical
						if (mainAltValue != -1 && dependentAltValue == -1) {
							if (i == mainAltValue) {
								list.add(j);
							} else {
								if (!list.contains(0)) {
									list.add(0);
								}
							}
						} else if (mainAltValue == -1
								&& dependentAltValue != -1) {
							if (i != 0) {
								if (j == dependentAltValue) {
								   list.add(j);
								}
							} else {
								if (j != dependentAltValue) {
									list.add(j);
								}
							}

						} else if (mainAltValue != -1
								&& dependentAltValue != -1) {
							if (i == mainAltValue) {
								if (j == dependentAltValue) {
								   list.add(j);
								}
							} else {
								if (j != dependentAltValue) {
									list.add(j);
								}
							}

						} else {
							if (i != 0) {
								list.add(j);
							} else {
								if (!list.contains(0)) {
									list.add(0);
								}
							}
						}
					}
				} else if ("excluded".equals(type)) {
					if ("less_or_equal".equals(operator)
							&& translate(dependent.getRange()[j]) > main
									.getRange()[i]) {
						list.add(j);
					} else if ("less".equals(operator)
							&& translate(dependent.getRange()[j]) >= main
									.getRange()[i]) {
						list.add(j);
					} else if ("greater_or_equal".equals(operator)
							&& translate(dependent.getRange()[j]) < main
									.getRange()[i]) {
						list.add(j);
					} else if ("greater".equals(operator)
							&& translate(dependent.getRange()[j]) <= main
									.getRange()[i]) {
						list.add(j);
					} else if ("equal".equals(operator)
							&& translate(dependent.getRange()[j]) != main
									.getRange()[i]) {
						list.add(j);
					} else if ("range".equals(operator)) {

						if (mainMin < 0 && mainMax < 0 && dependentMin > 0 && dependentMax > 0) {
							// If this case, the dependent values fall in to the
							// range only
							// when the main variable is selected. 0 means
							// deselected.

							if (mainAltValue != -1) {
								if (i == mainAltValue) {
									if (dependentMin > dependent.getRange()[j]
											&& dependentMax < dependent
													.getRange()[j]) {
										list.add(j);
									}
								} else {
									list.add(j);
								}
							} else {

								if (i != 0) {
									if (dependentMin > dependent.getRange()[j]
											&& dependentMax < dependent
													.getRange()[j]) {
										list.add(j);
									}
								} else {
									list.add(j);
								}
							}

						} else if (mainMin > 0 && mainMax > 0 && dependentMin < 0 && dependentMax < 0) {
							
							if (dependentAltValue != -1) {
								if (mainMin <= main.getRange()[i]
																&& mainMax >= main
																		.getRange()[i]) {
								if (j != dependentAltValue) {
								
										list.add(j);
									
								} 
							  } else {
									list.add(j);
								}
							} else {
								if (mainMin <= main.getRange()[i]
																&& mainMax >= main
																		.getRange()[i]) {
								if (j == 0) {
									
										list.add(j);
									
								}
							} else {
								list.add(j);
							}
							}

							
						} else {
							// If this case, the dependent values fall in to the
							// range only
							// when the main variable is with the given range. 0
							// means deselected.
							if (mainMax <= main.getRange()[i]
									&& mainMin >= main.getRange()[i]) {
								if (dependentMin > dependent.getRange()[j]
										&& dependentMax < dependent.getRange()[j]) {
									list.add(j);
								}
							} else {
								list.add(j);
							}
						}
					} else if (operator == null) {
						// categorical
						if(mainAltValue != -1 && dependentAltValue == -1) {
							if(i != mainAltValue) {
								list.add(j);
							} else {
								if(!list.contains(0)) {
									  list.add(0);
									}
							}
						} else if(mainAltValue == -1 && dependentAltValue != -1) {
							    if(i == 0) {
							    	 list.add(j);
								} else {
									if(j != dependentAltValue) {
									  list.add(j);
									}
								}
							
						} else if(mainAltValue != -1 && dependentAltValue != -1) {
						    if(i != mainAltValue) {
						    	 list.add(j);
							} else {
								if(j != dependentAltValue) {
								  list.add(j);
								} 
							}
						
					    } else {
							if(i == 0) {
								list.add(j);
							} else {
								if(!list.contains(0)) {
									  list.add(0);
									}
							}
						}
					}
				}
				
				
			}
			
			if(list.size() == 0) {
				throw new RuntimeException("the " + i + " index of main " + main.getName() + " has no valid values");
			}
			
			result[i] = list.toArray(new Integer[list.size()]);
		}
		
	
		
		return result;
	}
	

	@Override
	public Branch getMain() {
		return main;
	}

	@Override
	public Branch getDependent() {
		return dependent;
	}
	
	public void setMainRange(double min, double max) {
		mainMax = max;
		mainMin = min;
	}
	
	public void setDependentRange(double min, double max) {
		dependentMax = max;
		dependentMin = min;
	}
	
	public void setTranslation(String translation) {
		this.translation = translation;
	}
	
	public void setMainIfNotPresent(Branch main) {
		if(this.main == null) {
			this.main = main;
		}
	}
	
	public void replaceMain(Branch main) {
		this.main = main;
	}
	
	public void replaceDependent(Branch dependent) {
		this.dependent = dependent;
		dependent.addCrossDependency(this);
	}
	
	private double translate(double v){
		if(translation != null) {
			String op = translation.split(" ")[0];
			double number = Double.parseDouble(translation.split(" ")[1]);
			
			if("plus".equals(op)) {
				return v+number;
			} else if("minus".equals(op)) {
				return v-number;
			} else if("time".equals(op)) {
				return v*number;
			} else if("divide".equals(op)) {
				return v/number;
			}
		}
		return v;
	}
	
	public boolean isCanMerge(InBranchDependency another) {
		return !"at-least-one-required".equals(type) &&
		!"at-least-one-exist".equals(another.type) &&
		main.equals(another.main) &&
		dependent.equals(another.dependent);
	}
	
	
	public boolean isCanMerge(CrossDependency another) {
		return !"at-least-one-required".equals(type) &&
		!"at-least-one-required".equals(another.getType()) &&
		main.equals(another.getMain()) &&
		dependent.equals(another.getDependent());
	}
	
	public Dependency merge(CrossDependency another) {
		return internalMerge(another);
	}
	
	public Dependency merge(InBranchDependency another) {
		return internalMerge(another);
	}
	
	
	private Dependency internalMerge(Dependency another) {
		Integer[][] a = this.getRangeBasedonMainVariable();
		Integer[][] b = another.getRangeBasedonMainVariable();
		
		if(a.length != b.length) {
			throw new RuntimeException("Merge should have the same length!");
		}
		CrossDependency d = new CrossDependency(main, dependent, type /*we assume the type should be the same*/);
		
		
		d.mergeResult = new Integer[a.length][];
		for( int i = 0; i < d.mergeResult.length; i++) {
			d.mergeResult[i] = getIntersection(a[i], b[i]);
		}
		
		d.mergedFrom = new ArrayList<Dependency>();
		d.mergedFrom.add(this);
		d.mergedFrom.add(another);
		
		if(mergedFrom != null) {
			d.mergedFrom.addAll(mergedFrom);
		}
		
		if(another instanceof CrossDependency) {
			if(((CrossDependency)another).mergedFrom != null) {
				d.mergedFrom.addAll(((CrossDependency)another).mergedFrom);
			}
		} else {
			if(((InBranchDependency)another).mergedFrom != null) {
				d.mergedFrom.addAll(((InBranchDependency)another).mergedFrom);
			}
		}
		
		
		
		return d;
	}
	
	private Integer[] getIntersection(Integer[] a, Integer[] b) {
		
		if(a == null) return b;
		if(b == null) return a;
		
		Set<Integer> set = new HashSet<Integer>();
		List<Integer> inter = new ArrayList<Integer>();
		for(Integer i : a) {
			set.add(i);
		}
		
		for(Integer i : b) {
			if(set.contains(i)) {
				inter.add(i);
			}
		}
		
		// Might be this is not necessary.
		Collections.sort(inter);
		
		return inter.toArray(new Integer[inter.size()]);
	}
	
	
	public void addBranchInGroup(Branch branch) {
		if(ORGroup == null) {
			ORGroup = new ArrayList<Branch>();
		}
		
		ORGroup.add(branch);
	}
	
	public void setMainAltValue(int mainAltValue) {
		this.mainAltValue = mainAltValue;
	}
	
	public void setDependentAltValue(int dependentAltValue) {
		this.dependentAltValue = dependentAltValue;
	}

	@Override
	public void debug() {
		
		if ("at-least-one-required".equals(type) ){
			logger.debug("Dependent: " + dependent.getName());
			logger.debug("Type: " + type);
			logger.debug("Operator: " + operator);
			logger.debug("Translation: " + translation);
			logger.debug("mainAltValue: " + mainAltValue);
			logger.debug("dependentAltValue: " + dependentAltValue);
			logger.debug("mainMin: " + mainMin);
			logger.debug("mainMax: " + mainMax);
			logger.debug("dependentMin: " + dependentMin);
			logger.debug("dependentMax: " + dependentMax);
			logger.debug("-----------OR group main member-----------");
			for(Branch b : ORGroup) {
				logger.debug(b.getName());
			}
			return;
		}
		
		logger.debug("Main: " + main.getName());
		logger.debug("Dependent: " + dependent.getName());
		logger.debug("Type: " + type);
		logger.debug("Operator: " + operator);
		logger.debug("Translation: " + translation);
		logger.debug("mainAltValue: " + mainAltValue);
		logger.debug("dependentAltValue: " + dependentAltValue);
		logger.debug("mainMin: " + mainMin);
		logger.debug("mainMax: " + mainMax);
		logger.debug("dependentMin: " + dependentMin);
		logger.debug("dependentMax: " + dependentMax);
		
		if(mergedFrom != null) {
			logger.debug("-----------merged from-----------");
			for(Dependency d : mergedFrom) {
				
				if(d instanceof InBranchDependency) {
					logger.debug("Main: " + ((InBranchDependency)d).main.getName());
					logger.debug("Dependent: " + ((InBranchDependency)d).dependent.getName());
					logger.debug("Type: " + ((InBranchDependency)d).type);
					logger.debug("mainAltValue: " + ((InBranchDependency)d).mainAltValue);
					logger.debug("dependentAltValue: " + ((InBranchDependency)d).dependentAltValue);
					
					
					if("at-least-one-exist".equals(type)) {		
						logger.debug("-----------OR group dependency member-----------");
						for(Branch b : ORGroup) {
							logger.debug(b.getName());
						}
					}
					
				}
				
				if(d instanceof CrossDependency) {
				
				logger.debug("Main: " + ((CrossDependency)d).main.getName());
				logger.debug("Dependent: " + ((CrossDependency)d).dependent.getName());
				logger.debug("Type: " + ((CrossDependency)d).type);
				logger.debug("Operator: " + ((CrossDependency)d).operator);
				logger.debug("Translation: " + ((CrossDependency)d).translation);
				logger.debug("mainAltValue: " + ((CrossDependency)d).mainAltValue);
				logger.debug("dependentAltValue: " + ((CrossDependency)d).dependentAltValue);
				logger.debug("mainMin: " + ((CrossDependency)d).mainMin);
				logger.debug("mainMax: " + ((CrossDependency)d).mainMax);
				logger.debug("dependentMin: " + ((CrossDependency)d).dependentMin);
				logger.debug("dependentMax: " + ((CrossDependency)d).dependentMax);
				}
			}
		}
		
	}
}
