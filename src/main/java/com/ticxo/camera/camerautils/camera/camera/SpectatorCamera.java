package com.ticxo.camera.camerautils.camera.camera;

import com.mojang.datafixers.util.Pair;
import com.ticxo.camera.camerautils.utils.NMSTools;
import lombok.Getter;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class SpectatorCamera extends AbstractCamera {

	protected final EntityVillager entity;
	@Getter
	protected final double cameraHeight;

	public SpectatorCamera(Location location) {
		// Packet villager
		World world = ((CraftWorld) location.getWorld()).getHandle();

		entity = new EntityVillager(EntityTypes.VILLAGER, world);
		entity.setLocation(location.getX(), location.getY() - entity.getHeadHeight(), location.getZ(), location.getYaw(), location.getPitch());
		entity.aC = location.getYaw();
		entity.setInvisible(true);
		entity.setBaby(false);
		entity.setSilent(true);
		entity.setNoAI(true);

		cameraHeight = entity.getHeadHeight();
	}

	@Override
	public void addViewer(Player player) {
		super.addViewer(player);

		player.teleport(entity.getBukkitEntity());

		NMSTools.hideHotbar(NMSTools.getNMSPlayer(player));

		PacketPlayOutEntityEquipment equipment = new PacketPlayOutEntityEquipment(player.getEntityId(), emptyEquipment);
		PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(entity);
		PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true);
		PacketPlayOutCamera camera = new PacketPlayOutCamera(entity);

		NMSTools.sendPackets(player, equipment, spawn, metadata, camera);

	}

	@Override
	public void removeViewer(Player player) {
		super.removeViewer(player);

		EntityPlayer nmsPlayer = NMSTools.getNMSPlayer(player);

		List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> eq = Arrays.asList(
				new Pair<>(EnumItemSlot.CHEST, nmsPlayer.getEquipment(EnumItemSlot.CHEST)),
				new Pair<>(EnumItemSlot.FEET, nmsPlayer.getEquipment(EnumItemSlot.FEET)),
				new Pair<>(EnumItemSlot.HEAD, nmsPlayer.getEquipment(EnumItemSlot.HEAD)),
				new Pair<>(EnumItemSlot.LEGS, nmsPlayer.getEquipment(EnumItemSlot.LEGS)),
				new Pair<>(EnumItemSlot.MAINHAND, nmsPlayer.getEquipment(EnumItemSlot.MAINHAND)),
				new Pair<>(EnumItemSlot.OFFHAND, nmsPlayer.getEquipment(EnumItemSlot.OFFHAND))
		);
		PacketPlayOutEntityEquipment equipment = new PacketPlayOutEntityEquipment(player.getEntityId(), eq);
		PacketPlayOutCamera camera = new PacketPlayOutCamera(NMSTools.getNMSPlayer(player));
		PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entity.getId());

		NMSTools.sendPackets(player, equipment, camera, destroy);
	}

	@Override
	public boolean tick() {
		boolean r = super.tick();
		updateLocation();

		return r;
	}

	@Override
	public void setCameraLocation(Location location) {

		entity.setLocation(location.getX(), location.getY() - cameraHeight, location.getZ(), location.getYaw(), location.getPitch());
		entity.aC = location.getYaw();

	}

	@Override
	public void setCameraPosition(Vector position) {

		entity.setPosition(position.getX(), position.getY() - cameraHeight, position.getZ());

	}

	@Override
	public void setCameraRotation(double yaw, double pitch) {

		entity.yaw = (float) yaw;
		entity.aC = (float) yaw;
		entity.pitch = (float) pitch;

	}

	@Override
	public Location getCurrentLocation() {
		return entity.getBukkitLivingEntity().getLocation().add(0, cameraHeight, 0);
	}

	protected void updateLocation() {
		PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(entity);
		PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(entity, (byte) ((int)(entity.yaw * 256f / 360f)));

		for(Player player : getViewers()) {
			NMSTools.sendPackets(player, teleport, rotation);
		}
	}

}
