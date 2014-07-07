package org.ssascaling.planner;

import java.util.LinkedHashMap;
import java.util.List;

import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.region.SuperRegionControl;

public class Planner {


	public static LinkedHashMap<ControlPrimitive, Double> optimize (Objective obj, String uuid) {
		return SuperRegionControl.getInstance().optimize(obj, uuid);
	}
}
