package com.techjar.vivecraftfabric.network.packet;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.Supplier;

import com.techjar.vivecraftfabric.Config;
import com.techjar.vivecraftfabric.VivecraftFabric;
import com.techjar.vivecraftfabric.network.ChannelHandler;
import com.techjar.vivecraftfabric.network.IPacket;
import com.techjar.vivecraftfabric.util.LogHelper;
import com.techjar.vivecraftfabric.util.PlayerTracker;
import com.techjar.vivecraftfabric.util.VRPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.world.entity.player.Player;

/*
 * Why the fuck does the client want a length-prefixed string, but sends
 * a string that's just char bytes with no length prefix? This whole
 * protocol is an awful mess. I didn't write it, so don't blame me.
 */
public class PacketVersion implements IPacket {
	public String message;

	public PacketVersion() {
	}

	public PacketVersion(String message) {
		this.message = message;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUtf(message);
	}

	@Override
	public void decode(FriendlyByteBuf buffer) {
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.readBytes(bytes);
		message = new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public void handleClient(final Supplier<NetworkManager.PacketContext> context) {
	}

	@Override
	public void handleServer(final Supplier<NetworkManager.PacketContext> context) {
		ServerPlayer player = (ServerPlayer) context.get().getPlayer();
		ChannelHandler.sendTo(new PacketVersion(VivecraftFabric.MOD_INFO.getMetadata().getName() + " " + VivecraftFabric.MOD_INFO.getMetadata().getVersion().getFriendlyString()), player);
		if (!message.contains("NONVR")) {
			LogHelper.info("VR player joined: {}", message);
			ChannelHandler.sendTo(new PacketRequestData(), player);

			if (Config.teleportEnabled.get())
				ChannelHandler.sendTo(new PacketTeleport(), player);
			if (Config.climbeyEnabled.get())
				ChannelHandler.sendTo(new PacketClimbing(Config.blockListMode.get(), Config.blockList.get()), player);
			if (Config.crawlingEnabled.get())
				ChannelHandler.sendTo(new PacketCrawl(), player);

			if (Config.teleportLimited.get()) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("limitedTeleport", true);
				map.put("teleportLimitUp", Config.teleportLimitUp.get());
				map.put("teleportLimitDown", Config.teleportLimitDown.get());
				map.put("teleportLimitHoriz", Config.teleportLimitHoriz.get());
				ChannelHandler.sendTo(new PacketSettingOverride(map), player);
			}

			if (Config.worldScaleLimited.get()) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("worldScale.min", Config.worldScaleMin.get());
				map.put("worldScale.max", Config.worldScaleMax.get());
				ChannelHandler.sendTo(new PacketSettingOverride(map), player);
			}

			context.get().queue(() -> {
				PlayerTracker.players.put(player.getGameProfile().getId(), new VRPlayerData());
				if (Config.enableJoinMessages.get() && !Config.joinMessageVR.get().isEmpty())
					player.getServer().getPlayerList().broadcastMessage(new TextComponent(String.format(Config.joinMessageVR.get(), player.getDisplayName())), ChatType.SYSTEM, net.minecraft.Util.NIL_UUID);
			});
		} else {
			LogHelper.info("Non-VR player joined: {}", message);
			context.get().queue(() -> {
				PlayerTracker.nonvrPlayers.add(player.getGameProfile().getId());
				if (Config.enableJoinMessages.get() && !Config.joinMessageNonVR.get().isEmpty())
					player.getServer().getPlayerList().broadcastMessage(new TextComponent(String.format(Config.joinMessageNonVR.get(), player.getDisplayName())), ChatType.SYSTEM, net.minecraft.Util.NIL_UUID);
			});
		}
	}
}
