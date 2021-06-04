package com.ticxo.camera.camerautils.utils.location;

import org.bukkit.Location;

public class SingleLocation implements WrappedLocation {

	private final Location location;

	public SingleLocation(Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return location;
	}

}
