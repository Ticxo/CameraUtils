package com.ticxo.camera.camerautils;

import com.ticxo.camera.camerautils.camera.camera.SpectatorCamera;
import com.ticxo.camera.camerautils.camera.tickable.PathTickable;
import com.ticxo.camera.camerautils.utils.location.SingleLocation;
import com.ticxo.camera.camerautils.utils.location.TargetLocation;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class TestListener implements Listener {

	@EventHandler
	public void onClick(PlayerInteractEvent event) {

		Player player = event.getPlayer();

		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null) {

			SpectatorCamera camera = new SpectatorCamera(player.getLocation());
			camera.addViewer(player);

			PathTickable path = new PathTickable(camera) {
				@Override
				public void onEnd() {
					super.onEnd();
					Bukkit.getScheduler().runTask(CameraUtils.instance, () -> {
						player.teleport(camera.getCurrentLocation());
						camera.removeViewer(player);
						player.setGameMode(GameMode.CREATIVE);
						camera.setRunning(false);
					});
				}
			};

			Location zero = player.getLocation().clone().zero().add(0.5, 0, 0.5);
			Location loc = player.getLocation().clone().add(-9, 5, 10);
			Location loc2 = loc.clone().add(4, -2, -4);

			path.addLocationNode(0, new SingleLocation(player.getLocation()));
			path.addLocationNode(100, new SingleLocation(loc));
			path.addLocationNode(200, new SingleLocation(loc2));

			path.addRotationNode(0, new TargetLocation(camera, new SingleLocation(zero)));
			path.addRotationNode(100, new TargetLocation(camera, new SingleLocation(zero.clone().add(0, 0, 3))));
			path.addRotationNode(200, new TargetLocation(camera, new SingleLocation(zero.clone().add(-3, 0, 0))));

			path.setSmooth(true);

			camera.addCameraTickable(path);
			camera.activate();
		}

	}

}
