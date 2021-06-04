package com.ticxo.camera.camerautils.camera.tickable;

import com.ticxo.camera.camerautils.camera.ICamera;
import com.ticxo.camera.camerautils.utils.WrappedRotation;
import com.ticxo.camera.camerautils.utils.location.WrappedLocation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.TreeMap;

@Getter
public class PathTickable extends AbstractCameraTickable {

	private final ICamera camera;
	private final TreeMap<Integer, WrappedLocation> positionNodes = new TreeMap<>();
	private final TreeMap<Integer, WrappedRotation> rotationNodes = new TreeMap<>();

	@Setter
	private boolean isSmooth;
	private int frame = 0;

	public PathTickable(ICamera camera) {
		this.camera = camera;
	}

	@Override
	public boolean tick() {
		boolean r;

		if(isSmooth) {
			r = smoothTick();
		}else {
			r = linearTick();
		}

		if(!r)
			onEnd();
		return r;
	}

	public void addLocationNode(int frame, WrappedLocation location) {
		positionNodes.put(frame, location);
	}

	public void addRotationNode(int frame, WrappedLocation location) {
		rotationNodes.put(frame, new WrappedRotation(location.getLocation().getYaw(), location.getLocation().getPitch()));
	}

	protected boolean linearTick() {
		if (positionNodes.isEmpty() && rotationNodes.isEmpty())
			return false;

		boolean hasLocation = !positionNodes.isEmpty();
		boolean hasRotation = !rotationNodes.isEmpty();

		if (positionNodes.containsKey(frame)) {
			camera.setCameraLocation(positionNodes.get(frame).getLocation());
		} else {
			Integer nextFrame = positionNodes.higherKey(frame);
			if (nextFrame == null) {// Last frame reached, stopping
				hasLocation = false;
			}else {
				Integer prevFrame = positionNodes.lowerKey(frame);
				if (prevFrame == null) { // No previous frame. Lerp from current location
					camera.setCameraLocation(ICamera.lerp(camera.getCurrentLocation(), positionNodes.get(nextFrame).getLocation(), 1 / (double) (nextFrame - frame)));
				} else { // Previous frame exists. Lerp from previous frame
					camera.setCameraLocation(ICamera.lerp(positionNodes.get(prevFrame).getLocation(), positionNodes.get(nextFrame).getLocation(), (double) (frame - prevFrame) / (nextFrame - prevFrame)));
				}
			}
		}

		if (rotationNodes.containsKey(frame)) {
			WrappedRotation rot = rotationNodes.get(frame);
			camera.setCameraRotation(rot.getYaw(), rot.getPitch());
		} else {
			Integer nextFrame = rotationNodes.higherKey(frame);
			if (nextFrame == null) {// Last frame reached, stopping
				hasRotation = false;
			}else {
				Integer prevFrame = rotationNodes.lowerKey(frame);
				WrappedRotation rot;
				if (prevFrame == null) { // No previous frame. Lerp from current location
					rot = new WrappedRotation(camera.getCurrentLocation().getYaw(), camera.getCurrentLocation().getPitch());
					rot = ICamera.rotLerp(rot, rotationNodes.get(nextFrame), 1 / (double) (nextFrame - frame));
				} else {
					rot = ICamera.rotLerp(rotationNodes.get(prevFrame), rotationNodes.get(nextFrame), (double) (frame - prevFrame) / (nextFrame - prevFrame));
				}
				camera.setCameraRotation(rot.getYaw(), rot.getPitch());
			}
		}

		++frame;

		return hasLocation || hasRotation;
	}

	protected boolean smoothTick() {
		if (positionNodes.isEmpty() && rotationNodes.isEmpty())
			return false;

		boolean hasLocation = !positionNodes.isEmpty();
		boolean hasRotation = !rotationNodes.isEmpty();

		if (positionNodes.containsKey(frame)) {
			camera.setCameraLocation(positionNodes.get(frame).getLocation());
		} else {
			Integer nextFrame = positionNodes.higherKey(frame);
			if (nextFrame == null) {// Last frame reached, stopping
				hasLocation = false;
			}else {
				Integer nnFrame = positionNodes.higherKey(nextFrame);
				if (nnFrame == null)
					nnFrame = nextFrame;

				Integer prevFrame = positionNodes.lowerKey(frame);
				if (prevFrame == null) { // No previous frame. Lerp from current location
					Location serp = ICamera.serp(0, camera.getCurrentLocation(), camera.getCurrentLocation(), positionNodes.get(nextFrame).getLocation(), positionNodes.get(nnFrame).getLocation(), 1 / (double) (nextFrame - frame));
					camera.setCameraLocation(serp);
				} else { // Previous frame exists. Lerp from previous frame
					Integer ppFrame = positionNodes.lowerKey(prevFrame);
					if (ppFrame == null)
						ppFrame = prevFrame;

					Location serp = ICamera.serp(0, positionNodes.get(ppFrame).getLocation(), positionNodes.get(prevFrame).getLocation(), positionNodes.get(nextFrame).getLocation(), positionNodes.get(nnFrame).getLocation(), (double) (frame - prevFrame) / (nextFrame - prevFrame));
					camera.setCameraLocation(serp);
				}
			}
		}

		if (rotationNodes.containsKey(frame)) {
			WrappedRotation rot = rotationNodes.get(frame);
			camera.setCameraRotation(rot.getYaw(), rot.getPitch());
		} else {
			Integer nextFrame = rotationNodes.higherKey(frame);
			if (nextFrame == null) {// Last frame reached, stopping
				hasRotation = false;
			}else {

				Integer prevFrame = rotationNodes.lowerKey(frame);
				WrappedRotation rot;
				if (prevFrame == null) { // No previous frame. Lerp from current location
					rot = new WrappedRotation(camera.getCurrentLocation().getYaw(), camera.getCurrentLocation().getPitch());
					rot = ICamera.rotLerp(rot, rotationNodes.get(nextFrame), 1 / (double) (nextFrame - frame));
				} else {
					rot = ICamera.rotLerp(rotationNodes.get(prevFrame), rotationNodes.get(nextFrame), (double) (frame - prevFrame) / (nextFrame - prevFrame));
				}
				camera.setCameraRotation(rot.getYaw(), rot.getPitch());
			}
		}

		++frame;

		return hasLocation || hasRotation;
	}

}
