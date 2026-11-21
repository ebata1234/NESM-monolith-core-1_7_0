package com.esm.nightmare.MobBehavs;

import com.esm.nightmare.siege.SiegeProtection;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Places an NESM construction block without overwriting solid blocks. */
public class MobPlaceBlock {

	private final boolean placed;

	public MobPlaceBlock(Entity entity, Block blockType, BlockPos pos) {
		boolean success = false;
		if (entity != null && blockType != null && pos != null) {
			Level level = entity.level();
			if (!level.isClientSide && level.isInWorldBounds(pos) && level.hasChunkAt(pos)
					&& !SiegeProtection.isProtected(level, pos)) {
				BlockState existing = level.getBlockState(pos);

				// Allow air, replaceable plants, and fluid blocks (including flowing
				// water), but never overwrite a non-replaceable solid block.
				if (existing.isAir() || existing.canBeReplaced() || !existing.getFluidState().isEmpty()) {
					BlockState placedState = blockType.defaultBlockState();
					success = level.setBlockAndUpdate(pos, placedState)
							&& level.getBlockState(pos).is(blockType);
					if (success) {
						if (level instanceof ServerLevel) {
							ServerLevel serverLevel = (ServerLevel) level;
							MobPlacedBlocks.get(serverLevel).add(MobPlacedBlocks.dimKey(serverLevel), pos);
						}
						SoundEvent sound = blockType.getSoundType(placedState).getPlaceSound();
						level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.6F, 0.9F);
					}
				}
			}
		}
		this.placed = success;
	}

	public boolean wasPlaced() {
		return placed;
	}
}
