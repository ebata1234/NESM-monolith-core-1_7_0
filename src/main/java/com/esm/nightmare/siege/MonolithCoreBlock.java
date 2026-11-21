package com.esm.nightmare.siege;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Looks like a Lodestone, but it is its own block ("Monolith Core").
 * While it exists in the world, its chunk is protected and the difficulty goes up.
 * Placing, removing, explosions, commands: all go through onPlace / onRemove,
 * so the count always follows the blocks that really exist.
 */
public class MonolithCoreBlock extends Block {

	public MonolithCoreBlock() {
		super(BlockBehaviour.Properties.copy(Blocks.LODESTONE));
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (level instanceof ServerLevel && !oldState.is(this)) {
			ServerLevel serverLevel = (ServerLevel) level;
			LanternData.get(serverLevel).add(LanternData.dimKey(serverLevel), pos);
			LanternDifficulty.refresh(serverLevel.getServer());
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (level instanceof ServerLevel && !newState.is(this)) {
			ServerLevel serverLevel = (ServerLevel) level;
			LanternData.get(serverLevel).remove(LanternData.dimKey(serverLevel), pos);
			LanternDifficulty.refresh(serverLevel.getServer());
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}
}
