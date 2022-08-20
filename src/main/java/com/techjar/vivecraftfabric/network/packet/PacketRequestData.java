package com.techjar.vivecraftfabric.network.packet;

import java.util.function.Supplier;

import com.techjar.vivecraftfabric.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import dev.architectury.networking.NetworkManager;

public class PacketRequestData implements IPacket {
	public PacketRequestData() {
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
	}

	@Override
	public void handleClient(final Supplier<NetworkManager.PacketContext> context) {
	}

	@Override
	public void handleServer(final Supplier<NetworkManager.PacketContext> context) {
	}
}
