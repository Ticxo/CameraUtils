package com.ticxo.camera.camerautils.camera.camera;

import com.mojang.datafixers.util.Pair;
import com.ticxo.camera.camerautils.CameraUtils;
import com.ticxo.camera.camerautils.camera.ICamera;
import com.ticxo.camera.camerautils.camera.ICameraTickable;
import com.ticxo.camera.camerautils.utils.WrappedRotation;
import com.ticxo.camera.camerautils.utils.location.WrappedLocation;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCamera implements ICamera {

	protected static final List<Pair<EnumItemSlot, ItemStack>> emptyEquipment = Arrays.asList(
			new Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR))),
			new Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR))),
			new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR))),
			new Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR))),
			new Pair<>(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR))),
			new Pair<>(EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR)))
	);

	private final List<Player> viewers = new ArrayList<>();
	private final List<ICameraTickable> tickables = new ArrayList<>();

	private boolean running = true;

	@Override
	public void activate() {
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

	@Override
	public Location getLocation() {
		return getCurrentLocation();
	}

	@Override
	public Location lerp(WrappedLocation prevFrame, double ratio) {
		return ICamera.lerp(prevFrame.getLocation(), getLocation(), ratio);
	}

	@Override
	public Location serp(WrappedLocation prevControlFrame, WrappedLocation prevFrame, WrappedLocation nextControlFrame, double ratio) {
		return ICamera.serp(0, prevControlFrame.getLocation(), prevFrame.getLocation(), getLocation(), nextControlFrame.getLocation(), ratio);
	}

	@Override
	public WrappedRotation rotLerp(WrappedLocation prevFrame, double ratio) {
		return ICamera.rotLerp(new WrappedRotation(prevFrame.getLocation().getYaw(), prevFrame.getLocation().getPitch()), new WrappedRotation(getLocation().getYaw(), getLocation().getPitch()), ratio);
	}

	@Override
	public WrappedRotation rotSerp(WrappedLocation prevFrame, double ratio) {
		return ICamera.rotSlerp(new WrappedRotation(prevFrame.getLocation().getYaw(), prevFrame.getLocation().getPitch()), new WrappedRotation(getLocation().getYaw(), getLocation().getPitch()), ratio);
	}
}
