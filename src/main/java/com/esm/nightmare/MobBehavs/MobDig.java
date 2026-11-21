package com.esm.nightmare.MobBehavs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Horizontal block breaking toward the target.
 *
 * v1.7.0: the direction comes from where the target is (longer axis first, then the other
 * one), not from where the mob happens to be facing, so a mob that cannot path to a target
 * behind a wall still digs the right way. The target can be a player or a villager and does
 * not have to be visible. It runs on every dig interval (it used to run on about a third of
 * them and was also blocked for ~6 ticks after every swing).
 */
public class MobDig {

	public boolean isDoing;

	public MobDig(Entity entity) {
		if (!(entity instanceof Mob) || entity.level().isClientSide) {
			return;
		}
		Mob mob = (Mob) entity;
		LivingEntity target = ApproachTarget.get(mob);
		if (target == null) {
			return;
		}

		double dx = target.getX() - mob.getX();
		double dz = target.getZ() - mob.getZ();
		// Right above / below the target: MobDigUp, MobDigDown and MobBuildUp handle that
		if (Math.abs(dx) < 0.75D && Math.abs(dz) < 0.75D) {
			return;
		}

		Level level = mob.level();
		BlockPos base = mob.blockPosition();
		for (Direction direction : ApproachTarget.directionsToward(dx, dz)) {
			BlockPos feet = base.relative(direction);
			BlockPos head = feet.above();
			boolean feetSolid = isSolid(mob, level, feet);
			boolean headSolid = isSolid(mob, level, head);
			if (!feetSolid && !headSolid) {
				// The way ahead is open: walking (or MobBuildBridge) takes it from here
				return;
			}
			boolean broke = false;
			if (headSolid) {
				broke |= MobDamageBlock.tryDamageBlock(mob, head);
			}
			if (feetSolid) {
				broke |= MobDamageBlock.tryDamageBlock(mob, feet);
			}
			if (broke) {
				mob.swing(mob.getUsedItemHand());
				isDoing = true;
				return;
			}
			// Could not break it (protected, unbreakable or on the blacklist): try the other axis
		}
	}

	private static boolean isSolid(Entity entity, Level level, BlockPos pos) {
		if (!level.isInWorldBounds(pos) || !level.hasChunkAt(pos)) {
			return false;
		}
		BlockState state = level.getBlockState(pos);
		if (state.isAir() || !state.getFluidState().isEmpty()) {
			return false;
		}
		return !state.getCollisionShape(level, pos, CollisionContext.of(entity)).isEmpty();
	}
}
