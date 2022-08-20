package com.techjar.vivecraftfabric.network.packet;

import com.techjar.vivecraftfabric.Config;
import com.techjar.vivecraftfabric.network.IPacket;
import com.techjar.vivecraftfabric.util.BlockListMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import dev.architectury.networking.NetworkManager;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Supplier;

/*
 * For whatever reason this uses a serializer instead of just
 * packing the data into the buffer.
 */
public class PacketClimbing implements IPacket {
	public BlockListMode blockListMode;
	public List<? extends String> blockList;

	public PacketClimbing() {
	}

	public PacketClimbing(BlockListMode blockListMode, List<? extends String> blockList) {
		this.blockListMode = blockListMode;
		this.blockList = blockList;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeByte(1); // allow climbey
		buffer.writeByte(Config.blockListMode.get().ordinal());
		for (String s : Config.blockList.get()) {
			buffer.writeUtf(s);
		}
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
	}

	@Override
	public void handleClient(final Supplier<NetworkManager.PacketContext> context) {
	}

	@Override
	public void handleServer(final Supplier<NetworkManager.PacketContext> context) {
		ServerPlayer player = (ServerPlayer) context.get().getPlayer();
		player.fallDistance = 0;
		player.connection.aboveGroundTickCount = 0;
	}
}
