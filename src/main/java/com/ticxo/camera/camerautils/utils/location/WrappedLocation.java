package com.ticxo.camera.camerautils.utils.location;

import com.ticxo.camera.camerautils.utils.WrappedRotation;
import org.bukkit.Location;

public interface WrappedLocation {

	Location getLocation();

	Location lerp(WrappedLocation prevFrame, double ratio);
	Location serp(WrappedLocation prevControlFrame, WrappedLocation prevFrame, WrappedLocation nextControlFrame, double ratio);
	WrappedRotation rotLerp(WrappedLocation prevFrame, double ratio);
	WrappedRotation rotSerp(WrappedLocation prevFrame, double ratio);

}
