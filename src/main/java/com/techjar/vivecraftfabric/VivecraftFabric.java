package com.techjar.vivecraftfabric;

import com.techjar.vivecraftfabric.entity.ai.goal.VRCreeperSwellGoal;
import com.techjar.vivecraftfabric.entity.ai.goal.VREndermanFindPlayerGoal;
import com.techjar.vivecraftfabric.entity.ai.goal.VREndermanStareGoal;
import com.techjar.vivecraftfabric.eventhandler.EventHandlerServer;
import com.techjar.vivecraftfabric.mixin.ConnectionAccessor;
import com.techjar.vivecraftfabric.network.ChannelHandler;
import com.techjar.vivecraftfabric.network.packet.PacketUberPacket;
import com.techjar.vivecraftfabric.util.*;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class VivecraftFabric implements DedicatedServerModInitializer {
	public static ModContainer MOD_INFO;
	public ChannelHandler handler;
	public Map<ServerPlayer, List<ServerPlayer>> playerSeenMap = new HashMap<>();

	@Override
	public void onInitializeServer() {
		ModLoadingContext.registerConfig("vivecraftfabricextensions", ModConfig.Type.COMMON, Config.config);
		MOD_INFO = FabricLoader.getInstance().getModContainer("vivecraftfabricextensions").get();
		handler = new ChannelHandler();
		handler.init();

		if (Config.printMoney.get())
			LogHelper.warning(Util.getMoney());

		EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
			if (trackedEntity instanceof ServerPlayer serverPlayer) {
				playerSeenMap.computeIfAbsent(serverPlayer, a -> new ArrayList<>()).add(player);
			}
		});

		EntityTrackingEvents.STOP_TRACKING.register((trackedEntity, player) -> {
			if (trackedEntity instanceof ServerPlayer serverPlayer) {
				playerSeenMap.computeIfAbsent(serverPlayer, a -> new ArrayList<>()).remove(player);
			}
		});

		ServerTickEvents.END_SERVER_TICK.register((server -> {
			PlayerTracker.tick(server);
			PlayerList playerList = server.getPlayerList();
			int viewDist = playerList.getViewDistance();
			float range = Mth.clamp(viewDist / 8.0F, 1.0F, 2.5F) * 64.0F; // This is how the client determines entity render distance
			for (Map.Entry<UUID, VRPlayerData> entry : PlayerTracker.players.entrySet()) {
				ServerPlayer player = playerList.getPlayer(entry.getKey());
				if (player != null) {
					PacketUberPacket packet = PlayerTracker.getPlayerDataPacket(entry.getKey(), entry.getValue());
					ChannelHandler.sendToAllTrackingEntity(packet, playerSeenMap.getOrDefault(player, new ArrayList<>()));
				}
			}
		}));

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {

			if (entity instanceof ServerPlayer) {
				final ServerPlayer player = (ServerPlayer)entity;
				if (Config.vrOnly.get() && !player.hasPermissions(2)) {
					Util.scheduler.schedule(() -> {
						world.getServer().submit(() -> {
							if (player.connection.getConnection().isConnected() && !PlayerTracker.hasPlayerData(player)) {
								player.sendMessage(new TextComponent(Config.vrOnlyKickMessage.get()), net.minecraft.Util.NIL_UUID);
								player.sendMessage(new TextComponent("If this is not a VR client, you will be kicked in " + Config.vrOnlyKickDelay.get() + " seconds."), net.minecraft.Util.NIL_UUID);
								Util.scheduler.schedule(() -> {
									world.getServer().submit(() -> {
										if (player.connection.getConnection().isConnected() && !PlayerTracker.hasPlayerData(player)) {
											player.connection.disconnect(new TextComponent(Config.vrOnlyKickMessage.get()));
										}
									});
								}, Math.round(Config.vrOnlyKickDelay.get() * 1000), TimeUnit.MILLISECONDS);
							}
						});
					}, 1000, TimeUnit.MILLISECONDS);
				}
			} else if (entity instanceof Projectile) {
				Projectile projectile = (Projectile)entity;
				if (!(projectile.getOwner() instanceof Player))
					return;
				Player shooter = (Player)projectile.getOwner();
				if (!PlayerTracker.hasPlayerData(shooter))
					return;

				boolean arrow = projectile instanceof AbstractArrow && !(projectile instanceof ThrownTrident);
				VRPlayerData data = PlayerTracker.getPlayerDataAbsolute(shooter);
				Vec3 pos = data.getController(data.activeHand).getPos();
				Vec3 aim = data.getController(data.activeHand).getRot().multiply(new Vec3(0, 0, -1));

				if (arrow && !data.seated && data.bowDraw > 0) {
					pos = data.getController(0).getPos();
					aim = data.getController(1).getPos().subtract(pos).normalize();
				}

				pos = pos.add(aim.scale(0.6));
				double vel = projectile.getDeltaMovement().length();
				projectile.setPos(pos.x, pos.y, pos.z);
				projectile.shoot(aim.x, aim.y, aim.z, (float)vel, 0.0f);

				Vec3 shooterMotion = shooter.getDeltaMovement();
				projectile.setDeltaMovement(projectile.getDeltaMovement().add(shooterMotion.x, shooter.isOnGround() ? 0.0 : shooterMotion.y, shooterMotion.z));

				LogHelper.debug("Projectile direction: {}", aim);
				LogHelper.debug("Projectile velocity: {}", vel);
			} else if (entity instanceof Creeper) {
				Creeper creeper = (Creeper)entity;
				Util.replaceAIGoal(creeper, creeper.goalSelector, SwellGoal.class, () -> new VRCreeperSwellGoal(creeper));
			} else if (entity instanceof EnderMan) {
				EnderMan enderman = (EnderMan)entity;
				Util.replaceAIGoal(enderman, enderman.goalSelector, EnderMan.EndermanFreezeWhenLookedAt.class, () -> new VREndermanStareGoal(enderman));
				Util.replaceAIGoal(enderman, enderman.targetSelector, EnderMan.EndermanLookForPlayerGoal.class, () -> new VREndermanFindPlayerGoal(enderman, enderman::isAngryAt));
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler1, sender, server) -> {
			Connection netManager = handler1.getPlayer().connection.getConnection();
			((ConnectionAccessor) netManager).getChannel().pipeline().addBefore("packet_handler", "vr_aim_fix", new AimFixHandler(netManager));
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (entity instanceof Player) {
				Player target = (Player)entity;
				if (PlayerTracker.hasPlayerData(player)) {
					VRPlayerData data = PlayerTracker.getPlayerData(player);
					if (data.seated) { // Seated VR vs...
						if (PlayerTracker.hasPlayerData(target)) {
							VRPlayerData targetData = PlayerTracker.getPlayerData(target);
							if (targetData.seated) { // ...seated VR
								if (!Config.seatedVrVsSeatedVR.get()) return InteractionResult.FAIL;
							} else { // ...VR
								if (!Config.vrVsSeatedVR.get()) return InteractionResult.FAIL;
							}
						} else { // ...non-VR
							if (!Config.seatedVrVsNonVR.get()) return InteractionResult.FAIL;
						}
					} else { // VR vs...
						if (PlayerTracker.hasPlayerData(target)) {
							VRPlayerData targetData = PlayerTracker.getPlayerData(target);
							if (targetData.seated) { // ...seated VR
								if (!Config.vrVsSeatedVR.get()) return InteractionResult.FAIL;
							} else { // ...VR
								if (!Config.vrVsVR.get()) return InteractionResult.FAIL;
							}
						} else { // ...non-VR
							if (!Config.vrVsNonVR.get()) return InteractionResult.FAIL;
						}
					}
				} else { // Non-VR vs...
					if (PlayerTracker.hasPlayerData(target)) {
						VRPlayerData targetData = PlayerTracker.getPlayerData(target);
						if (targetData.seated) { // ...seated VR
							if (!Config.seatedVrVsNonVR.get()) return InteractionResult.FAIL;
						} else { // ...VR
							if (!Config.vrVsNonVR.get()) return InteractionResult.FAIL;
						}
					}
				}
			}
			return InteractionResult.SUCCESS;
		});


	}
}
