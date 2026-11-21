package com.esm.nightmare;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

//Enhancement that makes mobs much quicker when in the ocean, to destroy the viability of cheaping it out in the ocean.
public class AugmentOceanSpeed {

	private static final java.util.UUID OCEAN_ID = java.util.UUID.fromString("a3f1c2d4-6b7e-4f90-8a15-2c9d0e4b7f63");

	//private static final UUID SPEED_MODIFIER_SOUL_SPEED_UUID = UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");

	// Normal entity movement speed in water
	float Movement_Speed = 9F;

	// Creeper movement speed in water
	float Creeper_Movement_Speed = 12F;

	// If this entity is swimming in the ocean, augment their speed.
	public AugmentOceanSpeed(Mob M) {
		// Get position under feet
		BlockPos Pos = M.blockPosition();
		BlockPos Feet_Pos = Pos.below();

		// If spider has target, then
		LivingEntity Tgt = M.getTarget();

		if (Tgt instanceof ServerPlayer) {
			
			
			ServerPlayer Plyr = (ServerPlayer) Tgt;
			
			// Get block at this position
			//Level Curr_Level = M.level;		//1.19
			Level Curr_Level = M.level();		//1.20

			// Get block at feet
			Block Curr_Block = Curr_Level.getBlockState(Pos).getBlock();
			Block Feet_Block = Curr_Level.getBlockState(Feet_Pos).getBlock();

			// If feet block is water, then
			if (Feet_Block == Blocks.WATER || Curr_Block == Blocks.WATER) {
				M.lookAt(Plyr, 1F, 1F);
				tryAddOceanSpeed(M, Plyr);				
			} else {
				// Out of the water: remove the ocean boost only (not other speed modifiers, e.g. level speed)
				AttributeInstance Speed = M.getAttribute(Attributes.MOVEMENT_SPEED);
				if (Speed != null) {
					Speed.removeModifier(OCEAN_ID);
					// Older versions added a new modifier every tick; clean those up now and then
					if (M.tickCount % 100 == 0) {
						for (AttributeModifier Mod : new java.util.ArrayList<>(Speed.getModifiers())) {
							if ("Ocean Movement".equals(Mod.getName())) {
								Speed.removeModifier(Mod.getId());
							}
						}
					}
				}
			}
		}
	}

	// Swimming speed boost for creepers and spiders. The speed modifier is added once
	// (fixed id); before, a new one was added every tick, which stacked without limit.
	protected void tryAddOceanSpeed(Mob M, ServerPlayer Tgt) {
		boolean isCreeper = M.getType() == EntityType.CREEPER;
		boolean isSpider = M.getType() == EntityType.SPIDER || M.getType() == EntityType.CAVE_SPIDER;
		if (!isCreeper && !isSpider) {
			return;
		}

		M.lookAt(Tgt, 0.25F, 0.25F);

		float Swim = isCreeper ? Creeper_Movement_Speed : Movement_Speed;
		AttributeInstance SwimSpeed = M.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get());
		if (SwimSpeed != null && SwimSpeed.getBaseValue() != Swim) {
			SwimSpeed.setBaseValue(Swim);
		}

		AttributeInstance Speed = M.getAttribute(Attributes.MOVEMENT_SPEED);
		if (Speed != null && Speed.getModifier(OCEAN_ID) == null) {
			Speed.addPermanentModifier(new AttributeModifier(OCEAN_ID, "Ocean Movement", 2.0D, Operation.MULTIPLY_BASE));
		}
	}
}
