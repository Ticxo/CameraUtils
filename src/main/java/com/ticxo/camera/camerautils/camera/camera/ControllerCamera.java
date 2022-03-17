package com.ticxo.camera.camerautils.camera.camera;

import com.mojang.datafixers.util.Pair;
import com.ticxo.camera.camerautils.CameraUtils;
import com.ticxo.camera.camerautils.input.IInputTracker;
import com.ticxo.camera.camerautils.input.WrapperInput;
import com.ticxo.camera.camerautils.utils.NMSTools;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_18_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class ControllerCamera extends AbstractCamera implements IInputTracker {

	protected static final TextColor gray = TextColor.color(120, 120, 120);
	protected static final TextColor white = TextColor.color(255, 255, 255);

	protected final double verticalOffset = -1.645;
	protected final AreaEffectCloud anchor;
	protected final Slime hitbox;

	@Getter
	protected Player cameraController;
	protected ServerPlayer nmsCameraController;
	protected boolean escaped;
	protected int escapeCooldown = 0;

	public ControllerCamera(Player controller, Location location) {

		ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();

		anchor = new AreaEffectCloud(world, location.getX(), location.getY() + verticalOffset, location.getZ());
		anchor.setRadius(0);
		anchor.setInvisible(true);
		anchor.setParticle(CraftParticle.toNMS(Particle.BLOCK_CRACK, Material.AIR.createBlockData()));

		hitbox = new Slime(EntityType.SLIME, world);
		hitbox.setInvisible(true);
		hitbox.setSize(3, false);
		hitbox.collides = false;

		ClientboundAddEntityPacket spawnAnchor = new ClientboundAddEntityPacket(anchor);
		ClientboundSetEntityDataPacket metaAnchor = new ClientboundSetEntityDataPacket(anchor.getId(), anchor.getEntityData(), true);

		ClientboundAddMobPacket spawnHitbox = new ClientboundAddMobPacket(hitbox);
		ClientboundSetEntityDataPacket metaHitbox = new ClientboundSetEntityDataPacket(hitbox.getId(), hitbox.getEntityData(), true);

		ClientboundSetPassengersPacket mount = new ClientboundSetPassengersPacket(new FriendlyByteBuf(null) {
			@Override
			public int[] readVarIntArray() {
				return new int[] { controller.getEntityId(), hitbox.getId() };
			}

			@Override
			public int readVarInt() {
				return anchor.getId();
			}
		});

		NMSTools.sendPackets(controller, spawnAnchor, metaAnchor, spawnHitbox, metaHitbox, mount);

		setCameraController(controller);
	}

	public void setCameraController(Player player) {
		if(cameraController != null) {
			CameraUtils.instance.getInputManager().unregisterInputTracker(cameraController);
		}
		CameraUtils.instance.getInputManager().registerInputTracker(player, this);
		cameraController = player;
		nmsCameraController = NMSTools.getNMSPlayer(player);
	}

	public void removeCameraController() {
		if(cameraController == null)
			return;
		CameraUtils.instance.getInputManager().unregisterInputTracker(cameraController);

		ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(anchor.getId(), hitbox.getId());
		NMSTools.sendPackets(cameraController, destroy);

		cameraController = null;
		nmsCameraController = null;
	}

	@Override
	public void setPlayerEscaped(boolean escaped) {
		this.escaped = escaped;
	}

	@Override
	public void asyncInputEvent(WrapperInput input) {

		cameraController.sendActionBar(Component.join(
				JoinConfiguration.separator(Component.text(" ")),
				Component.keybind("key.forward", input.getForward() > 0 ? white : gray),
				Component.keybind("key.left", input.getSide() > 0 ? white : gray),
				Component.keybind("key.back", input.getForward() < 0 ? white : gray),
				Component.keybind("key.right", input.getSide() < 0 ? white : gray),
				Component.keybind("key.jump", input.isJump() ? white : gray)
		));
	}

	@Override
	public void syncInputEvent(WrapperInput input) {

	}

	@Override
	public void asyncLeftClickEvent() {
		cameraController.sendMessage(Component.keybind("key.attack"));
	}

	@Override
	public void syncLeftClickEvent() {

	}

	@Override
	public void asyncRightClickEvent() {
		cameraController.sendMessage(Component.keybind("key.use"));
	}

	@Override
	public void syncRightClickEvent() {

	}

	@Override
	public void asyncDropItem() {
		cameraController.sendMessage(Component.keybind("key.drop"));
	}

	@Override
	public void syncDropItem() {

	}

	@Override
	public void asyncSwapItem() {
		cameraController.sendMessage(Component.keybind("key.swapOffhand"));
	}

	@Override
	public void syncSwapItem() {

	}

	@Override
	public void asyncSwitchHeldSlot(int slot) {
		cameraController.sendMessage(Component.keybind("key.hotbar." + (slot + 1)));
	}

	@Override
	public void syncSwitchHeldSlot(int slot) {

	}

	@Override
	public void addViewer(Player player) {
		super.addViewer(player);

		player.teleport(cameraController);

		NMSTools.hideHotbar(NMSTools.getNMSPlayer(player));

		ClientboundSetEquipmentPacket equipment = new ClientboundSetEquipmentPacket(player.getEntityId(), emptyEquipment);
		PacketPlayOutCamera camera = new PacketPlayOutCamera(nmsCameraController);

		NMSTools.sendPackets(player, equipment, camera);
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

		NMSTools.sendPackets(player, equipment, camera);
	}

	@Override
	public boolean tick() {

		if(isRunning()) {
			escapeCooldown = (escapeCooldown + 1) % 2;
			if(escaped && escapeCooldown == 0) {
				ClientboundSetPassengersPacket dismount = new ClientboundSetPassengersPacket(new FriendlyByteBuf(null) {
					@Override
					public int[] readVarIntArray() {
						return new int[] { hitbox.getId() };
					}

					@Override
					public int readVarInt() {
						return anchor.getId();
					}
				});
				ClientboundSetPassengersPacket mount = new ClientboundSetPassengersPacket(new FriendlyByteBuf(null) {
					@Override
					public int[] readVarIntArray() {
						return new int[] { cameraController.getEntityId(), hitbox.getId() };
					}

					@Override
					public int readVarInt() {
						return anchor.getId();
					}
				});
				NMSTools.sendPackets(cameraController, dismount, mount);
			}
		}

		return super.tick();
	}

	@Override
	public void setCameraLocation(Location location) {
		anchor.moveTo(location.getX(), location.getY() + verticalOffset, location.getZ(), 0, 0);
		nmsCameraController.setYHeadRot(location.getYaw());
		nmsCameraController.setYBodyRot(location.getYaw());
		nmsCameraController.setYRot(location.getYaw());
		nmsCameraController.setXRot(location.getPitch());

		updateServersidePosition();

		PacketPlayOutEntityTeleport teleportAnchor = new PacketPlayOutEntityTeleport(anchor);

		NMSTools.sendPackets(cameraController, teleportAnchor);
	}

	@Override
	public void setCameraPosition(Vector position) {
		anchor.setPos(position.getX(), position.getY() + verticalOffset, position.getZ());

		updateServersidePosition();

		PacketPlayOutEntityTeleport teleportAnchor = new PacketPlayOutEntityTeleport(anchor);

		NMSTools.sendPackets(cameraController, teleportAnchor);
	}

	@Override
	public void setCameraRotation(double yaw, double pitch) {
		nmsCameraController.setYHeadRot((float) yaw);
		nmsCameraController.setYBodyRot((float) yaw);
		nmsCameraController.setYRot((float) yaw);
		nmsCameraController.setXRot((float) pitch);

		Vector dir = cameraController.getLocation().getDirection();
		ClientboundPlayerLookAtPacket lookController = new ClientboundPlayerLookAtPacket(
				EntityAnchorArgument.Anchor.EYES,
				nmsCameraController.getX() + dir.getX() * 20,
				nmsCameraController.getY() + dir.getY() * 20,
				nmsCameraController.getZ() + dir.getZ() * 20
		);

		NMSTools.sendPackets(cameraController, lookController);
	}

	@Override
	public Location getCurrentLocation() {
		return new Location(cameraController.getWorld(), anchor.getX(), anchor.getY() - verticalOffset, anchor.getZ(), nmsCameraController.getYRot(), nmsCameraController.getXRot());
	}

	protected void updateServersidePosition() {
		Bukkit.getScheduler().runTask(CameraUtils.instance, () -> {
			Location currentLocation = getCurrentLocation();
			NMSTools.setLocation(cameraController, currentLocation);
			for(Player viewer : getViewers()) {
				NMSTools.setLocation(viewer, currentLocation);
			}
		});
	}

}
