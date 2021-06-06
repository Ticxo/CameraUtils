package com.ticxo.camera.camerautils.camera.tickable;

import com.ticxo.camera.camerautils.camera.ICamera;
import com.ticxo.camera.camerautils.utils.WrappedRotation;
import com.ticxo.camera.camerautils.utils.location.WrappedLocation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.Locale;
import java.util.TreeMap;

@Getter
public class PathTickable extends AbstractCameraTickable {

	private final ICamera camera;
	private final TreeMap<Integer, WrappedLocation> positionNodes = new TreeMap<>();
	private final TreeMap<Integer, WrappedLocation> rotationNodes = new TreeMap<>();

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
		rotationNodes.put(frame, location);
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
					camera.setCameraLocation(positionNodes.get(nextFrame).lerp(camera, 1 / (double) (nextFrame - frame)));
				} else { // Previous frame exists. Lerp from previous frame
					camera.setCameraLocation(positionNodes.get(nextFrame).lerp(positionNodes.get(prevFrame), (double) (frame - prevFrame) / (nextFrame - prevFrame)));
				}
			}
		}

		if (rotationNodes.containsKey(frame)) {
			WrappedLocation rot = rotationNodes.get(frame);
			camera.setCameraRotation(rot.getLocation().getYaw(), rot.getLocation().getPitch());
		} else {
			Integer nextFrame = rotationNodes.higherKey(frame);
			if (nextFrame == null) {// Last frame reached, stopping
				hasRotation = false;
			}else {
				Integer prevFrame = rotationNodes.lowerKey(frame);
				WrappedRotation rot;
				if (prevFrame == null) { // No previous frame. Lerp from current location
					rot = rotationNodes.get(nextFrame).rotLerp(camera, 1 / (double) (nextFrame - frame));
				} else {
					rot = rotationNodes.get(nextFrame).rotLerp(rotationNodes.get(prevFrame), (double) (frame - prevFrame) / (nextFrame - prevFrame));
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
					camera.setCameraLocation(positionNodes.get(nextFrame).serp(camera, camera, positionNodes.get(nnFrame), 1 / (double) (nextFrame - frame)));
				} else { // Previous frame exists. Lerp from previous frame
					Integer ppFrame = positionNodes.lowerKey(prevFrame);
					if (ppFrame == null)
						ppFrame = prevFrame;

					camera.setCameraLocation(positionNodes.get(nextFrame).serp(positionNodes.get(ppFrame), positionNodes.get(prevFrame), positionNodes.get(nnFrame), (double) (frame - prevFrame) / (nextFrame - prevFrame)));
				}
			}
		}

		if (rotationNodes.containsKey(frame)) {
			WrappedLocation rot = rotationNodes.get(frame);
			camera.setCameraRotation(rot.getLocation().getYaw(), rot.getLocation().getPitch());
		} else {
			Integer nextFrame = rotationNodes.higherKey(frame);
			if (nextFrame == null) {// Last frame reached, stopping
				hasRotation = false;
			}else {
				Integer prevFrame = rotationNodes.lowerKey(frame);
				WrappedRotation rot;
				if (prevFrame == null) { // No previous frame. Lerp from current location
					rot = rotationNodes.get(nextFrame).rotSerp(camera, 1 / (double) (nextFrame - frame));
				} else {
					rot = rotationNodes.get(nextFrame).rotSerp(rotationNodes.get(prevFrame), (double) (frame - prevFrame) / (nextFrame - prevFrame));
				}
				camera.setCameraRotation(rot.getYaw(), rot.getPitch());
			}
		}

		++frame;

		return hasLocation || hasRotation;
	}

}
