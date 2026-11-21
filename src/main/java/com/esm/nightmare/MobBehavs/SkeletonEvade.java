package com.esm.nightmare.MobBehavs;

import java.util.ArrayList;
import java.util.List;

import com.esm.nightmare.ESMConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Skeletons sometimes leap away (back or sideways) when the player gets too close.
 *
 * - A dodge is usually 1-2 leaps in a row, and rarely 3 (config: skeletonEvadeTripleChance).
 * - After a dodge ends there is a cooldown of 10-15 seconds (config) before the next one.
 * - Checked twice a second with a chance, so it stays "now and then".
 *
 * State is kept in the mob's persistent data, so nothing needs cleaning up.
 */
public class SkeletonEvade {

	private static final String READY = "nesm_evade_ready"; // game time when a new dodge may start
	private static final String LEFT = "nesm_evade_left"; // leaps still to do in this dodge
	private static final String NEXT = "nesm_evade_next"; // game time of the next leap in this dodge

	private final MobListUtil mobList = new MobListUtil();

	@SubscribeEvent
	public void onTick(LivingEvent.LivingTickEvent event) {
		if (!(event.getEntity() instanceof AbstractSkeleton) || !ESMConfig.SkeletonEvade.get()) {
			return;
		}
		Mob mob = (Mob) event.getEntity();
		Level level = mob.level();
		if (level.isClientSide() || !mob.isAlive() || !(level instanceof ServerLevel)) {
			return;
		}
		if (!mobList.contains(ESMConfig.SkeletonEvadeMobs.get(), mob)) {
			return;
		}

		long now = level.getGameTime();
		CompoundTag data = mob.getPersistentData();
		// v1.7.0: while the skeleton is in its sword stance (flee / hit and run) that behaviour decides every move
		if (data.getBoolean("nesm_sword_stance")) {
			return;
		}
		int left = data.getInt(LEFT);

		LivingEntity target = mob.getTarget();
		if (!(target instanceof ServerPlayer) || !target.isAlive() || ((ServerPlayer) target).isCreative()
				|| ((ServerPlayer) target).isSpectator()) {
			if (left > 0) {
				endDodge(mob, data, now);
			}
			return;
		}
		double dist = mob.distanceTo(target);
		double trigger = ESMConfig.SkeletonEvadeTriggerDist.get();

		// In the middle of a dodge: do the next leap once it has landed
		if (left > 0) {
			if (now < data.getLong(NEXT) || !mob.onGround()) {
				return;
			}
			if (dist > trigger * 1.8D || !leap(mob, target)) {
				endDodge(mob, data, now);
				return;
			}
			left--;
			if (left <= 0) {
				endDodge(mob, data, now);
			} else {
				data.putInt(LEFT, left);
				data.putLong(NEXT, now + 8 + mob.getRandom().nextInt(5));
			}
			return;
		}

		// Not dodging: check twice a second
		if (now < data.getLong(READY) || (mob.tickCount + mob.getId()) % 10 != 0) {
			return;
		}
		if (dist > trigger || !mob.onGround() || mob.isPassenger()) {
			return;
		}
		if (mob.getRandom().nextInt(100) >= ESMConfig.SkeletonEvadeChance.get()) {
			return;
		}

		// How many leaps this time: usually 1-2, rarely 3
		int steps;
		if (mob.getRandom().nextInt(100) < ESMConfig.SkeletonEvadeTripleChance.get()) {
			steps = 3;
		} else {
			steps = mob.getRandom().nextInt(100) < 60 ? 2 : 1;
		}

		if (!leap(mob, target)) {
			// No room to jump: try again a little later instead of every check
			data.putLong(READY, now + 40);
			return;
		}
		if (steps > 1) {
			data.putInt(LEFT, steps - 1);
			data.putLong(NEXT, now + 8 + mob.getRandom().nextInt(5));
		} else {
			endDodge(mob, data, now);
		}
	}

	// The dodge is over: reset and start the cooldown
	private void endDodge(Mob mob, CompoundTag data, long now) {
		data.putInt(LEFT, 0);
		int min = ESMConfig.SkeletonEvadeCooldownMin.get();
		int max = Math.max(min, ESMConfig.SkeletonEvadeCooldownMax.get());
		int seconds = min + mob.getRandom().nextInt(max - min + 1);
		data.putLong(READY, now + seconds * 20L);
	}

	// One big leap away from the player. Returns false if there is nowhere safe to land.
	private boolean leap(Mob mob, LivingEntity target) {
		Vec3 away = new Vec3(mob.getX() - target.getX(), 0.0D, mob.getZ() - target.getZ());
		if (away.lengthSqr() < 1.0E-4D) {
			double angle = mob.getRandom().nextDouble() * Math.PI * 2.0D;
			away = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
		}
		away = away.normalize();
		double sign = mob.getRandom().nextBoolean() ? 1.0D : -1.0D;
		Vec3 side = new Vec3(-away.z, 0.0D, away.x).scale(sign);
		Vec3 diagA = away.add(side).normalize();
		Vec3 diagB = away.subtract(side).normalize();

		// Mostly backwards, sometimes sideways
		List<Vec3> candidates = new ArrayList<>();
		if (mob.getRandom().nextInt(100) < 55) {
			candidates.add(away);
			candidates.add(diagA);
			candidates.add(diagB);
			candidates.add(side);
		} else {
			candidates.add(side);
			candidates.add(side.scale(-1.0D));
			candidates.add(diagA);
			candidates.add(away);
		}

		double stepDist = ESMConfig.SkeletonEvadeStepDist.get();
		for (Vec3 dir : candidates) {
			if (canLand(mob, dir, stepDist)) {
				// About 6.8 blocks of travel per 1.0 of speed for this jump height
				double speed = stepDist / 6.8D;
				mob.getNavigation().stop();
				mob.setDeltaMovement(dir.x * speed, 0.38D, dir.z * speed);
				mob.hurtMarked = true;
				((ServerLevel) mob.level()).sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY() + 0.1D,
						mob.getZ(), 6, 0.2D, 0.05D, 0.2D, 0.02D);
				return true;
			}
		}
		return false;
	}

	// Free space on the way, and solid ground (not lava or water, not a big drop) where it would land
	private boolean canLand(Mob mob, Vec3 dir, double stepDist) {
		Level level = mob.level();
		if (!level.noCollision(mob, mob.getBoundingBox().move(dir.x * 2.0D, 0.1D, dir.z * 2.0D))) {
			return false;
		}
		if (!level.noCollision(mob, mob.getBoundingBox().move(dir.x * stepDist, 0.0D, dir.z * stepDist))) {
			return false;
		}
		BlockPos land = BlockPos.containing(mob.getX() + dir.x * stepDist, mob.getY(), mob.getZ() + dir.z * stepDist);
		for (int dy = 0; dy <= 3; dy++) {
			BlockPos pos = land.below(dy);
			BlockState state = level.getBlockState(pos);
			if (!state.getFluidState().isEmpty()) {
				return false;
			}
			if (!state.getCollisionShape(level, pos).isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
