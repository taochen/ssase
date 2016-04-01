package org.ssase.observation.listener;

import org.ssase.observation.event.ModelChangeEvent;

public interface ModelListener extends Listener {

	public void updateWhenModelChange(ModelChangeEvent event);
}
