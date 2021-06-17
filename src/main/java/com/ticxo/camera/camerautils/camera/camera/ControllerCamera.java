package com.ticxo.camera.camerautils.camera.camera;

import com.mojang.datafixers.util.Pair;
import com.ticxo.camera.camerautils.CameraUtils;
import com.ticxo.camera.camerautils.input.IInputTracker;
import com.ticxo.camera.camerautils.input.WrapperInput;
import com.ticxo.camera.camerautils.utils.NMSTools;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_16_R3.CraftParticle;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ControllerCamera extends AbstractCamera implements IInputTracker {

	protected static final TextColor gray = TextColor.color(120, 120, 120);
	protected static final TextColor white = TextColor.color(255, 255, 255);

	protected final double verticalOffset = -1.645;
	protected final EntityAreaEffectCloud anchor;
	protected final EntitySlime hitbox;

	@Getter
	protected Player cameraController;
	protected EntityPlayer nmsCameraController;
	protected boolean escaped;
	protected int escapeCooldown = 0;

	public ControllerCamera(Player controller, Location location) {

		World world = ((CraftWorld) location.getWorld()).getHandle();

		anchor = new EntityAreaEffectCloud(world, location.getX(), location.getY() + verticalOffset, location.getZ());
		anchor.setRadius(0);
		anchor.setInvisible(true);
		anchor.setParticle(CraftParticle.toNMS(Particle.BLOCK_CRACK, Material.AIR.createBlockData()));

		hitbox = new EntitySlime(EntityTypes.SLIME, world);
		hitbox.setInvisible(true);
		hitbox.setSize(3, false);
		hitbox.collides = false;

		PacketPlayOutSpawnEntity spawnAnchor = new PacketPlayOutSpawnEntity(anchor);
		PacketPlayOutEntityMetadata metaAnchor = new PacketPlayOutEntityMetadata(anchor.getId(), anchor.getDataWatcher(), true);

		PacketPlayOutSpawnEntityLiving spawnHitbox = new PacketPlayOutSpawnEntityLiving(hitbox);
		PacketPlayOutEntityMetadata metaHitbox = new PacketPlayOutEntityMetadata(hitbox.getId(), hitbox.getDataWatcher(), true);

		PacketPlayOutMount mount = new PacketPlayOutMount(anchor);

		try {
			mount.a(new PacketDataSerializer(null) {
				@Override
				public int[] b() {
					return new int[] { controller.getEntityId(), hitbox.getId() };
				}

				@Override
				public int i() {
					return anchor.getId();
				}
			});
		} catch (IOException ignore){}

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

		PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(anchor.getId(), hitbox.getId());
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
				Component.text(" "),
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

		PacketPlayOutEntityEquipment equipment = new PacketPlayOutEntityEquipment(player.getEntityId(), emptyEquipment);
		PacketPlayOutCamera camera = new PacketPlayOutCamera(nmsCameraController);

		NMSTools.sendPackets(player, equipment, camera);
	}

	@Override
	public void removeViewer(Player player) {
		super.removeViewer(player);

		EntityPlayer nmsPlayer = NMSTools.getNMSPlayer(player);

		List<Pair<EnumItemSlot, ItemStack>> eq = Arrays.asList(
				new Pair<>(EnumItemSlot.CHEST, nmsPlayer.getEquipment(EnumItemSlot.CHEST)),
				new Pair<>(EnumItemSlot.FEET, nmsPlayer.getEquipment(EnumItemSlot.FEET)),
				new Pair<>(EnumItemSlot.HEAD, nmsPlayer.getEquipment(EnumItemSlot.HEAD)),
				new Pair<>(EnumItemSlot.LEGS, nmsPlayer.getEquipment(EnumItemSlot.LEGS)),
				new Pair<>(EnumItemSlot.MAINHAND, nmsPlayer.getEquipment(EnumItemSlot.MAINHAND)),
				new Pair<>(EnumItemSlot.OFFHAND, nmsPlayer.getEquipment(EnumItemSlot.OFFHAND))
		);
		PacketPlayOutEntityEquipment equipment = new PacketPlayOutEntityEquipment(player.getEntityId(), eq);
		PacketPlayOutCamera camera = new PacketPlayOutCamera(NMSTools.getNMSPlayer(player));

		NMSTools.sendPackets(player, equipment, camera);
	}

	@Override
	public boolean tick() {

		if(isRunning()) {
			escapeCooldown = (escapeCooldown + 1) % 2;
			if(escaped && escapeCooldown == 0) {
				PacketPlayOutMount dismount = new PacketPlayOutMount(anchor);
				PacketPlayOutMount mount = new PacketPlayOutMount(anchor);
				try {
					dismount.a(new PacketDataSerializer(null) {
						@Override
						public int[] b() {
							return new int[] { hitbox.getId() };
						}

						@Override
						public int i() {
							return anchor.getId();
						}
					});
					mount.a(new PacketDataSerializer(null) {
						@Override
						public int[] b() {
							return new int[] { cameraController.getEntityId(), hitbox.getId() };
						}

						@Override
						public int i() {
							return anchor.getId();
						}
					});
					NMSTools.sendPackets(cameraController, dismount, mount);
				} catch (IOException ignore){}
			}
		}

		return super.tick();
	}

	@Override
	public void setCameraLocation(Location location) {
		anchor.setLocation(location.getX(), location.getY() + verticalOffset, location.getZ(), 0, 0);
		nmsCameraController.yaw = location.getYaw();
		nmsCameraController.aC = location.getYaw();
		nmsCameraController.pitch = location.getPitch();

		updateServersidePosition();

		PacketPlayOutEntityTeleport teleportAnchor = new PacketPlayOutEntityTeleport(anchor);

		NMSTools.sendPackets(cameraController, teleportAnchor);
	}

	@Override
	public void setCameraPosition(Vector position) {
		anchor.setPosition(position.getX(), position.getY() + verticalOffset, position.getZ());

		updateServersidePosition();

		PacketPlayOutEntityTeleport teleportAnchor = new PacketPlayOutEntityTeleport(anchor);

		NMSTools.sendPackets(cameraController, teleportAnchor);
	}

	@Override
	public void setCameraRotation(double yaw, double pitch) {
		nmsCameraController.yaw = (float) yaw;
		nmsCameraController.aC = (float) yaw;
		nmsCameraController.pitch = (float) pitch;

		Vector dir = cameraController.getLocation().getDirection();
		PacketPlayOutLookAt lookController = new PacketPlayOutLookAt(
				ArgumentAnchor.Anchor.EYES,
				nmsCameraController.locX() + dir.getX() * 20,
				nmsCameraController.locY() + dir.getY() * 20,
				nmsCameraController.locZ() + dir.getZ() * 20
		);

		NMSTools.sendPackets(cameraController, lookController);
	}

	@Override
	public Location getCurrentLocation() {
		return new Location(cameraController.getWorld(), anchor.locX(), anchor.locY() - verticalOffset, anchor.locZ(), nmsCameraController.yaw, nmsCameraController.pitch);
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
