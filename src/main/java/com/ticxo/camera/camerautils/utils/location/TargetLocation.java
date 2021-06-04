package com.ticxo.camera.camerautils.utils.location;

import com.ticxo.camera.camerautils.camera.ICamera;
import org.bukkit.Location;

public class TargetLocation implements WrappedLocation {

	private final WrappedLocation origin;
	private final WrappedLocation location;

	public TargetLocation(Location origin, Location location) {
		this(new SingleLocation(origin), new SingleLocation(location));
	}

	public TargetLocation(WrappedLocation origin, WrappedLocation location) {
		this.origin = origin;
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return ICamera.face(origin.getLocation(), location.getLocation());
	}
}
