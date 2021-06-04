package com.ticxo.camera.camerautils.camera.tickable;

import com.ticxo.camera.camerautils.camera.ICameraTickable;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractCameraTickable implements ICameraTickable {

	@Getter
	@Setter
	private boolean isRunning = true;

	@Override
	public boolean tick() {

		if(!isRunning)
			onEnd();

		return isRunning;
	}

	@Override
	public void onEnd() {

	}
}
