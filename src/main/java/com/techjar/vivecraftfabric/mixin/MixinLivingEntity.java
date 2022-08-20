package com.techjar.vivecraftfabric.mixin;

import com.techjar.vivecraftfabric.Config;
import com.techjar.vivecraftfabric.util.PlayerTracker;
import com.techjar.vivecraftfabric.util.Util;
import com.techjar.vivecraftfabric.util.VRPlayerData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Redirect(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float setDamage(LivingEntity instance, DamageSource source, float f) {
        if (source.getDirectEntity() instanceof Arrow arrow && source.getEntity() instanceof Player attacker) {
            if (PlayerTracker.hasPlayerData(attacker)) {
                VRPlayerData data = PlayerTracker.getPlayerData(attacker);
                boolean headshot = Util.isHeadshot(instance, arrow);
                if (data.seated) {
                    if (headshot) f = f * Config.bowSeatedHeadshotMul.get().floatValue();
                    else f = f * Config.bowSeatedMul.get().floatValue();
                } else {
                    if (headshot) f = f * Config.bowStandingHeadshotMul.get().floatValue();
                    else f = f * Config.bowStandingMul.get().floatValue();
                }
            }
        }
        
        return f;
    }
}
