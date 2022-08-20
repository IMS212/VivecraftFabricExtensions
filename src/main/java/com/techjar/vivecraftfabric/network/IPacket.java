package com.techjar.vivecraftfabric.network;

import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import dev.architectury.networking.NetworkManager;

public interface IPacket {
	void encode(final FriendlyByteBuf buffer);

	void decode(final FriendlyByteBuf buffer);

	void handleClient(final Supplier<NetworkManager.PacketContext> context);

	void handleServer(final Supplier<NetworkManager.PacketContext> context);
}
