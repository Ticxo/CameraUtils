package com.ticxo.camera.camerautils.manager;

import com.ticxo.camera.camerautils.input.IInputTracker;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InputManager {

	private final Map<Player, IInputTracker> inputTrackers = new ConcurrentHashMap<>();

	public void registerInputTracker(Player player, IInputTracker tracker) {
		inputTrackers.put(player, tracker);
	}

	public void unregisterInputTracker(Player player) {
		inputTrackers.remove(player);
	}

	public boolean hasInputTracker(Player player) {
		return inputTrackers.containsKey(player);
	}

	public IInputTracker getInputTracker(Player player) {
		return inputTrackers.get(player);
	}

}
