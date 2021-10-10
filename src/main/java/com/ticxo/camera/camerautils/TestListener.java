package com.ticxo.camera.camerautils;

import com.ticxo.camera.camerautils.camera.camera.ControllerCamera;
import com.ticxo.camera.camerautils.camera.tickable.PathTickable;
import com.ticxo.camera.camerautils.input.WrapperInput;
import com.ticxo.camera.camerautils.input.tracker.DefaultInputTracker;
import com.ticxo.camera.camerautils.packet.CameraChannelHandler;
import com.ticxo.camera.camerautils.utils.NMSTools;
import com.ticxo.camera.camerautils.utils.location.SingleLocation;
import com.ticxo.camera.camerautils.utils.location.TargetLocation;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TestListener implements Listener {

	@EventHandler
	public void onClick(PlayerInteractEvent event) {

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		CameraChannelHandler.injectPlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		CameraChannelHandler.removePlayer(event.getPlayer());
	}

	private void test1(Player player) {
		ControllerCamera camera = new ControllerCamera(player, player.getLocation());

		PathTickable path = new PathTickable(camera) {
			final GameMode mode = player.getGameMode();
			@Override
			public void onEnd() {
				super.onEnd();
				Bukkit.getScheduler().runTask(CameraUtils.instance, () -> {
					camera.removeCameraController();
					player.setGameMode(mode);
					camera.setRunning(false);
				});
			}
		};

		NMSTools.hideHotbar(NMSTools.getNMSPlayer(player));

		Location zero = player.getLocation().clone().zero().add(0.5, 4, 0.5);
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

	private void test2(Player player) {
		CameraUtils.instance.getInputManager().registerInputTracker(player, new DefaultInputTracker(player, player.getLocation()) {
			final GameMode mode = player.getGameMode();
			@Override
			public void syncInputEvent(WrapperInput input) {
				super.syncInputEvent(input);
				if(input.isSneak())
					player.setGameMode(mode);
			}
		});
		NMSTools.hideHotbar(NMSTools.getNMSPlayer(player));
	}

}
