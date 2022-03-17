package com.ticxo.camera.camerautils.packet;

import com.ticxo.camera.camerautils.CameraUtils;
import com.ticxo.camera.camerautils.input.IInputTracker;
import com.ticxo.camera.camerautils.input.WrapperInput;
import com.ticxo.camera.camerautils.manager.InputManager;
import io.netty.channel.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CameraChannelHandler extends ChannelDuplexHandler {

	@Setter
	private static InputManager inputManager;

	@Getter
	private static final Map<Player, CameraChannelHandler> handlers = new ConcurrentHashMap<>();

	@Getter
	private final ServerPlayer player;

	private boolean isDroppingItem = false;

	public static void injectPlayer(Player player) {
		ServerPlayer ply = ((CraftPlayer) player).getHandle();
		CameraChannelHandler cdh = new CameraChannelHandler(ply);
		handlers.put(player, cdh);

		ChannelPipeline pipeline = ply.connection.connection.channel.pipeline();
		for(String name : pipeline.toMap().keySet()) {
			if(pipeline.get(name) instanceof Connection) {
				pipeline.addBefore(name, "camera_util_packet_handler", cdh);
				break;
			}
		}
	}

	public static void removePlayer(Player player) {
		Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
		channel.eventLoop().submit(() -> {
			handlers.remove(player);
			channel.pipeline().remove("camera_util_packet_handler");
			return null;
		});
	}

	public CameraChannelHandler(ServerPlayer player) {
		this.player = player;
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
		super.write(ctx, packet, promise);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {

		if(inputManager.hasInputTracker(player.getBukkitEntity())) {

			IInputTracker tracker = inputManager.getInputTracker(player.getBukkitEntity());

			if (packet instanceof ServerboundPlayerInputPacket) {
				ServerboundPlayerInputPacket steer = (ServerboundPlayerInputPacket) packet;
				tracker.setPlayerEscaped(steer.isShiftKeyDown());

				WrapperInput input = new WrapperInput(steer.getZza(), steer.getXxa(), steer.isJumping(), steer.isShiftKeyDown());
				tracker.asyncInputEvent(input.clone());
				Bukkit.getScheduler().runTask(CameraUtils.instance, () -> {
					tracker.syncInputEvent(input);
				});

				return;
			}else if(packet instanceof ServerboundSwingPacket) {
				if(!isDroppingItem) {
					tracker.asyncLeftClickEvent();
					Bukkit.getScheduler().runTask(CameraUtils.instance, tracker::syncLeftClickEvent);
				}else {
					isDroppingItem = false;
				}

				return;
			}else if(packet instanceof ServerboundInteractPacket) {
				ServerboundInteractPacket useEntity = (ServerboundInteractPacket) packet;

				useEntity.dispatch(new ServerboundInteractPacket.Handler() {
					@Override
					public void onInteraction(InteractionHand interactionHand) {
						if(interactionHand != InteractionHand.MAIN_HAND)
							return;
						tracker.asyncRightClickEvent();
						Bukkit.getScheduler().runTask(CameraUtils.instance, tracker::syncRightClickEvent);
					}

					@Override
					public void onInteraction(InteractionHand interactionHand, Vec3 vec3) {

					}

					@Override
					public void onAttack() {

					}
				});

				return;
			}else if(packet instanceof ServerboundSetCarriedItemPacket) {
				ServerboundSetCarriedItemPacket heldItemSlot = (ServerboundSetCarriedItemPacket) packet;

				tracker.asyncSwitchHeldSlot(heldItemSlot.getSlot());
				Bukkit.getScheduler().runTask(CameraUtils.instance, () -> tracker.syncSwitchHeldSlot(heldItemSlot.getSlot()));

				return;
			}else if(packet instanceof ServerboundPlayerActionPacket) {
				ServerboundPlayerActionPacket dig = (ServerboundPlayerActionPacket) packet;

				switch (dig.getAction()) {
					case DROP_ITEM:
						isDroppingItem = true;
						tracker.asyncDropItem();
						Bukkit.getScheduler().runTask(CameraUtils.instance, tracker::syncDropItem);
						break;
					case SWAP_ITEM_WITH_OFFHAND:
						tracker.asyncSwapItem();
						Bukkit.getScheduler().runTask(CameraUtils.instance, tracker::syncSwapItem);
						break;
				}

				return;
			}else if(packet instanceof ServerboundTeleportToEntityPacket) {
				return;
			}
		}

		super.channelRead(ctx, packet);
	}

}
