package com.techjar.vivecraftfabric.network.packet;

import java.util.function.Supplier;

import com.techjar.vivecraftfabric.network.IPacket;
import com.techjar.vivecraftfabric.util.PlayerTracker;
import com.techjar.vivecraftfabric.util.VRPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import dev.architectury.networking.NetworkManager;
import net.minecraft.world.entity.player.Player;

public class PacketActiveHand implements IPacket {
	public int activeHand;

	public PacketActiveHand() {
	}

	public PacketActiveHand(int activeHand) {
		this.activeHand = activeHand;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeByte(activeHand);
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
		activeHand = buffer.readByte();
	}

	@Override
	public void handleClient(final Supplier<NetworkManager.PacketContext> context) {
	}

	@Override
	public void handleServer(final Supplier<NetworkManager.PacketContext> context) {
		Player player = context.get().getPlayer();
		context.get().queue(() -> {
			if (!PlayerTracker.hasPlayerData(player))
				return;
			VRPlayerData data = PlayerTracker.getPlayerData(player, true);
			data.activeHand = activeHand;
		});
	}
}
