package org.ssascaling.executor;

import java.util.HashMap;
import java.util.Map;

public class CPUCore {

	private Map<VM, Long> vms;
	private long remaining = 100; /*%*/
	// index of the newly added core (physical one)
	private int id;
	
	public CPUCore (int id, VM...array){
		this.id  = id;
		vms = new HashMap<VM, Long>();
		for (VM vm : array) {
			vms.put(vm, vm.getCpuCap());
			remaining -= vm.getCpuCap();
		}
	}
	
	public int getPhysicalID(){
		return id;
	}
	
	public Map<VM, Long> getVMs(){
		return vms;
	}
	
	public void update(long change){
		remaining += change;
	}
	
	public long allocate(long cpu) {
		if (remaining == 0) {
			return 0;
		}
		
		return remaining > cpu? cpu : remaining;
	}
	
	public void print(){
		String result = "CPU core " + id + ": \n";
		for (Map.Entry<VM, Long> entry : vms.entrySet()) {
			result += entry.getKey().print() + ", actual CPU=" + entry.getValue() + "\n";
		}
		
		result += "remaining=" + remaining + "\n";
		
		System.out.print(result);
	}
}
