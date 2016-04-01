package org.ssase.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ssase.primitive.*;
public class VM {

	// VM_ID
	private String VM_ID;
	private Map<Type, HardwareControlPrimitive> map;
	private List<HardwareControlPrimitive> list = new ArrayList<HardwareControlPrimitive>();
	// Software CP that shared by all services on the VM, however, they are still controlled
	// via DomU not Dom0.
	private List<SoftwareControlPrimitive> software = new ArrayList<SoftwareControlPrimitive>();
	private Map<Type, SoftwareControlPrimitive> softwareMap;
	public VM(String VM_ID, HardwareControlPrimitive... primitives) {
		super();
		map = new HashMap<Type, HardwareControlPrimitive>();
		softwareMap = new HashMap<Type, SoftwareControlPrimitive>();
		this.VM_ID = VM_ID;
		for (HardwareControlPrimitive p : primitives) {
			map.put(p.getType(), p);
			list.add(p);
		}
	}

	public HardwareControlPrimitive getHardwareControlPrimitive(String name) {
		return map.get(Type.getTypeByString(name));
	}
	

	public SoftwareControlPrimitive getSoftwareControlPrimitive(String name) {
		return softwareMap.get(Type.getTypeByString(name));
	}
	
	public void setSharedSoftwareControlPrimitives (List<SoftwareControlPrimitive> software) {
		this.software = software;
		
		for (SoftwareControlPrimitive p : software) {
			softwareMap.put(p.getType(), p);
		}
	}
	
	public boolean isScaleUp(long cap) {
		return cap > map.get(Type.CPU).getProvision();
	}
	
	public boolean isScaleDown(long cap) {
		return cap < map.get(Type.CPU).getProvision();
	}
	
	public long getCPUNo(){
		double d = map.get(Type.CPU).getProvision()%100;
		return  d > 0 ? (long)map.get(Type.CPU).getProvision()/100 + 1 : 
			(long)map.get(Type.CPU).getProvision()/100;
	}

	public long getCpuCap() {
		return Math.round(map.get(Type.CPU).getProvision());
	}
	
	public long getMemory(){
		return Math.round(map.get(Type.Memory).getProvision());
	}
	

	public long getMaxCpuCap() {
		return Math.round(map.get(Type.CPU).getValueVector()[map.get(Type.CPU).getValueVector().length - 1]);
	}
	
	public long getMaxCPUNo(){
		double d = map.get(Type.CPU).getValueVector()[map.get(Type.CPU).getValueVector().length - 1]%100;
		return  d > 0 ? (long)map.get(Type.CPU).getValueVector()[map.get(Type.CPU).getValueVector().length - 1]/100 + 1 : 
			(long)map.get(Type.CPU).getValueVector()[map.get(Type.CPU).getValueVector().length - 1]/100;
    }
	
	public long getMaxMemory(){
		return Math.round(map.get(Type.Memory).getValueVector()[map.get(Type.Memory).getValueVector().length - 1]);
	}
	
	public Collection<HardwareControlPrimitive> getAllHardwarePrimitives(){
		return list;
	}
	
	
	public Collection<SoftwareControlPrimitive> getAllSharedSoftwarePrimitives(){
		return software;
	}
	
	public String getID (){
		return VM_ID;
	}
	
	public String print(){
		return "VM " + VM_ID + ": CPU=" + map.get(Type.CPU).getProvision() + ", memory=" + map.get(Type.Memory).getProvision();
	}
	
}
