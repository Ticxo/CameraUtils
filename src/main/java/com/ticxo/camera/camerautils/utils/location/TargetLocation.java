package com.ticxo.camera.camerautils.utils.location;

import com.ticxo.camera.camerautils.camera.ICamera;
import com.ticxo.camera.camerautils.utils.WrappedRotation;
import lombok.Getter;
import org.bukkit.Location;

@Getter
public class TargetLocation extends AbstractLocation {

	private final WrappedLocation origin;
	private final WrappedLocation target;

	public TargetLocation(Location origin, Location target) {
		this(new SingleLocation(origin), new SingleLocation(target));
	}

	public TargetLocation(WrappedLocation origin, WrappedLocation target) {
		this.origin = origin;
		this.target = target;
	}

	public Location getLocation() {
		return ICamera.face(origin.getLocation(), target.getLocation());
	}

	@Override
	public Location lerp(WrappedLocation prevFrame, double ratio) {

		if(prevFrame instanceof TargetLocation) {
			TargetLocation prevTarget = (TargetLocation) prevFrame;
			Location origin = ICamera.lerp(prevTarget.getOrigin().getLocation(), getOrigin().getLocation(), ratio);
			Location target = ICamera.lerp(prevTarget.getTarget().getLocation(), getTarget().getLocation(), ratio);

			return ICamera.face(origin, target);
		}

		return super.lerp(prevFrame, ratio);
	}

	@Override
	public Location serp(WrappedLocation prevControlFrame, WrappedLocation prevFrame, WrappedLocation nextControlFrame, double ratio) {

		if(prevFrame instanceof TargetLocation) {
			TargetLocation prevTarget = (TargetLocation) prevFrame;
			Location origin = ICamera.serp(0, prevControlFrame.getLocation(), prevTarget.getOrigin().getLocation(), getOrigin().getLocation(), nextControlFrame.getLocation(), ratio);
			Location target = ICamera.lerp(prevTarget.getTarget().getLocation(), getTarget().getLocation(), ratio);

			return ICamera.face(origin, target);
		}

		return super.serp(prevControlFrame, prevFrame, nextControlFrame, ratio);
	}

	@Override
	public WrappedRotation rotLerp(WrappedLocation prevFrame, double ratio) {

		if(prevFrame instanceof TargetLocation) {
			Location loc = lerp(prevFrame, ratio);
			return new WrappedRotation(loc.getYaw(), loc.getPitch());
		}

		return super.rotLerp(prevFrame, ratio);
	}

	@Override
	public WrappedRotation rotSerp(WrappedLocation prevFrame, double ratio) {

		if(prevFrame instanceof TargetLocation) {
			return rotLerp(prevFrame, ratio);
		}

		return super.rotSerp(prevFrame, ratio);
	}
}
