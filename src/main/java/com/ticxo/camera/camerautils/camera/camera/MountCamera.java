package com.ticxo.camera.camerautils.camera.camera;

import com.ticxo.camera.camerautils.camera.IActionListener;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MountCamera extends AbstractCamera implements IActionListener {

	protected static final double level = -1.27;

	protected final ArmorStand cameraPoint;
	protected final EntityArmorStand nmsCameraPoint;

	public MountCamera(Location location) {

		location.add(0, level, 0);

		cameraPoint = location.getWorld().spawn(location, ArmorStand.class, armorStand -> {
			armorStand.setMarker(true);
			armorStand.setInvisible(true);
			armorStand.setGravity(false);
			armorStand.setSilent(true);
			armorStand.setInvulnerable(true);
		});

		nmsCameraPoint = ((CraftArmorStand) cameraPoint).getHandle();

	}
	@Override
	public void setCameraLocation(Location location) {
		nmsCameraPoint.setLocation(location.getX(), location.getY() + level, location.getZ(), location.getYaw(), location.getPitch());
		nmsCameraPoint.aC = location.getYaw();
	}

	@Override
	public void setCameraPosition(Vector position) {
		nmsCameraPoint.setPosition(position.getX(), position.getY() + level, position.getZ());
	}

	@Override
	public void setCameraRotation(double yaw, double pitch) {
		nmsCameraPoint.yaw = (float) yaw;
		nmsCameraPoint.aC = (float) yaw;
		nmsCameraPoint.pitch = (float) pitch;
	}

	@Override
	public Location getCurrentLocation() {
		return null;
	}

	@Override
	public void addViewer(Player player) {
		super.addViewer(player);

		cameraPoint.addPassenger(player);
	}

	@Override
	public void removeViewer(Player player) {
		super.removeViewer(player);

		cameraPoint.removePassenger(player);
	}

	@Override
	public boolean tick() {
		return super.tick() && !cameraPoint.isDead();
	}

	@Override
	public void move(float side, float forward, boolean jump, boolean sneak) {

	}

	@Override
	public void leftClick() {

	}

	@Override
	public void rightClick() {

	}

	@Override
	public void drop() {

	}

	@Override
	public void swap() {

	}

	@Override
	public void slot(int slot) {

	}
}
