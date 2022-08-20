package com.techjar.vivecraftfabric.network;

import com.techjar.vivecraftfabric.NetworkChannelVivecraft;
import com.techjar.vivecraftfabric.util.LogHelper;

import com.techjar.vivecraftfabric.network.packet.*;
import dev.architectury.networking.NetworkChannel;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;

public class ChannelHandler {
	private static NetworkChannelVivecraft CHANNEL;

	public static void init() {
		CHANNEL = NetworkChannelVivecraft.create(new ResourceLocation("vivecraft", "data"));

		addDiscriminator((byte) 0, new Message<>(PacketVersion.class));
		addDiscriminator((byte) 1, new Message<>(PacketRequestData.class));
		addDiscriminator((byte) 2, new Message<>(PacketHeadData.class));
		addDiscriminator((byte) 3, new Message<>(PacketController0Data.class));
		addDiscriminator((byte) 4, new Message<>(PacketController1Data.class));
		addDiscriminator((byte) 5, new Message<>(PacketWorldScale.class));
		addDiscriminator((byte) 6, new Message<>(PacketDraw.class));
		addDiscriminator((byte) 7, new Message<>(PacketMoveMode.class));
		addDiscriminator((byte) 8, new Message<>(PacketUberPacket.class));
		addDiscriminator((byte) 9, new Message<>(PacketTeleport.class));
		addDiscriminator((byte) 10, new Message<>(PacketClimbing.class));
		addDiscriminator((byte) 11, new Message<>(PacketSettingOverride.class));
		addDiscriminator((byte) 12, new Message<>(PacketHeight.class));
		addDiscriminator((byte) 13, new Message<>(PacketActiveHand.class));
		addDiscriminator((byte) 14, new Message<>(PacketCrawl.class));

		LogHelper.debug("Networking initialized");
	}

	private static <T extends IPacket> void addDiscriminator(byte d, Message<T> message) {
		CHANNEL.register(d, message.getPacketClass(), message::encode, message::decode, message::handle);
	}

	public static void sendTo(IPacket message, ServerPlayer player) {
		CHANNEL.sendToPlayer(player, message);
	}

	public static void sendToAllTrackingEntity(IPacket message, List<ServerPlayer> player) {
		CHANNEL.sendToPlayers(player, message);
	}
}
