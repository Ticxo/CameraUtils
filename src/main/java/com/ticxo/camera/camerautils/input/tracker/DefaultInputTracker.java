package com.ticxo.camera.camerautils.input.tracker;

import com.ticxo.camera.camerautils.CameraUtils;
import com.ticxo.camera.camerautils.input.WrapperInput;
import com.ticxo.camera.camerautils.utils.NMSTools;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_18_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.entity.Player;

public class DefaultInputTracker extends BaseInputTracker {

	protected static final TextColor gray = TextColor.color(120, 120, 120);
	protected static final TextColor white = TextColor.color(255, 255, 255);

	@Getter
	protected final Player player;
	protected final AreaEffectCloud tracker;
	protected final Slime hitbox;

	public DefaultInputTracker(Player player, Location location) {
		this.player = player;

		ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();

		tracker = new AreaEffectCloud(world, location.getX(), location.getY() - 1.645, location.getZ());
		tracker.setRadius(0);
		tracker.setRadiusOnUse(0);
		tracker.setRadiusPerTick(0);
		tracker.setDuration(10000000);
		tracker.setInvisible(true);
		tracker.setParticle(CraftParticle.toNMS(Particle.BLOCK_CRACK, Material.AIR.createBlockData()));

		hitbox = new Slime(EntityType.SLIME, world);
		hitbox.setInvisible(true);
		hitbox.setInvulnerable(true);
		hitbox.setSilent(true);
		hitbox.setNoAi(true);
		hitbox.setSize(3, false);
		hitbox.collides = false;

		ClientboundAddEntityPacket spawnAnchor = new ClientboundAddEntityPacket(tracker);
		ClientboundSetEntityDataPacket metaAnchor = new ClientboundSetEntityDataPacket(tracker.getId(), tracker.getEntityData(), true);

		ClientboundAddMobPacket spawnHitbox = new ClientboundAddMobPacket(hitbox);
		ClientboundSetEntityDataPacket metaHitbox = new ClientboundSetEntityDataPacket(hitbox.getId(), hitbox.getEntityData(), true);

		ClientboundSetPassengersPacket mount = new ClientboundSetPassengersPacket(new FriendlyByteBuf(null) {
			@Override
			public int[] readVarIntArray() {
				return new int[] { player.getEntityId(), hitbox.getId() };
			}

			@Override
			public int readVarInt() {
				return tracker.getId();
			}
		});

		NMSTools.sendPackets(player, spawnAnchor, metaAnchor, spawnHitbox, metaHitbox, mount);

	}

	@Override
	public void asyncInputEvent(WrapperInput input) {

		if(input.isSneak()) {
			player.sendActionBar(Component.text(" "));
			return;
		}

		player.sendActionBar(Component.join(
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
		if(!input.isSneak())
			return;

		CameraUtils.instance.getInputManager().unregisterInputTracker(player);
		ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(tracker.getId(), hitbox.getId());
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
