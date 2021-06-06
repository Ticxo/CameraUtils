package com.ticxo.camera.camerautils.utils.location;

import com.ticxo.camera.camerautils.camera.ICamera;
import com.ticxo.camera.camerautils.utils.WrappedRotation;
import org.bukkit.Location;

public abstract class AbstractLocation implements WrappedLocation {

	@Override
	public Location lerp(WrappedLocation prevFrame, double ratio) {
		return ICamera.lerp(prevFrame.getLocation(), getLocation(), ratio);
	}

	@Override
	public Location serp(WrappedLocation prevControlFrame, WrappedLocation prevFrame, WrappedLocation nextControlFrame, double ratio) {
		return ICamera.serp(0, prevControlFrame.getLocation(), prevFrame.getLocation(), getLocation(), nextControlFrame.getLocation(), ratio);
	}

	@Override
	public WrappedRotation rotLerp(WrappedLocation prevFrame, double ratio) {
		return ICamera.rotLerp(new WrappedRotation(prevFrame.getLocation().getYaw(), prevFrame.getLocation().getPitch()), new WrappedRotation(getLocation().getYaw(), getLocation().getPitch()), ratio);
	}

	@Override
	public WrappedRotation rotSerp(WrappedLocation prevFrame, double ratio) {
		return ICamera.rotSlerp(new WrappedRotation(prevFrame.getLocation().getYaw(), prevFrame.getLocation().getPitch()), new WrappedRotation(getLocation().getYaw(), getLocation().getPitch()), ratio);
	}
}
