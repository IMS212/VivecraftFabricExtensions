package com.techjar.vivecraftfabric.mixin;

import com.techjar.vivecraftfabric.util.LogHelper;
import com.techjar.vivecraftfabric.util.PlayerTracker;
import com.techjar.vivecraftfabric.util.VRPlayerData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class MixinBow {
    @Unique
    private LivingEntity player;

    @Shadow public abstract int getUseDuration(ItemStack itemStack);

    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void player(ItemStack itemStack, Level level, LivingEntity livingEntity, int i, CallbackInfo ci) {
        this.player = livingEntity;
    }

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"))
    private float a(int i) {
        if (!(player instanceof Player)) {
            return i;
        }

        Player player = (Player) this.player;

        VRPlayerData data = PlayerTracker.getPlayerData(player);
        if (data != null && !data.seated && data.bowDraw > 0) {
            LogHelper.debug("Bow draw: " + data.bowDraw);
            return Math.round(data.bowDraw * 20);
        }
        return i;
    }
}
