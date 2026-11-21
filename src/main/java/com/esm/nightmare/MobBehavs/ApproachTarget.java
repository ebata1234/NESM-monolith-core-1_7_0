package com.esm.nightmare.MobBehavs;

import com.esm.nightmare.ESMConfig;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.AbstractVillager;

/**
 * v1.7.0: who a siege mob digs, bridges and pillars toward.
 * A survival-mode player, or (config approachVillagers) a villager.
 * No line of sight is needed: the mob only has to have the target set (MobTargetPlayer).
 */
public final class ApproachTarget {

	private ApproachTarget() {
	}

	/** The mob's current target if it is one we approach, else null. */
	public static LivingEntity get(Mob mob) {
		LivingEntity target = mob.getTarget();
		return isValid(target) ? target : null;
	}

	public static boolean isValid(LivingEntity target) {
		if (target == null || !target.isAlive()) {
			return false;
		}
		if (target instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) target;
			return !player.isCreative() && !player.isSpectator();
		}
		return ESMConfig.ApproachVillagers.get() && target instanceof AbstractVillager;
	}

	/**
	 * The cardinal directions toward (dx, dz), the longer axis first. The second axis is only
	 * listed when the target is clearly off to that side (1.5 blocks or more).
	 */
	public static Direction[] directionsToward(double dx, double dz) {
		Direction x = dx >= 0.0D ? Direction.EAST : Direction.WEST;
		Direction z = dz >= 0.0D ? Direction.SOUTH : Direction.NORTH;
		double ax = Math.abs(dx);
		double az = Math.abs(dz);
		if (ax >= az) {
			return az >= 1.5D ? new Direction[] { x, z } : new Direction[] { x };
		}
		return ax >= 1.5D ? new Direction[] { z, x } : new Direction[] { z };
	}
}
