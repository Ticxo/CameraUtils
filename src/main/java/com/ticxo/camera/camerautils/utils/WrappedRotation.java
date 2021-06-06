package com.ticxo.camera.camerautils.utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.util.EulerAngle;

@Getter @Setter
public class WrappedRotation {

	private double yaw, pitch;

	public WrappedRotation() {

	}

	public WrappedRotation(double yaw, double pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public EulerAngle getEulerAngle() {
		return new EulerAngle(Math.toRadians(pitch), Math.toRadians(yaw), 0);
	}

}
