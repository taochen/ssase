package org.ssase.observation.listener;

import org.ssase.observation.event.SuperRegionChangeEvent;

public interface SuperRegionListener extends Listener {

	public void updateWhenSuperRegionChange(SuperRegionChangeEvent event);
}
