package org.ssase.objective.optimization.femosaa.variability.fm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.femosaa.core.SASVarEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.util.Tuple;

/**
 *
 * @author tao
 *
 */
public class InBranchDependency implements Dependency {

	protected static final Logger logger = LoggerFactory
	.getLogger(InBranchDependency.class);

	protected Branch main;
	protected Branch dependent;
	// This is the other branches that share OR group.
	protected List<Branch> ORGroup = new ArrayList<Branch>();
	// ALT, OR, OPT, Mandatory, mandatory or (when the OR group is used as main).
	//private String operator;
	protected String type;
	// This is the dependency for a parent whose relation with this is ALT.
	protected int mainAltValue = -1;
	
	protected int dependentAltValue = -1;
	
	
	protected Integer[][] mergeResult;
	protected List<Dependency> mergedFrom;
	
	
	
	public InBranchDependency(Branch main, Branch dependent, String type) {
		super();
		this.main = main;
		this.dependent = dependent;
		this.type = type;
	}
	
	


	/**
	 * Return a list of dependency accroding to the OR group, using the last feature
	 * as dependent variable.
	 * @return
	 */
	public Map<Branch, Integer[][]> getRangeBasedonMainVariableInOrGroup() {

		if (!"at-least-one-exist".equals(type))
			return null;

		Map<Branch, Integer[][]> map = new HashMap<Branch, Integer[][]>();
	
		
		
		List<Integer> list = new ArrayList<Integer>();
		List<Integer> none_zero_list = new ArrayList<Integer>();

		for (int j = 0; j < dependent.getRange().length; j++) {

			if (j == 0) {
				list.add(j);
			} else {
				list.add(j);
				none_zero_list.add(j);
			}

		
		
		for (Branch main : ORGroup) {

			if (dependent.equals(main))
				continue;

			Integer[][] result = new Integer[main.getRange().length][];

			for (int i = 0; i < main.getRange().length; i++) {
				result[i] = i == 0 ? none_zero_list.toArray(new Integer[none_zero_list
						.size()]) : list.toArray(new Integer[list.size()]);
			}

			map.put(main, result);
		}
		
		} 
//		else 	if ("mandatory_or".equals(operator)) {
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
//
//				if (dependent.equals(main))
//					continue;
//
//				Integer[][] result = new Integer[main.getRange().length][];
//
//				for (int i = 0; i < main.getRange().length; i++) {
//					result[i] = i == 0 ? new Integer[0] : list.toArray(new Integer[list.size()]);
//				}
//
//				map.put(main, result);
//			}
//		}
		
		return map;
	}
	
	@Override
	public Integer[][] getRangeBasedonMainVariable() {
		
		if(mergeResult != null) {
			return mergeResult;
		}
		
		if(!"required".equals(type)) return null;
		
		Integer[][] result = new Integer[main.getRange().length][];
		List<Integer> list = new ArrayList<Integer>();
		
			
			for (int i = 0; i < main.getRange().length; i++) {
				list.clear();
				for (int j = 0; j < dependent.getRange().length; j++) {
					
					if (mainAltValue == -1 && dependentAltValue == -1) {
						if (0 != i) {
							list.add(j);
						} else {
							if(!list.contains(0)) {
								  list.add(0);
								}
						}
					} else if (dependentAltValue == -1) {
						if (i == mainAltValue) {
							list.add(j);
						} else {
							if (!list.contains(0)) {
								list.add(0);
							}
						}
					} else if (mainAltValue == -1) {
						if (i != 0) {
							if (j == dependentAltValue) {
							   list.add(j);
							}
						} else {
							if (j != dependentAltValue) {
								list.add(j);
							}
						}
					}
				
					
					
//					if("alt".equals(operator)) {
//						if (altValue == i) {
//							list.add(j);
//						} else {
//							if(!list.contains(0)) {
//								  list.add(0);
//								}
//						}
//						
//					} else if("double_alt".equals(operator)) {
//						if (altValue == i) {
//							if(j != 0 && dependent.isCanSwitchOff())
//							   list.add(j);
//						} else {
//							if(!list.contains(0)) {
//								  list.add(0);
//								}
//						}
//						
//					
//						// same required
//					} else if("required".equals(operator)) {
//						if (0 != i) {
//							list.add(j);
//						} else {
//							if(!list.contains(0)) {
//								  list.add(0);
//								}
//						}
						
					
						// same required
					//} 
//					else if("double_required".equals(operator)) {
//						if (0 != i) {
//							if(j != 0 && dependent.isCanSwitchOff())
//							   list.add(j);
//						} else {
//							if(!list.contains(0)) {
//								  list.add(0);
//								}
//						}
//						
//					}
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
	
	public void setMainAltValue(int mainAltValue) {
		this.mainAltValue = mainAltValue;
	}
	
	public void setDependentAltValue(int dependentAltValue) {
		this.dependentAltValue = dependentAltValue;
	}
	
	public void addBranchInGroup(Branch branch) {
		if(ORGroup == null) {
			ORGroup = new ArrayList<Branch>();
		}
		
		ORGroup.add(branch);
	}

	public boolean isExistInGroup(Branch branch) {
		return ORGroup == null? false : ORGroup.contains(branch);
	}
	
	@Override
	public Dependency copy(Branch main, Branch dependent) {
		// TODO Auto-generated method stub
		return this;
	}
	
	public boolean isCanMerge(InBranchDependency another) {
		return !"at-least-one-exist".equals(type) &&
		!"at-least-one-exist".equals(another.type) &&
		main.equals(another.main) &&
		dependent.equals(another.dependent);
	}
	
	
	public boolean isCanMerge(CrossDependency another) {
		return !"at-least-one-exist".equals(type) &&
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
		InBranchDependency d = new InBranchDependency(main, dependent, type /*we assume the type should be the same*/);
		
		
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
	
	public boolean isMain(Branch b) {
		
		if(mergedFrom != null) {
			for(Dependency d : mergedFrom) {
				if(((CrossDependency)d).isMain(b)) {
					return true;
				}
			}
			
			return false;
		}
		
		
		return main.equals(b);
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
	public boolean isRequired(){
		return "required".equals(type);
	}
	
	
	@Override
	public void debug() {
		
		if("at-least-one-exist".equals(type)) {		
			logger.debug("Dependent: " + dependent.getName());
			logger.debug("Type: " + type);
			logger.debug("mainAltValue: " + mainAltValue);
			logger.debug("dependentAltValue: " + dependentAltValue);
			logger.debug("-----------OR group main member-----------");
			for(Branch b : ORGroup) {
				logger.debug(b.getName());
			}
			return;
		}
		
		logger.debug("Main: " + main.getName());
		logger.debug("Dependent: " + dependent.getName());
		logger.debug("Type: " + type);
		logger.debug("mainAltValue: " + mainAltValue);
		logger.debug("dependentAltValue: " + dependentAltValue);
		
		
		
		
		if(mergedFrom != null) {
			logger.debug("-----------merged from-----------");
			for(Dependency d : mergedFrom) {
				
				if(d instanceof InBranchDependency) {
					logger.debug("Main: " + ((InBranchDependency)d).main.getName());
					logger.debug("Dependent: " + ((InBranchDependency)d).dependent.getName());
					logger.debug("Type: " + ((InBranchDependency)d).type);
					logger.debug("mainAltValue: " + ((InBranchDependency)d).mainAltValue);
					logger.debug("dependentAltValue: " + ((InBranchDependency)d).dependentAltValue);
				}
				
					
					if(d instanceof CrossDependency) {
					
					logger.debug("Main: " + ((CrossDependency)d).main.getName());
					logger.debug("Dependent: " + ((CrossDependency)d).dependent.getName());
					logger.debug("Type: " + ((CrossDependency)d).type);
					logger.debug("Operator: " + ((CrossDependency)d).operator);
					logger.debug("Translation: " + ((CrossDependency)d).translation);
					logger.debug("mainAltValue: " + ((CrossDependency)d).mainAltValue);
					logger.debug("dependentAltValue: " + ((CrossDependency)d).dependentAltValue);
					for (double dd : ((CrossDependency)d).mainValues) {
						logger.debug("main value: " + dd);
					}
					for (double dd : ((CrossDependency)d).dependentValues) {
						logger.debug("dependent value: " + dd);
					}
					}
				
			
			}
		}
		
	}
}
