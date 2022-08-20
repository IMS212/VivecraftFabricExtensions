package com.techjar.vivecraftfabric.network.packet;

import java.util.function.Supplier;

import com.techjar.vivecraftfabric.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import dev.architectury.networking.NetworkManager;

public class PacketMoveMode implements IPacket {
	public boolean freeMove;

	public PacketMoveMode() {
	}

	public PacketMoveMode(boolean freeMove) {
		this.freeMove = freeMove;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		//buffer.writeBoolean(freeMove);
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
		//freeMove = buffer.readBoolean();
	}

	@Override
	public void handleClient(final Supplier<NetworkManager.PacketContext> context) {
	}

	@Override
	public void handleServer(final Supplier<NetworkManager.PacketContext> context) {
		/*ServerPlayerEntity player = context.get().getSender();
		context.get().enqueueWork(() -> {
			VRPlayerData data = PlayerTracker.getPlayerData(player, true);
			data.freeMove = freeMove;
		});*/
	}
}
