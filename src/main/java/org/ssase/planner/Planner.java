package org.ssase.planner;

import java.util.LinkedHashMap;
import java.util.List;

import org.ssase.objective.Objective;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.region.SuperRegionControl;

public class Planner {


	public static LinkedHashMap<ControlPrimitive, Double> optimize (Objective obj, String uuid) {
		return SuperRegionControl.getInstance().optimize(obj, uuid);
	}
}
