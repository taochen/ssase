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
	// String name - CP/EP, these are all direct primitive, not including the functional dependent ones.
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
		if (values.length == 1) {
			primitives.get(name).prepareToAddValue(values[0]) ;
		} else {
			primitives.get(name).prepareToAddValue(values) ;
		}
	}
	
	public void prepareToUpdateQoSValue(String name, double... values){
		if (values.length == 1) {
			((QualityOfService)objectives.get(name)).prepareToAddValue(values[0]) ;
		} else {
			((QualityOfService)objectives.get(name)).prepareToAddValue(values) ;
		}
	}
	
	public Collection<Objective> getObjectives(){
		return objectives.values();
	}
	
	public Collection<Primitive> getPrimitives(){
		return primitives.values();
	}
	
	public String getName(){
		return name;
	}
	
	
	public String getVMID(){
		return VM_ID;
	}
}
