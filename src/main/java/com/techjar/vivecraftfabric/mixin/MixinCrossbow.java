package com.techjar.vivecraftfabric.mixin;

import com.techjar.vivecraftfabric.util.LogHelper;
import com.techjar.vivecraftfabric.util.PlayerTracker;
import com.techjar.vivecraftfabric.util.VRPlayerData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public abstract class MixinCrossbow {
    @Unique
    private static Player player;

    @Shadow public abstract int getUseDuration(ItemStack itemStack);

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;performShooting(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FF)V"))
    private void player(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        MixinCrossbow.player = player;
    }

    @Inject(method = "getShootingPower", at = @At(value = "RETURN"), cancellable = true)
    private static void crossbowSpeed(ItemStack itemStack, CallbackInfoReturnable<Float> cir) {
        if (player == null) {
            return;
        }

        VRPlayerData data = PlayerTracker.getPlayerData(player);
        if (data != null && !data.seated && data.bowDraw > 0) {
            LogHelper.debug("Bow draw: " + data.bowDraw);
            cir.setReturnValue(cir.getReturnValue() * 2);
        }
    }
}
