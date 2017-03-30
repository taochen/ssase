package org.ssase.objective.optimization.femosaa.variability.fm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.femosaa.core.SASVarEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This represent a Gene
 * @author tao
 *
 */
public class Branch {

	
	protected static final Logger logger = LoggerFactory
	.getLogger(Branch.class);
	
	private String name;
	private boolean isOptional = false;
//	private double min = -1;
//	private double gap = -1;
//	private double max = -1;
	private double[] range;
	private boolean isNumeric = false;
	
	private boolean hasSwitchOff = false;
	private boolean hasSwitchOn = false;
	// for cross-dependency
	private List<Dependency> crossMain = new ArrayList<Dependency>();
	private List<Dependency> inBranchMain = new ArrayList<Dependency>();
	// These are all children, including the optional ones
	private List<Branch> normalGroup = new ArrayList<Branch>();
	private List<Branch> ORGroup = new ArrayList<Branch>();
	private List<Branch> XORGroup = new ArrayList<Branch>();
	// This is normally the list of values.
	//private List<Double> leafs = new ArrayList<Double>();  
	private Branch parent;
	private boolean isRoot = false;
	
	
	public Branch () {
		name = "root";
		isRoot = true;
	}
	
	public void mergeIntersectableDependency(){
		List<Dependency> allMain = new ArrayList<Dependency>();
		allMain.addAll(crossMain);
		allMain.addAll(inBranchMain);
		Map<Dependency, List<Dependency>> map = new HashMap<Dependency, List<Dependency>>();
		

		
		for (Dependency d : allMain) {
			map.put(d, new ArrayList<Dependency>());
			for (Dependency subD : allMain) {
				
				if(d.equals(subD)) {
					continue;
				}
				
				if(d instanceof CrossDependency) {
					
					if(subD instanceof CrossDependency) {
						if(((CrossDependency)d).isCanMerge((CrossDependency)subD)) {
							map.get(d).add(subD);
						}
					} else {
						if(((CrossDependency)d).isCanMerge((InBranchDependency)subD)) {
							map.get(d).add(subD);
						}
					}
					
					
				} else {
					if(subD instanceof CrossDependency) {
						if(((InBranchDependency)d).isCanMerge((CrossDependency)subD)) {
							map.get(d).add(subD);
						}
					} else {
						if(((InBranchDependency)d).isCanMerge((InBranchDependency)subD)) {
							map.get(d).add(subD);
						}
					}
				}
				
				
			}
			
			for(Dependency subD : map.get(d)) {
				map.remove(subD);
			}
			
			if(map.get(d).size() == 0) {
				map.remove(d);
			}
		}

		List<Dependency> merged = new ArrayList<Dependency>();
		for(Map.Entry<Dependency, List<Dependency>> entry : map.entrySet()) {
			if(entry.getKey() instanceof CrossDependency) {
				CrossDependency r = (CrossDependency) entry.getKey();
				if(entry.getValue().size() != 0) {
					
					for (Dependency d : entry.getValue()) {
						if(d instanceof CrossDependency) {
							r = (CrossDependency) r.merge((CrossDependency) d);	
						} else {
							r = (CrossDependency) r.merge((InBranchDependency) d);	
						}
							
					}
					
				}
				merged.add(r);
			} else {
				InBranchDependency r = (InBranchDependency) entry.getKey();
				if(entry.getValue().size() != 0) {
					
					for (Dependency d : entry.getValue()) {
						if(d instanceof CrossDependency) {
							r = (InBranchDependency) r.merge((CrossDependency) d);	
						} else {
							r = (InBranchDependency) r.merge((InBranchDependency) d);	
						}
							
					}
					
				}
				merged.add(r);
			}
			
			
			
		}
	
		// Remove old
		for(Map.Entry<Dependency, List<Dependency>> entry : map.entrySet()) {
			crossMain.remove(entry.getKey());
			inBranchMain.remove(entry.getKey());
			for (Dependency d : entry.getValue()) {
				crossMain.remove(d);
				inBranchMain.remove(d);
			}
		}
		

		// Add new
		for (Dependency d : merged) {
			if(d instanceof CrossDependency) {
				crossMain.add(d);
			} else {
				inBranchMain.add(d);
			}
			
			
		}
		
	}
	
	public List<Map<Branch, Integer[][]>> getMainBranchesOfThisBranch(){
		
		
		List<Map<Branch, Integer[][]>> list = new ArrayList<Map<Branch, Integer[][]>>();
		
		Map<Branch, Integer[][]> intercetion = new LinkedHashMap<Branch, Integer[][]>();
		
		// First one is intercetion
		list.add(intercetion);
		
		
	
		for (Dependency d : crossMain) {	
			Map<Branch, Integer[][]> m = ((CrossDependency)d).getRangeBasedonMainVariableInOrGroup();
			if(m != null) {
				Map<Branch, Integer[][]> union = new LinkedHashMap<Branch, Integer[][]>();
				union.putAll(m);
				list.add(union);
			} else {		
				intercetion.put(d.getMain(), d.getRangeBasedonMainVariable());
			}
			
		}
//		if(name.equals("cacheMode")) {
//			System.currentTimeMillis();
//		}
// 		
		for (Dependency d : inBranchMain) {		
			Map<Branch, Integer[][]> m = ((InBranchDependency)d).getRangeBasedonMainVariableInOrGroup();
			if(m != null) {
				Map<Branch, Integer[][]> union = new LinkedHashMap<Branch, Integer[][]>();
				union.putAll(m);
				list.add(union);
			} else {		
				intercetion.put(d.getMain(), d.getRangeBasedonMainVariable());
			}
		}
		

		
		
		if(list.size() == 1 && intercetion.size() == 0) {
			return null;
		}
		
		return list;
	}
	
	public Branch(String name, String isOptional, String type) {
		super();
		this.name = name;
		this.isOptional = "true".equals(isOptional);
		this.isNumeric = "numeric".equals(type)? true : false;
	
		if(this.isOptional) {
			enableSwitchOff();
		}
	}
	
	public Branch(String name, String isOptional, String type, double min, double max, double gap, String fixedZero) {
		super();
		this.name = name;
		this.isOptional = "true".equals(isOptional);
		this.isNumeric = "numeric".equals(type)? true : false;
	
		range = getRange("true".equals(fixedZero), min, max, gap);

		
		if(this.isOptional) {
			enableSwitchOff();
		}
	}

	public void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}
	
    public void enableSwitchOff(){
    	hasSwitchOff = true;
//    	
//    	if(!isNumeric) return;
//    	
//    	if(range[0] == 0) {
//    		return;
//    	}
//    	
//    	double[] newRange = new double[range.length + 1];
//    	newRange[0] = 0;
//    	System.arraycopy(range, 0 , newRange, 1, range.length);
    	
    }
    
    public void enableSwitchOn(){
    	hasSwitchOn = true;
    }
	
	public boolean isOptional(){
		return isOptional;
	}
	
	public boolean isCanSwitchOff(){
		return isOptional || hasSwitchOff;
	}
	
	public boolean isNumeric(){
		return isNumeric;
	}
	
	public double[] getRange(){
		return range;
	}
	
	public String getName(){
		return name;
	}
	
	public void addXOR(Branch b) {
		XORGroup.add(b);
	}
	
	public void addOR(Branch b) {
		ORGroup.add(b);
	}
	
	public void addNormal(Branch b) {
		normalGroup.add(b);
	}
	
	public void setParent(Branch parent) {
		this.parent = parent;
	}
	
	public Branch getParent(){
		return parent;
	}
	
	public boolean isPossibleToBecomeGene(){
		return (XORGroup.size() > 1 || isNumeric || (hasSwitchOff && hasSwitchOn));
	}
	
	
//	public void validateRequiredAndSwitchOff(){
//		if(this.isCanSwitchOff()) return;
//		
//		
//		for (Dependency d : crossMain) {
//			if(((CrossDependency)d).isRequiredRelation()) {
//				throw new RuntimeException(name + " has cross-dependency that contains required operator, but itself cannot be switchoff!");
//			}
//		}
//		
//	}
	
	public void addCrossDependency(Dependency d) {
		crossMain.add(d);
	}
	
	public void addInBranchDependency(Dependency d) {
		inBranchMain.add(d);
	}
	
	public boolean isRoot(){
		return isRoot;
	}
	
	public void generateInBranchOptionalDependency(List<Branch> chromosome) {
		if (chromosome.contains(this) && isOptional) {

			List<Branch> all = new ArrayList<Branch>();
			List<Branch> allWithoutOR = new ArrayList<Branch>();
			getNextLayerChildrenGene(chromosome, all);
			getNextLayerChildrenGeneWithoutOR(chromosome, allWithoutOR);
			for (Branch b : all) {
				
				if(allWithoutOR.contains(b)) {
					Branch firstSwitchoffNoMandatory = b.getFirstSwitchOffNonMandatoryParent();
					if(b.isCanSwitchOff() && b.isMandatory() && this.equals(firstSwitchoffNoMandatory)) {
						  InBranchDependency d = new InBranchDependency(this, b, "required");
						  b.addInBranchDependency(d);
						  d = new InBranchDependency(b, this, "required");
						  addInBranchDependency(d);
					} else {
						  InBranchDependency d = new InBranchDependency(this, b, "required");
						   b.addInBranchDependency(d);
					}
				} else {
				   InBranchDependency d = new InBranchDependency(this, b, "required");
				   b.addInBranchDependency(d);
				}
				
				
				
			}

			
		}
		
		for(Branch b : normalGroup) {
			b.generateInBranchOptionalDependency(chromosome);
		}
		for(Branch b : ORGroup) {
			b.generateInBranchOptionalDependency(chromosome);
		}
		for(Branch b : XORGroup) {
			b.generateInBranchOptionalDependency(chromosome);
		}

	}
	
	private void getAllChildrenGene(List<Branch> chromosome, List<Branch> result) {
		for(Branch b : normalGroup) {
			if(chromosome.contains(b)) {
				result.add(b);
			}
			b.getAllChildrenGene(chromosome, result);
		}
		for(Branch b : ORGroup) {
			if(chromosome.contains(b)) {
				result.add(b);
			}
			b.getAllChildrenGene(chromosome, result);
		}
		for(Branch b : XORGroup) {
			if(chromosome.contains(b)) {
				result.add(b);
			}
			b.getAllChildrenGene(chromosome, result);
		}
	}
	
	
	private void getAllChildrenGeneWithoutOR(List<Branch> chromosome, List<Branch> result) {
		for(Branch b : normalGroup) {
			if(chromosome.contains(b)) {
				result.add(b);
			}
			b.getAllChildrenGeneWithoutOR(chromosome, result);
		}
		for(Branch b : XORGroup) {
			if(chromosome.contains(b)) {
				result.add(b);
			}
			b.getAllChildrenGeneWithoutOR(chromosome, result);
		}
		
	}
	
	private void getNextLayerChildrenGene(List<Branch> chromosome, List<Branch> result) {
		for(Branch b : normalGroup) {
			if(chromosome.contains(b)) {
				result.add(b);
			} else {
			   b.getNextLayerChildrenGene(chromosome, result);
			}
		}
		for(Branch b : ORGroup) {
			if(chromosome.contains(b)) {
				result.add(b);
			}
			 else {
				   b.getNextLayerChildrenGene(chromosome, result);
				}
		}
		for(Branch b : XORGroup) {
			if(chromosome.contains(b)) {
				result.add(b);
			}
			 else {
				   b.getNextLayerChildrenGene(chromosome, result);
				}
		}
	}
	

	
	
	private void getNextLayerChildrenGeneWithoutOR(List<Branch> chromosome, List<Branch> result) {
		for(Branch b : normalGroup) {
			if(chromosome.contains(b)) {
				result.add(b);
			} else {
			b.getNextLayerChildrenGeneWithoutOR(chromosome, result);
			}
		}
		for(Branch b : XORGroup) {
			if(chromosome.contains(b)) {
				result.add(b);
			} else {
			b.getNextLayerChildrenGeneWithoutOR(chromosome, result);
			}
		}
		
	}
	
	
	private Branch getFirstSwitchOffNonMandatoryParent(){
		Branch temp = parent;
		do {
			if(temp.isCanSwitchOff() && !temp.isMandatory()) {
				return temp;
			}
			
			temp = temp.parent;
			
		} while (temp != null && !temp.isRoot);
		
		return null;
	}
	
	private Branch getRoot(){
		Branch temp = parent;
		do {
			if(temp.isRoot) {
				return temp;
			}
			
			temp = temp.parent;
			
		} while (temp != null);
		
		return null;
	}
	

	
	public boolean isMandatory(){
		return parent != null && parent.normalGroup.contains(this) && !isOptional;
	}
	
	public void generateInBranchXORDependency(List<Branch> chromosome) {
		// If this is true, then the parent should be also in the chromosome.
	
		
		if (parent != null && parent.XORGroup.contains(this) && chromosome.contains(parent)) {

			
			if(chromosome.contains(this)) {
				
				 InBranchDependency d = new InBranchDependency(parent, this, "required");
					d.setMainAltValue(parent.isCanSwitchOff()?
							parent.XORGroup.indexOf(this) + 1 : 
								parent.XORGroup.indexOf(this));
				   addInBranchDependency(d);
				   
				   
				   d = new InBranchDependency(this, parent, "required");
					d.setDependentAltValue(parent.isCanSwitchOff()?
							parent.XORGroup.indexOf(this) + 1 : 
								parent.XORGroup.indexOf(this));
					parent.addInBranchDependency(d);
				   
				   
				   if(isCanSwitchOff()) {
					   List<Branch> all = new ArrayList<Branch>();
						List<Branch> allWithoutOR = new ArrayList<Branch>();
						getNextLayerChildrenGene(chromosome, all);
						getNextLayerChildrenGeneWithoutOR(chromosome, allWithoutOR);
						for (Branch b : all) {

							if (allWithoutOR.contains(b)) {
								Branch firstSwitchoffNoMandatory = b
										.getFirstSwitchOffNonMandatoryParent();
								if (b.isCanSwitchOff() && b.isMandatory()
										&& this.equals(firstSwitchoffNoMandatory)) {
									 d = new InBranchDependency(
											this, b, "required");
									b.addInBranchDependency(d);
									d = new InBranchDependency(b, this, "required");
									  addInBranchDependency(d);
								} else {
									 d = new InBranchDependency(
											 this, b, "required");
									b.addInBranchDependency(d);
								}
							} else {
								 d = new InBranchDependency(this,
										b, "required");
								b.addInBranchDependency(d);
							}

						}
				   }
				   
				   
			} else {

				List<Branch> all = new ArrayList<Branch>();
				List<Branch> allWithoutOR = new ArrayList<Branch>();
				getNextLayerChildrenGene(chromosome, all);
				getNextLayerChildrenGeneWithoutOR(chromosome, allWithoutOR);
				for (Branch b : all) {

					if (allWithoutOR.contains(b)) {
						Branch firstSwitchoffNoMandatory = b
								.getFirstSwitchOffNonMandatoryParent();
						if (b.isCanSwitchOff() && b.isMandatory()
								&& parent.equals(firstSwitchoffNoMandatory)) {
							InBranchDependency d = new InBranchDependency(
									parent, b, "required");
							d.setMainAltValue(parent.isCanSwitchOff()?
									parent.XORGroup.indexOf(this) + 1 : 
										parent.XORGroup.indexOf(this));
							b.addInBranchDependency(d);
							
							d = new InBranchDependency(b, parent, "required");
							d.setDependentAltValue(parent.isCanSwitchOff()?
									parent.XORGroup.indexOf(this) + 1 : 
										parent.XORGroup.indexOf(this));
							parent.addInBranchDependency(d);
							
						} else {
							InBranchDependency d = new InBranchDependency(
									parent, b, "required");
							d.setMainAltValue(parent.isCanSwitchOff()?
									parent.XORGroup.indexOf(this) + 1 : 
										parent.XORGroup.indexOf(this));
							b.addInBranchDependency(d);;
						}
					} else {
						InBranchDependency d = new InBranchDependency(
								parent, b, "required");
						d.setMainAltValue(parent.isCanSwitchOff()?
								parent.XORGroup.indexOf(this) + 1 : 
									parent.XORGroup.indexOf(this));
						b.addInBranchDependency(d);
					}

				}

			}

			
		}
		
		for(Branch b : normalGroup) {
			b.generateInBranchXORDependency(chromosome);
		}
		for(Branch b : ORGroup) {
			b.generateInBranchXORDependency(chromosome);
		}
		for(Branch b : XORGroup) {
			b.generateInBranchXORDependency(chromosome);
		}
	}
	
	public void generateInBranchMandatoryDependency(List<Branch> chromosome) {
		
		if (chromosome.contains(this) && isMandatory()  && isCanSwitchOff()) {

			List<Branch> all = new ArrayList<Branch>();
			
			
	        Branch firstSwitchoff = getFirstSwitchOffNonMandatoryParent();
	        
	    	Branch p = parent;
			boolean isHasGene = false;
			while(!firstSwitchoff.equals(p)) {
				
				if(chromosome.contains(p)) {
					isHasGene = true;
					break;
				}
				
				
				p = p.parent;
			}
	        
			// For the neighbours.
			if(firstSwitchoff != null && !chromosome.contains(firstSwitchoff) && !isHasGene) {
				firstSwitchoff.getNextLayerChildrenGene(chromosome, all);
				
				if(!all.contains(this)) {
					throw new RuntimeException("May be a bug, " + name + " is not in the first layer gene of " + firstSwitchoff.getName() + "?");
				}
				
				// Only single way
				for (Branch b : all) {
					
					if(b.equals(this)) {
						continue;
					}
					
					CrossDependency d = new CrossDependency(this, b, "required");
					b.addCrossDependency(d);
		
					
				}
				
			
				
			} else {
				throw new RuntimeException("May be a bug, " + name + " has no FirstSwitchOffNonMandatoryParent?");
			}
			
			// For itself
			all.clear();
			List<Branch> allWithoutOR = new ArrayList<Branch>();
			getNextLayerChildrenGene(chromosome, all);
			getNextLayerChildrenGeneWithoutOR(chromosome, allWithoutOR);
			
			for (Branch b : all) {
				
				if(allWithoutOR.contains(b)) {
					//Branch firstSwitchoffNoMandatory = b.getFirstSwitchOffNonMandatoryParent();
					
					p = b.parent;
					boolean isOnlyMan = true;
					while(!this.equals(p)) {
						
						if(!p.isMandatory()) {
							isOnlyMan = false;
							break;
						}
						
						
						p = p.parent;
					}
					
					if(b.isCanSwitchOff() && b.isMandatory() && isOnlyMan) {
						  InBranchDependency d = new InBranchDependency(this, b, "required");
						  b.addInBranchDependency(d);
						  d = new InBranchDependency(b, this, "required");
						  addInBranchDependency(d);
					} else {
						  InBranchDependency d = new InBranchDependency(this, b, "required");
						   b.addInBranchDependency(d);
					}
				} else {
				   InBranchDependency d = new InBranchDependency(this, b, "required");
				   b.addInBranchDependency(d);
				}
				
				
				
			}
		
		}
		
		for(Branch b : normalGroup) {
			b.generateInBranchMandatoryDependency(chromosome);
		}
		for(Branch b : ORGroup) {
			b.generateInBranchMandatoryDependency(chromosome);
		}
		for(Branch b : XORGroup) {
			b.generateInBranchMandatoryDependency(chromosome);
		}
	}
	
	private boolean isParentBelongsToORGroup(Branch branch) {
		
		
		Branch b = branch.parent;
		do {
			
			if(ORGroup.contains(b)) {
				return true;
			}
			b = b.parent;
		} while(b.equals(this));
		
		return false;
		
	}
	
	public void generateInBranchORDependency(List<Branch> chromosome) {
		
		// Do the at-least-one dependency
		if (parent != null && parent.ORGroup.contains(this)) {
			boolean isNeedORGroupConstraint = true;
		
			if(!parent.isOptional && parent.isCanSwitchOff()) {
				isNeedORGroupConstraint = false;
				if(chromosome.contains(parent)) {
					isNeedORGroupConstraint = true;
				}
				// Conditionally optional
					
					Branch firstSwitchoff = parent.getFirstSwitchOffNonMandatoryParent();
					
					// if there exist at least one ancestor is gene.
					// in case no firstSwitchoff.
//					if (firstSwitchoff == null) {
//						firstSwitchoff = getRoot();
//					}
					
				
					if (firstSwitchoff != null) {
					
					  	Branch p = parent;
						boolean isHasGene = false;
						while(!firstSwitchoff.equals(p)) {
							
							if(chromosome.contains(p)) {
								isHasGene = true;
								break;
							}
							
							
							p = p.parent;
						} 
				        
						
						if(chromosome.contains(firstSwitchoff) || isHasGene) {
							isNeedORGroupConstraint = true;
						} else {
							List<Branch> all = new ArrayList<Branch>();
							firstSwitchoff.getNextLayerChildrenGene(chromosome, all);
							
							for (Branch b : all) {
								
								if(b.isMandatory() && chromosome.contains(b) 
										&& firstSwitchoff.equals( b.getFirstSwitchOffNonMandatoryParent())) {
									isNeedORGroupConstraint = true;
									break;
								}
							}
						}
						
						
					} else {
						isNeedORGroupConstraint = true;
					}
					
				
				
			}
			
			if (parent.isOptional) {
				isNeedORGroupConstraint = false;
				if(chromosome.contains(parent)) {
					isNeedORGroupConstraint = true;
				}
				
					List<Branch> all = new ArrayList<Branch>();
					parent.getNextLayerChildrenGene(chromosome, all);
					
					for (Branch b : all) {
						if(b.isMandatory() && chromosome.contains(b)
								&& parent.equals( b.getFirstSwitchOffNonMandatoryParent())) {
							isNeedORGroupConstraint = true;
							break;
						}
					}
				
			}
			
			System.out.print("isNeedORGroupConstraint: " + isNeedORGroupConstraint + "******\n");
			
			// Handle the optional to OR group member
			if(!isNeedORGroupConstraint) {
				
				if(parent.isOptional) {
					
					
					List<Branch> all = new ArrayList<Branch>();
					parent.getNextLayerChildrenGene(chromosome, all);
					
					for (Branch b : all) {
						
						if(!parent.isParentBelongsToORGroup(b) && (!b.isMandatory() || !parent.equals( b.getFirstSwitchOffNonMandatoryParent()))) {
							boolean already = false;
							for(Dependency d : b.crossMain) {
								CrossDependency cd = (CrossDependency)d;
								
								if(cd.getORGroup().size() != 0) {
									if(parent.isParentBelongsToORGroup(cd.getORGroup().get(0))) {
										already = true;
									}
								}
								
							}
							
							if(!already) {
								  InBranchDependency d = new InBranchDependency(null, b, "at-least-one-required");
								  for (Branch temp : parent.ORGroup) {
									  
									  if(chromosome.contains(temp)) {
										  d.addBranchInGroup(temp);
									  } else {
										  List<Branch> tempList = new ArrayList<Branch>();
										  temp.getNextLayerChildrenGene(chromosome, tempList);
										  
										  for (Branch nest : tempList) {
											  d.addBranchInGroup(nest);
										  }
									  }
									  
									  
								  }
								  b.addInBranchDependency(d);
							}
							
						}
						
					
					}
					
					
					
					
				} else if(parent.isMandatory() && parent.isCanSwitchOff()) {
					
					List<Branch> all = new ArrayList<Branch>();
					
					Branch firstSwitchoff = parent.getFirstSwitchOffNonMandatoryParent();
					
					firstSwitchoff.getNextLayerChildrenGene(chromosome, all);
					
					for (Branch b : all) {
						
						if(!parent.isParentBelongsToORGroup(b) &&  (!b.isMandatory() || !firstSwitchoff.equals( b.getFirstSwitchOffNonMandatoryParent()))) {
							boolean already = false;
							for(Dependency d : b.crossMain) {
								CrossDependency cd = (CrossDependency)d;
								
								if(cd.getORGroup().size() != 0) {
									if(parent.isParentBelongsToORGroup(cd.getORGroup().get(0))) {
										already = true;
									}
								}
								
							}
							
							if(!already) {
								  InBranchDependency d = new InBranchDependency(null, b, "at-least-one-required");
								  for (Branch temp : parent.ORGroup) {
									  
									  if(chromosome.contains(temp)) {
										  d.addBranchInGroup(temp);
									  } else {
										  List<Branch> tempList = new ArrayList<Branch>();
										  temp.getNextLayerChildrenGene(chromosome, tempList);
										  
										  for (Branch nest : tempList) {
											  d.addBranchInGroup(nest);
										  }
									  }
									  
									
									  
									  
								  }
								  
								  b.addInBranchDependency(d);
							}
							
						}
						
					
					}
					
				}
				
				return;
			}
       
			List<List<Branch>> total = new ArrayList<List<Branch>>();
			List<Branch> targetList = null;
			
	        for (Branch orBranch : parent.ORGroup) {	
	        	List<Branch> subAll = new ArrayList<Branch>();
	        	if(chromosome.contains(orBranch)) {
	        		subAll.add(orBranch);
	        	} else {
	        		orBranch.getNextLayerChildrenGene(chromosome, subAll);
	        	}
	        	
	        	
	        	if(this.equals(orBranch)) {
	        		targetList = subAll;
	        	} else {
	        		total.add(subAll);
	        	}
			
	        }
	        
	        for (Branch dependent : targetList) {
	        	InBranchDependency d = new InBranchDependency(parent,
						dependent, "at-least-one-exist");
	        	for (List<Branch> subList : total) {
	        		
	        		for (Branch main : subList) {
	        	     // Do not add at-least-one-exist if there is one exist between two genes,
	   	        	 // even if they are related to different root, as HR-3 would ensure the need of such.
	        		 boolean exist = false;
	   	        	 if(dependent.inBranchMain.size() != 0) {
	   	        		
	   	        		 for(Dependency dep : dependent.inBranchMain) {
	   	        			if(((InBranchDependency)dep).isExistInGroup(main)) {
	   	        				exist = true;
	   	        				break;
	   	        			}
	   	        		 }
	   	        		 
	   	        	 }
	   	        	 
	   	        	 
	   	        	 if(!exist) {
	   	        		d.addBranchInGroup(main); 
	   	        	 }
						
					}
	        		
	        	}
	        	
	        	// Add the same decedant in the target list
	        	 for (Branch subMain : targetList) {
	        		 if(!dependent.equals(subMain)) {
	        			 
	       	        	     // Do not add at-least-one-exist if there is one exist between two genes,
	       	   	        	 // even if they are related to different root, as HR-3 would ensure the need of such.
	       	        		 boolean exist = false;
	       	   	        	 if(dependent.inBranchMain.size() != 0) {
	       	   	        		
	       	   	        		 for(Dependency dep : dependent.inBranchMain) {
	       	   	        			if(((InBranchDependency)dep).isExistInGroup(subMain)) {
	       	   	        				exist = true;
	       	   	        				break;
	       	   	        			}
	       	   	        		 }
	       	   	        		 
	       	   	        	 }
	       	   	        	 
	       	   	        	 
	       	   	        	 if(!exist) {
	       	   	        	     d.addBranchInGroup(subMain);
	       	   	        	 }
	        			 
	        			
	        		 }
	        	 }
	        	 
	        	
	        	 
	        	dependent.addInBranchDependency(d);
	        }

		
			
		}
		
		
		// If this is a gene, then its children needs to depends on it.
		if(chromosome.contains(this) && parent != null && parent.ORGroup.contains(this)) {
			List<Branch> all = new ArrayList<Branch>();
			List<Branch> allWithoutOR = new ArrayList<Branch>();
			getNextLayerChildrenGene(chromosome, all);
			getNextLayerChildrenGeneWithoutOR(chromosome, allWithoutOR);
			for (Branch b : all) {
				
				if(allWithoutOR.contains(b)) {
					Branch firstSwitchoffNoMandatory = b.getFirstSwitchOffNonMandatoryParent();
					if(b.isCanSwitchOff() && b.isMandatory() && this.equals(firstSwitchoffNoMandatory)) {
						  InBranchDependency d = new InBranchDependency(this, b, "required");
						  b.addInBranchDependency(d);
						  d = new InBranchDependency(b, this, "required");
						  addInBranchDependency(d);
					} else {
						  InBranchDependency d = new InBranchDependency(this, b, "required");
						   b.addInBranchDependency(d);
					}
				} else {
				   InBranchDependency d = new InBranchDependency(this, b, "required");
				   b.addInBranchDependency(d);
				}
				
				
				
			}
			
		}
		
		for(Branch b : normalGroup) {
			b.generateInBranchORDependency(chromosome);
		}
		for(Branch b : ORGroup) {
			b.generateInBranchORDependency(chromosome);
		}
		for(Branch b : XORGroup) {
			b.generateInBranchORDependency(chromosome);
		}
		
	}
	
	
	/**
	 * For dependent, add cross dependency regardless if it is a gene.
	 * For main, add only if it is a gene.
	 * @param chromosome
	 */
	public void inheritCrossDepedencyToChildrenOrParent(List<Branch> chromosome){
	
//		if("gzip".equals(name)) {
//			System.out.print("");
//		}
		
		
		for(Dependency d : crossMain) {
			
			if(d.getMain().isLeaf() || d.getDependent().isLeaf()) {
			    // The parent of the main (which is leaf) would always be gene.
				if(d.getMain().isLeaf()){
					if(!chromosome.contains(d.getMain())) {
						((CrossDependency)d).setMainAltValue(d.getMain().getParent().isCanSwitchOff()?
								d.getMain().getParent().XORGroup.indexOf(d.getMain()) + 1 :
									d.getMain().getParent().XORGroup.indexOf(d.getMain()));
						
						if(!d.getMain().getParent().XORGroup.contains(d.getMain())) {
							throw new RuntimeException(d.getMain().getParent().getName() + " should have " + d.getMain().getName() + " in its XOR group!");
						}
						((CrossDependency)d).replaceMain(d.getMain().getParent());
					}
				}
				
				
				if(d.getDependent().isLeaf()){
					if(!chromosome.contains(d.getDependent())) {
						((CrossDependency)d).setDependentAltValue(d.getDependent().getParent().isCanSwitchOff()?
								d.getDependent().getParent().XORGroup.indexOf(d.getDependent()) + 1 : 
									d.getDependent().getParent().XORGroup.indexOf(d.getDependent()));
						
						if(!d.getDependent().getParent().XORGroup.contains(d.getDependent())) {
							throw new RuntimeException( d.getDependent().getParent().getName() + " should have " + d.getDependent().getName() + " in its XOR group!");
						}
						((CrossDependency)d).replaceDependent(d.getDependent().getParent());
					}
				}
				
			} else {
			
			if(chromosome.contains(d.getMain()) && !chromosome.contains(this)) {
				List<Dependency> list = new ArrayList<Dependency>();
				changeDependentToCrossDependency(d, chromosome, list);
		
			} else if(!chromosome.contains(d.getMain()) && !chromosome.contains(this)) {
				List<Dependency> list = new ArrayList<Dependency>();
				changeDependentToCrossDependency(d, chromosome, list);
				for (Dependency dep : list) {
					changeMainToCrossDependency(chromosome, dep);
					dep.getDependent().crossMain.remove(dep);
				}
				
			
			} else if(!chromosome.contains(d.getMain()) && chromosome.contains(this)) {
				changeMainToCrossDependency(chromosome, d);
				d.getDependent().crossMain.remove(d);
			}
			}
		}
	}
	
	
	private void changeDependentToCrossDependency(Dependency d, List<Branch> chromosome, List<Dependency> list) {
		for(Branch b : normalGroup) {
			if (chromosome.contains(b)) {
				Dependency dep = d.copy(d.getMain(), b);
				list.add(dep);
				b.addCrossDependency(dep);
			} else {
				b.changeDependentToCrossDependency(d, chromosome, list);
			}
		}
		for(Branch b : ORGroup) {
			if (chromosome.contains(b)) {
				Dependency dep = d.copy(d.getMain(), b);
				list.add(dep);
				b.addCrossDependency(dep);
			} else {
				b.changeDependentToCrossDependency(d, chromosome, list);
			}
		}
		for(Branch b : XORGroup) {
			if (chromosome.contains(b)) {
				Dependency dep = d.copy(d.getMain(), b);
				list.add(dep);
				b.addCrossDependency(dep);
			} else {
				b.changeDependentToCrossDependency(d, chromosome, list);
			}
		}
	}
	

	
	private void changeMainToCrossDependency(List<Branch> chromosome, Dependency d) {

		for(Branch br : normalGroup) {
			if(chromosome.contains(br)) {
				Dependency dep = d.copy(br, d.getDependent());
				d.getDependent().addCrossDependency(dep);
				
			}  else {
				br.changeMainToCrossDependency(chromosome, d);
			}
		}
		
		List<Branch> total = new ArrayList<Branch>();
		for(Branch br : ORGroup) {

	        	if(chromosome.contains(br)) {
	        		total.add(br);
	        	} else {
	        		br.getNextLayerChildrenGene(chromosome, total);
	        	}
	        	
	
		}
		
		
		if("excluded".equals(((CrossDependency)d).getType())){
//			for (int i = 0; i <  total.size();i++) {
//				// Reverse the dependency
//				total.get(i).addCrossDependency(d.copy(d.getDependent(), d.getMain()));
//			
//				d.getDependent().crossMain.remove(d);
//			}
			for (int i = 0; i <  total.size();i++) {
				Dependency dep = d.copy(total.get(i), d.getDependent());
				d.getDependent().addCrossDependency(dep);
			}
		} else if("required".equals(((CrossDependency)d).getType())) {
			Dependency dep = d.copy(d.getMain(), d.getDependent());
			((CrossDependency)dep).setType("at-least-one-required");
			for (int i = 0; i <  total.size();i++) {		
				((CrossDependency)d).addBranchInGroup(total.get(i));
			}
		}
		
		
		
		for(Branch br : XORGroup) {
			if(chromosome.contains(br)) {
				Dependency dep = d.copy(br, d.getDependent());
				d.getDependent().addCrossDependency(dep);
				
				
			}  else {
				br.changeMainToCrossDependency(chromosome, d);
			}
		}
	}
	
	public boolean isRangeCorrect(double[] given) {
		//if(isNumeric) {
			if(range.length != given.length) {
				return false;
			}
			
			
			for(int i = 0; i < given.length; i++) {
				if(given[i] != range[i]) {
					return false;
				}
			}
			
			return true;
			
		//} 
//		else {
//			// categorical has already included 0 as switchoff, if there is one.
//			range = given;
//			
//			
//			return (XORGroup.size() == given.length) || (isCanSwitchOff() && XORGroup.size() + 1 == given.length)
//			|| ((hasSwitchOff && hasSwitchOn) && 2 == given.length);
//		}
	}

	public void insertRange() {
		if (isNumeric) {

			if (isCanSwitchOff() && range[0] != 0) {
				throw new RuntimeException(name + " is a numeric feautre and it needs to be siwtch off, but it does not have predfeind " +
						"0 in its children, this is needed as all children of numeric feature  needs to be given a validated for correctness in the FM");
			}
		} else {
			if (isCanSwitchOff()) {

				if(XORGroup.size() == 0) {
					 range = new double[2];
				} else {
				   range = new double[XORGroup.size() + 1];
				}

			} else {
				range = new double[XORGroup.size()];
			}
			for (int i = 0; i < range.length; i++) {
				range[i] = i;
			}
		}
	}
	
	public boolean isLeaf(){
		return (normalGroup.size() == 0 && ORGroup.size() == 0 && XORGroup.size() == 0 && !isNumeric && !(hasSwitchOff && hasSwitchOn));
	}
	
	public String getRangeAsPlain(){
		String s = "";
		for (double d : range) {
			s = s + String.valueOf(d) + " ";
		}
		
		return s;
	}
	
	private double[] getRange (boolean fixedZero, double minProvision, double maxProvision, double a){
		int max = (int)Math.ceil(maxProvision);
		int min = (int)Math.ceil(minProvision);
		
		
		
		int length = (int) (1 + (max - min)/a);
//		if (length % a != 0) {
//			length += 1;
//		}
		double[] valueVector = null;
		
		if(fixedZero) {
			valueVector = new double[length+1];
			valueVector[0] = 0;
			double value = min - a;
			for (int i = 1; i < valueVector.length; i++) {
				if (value + a >= max) {
					value = max;
				} else {
					value = value + a;
				}
				
				
				valueVector[i] = value;
			}
			
		} else {
			valueVector = new double[length]; 
			double value = min - a;
			for (int i = 0; i < valueVector.length; i++) {
				if (value + a >= max) {
					value = max;
				} else {
					value = value + a;
				}
				
				
				valueVector[i] = value;
			}
			
		}
		
		
		
		
		return valueVector;
	}
	
	
	public boolean isMainOfNumericDependency(Branch b){
		for(Dependency d : crossMain) {
			if(((CrossDependency)d).isMain(b) && ((CrossDependency)d).isNumeric()) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isHasClosedLoopWithNumericDependency(Branch origin){
		
		// Only cross dependency might involve numeric dependency.
		for(Dependency d : crossMain) {
		  if(((CrossDependency)d).isMain(origin) &&	((CrossDependency)d).isNumeric()) {
			  return true;
		  }
		  
		  if(d.getMain().isHasClosedLoopWithNumericDependency(origin)){
			  return true;
		  }
		}
		
		return false;
	}
	
	public List<Branch> getNormalGroup() {
		return normalGroup;
	}

	public List<Branch> getORGroup() {
		return ORGroup;
	}

	public List<Branch> getXORGroup() {
		return XORGroup;
	}

	public void debug(){
		logger.debug(getName() + " can be considered as gene, range is: " + getRangeAsPlain());
		logger.debug("Parent: " + parent.getName());
		logger.debug("Numeric: " + isNumeric);
		logger.debug("isCanSwitchOff: " + this.isCanSwitchOff());
		logger.debug("-----------normal children-----------");
		for(Branch b : normalGroup) {
			logger.debug(b.getName());
		}
		
		logger.debug("-----------XOR children-----------");
		for(Branch b : XORGroup) {
			logger.debug(b.getName());
		}
		
		logger.debug("-----------OR children-----------");
		for(Branch b : ORGroup) {
			logger.debug(b.getName());
		}
		
		logger.debug("-----------cross tree dependency-----------");
		for(Dependency d : crossMain) {
			d.debug();
		}
		
		logger.debug("-----------in-branch dependency-----------");
		for(Dependency d : inBranchMain) {
			d.debug();
		}
		
	}
}
