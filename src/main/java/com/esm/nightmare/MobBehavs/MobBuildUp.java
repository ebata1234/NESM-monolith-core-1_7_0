package com.esm.nightmare.MobBehavs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.lang.Math;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.esm.nightmare.SnapToBlockCenter;
import com.esm.nightmare.siege.SiegeProtection;

public class MobBuildUp {

	private static final Logger LOGGER = LogManager.getLogger();
	
	// Distance at which entities start building up
	int BuildUpDist = 4;

	// Block for zombie to place when building up
	Block Block_Type = Blocks.COBBLESTONE;

	// Is zombie building up?
	public boolean isDoing;

	// Behavior for this zombie building up to a player if they build a base in the
	// sky. Zombies husks and zombie villagers should use this behavior
	public MobBuildUp(Mob M) {

		// Get this monster's target (a survival player or, in v1.7.0, a villager as well)
		Entity Tgt = ApproachTarget.get(M);

		// If the mob has a target it can approach, and is close enough to it, then
		if (Tgt != null && isCloseEnough(M, Tgt)) {

			// Monster as entity class
			Entity Entity_Class = (Entity) M;

			// If monster is below the target (and the target is high enough), then
			if (isBelowPlayer(M, Tgt) && isPlayerHighEnough(M, Tgt)) {

				// Get monster's block position
				BlockPos M_BlockPos = M.blockPosition();

				// Get block position above head position
				BlockPos M_BlockPos_AboveHead = M_BlockPos.above().above();

				Level Server_Level = M.level();

				// Only build up if the position above is air
				BlockState Block_State = Server_Level.getBlockState(M_BlockPos_AboveHead);
				if (Block_State.isAir()) {

					// Get build direction
					Vec3 Norm_Build_Dir = GetBuildDir(M, Tgt);
					Vec3i Norm_Build_Dir_I = new Vec3i((int)Norm_Build_Dir.x,(int)Norm_Build_Dir.y,(int)Norm_Build_Dir.z);

					BlockPos New_Pos = M_BlockPos.offset(Norm_Build_Dir_I);

					// Protection Lantern: if either the zombie's own block or the block it
					// wants to place is in a protected chunk, do nothing at all.
					// (Otherwise the zombie would still be lifted up without a pillar.)
					if (!SiegeProtection.isProtected(Server_Level, M_BlockPos)
							&& !SiegeProtection.isProtected(Server_Level, New_Pos)) {
						// Do not rise unless a block was actually placed. This also
						// prevents moving into/through a Monolith Core protected chunk.
						MobPlaceBlock placement = new MobPlaceBlock(Entity_Class, Block_Type, New_Pos);
						if (placement.wasPlaced()) {
							M.setPos(M.blockPosition().getX(), M.blockPosition().getY() + 1,
									M.blockPosition().getZ());
							new SnapToBlockCenter(Entity_Class);
							M.swing(M.getUsedItemHand());
							isDoing = true;
						}
					}
				}
			}
		}
	}

	// Get distance from target - is this monster close enough to start digging up?
	boolean isCloseEnough(Mob M, Entity Tgt) {

		// Get x and z for monster and target
		double M_X = M.blockPosition().getX();
		double M_Z = M.blockPosition().getZ();
		double Tgt_X = Tgt.blockPosition().getX();
		double Tgt_Z = Tgt.blockPosition().getZ();

		return (Math.abs(M_X - Tgt_X) < BuildUpDist) && (Math.abs(M_Z - Tgt_Z) < BuildUpDist);

	}
	
	
	//Player must be over 1 block above monster
	boolean isPlayerHighEnough(Mob M, Entity Tgt)
	{
		int Dist = Math.abs(M.blockPosition().getY() - Tgt.blockPosition().getY());
		return Dist > 1;
	}

	// Is this monster below the player?
	boolean isBelowPlayer(Mob M, Entity Tgt) {
		// Get monster block position
		BlockPos M_BlockPos = M.blockPosition();

		// Get target block position
		BlockPos T_BlockPos = Tgt.blockPosition();

		// if target block position is above zombie, then, build up
		return M_BlockPos.getY() < T_BlockPos.getY();
	}

	// Get build direction for zombie blocks
	// M - This zombie
	// Tgt - The zombie's current target
	Vec3 GetBuildDir(Mob M, Entity Tgt) {

		// Get target block position
		BlockPos Tgt_Block_Pos = Tgt.blockPosition();

		// Tgt X,Y,Z
		float Tgt_X = Tgt_Block_Pos.getX();
		float Tgt_Y = Tgt_Block_Pos.getY();
		float Tgt_Z = Tgt_Block_Pos.getZ();

		// Get zombie block position
		BlockPos M_Block_Pos = M.blockPosition();

		// Zombie X,Y,Z
		float Z_X = M_Block_Pos.getX();
		float Z_Y = M_Block_Pos.getY();
		float Z_Z = M_Block_Pos.getZ();

		// Get normalized direction
		float Diff_X = Tgt_X - Z_X;
		float Diff_Y = Tgt_Y - Z_Y;
		float Diff_Z = Tgt_Z - Z_Z;
		Vec3 BlockDir = new Vec3(Diff_X, Diff_Y, Diff_Z);
		BlockDir = BlockDir.normalize();

		return BlockDir;
	}

}
