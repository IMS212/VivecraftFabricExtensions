package com.techjar.vivecraftfabric.mixin;

import com.techjar.vivecraftfabric.util.PlayerTracker;
import com.techjar.vivecraftfabric.util.VRPlayerData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity {
    protected MixinPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickVR(CallbackInfo ci) {
        VRPlayerData data = PlayerTracker.getPlayerData(((Player) (Object) this));
        if (data != null && data.crawling)
            this.setPose(Pose.SWIMMING);
    }

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"))
    private void editEntity(ItemStack itemStack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> cir) {
        if (!PlayerTracker.hasPlayerData(((Player) (Object) this)) || this.dead || cir.getReturnValue() == null)
            return;

        VRPlayerData data = PlayerTracker.getPlayerDataAbsolute(((Player) (Object) this));
        ItemEntity item = cir.getReturnValue();

        Vec3 pos = data.getController(0).getPos();
        Vec3 aim = data.getController(0).getRot().multiply(new Vec3(0, 0, -1));
        Vec3 aimUp = data.getController(0).getRot().multiply(new Vec3(0, 1, 0));
        double pitch = Math.toDegrees(Math.asin(-aim.y));

        pos = pos.add(aim.scale(0.2)).subtract(aimUp.scale(0.4 * (1 - Math.abs(pitch) / 90)));
        double vel = 0.3;
        item.setPos(pos.x, pos.y, pos.z);
        item.setDeltaMovement(aim.scale(vel));
    }
}
