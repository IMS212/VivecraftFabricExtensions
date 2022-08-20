package com.techjar.vivecraftfabric.network.packet;

import java.util.function.Supplier;

import com.techjar.vivecraftfabric.network.IPacket;
import com.techjar.vivecraftfabric.util.PlayerTracker;
import com.techjar.vivecraftfabric.util.VRPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import dev.architectury.networking.NetworkManager;
import net.minecraft.world.entity.player.Player;

public class PacketDraw implements IPacket {
	public float drawDist;

	public PacketDraw() {
	}

	public PacketDraw(float drawDist) {
		this.drawDist = drawDist;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeFloat(drawDist);
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
		drawDist = buffer.readFloat();
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
			data.bowDraw = drawDist;
		});
	}
}
