package com.ticxo.camera.camerautils.utils.location;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

@Getter @Setter
public class SphericalLocation implements WrappedLocation {

	private final World world;

	private float yaw;
	private float pitch;
	private double distance;

	public SphericalLocation(World world) {
		this.world = world;
	}

	public SphericalLocation(World world, float yaw, float pitch, double distance) {
		this.world = world;
		this.yaw = yaw;
		this.pitch = pitch;
		this.distance = distance;
	}

	@Override
	public Location getLocation() {

		Vector vec = new Vector(0, 0, -distance);

		rotatePitch(vec, pitch);
		rotateYaw(vec, yaw);

		return vec.toLocation(world);
	}

	private static void rotatePitch(Vector vec, double pitch) {

		double sin = Math.sin(pitch);
		double cos = Math.cos(pitch);
		double y = vec.getY() * cos - vec.getZ() * sin;
		double z = vec.getY() * sin + vec.getZ() * cos;

		vec.setY(y).setZ(z);

	}

	private static void rotateYaw(Vector vec, double yaw) {

		double sin = Math.sin(yaw);
		double cos = Math.cos(yaw);
		double x = vec.getX() * cos - vec.getZ() * sin;
		double z = vec.getX() * sin + vec.getZ() * cos;

		vec.setX(x).setZ(z);

	}

}
