package com.esm.nightmare.MobBehavs;

import java.lang.Math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Breaks the floor toward a lower player target, but never removes the last
 * supporting block beneath a one-block-thick bridge/platform.
 */
public class MobDigDown {

	private static final double DIG_DISTANCE = 16.0D;

	public boolean isDoing;

	public MobDigDown(Mob mob) {
		if (mob.swinging || !mob.onGround() || mob.isInWaterOrBubble()) {
			return;
		}

		Entity target = ApproachTarget.get(mob);
		if (target == null) {
			return;
		}
		if (!isCloseEnough(mob, target) || target.blockPosition().getY() >= mob.blockPosition().getY()) {
			return;
		}

		Level level = mob.level();
		BlockPos blockToBreak = mob.blockPosition().below();
		BlockPos nextSupportPos = blockToBreak.below();
		BlockState nextSupport = level.getBlockState(nextSupportPos);

		// Keep at least one solid layer below the block being removed. This is
		// deliberately conservative: a one-block-thick bridge over a void is safe.
		if (!nextSupport.getFluidState().isEmpty()
				|| nextSupport.getCollisionShape(level, nextSupportPos, CollisionContext.of(mob)).isEmpty()
				|| !nextSupport.isFaceSturdy(level, nextSupportPos, Direction.UP)) {
			return;
		}

		if (MobDamageBlock.tryDamageBlock(mob, blockToBreak)) {
			mob.swing(mob.getUsedItemHand());
			isDoing = true;
		}
	}

	private boolean isCloseEnough(Mob mob, Entity target) {
		double dx = Math.abs(target.blockPosition().getX() - mob.blockPosition().getX());
		double dz = Math.abs(target.blockPosition().getZ() - mob.blockPosition().getZ());
		return dx < DIG_DISTANCE && dz < DIG_DISTANCE;
	}
}
