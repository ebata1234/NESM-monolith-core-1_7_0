package com.esm.nightmare.MobBehavs;

import com.esm.nightmare.ESMConfig;
import com.esm.nightmare.siege.SiegeProtection;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Creepers that blow a wall on purpose.
 *
 * v1.7.0 rules:
 *  - The creeper notices its target without line of sight (MobTargetPlayer does that).
 *  - It only blasts a wall on purpose when a SURVIVAL PLAYER is within creeperBreachPlayerDist
 *    (16 blocks). Before, any target within 64 blocks was enough, so creepers could blow
 *    themselves up with nobody around.
 *  - The wall is a solid block on the straight line between the creeper and the player, within
 *    creeperBreachWallDist (4 blocks) of the creeper. If it is still like that after
 *    obstructedCreeperExpTicks (3 seconds) the creeper ignites on the spot.
 *  - It never does so if the blast could reach a Monolith Core protected chunk
 *    (creeperBreachRespectsMonolith).
 *  - Unchanged: a creeper within creeperExplodeDist of the player, and one hovering above the
 *    player, still ignite as before (a player is right there).
 *
 * The checks run every 4 ticks per creeper (a ray cast every tick was too much).
 */
public class CreeperBreachWalls {

	private static final int CHECK_INTERVAL = 4;

	public CreeperBreachWalls(Creeper C) {
		if (C.level().isClientSide || ((C.tickCount + C.getId()) % CHECK_INTERVAL) != 0) {
			return;
		}

		// Only a survival player counts: nothing is blown up on purpose for a villager or for nobody
		LivingEntity Creeper_Target = C.getTarget();
		if (!(Creeper_Target instanceof ServerPlayer)) {
			ResetTicks(C);
			return;
		}
		ServerPlayer Plyr = (ServerPlayer) Creeper_Target;
		if (Plyr.isCreative() || Plyr.isSpectator() || !Plyr.isAlive() || Plyr.level() != C.level()) {
			ResetTicks(C);
			return;
		}

		float Dist_To_Tgt = C.distanceTo(Plyr);
		boolean striking = isWithinStrikingDistance(C, Plyr);
		boolean walled = Dist_To_Tgt < ESMConfig.CreeperBreachPlayerDist.get() && isWallBetween(C, Plyr);

		if (striking || walled) {
			int Ticks = GetTicks(C) + CHECK_INTERVAL;
			SetTicks(C, Ticks);
			if (Ticks > ESMConfig.CreeperObstructedExplodeTicks.get()) {
				// A blast made only to open a wall must not reach the Monolith Core chunks
				if (!striking && ESMConfig.CreeperBreachRespectsMonolith.get() && blastMayReachProtected(C)) {
					ResetTicks(C);
				} else {
					new CreeperExplode(C);
				}
			}
		} else {
			ResetTicks(C);
		}

		// If the creeper is directly above the player and within distance, then
		if (isHoveringAbove(C, Plyr)) {
			new CreeperExplode(C);
		}
	}

	// Set number of ticks for creeper
	void SetTicks(Creeper C, int Amt) {
		C.getPersistentData().putInt("nightmare_ticks", Amt);
	}

	// Reset this creeper's ticks for being against a wall
	void ResetTicks(Creeper C) {
		SetTicks(C, 0);
	}

	// Get current amount of ticks this creeper has been blocked / close to the player
	int GetTicks(Creeper C) {
		return C.getPersistentData().getInt("nightmare_ticks");
	}

	// A solid block lies on the straight line to the player, close to the creeper
	boolean isWallBetween(Creeper C, ServerPlayer Player_Target) {
		Level level = C.level();
		Vec3 from = C.getEyePosition();
		Vec3 to = Player_Target.getEyePosition();
		BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, C));
		if (hit.getType() != HitResult.Type.BLOCK) {
			return false;
		}
		double wall = ESMConfig.CreeperBreachWallDist.get();
		return hit.getLocation().distanceToSqr(from) <= wall * wall;
	}

	// Could the blast of this creeper (radius from its own data, doubled when charged) reach a protected chunk?
	boolean blastMayReachProtected(Creeper C) {
		Level level = C.level();
		int radius = C.serializeNBT().getByte("ExplosionRadius");
		if (radius <= 0) {
			radius = 3;
		}
		if (C.isPowered()) {
			radius *= 2;
		}
		// Absurdly big blasts (e.g. nuclear creepers): do not take the risk
		if (radius > 64) {
			return true;
		}
		int margin = radius + 1;
		BlockPos centre = C.blockPosition();
		// A grid with 8 block steps (half a chunk) cannot step over a protected chunk
		for (int ox = -margin; ; ox += 8) {
			int x = Math.min(ox, margin);
			for (int oz = -margin; ; oz += 8) {
				int z = Math.min(oz, margin);
				if (SiegeProtection.isProtected(level, centre.offset(x, 0, z))) {
					return true;
				}
				if (oz >= margin) {
					break;
				}
			}
			if (ox >= margin) {
				break;
			}
		}
		return false;
	}

	// Is this creeper hovering above the player?
	boolean isHoveringAbove(Creeper C, ServerPlayer Player_Target) {

		BlockPos Creeper_Pos = C.blockPosition();
		BlockPos Player_Pos = Player_Target.blockPosition();

		// X difference and Z difference
		float x_diff = Math.abs(Creeper_Pos.getX() - Player_Pos.getX());
		float z_diff = Math.abs(Creeper_Pos.getZ() - Player_Pos.getZ());

		// If within distance square and player is below creeper, then
		return ((x_diff < ESMConfig.CreeperAboveExplodeDistance.get() && z_diff < ESMConfig.CreeperAboveExplodeDistance.get()) && Player_Pos.getY() < Creeper_Pos.getY());
	}

	// Is this creeper within a certain distance where it can explode and harm the player?
	boolean isWithinStrikingDistance(Creeper C, ServerPlayer Player_Target) {
		float Creeper_Dist = C.distanceTo(Player_Target);
		return Creeper_Dist < ESMConfig.CreeperStrikingDistance.get();
	}
}
