package com.ticxo.camera.camerautils.camera.tickable;

import com.ticxo.camera.camerautils.camera.ICamera;
import com.ticxo.camera.camerautils.utils.location.EntityLocation;
import com.ticxo.camera.camerautils.utils.location.SingleLocation;
import com.ticxo.camera.camerautils.utils.location.WrappedLocation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class TargetTickable extends AbstractCameraTickable {

	private final ICamera camera;
	private final WrappedLocation location;

	@Getter @Setter
	private boolean isRunning = true;

	public TargetTickable(ICamera camera, Location location) {
		this.camera = camera;
		this.location = new SingleLocation(location);
	}

	public TargetTickable(ICamera camera, Entity entity) {
		this.camera = camera;
		this.location = new EntityLocation(entity);
	}

	@Override
	public boolean tick() {

		camera.setCameraLocation(ICamera.face(camera.getCurrentLocation(), location.getLocation()));

		return super.tick();
	}

}
