package com.ticxo.camera.camerautils.input.tracker;

import com.ticxo.camera.camerautils.CameraUtils;
import com.ticxo.camera.camerautils.input.WrapperInput;
import com.ticxo.camera.camerautils.utils.NMSTools;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_16_R3.CraftParticle;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.io.IOException;

public class DefaultInputTracker extends BaseInputTracker {

	protected static final TextColor gray = TextColor.color(120, 120, 120);
	protected static final TextColor white = TextColor.color(255, 255, 255);

	@Getter
	protected final Player player;
	protected final EntityAreaEffectCloud tracker;
	protected final EntitySlime hitbox;

	public DefaultInputTracker(Player player, Location location) {
		this.player = player;

		World world = ((CraftWorld) location.getWorld()).getHandle();

		tracker = new EntityAreaEffectCloud(world, location.getX(), location.getY() - 1.645, location.getZ());
		tracker.setRadius(0);
		tracker.setRadiusOnUse(0);
		tracker.setRadiusPerTick(0);
		tracker.setDuration(10000000);
		tracker.setInvisible(true);
		tracker.setParticle(CraftParticle.toNMS(Particle.BLOCK_CRACK, Material.AIR.createBlockData()));

		hitbox = new EntitySlime(EntityTypes.SLIME, world);
		hitbox.setInvisible(true);
		hitbox.setInvulnerable(true);
		hitbox.setSilent(true);
		hitbox.setNoAI(true);
		hitbox.setSize(3, false);
		hitbox.collides = false;

		PacketPlayOutSpawnEntity spawnAnchor = new PacketPlayOutSpawnEntity(tracker);
		PacketPlayOutEntityMetadata metaAnchor = new PacketPlayOutEntityMetadata(tracker.getId(), tracker.getDataWatcher(), true);

		PacketPlayOutSpawnEntityLiving spawnHitbox = new PacketPlayOutSpawnEntityLiving(hitbox);
		PacketPlayOutEntityMetadata metaHitbox = new PacketPlayOutEntityMetadata(hitbox.getId(), hitbox.getDataWatcher(), true);

		PacketPlayOutMount mount = new PacketPlayOutMount(tracker);

		try {
			mount.a(new PacketDataSerializer(null) {
				@Override
				public int[] b() {
					return new int[] { player.getEntityId(), hitbox.getId() };
				}

				@Override
				public int i() {
					return tracker.getId();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		NMSTools.sendPackets(player, spawnAnchor, metaAnchor, spawnHitbox, metaHitbox, mount);

	}

	@Override
	public void asyncInputEvent(WrapperInput input) {

		if(input.isSneak()) {
			player.sendActionBar(Component.text(" "));
			return;
		}

		player.sendActionBar(Component.join(
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
		if(!input.isSneak())
			return;

		CameraUtils.instance.getInputManager().unregisterInputTracker(player);
		PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(tracker.getId(), hitbox.getId());
		NMSTools.sendPackets(player, destroy);
	}

	@Override
	public void asyncLeftClickEvent() {
		player.sendMessage(Component.keybind("key.attack"));
	}

	@Override
	public void syncLeftClickEvent() {
		super.syncLeftClickEvent();
	}

	@Override
	public void asyncRightClickEvent() {
		player.sendMessage(Component.keybind("key.use"));
	}

	@Override
	public void syncRightClickEvent() {
		super.syncRightClickEvent();
	}

	@Override
	public void asyncDropItem() {
		player.sendMessage(Component.keybind("key.drop"));
	}

	@Override
	public void syncDropItem() {
		super.syncDropItem();
	}

	@Override
	public void asyncSwapItem() {
		player.sendMessage(Component.keybind("key.swapOffhand"));
	}

	@Override
	public void syncSwapItem() {
		super.syncSwapItem();
	}

	@Override
	public void asyncSwitchHeldSlot(int slot) {
		player.sendMessage(Component.keybind("key.hotbar." + (slot + 1)));
	}

	@Override
	public void syncSwitchHeldSlot(int slot) {
		super.syncSwitchHeldSlot(slot);
	}
}
