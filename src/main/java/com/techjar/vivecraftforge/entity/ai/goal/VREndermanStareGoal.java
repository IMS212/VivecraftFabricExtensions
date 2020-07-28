package com.techjar.vivecraftforge.entity.ai.goal;

import com.techjar.vivecraftforge.util.LogHelper;
import com.techjar.vivecraftforge.util.PlayerTracker;
import com.techjar.vivecraftforge.util.Quaternion;
import com.techjar.vivecraftforge.util.Util;
import com.techjar.vivecraftforge.util.VRPlayerData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

public class VREndermanStareGoal extends EndermanEntity.StareGoal {
	public VREndermanStareGoal(EndermanEntity enderman) {
		super(enderman);
	}

	@Override
	public boolean shouldExecute() {
		boolean orig = super.shouldExecute(); // call this always so stuff gets set up

		LivingEntity target = this.enderman.getAttackTarget();
		if (target instanceof PlayerEntity && PlayerTracker.hasPlayerData((PlayerEntity)target)) {
			double dist = target.getDistanceSq(this.enderman);
			return dist <= 256.0D && Util.shouldEndermanAttackVRPlayer(this.enderman, (PlayerEntity)target);
		}

		return orig;
	}

	@Override
	public void tick() {
		LivingEntity target = this.enderman.getAttackTarget();
		if (target instanceof PlayerEntity && PlayerTracker.hasPlayerData((PlayerEntity)target)) {
			VRPlayerData data = PlayerTracker.getPlayerDataAbsolute((PlayerEntity)target);
			this.enderman.getLookController().setLookPosition(data.head.posX, data.head.posY, data.head.posZ);
		} else {
			super.tick();
		}
	}
}
