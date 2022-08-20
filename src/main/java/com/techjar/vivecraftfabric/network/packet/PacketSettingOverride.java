package com.techjar.vivecraftfabric.network.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.techjar.vivecraftfabric.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import dev.architectury.networking.NetworkManager;

public class PacketSettingOverride implements IPacket {
	public Map<String, Object> settings = new HashMap<>();

	public PacketSettingOverride() {
	}

	public PacketSettingOverride(Map<String, Object> settings) {
		this.settings = settings;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		for (Map.Entry<String, Object> entry : settings.entrySet()) {
			buffer.writeUtf(entry.getKey());
			buffer.writeUtf(entry.getValue().toString());
		}
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
		while (buffer.readableBytes() > 0) {
			settings.put(buffer.readUtf(32767), buffer.readUtf(32767));
		}
	}

	@Override
	public void handleClient(final Supplier<NetworkManager.PacketContext> context) {
	}

	@Override
	public void handleServer(final Supplier<NetworkManager.PacketContext> context) {
	}
}
