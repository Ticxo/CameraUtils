package com.ticxo.camera.camerautils.utils;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WrappedRotation {

	private float yaw, pitch;

	public WrappedRotation() {

	}

	public WrappedRotation(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}

}
