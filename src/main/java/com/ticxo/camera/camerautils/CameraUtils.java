package com.ticxo.camera.camerautils;

import com.ticxo.camera.camerautils.camera.ICamera;
import com.ticxo.camera.camerautils.manager.TickableManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class CameraUtils extends JavaPlugin implements Listener {

	public static CameraUtils instance;

	@Getter
	private TickableManager tickableManager;

	@Override
	public void onEnable() {
		// Plugin startup logic
		instance = this;

		tickableManager = new TickableManager();
		tickableManager.start();

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
