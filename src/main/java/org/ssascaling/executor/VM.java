package org.ssascaling.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ssascaling.primitive.*;
public class VM {

	// VM_ID
	private String VM_ID;
	private Map<Type, HardwareControlPrimitive> map;
	private List<HardwareControlPrimitive> list = new ArrayList<HardwareControlPrimitive>();
	
	
	public VM(String VM_ID, HardwareControlPrimitive... primitives) {
		super();
		map = new HashMap<Type, HardwareControlPrimitive>();
		this.VM_ID = VM_ID;
		for (HardwareControlPrimitive p : primitives) {
			map.put(p.getType(), p);
			list.add(p);
		}
	}

	public HardwareControlPrimitive getHardwareControlPrimitive(String name) {
		return map.get(Type.getTypeByString(name));
	}
	
	public boolean isScaleUp(long cap) {
		return cap > map.get(Type.CPU).getProvision();
	}
	
	public long getCPUNo(){
		return  Math.round(map.get(Type.CPU).getProvision())/100 + 1;
	}

	public long getCpuCap() {
		return Math.round(map.get(Type.CPU).getProvision());
	}
	
	public long getMemory(){
		return Math.round(map.get(Type.Memory).getProvision());
	}
	
	public Collection<HardwareControlPrimitive> getAllPrimitives(){
		return list;
	}
	
	public String print(){
		return "VM " + VM_ID + ": CPU=" + map.get(Type.CPU).getProvision() + ", memory=" + map.get(Type.Memory).getProvision();
	}
	
}
