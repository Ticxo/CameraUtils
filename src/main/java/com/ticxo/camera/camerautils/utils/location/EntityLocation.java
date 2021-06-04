package com.ticxo.camera.camerautils.utils.location;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class EntityLocation implements WrappedLocation {

	private final Entity entity;

	public EntityLocation(Entity entity) {
		this.entity = entity;
	}

	@Override
	public Location getLocation() {
		return entity.getLocation();
	}

}
