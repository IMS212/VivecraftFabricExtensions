package com.techjar.vivecraftfabric.network;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import dev.architectury.networking.NetworkManager;

public class Message<T extends IPacket> {
	private final Class<T> tClass;

	public Message(Class<T> tClass) {
		this.tClass = tClass;
	}

	public Class<T> getPacketClass() {
		return tClass;
	}

	public final void encode(IPacket packet, FriendlyByteBuf buffer) {
		packet.encode(buffer);
	}

	public final T decode(FriendlyByteBuf buffer) {
		T packet;
		try {
			packet = tClass.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("instantiating packet", e);
		}

		packet.decode(buffer);
		return packet;
	}

	public final void handle(IPacket packet, Supplier<NetworkManager.PacketContext> context) {
		if (context.get().getEnv() == EnvType.SERVER)
			packet.handleServer(context);
		else
			packet.handleClient(context);
	}
}
