package com.ticxo.camera.camerautils;

import com.ticxo.camera.camerautils.camera.ICamera;
import com.ticxo.camera.camerautils.manager.InputManager;
import com.ticxo.camera.camerautils.manager.TickableManager;
import com.ticxo.camera.camerautils.packet.CameraChannelHandler;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class CameraUtils extends JavaPlugin implements Listener {

	public static CameraUtils instance;

	@Getter
	private TickableManager tickableManager;
	@Getter
	private InputManager inputManager;

	@Override
	public void onEnable() {
		// Plugin startup logic
		instance = this;

		tickableManager = new TickableManager();
		tickableManager.start();

		inputManager = new InputManager();
		CameraChannelHandler.setInputManager(inputManager);

		// Bukkit.getPluginManager().registerEvents(new TestListener(), this);

	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic

		tickableManager.cancel();
	}

	public static void registerCamera(ICamera camera) {
		instance.getTickableManager().getTickables().add(camera);
	}

	public static void unregisterAllCamera() {
		instance.getTickableManager().getTickables().clear();
	}

}
