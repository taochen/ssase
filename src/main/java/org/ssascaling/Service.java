package org.ssascaling;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.qos.QualityOfService;
/**
 * This is actually a service-instance.
 * @author tao
 *
 */
public class Service {

	// String name - QoS/Cost
	private Map<String, Objective> objectives;
	// String name - software CP/EP, these are all direct primitive, not including the functional dependent ones.
	private Map<String, Primitive> primitives;

	private Set<Primitive> possibleInputs = new HashSet<Primitive>();
	
	// Functionally dependent service, can use this to get their primitives.
	private Set<Service> dependentServices;
	
	private String VM_ID;
	
	
	private String name;
	
	@Deprecated
	public Service(){
		
	}
	
	public Service( 
			String vM_ID, 
			String name,
			Map<String, Objective> objectives /*The objective here is with a null Model instance*/,
			Map<String, Primitive> primitives) {
		super();
		this.objectives = objectives;
		this.primitives = primitives;
		VM_ID = vM_ID;
		this.name = name;
	}
	
	public void addPossiblePrimitive (Collection<Primitive> p) {
		possibleInputs.addAll(p);
	}
	
	public void removePossiblePrimitive (Collection<Primitive> p) {
		possibleInputs.removeAll(p);
	}
	
	public void addPossiblePrimitive (Primitive p) {
		possibleInputs.add(p);
	}
	
	public void removePossiblePrimitive (Primitive p) {
		possibleInputs.remove(p);
	}
	
	public void initializeModelForQoS(){
		for (Objective obj : objectives.values()) {
			if(obj instanceof QualityOfService) {
				QualityOfService qos = (QualityOfService)obj;
				qos.buildModel(
						possibleInputs, 
						ModelFunctionConfigGenerator.getFunctionConfiguration(obj.getName()), 
						ModelFunctionConfigGenerator.getStructureConfiguration(obj.getName()));
			}
		}
	}

	public void prepareToUpdatePrimitiveValue(String name, double... values){
		if (!primitives.containsKey(name)) {
			return;
		}
		
		for (double v : values) {
			primitives.get(name).prepareToAddValue(v) ;
		} 
	}
	
	public void prepareToUpdateQoSValue(String name, double... values){
		for (double v : values) {
			((QualityOfService)objectives.get(name)).prepareToAddValue(v) ;
		} 
	}
	
	public Collection<Objective> getObjectives(){
		return objectives.values();
	}
	
	public Collection<Primitive> getPrimitives(){
		return primitives.values();
	}
	
	public Primitive getPrimitive(String name){
		return primitives.get(name);
	}
	
	public Set<Primitive> getPossiblePrimitives(){
		return possibleInputs;
	}
	
	public Objective getObjective(String name){
		return objectives.get(name);
	}
	
	public String getName(){
		return name;
	}
	
	
	public String getVMID(){
		return VM_ID;
	}
}
