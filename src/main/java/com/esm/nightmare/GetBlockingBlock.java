package com.esm.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Finds the two blocks directly in front of an entity: the upper/head block
 * and the lower/body block. The direction is horizontal, so looking up/down
 * does not make the mob inspect blocks above or below its actual path.
 */
public class GetBlockingBlock {

	/** The position of the block at the entity's eye/head height. */
	public BlockPos Current_Position;

	/** Block at Current_Position. */
	public Block Current_Block;

	/** True only when Current_Block has a collision shape and is not a fluid. */
	public boolean isBlocking;

	/** The lower block directly in front of the entity. */
	public BlockPos Feet_Position;

	/** Block at Feet_Position. */
	public Block Feet_Block;

	/** True only when Feet_Block has a collision shape and is not a fluid. */
	public boolean isFootBlocked;

	public GetBlockingBlock(Entity entity) {
		Level level = entity.level();

		// Direction is based on yaw only. Do not use getForward(): it includes pitch.
		Direction direction = entity.getDirection();
		Vec3 eyePosition = entity.getEyePosition();

		// BlockPos.containing uses floor semantics and therefore works for negative
		// world coordinates (a Java (int) cast truncates toward zero instead).
		BlockPos eyeBlockPos = BlockPos.containing(eyePosition.x, eyePosition.y, eyePosition.z);
		Current_Position = eyeBlockPos.relative(direction);
		Feet_Position = Current_Position.below();

		// Do not query an unloaded neighbouring chunk just to decide whether to dig.
		if (!level.hasChunkAt(Current_Position) || !level.hasChunkAt(Feet_Position)) {
			Current_Block = Blocks.AIR;
			Feet_Block = Blocks.AIR;
			isBlocking = false;
			isFootBlocked = false;
			return;
		}

		BlockState currentState = level.getBlockState(Current_Position);
		Current_Block = currentState.getBlock();
		isBlocking = blocksMovement(entity, level, Current_Position, currentState);

		BlockState feetState = level.getBlockState(Feet_Position);
		Feet_Block = feetState.getBlock();
		isFootBlocked = blocksMovement(entity, level, Feet_Position, feetState);
	}

	private static boolean blocksMovement(Entity entity, Level level, BlockPos pos, BlockState state) {
		if (state.isAir() || !state.getFluidState().isEmpty()) {
			return false;
		}
		return !state.getCollisionShape(level, pos, CollisionContext.of(entity)).isEmpty();
	}
}
