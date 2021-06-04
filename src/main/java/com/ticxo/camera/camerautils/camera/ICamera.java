package com.ticxo.camera.camerautils.camera;

import com.ticxo.camera.camerautils.utils.WrappedRotation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public interface ICamera extends ICameraTickable {

	void addViewer(Player player);
	void removeViewer(Player player);
	List<Player> getViewers();

	void setCameraLocation(Location location);
	void setCameraPosition(Vector position);
	void setCameraRotation(float yaw, float pitch);

	void addCameraTickable(ICameraTickable tickable);
	void setRunning(boolean running);
	boolean isRunning();

	Location getCurrentLocation();

	static Location lerp(Location pointA, Location pointB, double ratio) {
		Location loc = pointB.clone().subtract(pointA).multiply(ratio).add(pointA);
		return loc;
	}

	static Location serp(double alpha, Location a, Location b, Location c, Location d, double ratio) {
		double t0 = 0;
		double t1 = getT(alpha, t0, a, b);
		double t2 = getT(alpha, t1, b, c);
		double t3 = getT(alpha, t2, c, d);
		double t = (t2 - t1) * ratio + t1;

		Location a1 = a.clone().multiply((t1 - t) / (t1 - t0)).add(b.clone().multiply((t - t0) / (t1 - t0)));
		Location a2 = b.clone().multiply((t2 - t) / (t2 - t1)).add(c.clone().multiply((t - t1) / (t2 - t1)));
		Location a3 = c.clone().multiply((t3 - t) / (t3 - t2)).add(d.clone().multiply((t - t2) / (t3 - t2)));

		Location b1 = a1.clone().multiply((t2 - t) / (t2 - t0)).add(a2.clone().multiply((t - t0) / (t2 - t0)));
		Location b2 = a2.clone().multiply((t3 - t) / (t3 - t1)).add(a3.clone().multiply((t - t1) / (t3 - t1)));

		Location loc = b1.multiply((t2 - t) / (t2 - t1)).add(b2.multiply((t - t1) / (t2 - t1)));

		return loc;
	}

	static WrappedRotation rotLerp(WrappedRotation rotA, WrappedRotation rotB, double ratio) {

		WrappedRotation rot = new WrappedRotation();

		rot.setPitch((float) ((rotB.getPitch() - rotA.getPitch()) * ratio + rotA.getPitch()));
		rot.setYaw((float) ((((rotB.getYaw() - rotA.getYaw()) % 360 + 540) % 360 - 180) * ratio + rotA.getYaw()));

		return rot;
	}

	static Location face(Location origin, Location location) {
		Vector delta = location.toVector().subtract(origin.toVector());
		float yaw = (float) Math.toDegrees(Math.atan2(delta.getZ(), delta.getX()));

		double lSq = Math.sqrt(delta.getX() * delta.getX() + delta.getZ() * delta.getZ());
		float pitch = (float) Math.toDegrees(Math.atan2(lSq, delta.getY()));

		origin.setYaw(yaw - 90);
		origin.setPitch(pitch - 90);

		System.out.println(pitch);

		return origin;
	}

	static boolean isSimilar(Location locationA, Location locationB, double maxError, double maxRotError) {

		boolean dis = locationA.distanceSquared(locationB) < maxError * maxError;
		boolean yaw = Math.abs(((locationB.getYaw() - locationA.getYaw()) % 360 + 540) % 360 - 180) < maxRotError;
		boolean pitch = Math.abs(locationB.getPitch() - locationA.getPitch()) < maxRotError;

		return dis && yaw && pitch;
	}

	static double getT(double alpha, double t, Location a, Location b)
	{
		double aa = Math.pow(b.getX() - a.getX(), 2) + Math.pow(b.getY() - a.getY(), 2) + Math.pow(b.getZ() - a.getZ(), 2);
		double bb = Math.pow(aa, alpha * 0.5);

		return bb + t;
	}

}
