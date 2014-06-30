package org.ssascaling.observation.listener;

import org.ssascaling.observation.event.SuperRegionChangeEvent;

public interface SuperRegionListener extends Listener {

	public void updateWhenSuperRegionChange(SuperRegionChangeEvent event);
}
