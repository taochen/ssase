package org.ssascaling.observation.listener;

import org.ssascaling.observation.event.ModelChangeEvent;

public interface ModelListener extends Listener {

	public void updateWhenModelChange(ModelChangeEvent event);
}
