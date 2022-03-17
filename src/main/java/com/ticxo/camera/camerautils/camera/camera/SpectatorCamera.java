package com.ticxo.camera.camerautils.camera.camera;

import com.mojang.datafixers.util.Pair;
import com.ticxo.camera.camerautils.utils.NMSTools;
import lombok.Getter;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class SpectatorCamera extends AbstractCamera {

	protected final Villager entity;
	@Getter
	protected final double cameraHeight;

	public SpectatorCamera(Location location) {
		// Packet villager
		ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();

		entity = new Villager(EntityType.VILLAGER, world);
		entity.moveTo(location.getX(), location.getY() - entity.getEyeHeight(), location.getZ(), location.getYaw(), location.getPitch());
		entity.setYHeadRot(location.getYaw());
		entity.setYBodyRot(location.getYaw());
		entity.setInvisible(true);
		entity.setBaby(false);
		entity.setSilent(true);
		entity.setNoAi(true);

		cameraHeight = entity.getEyeHeight();
	}

	@Override
	public void addViewer(Player player) {
		super.addViewer(player);

		player.teleport(entity.getBukkitEntity());

		NMSTools.hideHotbar(NMSTools.getNMSPlayer(player));

		ClientboundSetEquipmentPacket equipment = new ClientboundSetEquipmentPacket(player.getEntityId(), emptyEquipment);
		ClientboundAddMobPacket spawn = new ClientboundAddMobPacket(entity);
		ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData(), true);
		ClientboundTeleportEntityPacket camera = new ClientboundTeleportEntityPacket(entity);

		NMSTools.sendPackets(player, equipment, spawn, metadata, camera);

	}

	@Override
	public void removeViewer(Player player) {
		super.removeViewer(player);

		ServerPlayer nmsPlayer = NMSTools.getNMSPlayer(player);

		List<Pair<EquipmentSlot, ItemStack>> eq = Arrays.asList(
				new Pair<>(EquipmentSlot.CHEST, nmsPlayer.getItemBySlot(EquipmentSlot.CHEST)),
				new Pair<>(EquipmentSlot.FEET, nmsPlayer.getItemBySlot(EquipmentSlot.FEET)),
				new Pair<>(EquipmentSlot.HEAD, nmsPlayer.getItemBySlot(EquipmentSlot.HEAD)),
				new Pair<>(EquipmentSlot.LEGS, nmsPlayer.getItemBySlot(EquipmentSlot.LEGS)),
				new Pair<>(EquipmentSlot.MAINHAND, nmsPlayer.getItemBySlot(EquipmentSlot.MAINHAND)),
				new Pair<>(EquipmentSlot.OFFHAND, nmsPlayer.getItemBySlot(EquipmentSlot.OFFHAND))
		);
		ClientboundSetEquipmentPacket equipment = new ClientboundSetEquipmentPacket(player.getEntityId(), eq);
		ClientboundTeleportEntityPacket camera = new ClientboundTeleportEntityPacket(NMSTools.getNMSPlayer(player));
		ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(entity.getId());

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

		entity.moveTo(location.getX(), location.getY() - cameraHeight, location.getZ(), location.getYaw(), location.getPitch());
		entity.setYHeadRot(location.getYaw());
		entity.setYBodyRot(location.getYaw());

	}

	@Override
	public void setCameraPosition(Vector position) {

		entity.setPos(position.getX(), position.getY() - cameraHeight, position.getZ());

	}

	@Override
	public void setCameraRotation(double yaw, double pitch) {

		entity.setYHeadRot((float) yaw);
		entity.setYBodyRot((float) yaw);
		entity.setYRot((float) yaw);
		entity.setXRot((float) pitch);

	}

	@Override
	public Location getCurrentLocation() {
		return entity.getBukkitEntity().getLocation().add(0, cameraHeight, 0);
	}

	protected void updateLocation() {
		PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(entity);
		PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(entity, (byte) ((int)(entity.getYRot() * 256f / 360f)));

		for(Player player : getViewers()) {
			NMSTools.sendPackets(player, teleport, rotation);
		}
	}

}
