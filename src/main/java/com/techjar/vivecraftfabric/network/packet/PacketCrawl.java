package com.techjar.vivecraftfabric.network.packet;

import java.util.function.Supplier;

import com.techjar.vivecraftfabric.Config;
import com.techjar.vivecraftfabric.network.IPacket;
import com.techjar.vivecraftfabric.util.PlayerTracker;
import com.techjar.vivecraftfabric.util.VRPlayerData;
import net.minecraft.world.entity.Pose;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import dev.architectury.networking.NetworkManager;
import net.minecraft.world.entity.player.Player;

public class PacketCrawl implements IPacket {
	public boolean crawling;

	public PacketCrawl() {
	}

	public PacketCrawl(boolean crawling) {
		this.crawling = crawling;
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
	}

	@Override
	public void decode(FriendlyByteBuf buffer) {
		crawling = buffer.readBoolean();
	}

	@Override
	public void handleClient(Supplier<NetworkManager.PacketContext> context) {
	}

	@Override
	public void handleServer(Supplier<NetworkManager.PacketContext> context) {
		if (Config.crawlingEnabled.get()) {
			Player player = context.get().getPlayer();
			context.get().queue(() -> {
				if (!PlayerTracker.hasPlayerData(player))
					return;
				VRPlayerData data = PlayerTracker.getPlayerData(player, true);
				data.crawling = crawling;
				if (data.crawling)
					player.setPose(Pose.SWIMMING);
			});
		}
	}
}
