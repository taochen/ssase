package edu.rice.rubis.invoker;

import org.ssase.actuator.Invoker;
import org.ssase.sensor.Sensor;
import org.ssase.sensor.SensoringController;
import org.ssase.sensor.control.SpareThreadSensor;

import edu.rice.rubis.beans.StandardThreadExecutorWrapper;

public class MinSpareThreadsInvoker implements Invoker {

	private StandardThreadExecutorWrapper wrapper;
	
	public MinSpareThreadsInvoker(StandardThreadExecutorWrapper wrapper) {
		super();
		this.wrapper = wrapper;
	}

	public boolean invoke(String service, long value) {	
		wrapper.setMinSpareThreads((int)value);
		Sensor cms = SensoringController.getSensor(null, "minSpareThreads");
		// Update the sensor
		((SpareThreadSensor)cms).updateSpare((int)value);
		System.out.print("minSpareThread set to new value " + value + "\n");
		return true;
	}

}
