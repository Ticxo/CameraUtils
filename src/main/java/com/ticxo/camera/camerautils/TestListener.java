package com.ticxo.camera.camerautils;

import com.ticxo.camera.camerautils.camera.camera.SpectatorCamera;
import com.ticxo.camera.camerautils.camera.tickable.PathTickable;
import com.ticxo.camera.camerautils.utils.NMSTools;
import com.ticxo.camera.camerautils.utils.location.SingleLocation;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
						camera.removeViewer(player);
						player.setGameMode(GameMode.CREATIVE);
						camera.setRunning(false);
					});
				}
			};
			path.addRotationNode(0, 0, 0);
			path.addRotationNode(20, 66, 75);

			path.addLocationNode(0, new SingleLocation(player.getLocation()));
			path.addLocationNode(50, new SingleLocation(player.getLocation().clone().add(0, 5, 0)));

			camera.addCameraTickable(path);

		}

	}

}
