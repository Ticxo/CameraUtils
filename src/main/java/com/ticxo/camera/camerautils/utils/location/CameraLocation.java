package com.ticxo.camera.camerautils.utils.location;

import com.ticxo.camera.camerautils.camera.ICamera;
import org.bukkit.Location;

public class CameraLocation implements WrappedLocation {

	private final ICamera camera;

	public CameraLocation(ICamera camera) {
		this.camera = camera;
	}

	@Override
	public Location getLocation() {
		return camera.getCurrentLocation();
	}
}
