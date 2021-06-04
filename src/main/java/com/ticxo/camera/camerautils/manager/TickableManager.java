package com.ticxo.camera.camerautils.manager;

import com.ticxo.camera.camerautils.CameraUtils;
import com.ticxo.camera.camerautils.camera.ICameraTickable;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TickableManager extends BukkitRunnable {

	private final List<ICameraTickable> tickables = new ArrayList<>();

	@Override
	public void run() {
		tickables.removeIf(tickable -> !tickable.tick());
	}

	public void start() {
		runTaskTimerAsynchronously(CameraUtils.instance, 0, 1);
	}

}
