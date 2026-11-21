package com.esm.nightmare.MobBehavs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import com.esm.nightmare.siege.SiegeProtection;

/**
 * Builds a horizontal cobblestone bridge over air or fluids toward the target.
 *
 * v1.7.0:
 *  - Works at any distance. It used to need the target at least 4 blocks away, so a gap of
 *    3 blocks or less at the same height was handled by nobody (MobBuildUp only does height).
 *  - The target may be a player or a villager and does not have to be visible.
 *  - Targets up to 1 block below count as "level" (it used to be strictly level or higher).
 *  - Tries the longer axis first, then the other one.
 *  - Skips the bridge when the mob already has a complete walking route to the target.
 */
public class MobBuildBridge {

	private static final Block BLOCK_TYPE = Blocks.COBBLESTONE;

	public boolean isDoing;

	public MobBuildBridge(Mob mob) {
		if (mob == null || mob.level().isClientSide) {
			return;
		}

		LivingEntity target = ApproachTarget.get(mob);
		if (target == null) {
			return;
		}

		Level level = mob.level();
		BlockPos mobPos = mob.blockPosition();

		// Never let a mob leave a protected area by using a block-building action.
		if (SiegeProtection.isProtected(level, mobPos)) {
			return;
		}

		double dx = target.getX() - mob.getX();
		double dz = target.getZ() - mob.getZ();
		// Next to the target: it attacks instead
		if (Math.max(Math.abs(dx), Math.abs(dz)) < 1.5D) {
			return;
		}

		// Only toward targets at roughly the same height or above (MobBuildUp / MobDigDown do the rest)
		if (target.blockPosition().getY() < mobPos.getY() - 1) {
			return;
		}

		boolean mobIsInWater = mob.isInWaterOrBubble()
				|| !level.getBlockState(mobPos).getFluidState().isEmpty();

		// A complete route exists: just walk it
		if (!mobIsInWater && hasWalkingRoute(mob)) {
			return;
		}

		mob.lookAt(target, 30.0F, 30.0F);
		for (Direction direction : ApproachTarget.directionsToward(dx, dz)) {
			if (tryBridge(mob, level, mobPos, direction, mobIsInWater)) {
				return;
			}
		}
	}

	/** True when the mob's current path really reaches the target. */
	private static boolean hasWalkingRoute(Mob mob) {
		Path path = mob.getNavigation().getPath();
		return path != null && path.canReach();
	}

	/**
	 * Returns true when this direction was dealt with (a block was placed, or there is already
	 * a floor so the mob can simply walk), false when it cannot be used (a wall, protected, ...).
	 */
	private boolean tryBridge(Mob mob, Level level, BlockPos mobPos, Direction direction, boolean mobIsInWater) {
		BlockPos nextBodyPos = mobPos.relative(direction);
		BlockPos nextHeadPos = nextBodyPos.above();
		BlockPos bridgeBlockPos = nextBodyPos.below();
		if (!level.isInWorldBounds(nextBodyPos) || !level.isInWorldBounds(nextHeadPos)
				|| !level.isInWorldBounds(bridgeBlockPos) || !level.hasChunkAt(nextBodyPos)
				|| !level.hasChunkAt(nextHeadPos) || !level.hasChunkAt(bridgeBlockPos)) {
			return false;
		}

		// A mob must not be nudged toward a protected chunk.
		if (SiegeProtection.isProtected(level, nextBodyPos)
				|| SiegeProtection.isProtected(level, bridgeBlockPos)) {
			return false;
		}

		BlockState bodyState = level.getBlockState(nextBodyPos);
		BlockState headState = level.getBlockState(nextHeadPos);
		if (!isPassable(level, nextBodyPos, bodyState) || !isPassable(level, nextHeadPos, headState)) {
			return false; // a wall: MobDig breaks it
		}

		BlockState floorState = level.getBlockState(bridgeBlockPos);
		boolean hasFloorSupport = isSafeSupport(level, bridgeBlockPos, floorState);

		// If water occupies the next body cell but a solid riverbed is directly
		// underneath, make a raised cobblestone step in the water cell itself.
		boolean raisedWaterStep = mobIsInWater && !bodyState.getFluidState().isEmpty()
				&& hasFloorSupport;
		if (raisedWaterStep) {
			BlockPos raisedHeadPos = nextHeadPos.above();
			if (!level.isInWorldBounds(raisedHeadPos)
					|| !isPassable(level, raisedHeadPos, level.getBlockState(raisedHeadPos))) {
				return false;
			}
		}

		if (hasFloorSupport && !raisedWaterStep) {
			if (mobIsInWater) {
				// Solid floor ahead: do not place anything, but hop out of shallow flowing water.
				stepOutOfWater(mob, direction, nextBodyPos);
				isDoing = true;
			}
			return true; // walkable
		}

		BlockPos placementPos = raisedWaterStep ? nextBodyPos : bridgeBlockPos;
		if (SiegeProtection.isProtected(level, placementPos)) {
			return false;
		}

		// Movement happens only after placement reports success.
		MobPlaceBlock placement = new MobPlaceBlock(mob, BLOCK_TYPE, placementPos);
		if (!placement.wasPlaced()) {
			return false;
		}

		mob.swing(mob.getUsedItemHand());
		stepOntoNewBridge(mob, direction, nextBodyPos, bodyState, raisedWaterStep, mobIsInWater);
		isDoing = true;
		return true;
	}

	private static boolean isPassable(Level level, BlockPos pos, BlockState state) {
		return state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty()
				|| state.getCollisionShape(level, pos).isEmpty();
	}

	private static boolean isSafeSupport(Level level, BlockPos pos, BlockState state) {
		return state.getFluidState().isEmpty() && !state.getCollisionShape(level, pos).isEmpty()
				&& state.isFaceSturdy(level, pos, Direction.UP);
	}

	private static void stepOntoNewBridge(Mob mob, Direction direction, BlockPos nextBodyPos,
			BlockState originalBodyState, boolean raisedWaterStep, boolean mobIsInWater) {
		double targetY = mob.getY() + (raisedWaterStep ? 1.0D : 0.0D);
		double yBoost = raisedWaterStep ? 0.42D
				: ((mobIsInWater || !originalBodyState.getFluidState().isEmpty()) ? 0.22D : 0.0D);
		stepForward(mob, direction, nextBodyPos, targetY, yBoost);
	}

	private static void stepOutOfWater(Mob mob, Direction direction, BlockPos nextBodyPos) {
		stepForward(mob, direction, nextBodyPos, mob.getY(), 0.22D);
	}

	private static void stepForward(Mob mob, Direction direction, BlockPos nextBodyPos,
			double targetY, double yBoost) {
		// Stop a stale failed path from immediately steering the mob away from the
		// newly created bridge, then give both MoveControl and physical motion a nudge.
		mob.getNavigation().stop();
		mob.getMoveControl().setWantedPosition(nextBodyPos.getX() + 0.5D, targetY,
			nextBodyPos.getZ() + 0.5D, 1.15D);

		Vec3 motion = mob.getDeltaMovement();
		double push = 0.12D;
		mob.setDeltaMovement(motion.x * 0.35D + direction.getStepX() * push,
				Math.max(motion.y, yBoost),
				motion.z * 0.35D + direction.getStepZ() * push);
	}
}
