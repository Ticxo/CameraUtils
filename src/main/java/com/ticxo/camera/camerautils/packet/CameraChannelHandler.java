package com.ticxo.camera.camerautils.packet;

import com.ticxo.camera.camerautils.CameraUtils;
import com.ticxo.camera.camerautils.input.IInputTracker;
import com.ticxo.camera.camerautils.input.WrapperInput;
import com.ticxo.camera.camerautils.manager.InputManager;
import io.netty.channel.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CameraChannelHandler extends ChannelDuplexHandler {

	@Setter
	private static InputManager inputManager;

	@Getter
	private static final Map<Player, CameraChannelHandler> handlers = new ConcurrentHashMap<>();

	@Getter
	private final EntityPlayer player;

	private boolean isDroppingItem = false;

	public static void injectPlayer(Player player) {
		EntityPlayer ply = ((CraftPlayer) player).getHandle();
		CameraChannelHandler cdh = new CameraChannelHandler(ply);
		handlers.put(player, cdh);

		ChannelPipeline pipeline = ply.playerConnection.networkManager.channel.pipeline();
		for(String name : pipeline.toMap().keySet()) {
			if(pipeline.get(name) instanceof NetworkManager) {
				pipeline.addBefore(name, "camera_util_packet_handler", cdh);
				break;
			}
		}
	}

	public static void removePlayer(Player player) {
		Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
		channel.eventLoop().submit(() -> {
			handlers.remove(player);
			channel.pipeline().remove("camera_util_packet_handler");
			return null;
		});
	}

	public CameraChannelHandler(EntityPlayer player) {
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

			if (packet instanceof PacketPlayInSteerVehicle) {

				tracker.setPlayerEscaped(false);

				PacketPlayInSteerVehicle steer = (PacketPlayInSteerVehicle) packet;
				WrapperInput input = new WrapperInput(steer.c(), steer.b(), steer.d(), steer.e());
				tracker.asyncInputEvent(input.clone());
				Bukkit.getScheduler().runTask(CameraUtils.instance, () -> {
					tracker.syncInputEvent(input);
				});

				if(steer.e())
					tracker.setPlayerEscaped(true);

				packet = new PacketPlayInSteerVehicle() {

					@Override
					public float b() {
						return steer.b();
					}

					@Override
					public float c() {
						return steer.c();
					}

					@Override
					public boolean d() {
						return steer.d();
					}

					@Override
					public boolean e() {
						return false;
					}
				};

			}else if(packet instanceof PacketPlayInArmAnimation) {
				if(!isDroppingItem) {
					tracker.asyncLeftClickEvent();
					Bukkit.getScheduler().runTask(CameraUtils.instance, tracker::syncLeftClickEvent);
				}else {
					isDroppingItem = false;
				}
				return;
			}else if(packet instanceof PacketPlayInUseEntity) {
				PacketPlayInUseEntity useEntity = (PacketPlayInUseEntity) packet;

				if(useEntity.b() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT && useEntity.c() == EnumHand.MAIN_HAND) {
					tracker.asyncRightClickEvent();
					Bukkit.getScheduler().runTask(CameraUtils.instance, tracker::syncRightClickEvent);
				}
				return;
			}else if(packet instanceof PacketPlayInHeldItemSlot) {
				PacketPlayInHeldItemSlot heldItemSlot = (PacketPlayInHeldItemSlot) packet;

				tracker.asyncSwitchHeldSlot(heldItemSlot.b());
				Bukkit.getScheduler().runTask(CameraUtils.instance, () -> tracker.syncSwitchHeldSlot(heldItemSlot.b()));
				return;
			}else if(packet instanceof PacketPlayInBlockDig) {
				PacketPlayInBlockDig dig = (PacketPlayInBlockDig) packet;

				switch (dig.d()) {
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
			}else if(packet instanceof PacketPlayInSpectate) {
				return;
			}
		}

		super.channelRead(ctx, packet);
	}

}
