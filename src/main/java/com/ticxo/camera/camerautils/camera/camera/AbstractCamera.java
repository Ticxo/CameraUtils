package com.ticxo.camera.camerautils.camera.camera;

import com.ticxo.camera.camerautils.CameraUtils;
import com.ticxo.camera.camerautils.camera.ICamera;
import com.ticxo.camera.camerautils.camera.ICameraTickable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCamera implements ICamera {

	private final List<Player> viewers = new ArrayList<>();
	private final List<ICameraTickable> tickables = new ArrayList<>();

	private boolean running = true;

	public AbstractCamera() {
		CameraUtils.registerCamera(this);
	}

	@Override
	public void addViewer(Player player) {
		viewers.add(player);
	}

	@Override
	public void removeViewer(Player player) {
		viewers.remove(player);
	}

	@Override
	public List<Player> getViewers() {
		return viewers;
	}

	@Override
	public void addCameraTickable(ICameraTickable tickable) {
		tickables.add(tickable);
	}

	@Override
	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public boolean tick() {

		tickables.removeIf(tickable -> !tickable.tick());

		return isRunning();
	}

	@Override
	public void onEnd() {

	}

}
