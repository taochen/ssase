package org.ssase.objective.optimization.femosaa.variability.fm;

public interface Dependency {
	public Integer[][] getRangeBasedonMainVariable();
	
	public Branch getMain();
	
	public Branch getDependent();
	
	public Dependency copy(Branch main, Branch dependent);
	
	public void debug();
}
