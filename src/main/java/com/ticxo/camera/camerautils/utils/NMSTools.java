package com.ticxo.camera.camerautils.utils;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NMSTools {

	public static ServerPlayer getNMSPlayer(Player player) {
		return ((CraftPlayer) player).getHandle();
	}

	public static void hideHotbar(ServerPlayer player) {

		player.setGameMode(GameType.CREATIVE);
		ClientboundPlayerInfoPacket info = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE, player);
		player.setGameMode(GameType.SPECTATOR);
		player.connection.connection.send(info);

	}

	public static void sendPackets(Player player, Packet<?>... packets) {
		ServerPlayer nmsPlayer = getNMSPlayer(player);

		for(Packet<?> packet : packets) {
			nmsPlayer.connection.connection.send(packet);
		}
	}

	public static void setLocation(Entity entity, Location location) {
		net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		nmsEntity.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

}
