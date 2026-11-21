package com.esm.nightmare.MobBehavs;

import java.util.EnumSet;

import com.esm.nightmare.ESMConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Skeleton close-range behaviour (rewritten in v1.7.0).
 *
 * Player within skeletonFleeDist (6 blocks):
 *   the bow goes to the off hand, a sword to the main hand, and the skeleton RUNS AWAY WITH ITS BACK
 *   TO THE PLAYER at skeletonFleeSpeed until it is skeletonFleeUntilDist (10 blocks) away. Then the
 *   bow comes back and it shoots.
 *
 * Player within skeletonHitAndRunDist (4 blocks) - and only then:
 *   hit and run. The skeleton leaps at the player, strikes as soon as it is within
 *   skeletonLungeReach, then hops back out of the player's 3-block reach and keeps running.
 *   It waits skeletonLungeCooldownMin..Max ticks between two leaps (that is when it runs for the
 *   bow range). A skeleton that cannot run anywhere (cornered) leaps again sooner.
 *
 * The sword is iron at the lowest. With L2Hostility the sword gets stronger (diamond, netherite)
 * and enchanted as the mob's level goes up. The sword is never dropped.
 */
public class SkeletonMeleeStance {

	private static final String FLAG = "nesm_sword_stance";

	private final MobListUtil mobList = new MobListUtil();

	@SubscribeEvent
	public void onJoin(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide() || !(event.getEntity() instanceof AbstractSkeleton)) {
			return;
		}
		AbstractSkeleton skeleton = (AbstractSkeleton) event.getEntity();
		if (!mobList.contains(ESMConfig.SkeletonSwordMobs.get(), skeleton)) {
			return;
		}
		// The world was saved while it held the sword: give the bow back
		if (skeleton.getPersistentData().getBoolean(FLAG)) {
			restoreBow(skeleton);
		}
		for (WrappedGoal wrapped : skeleton.goalSelector.getAvailableGoals()) {
			if (wrapped.getGoal() instanceof StanceGoal) {
				return;
			}
		}
		// Priority 1: wins over the bow goal and the melee goal (both priority 4)
		skeleton.goalSelector.addGoal(1, new StanceGoal(skeleton));
	}

	private static void restoreBow(AbstractSkeleton skeleton) {
		ItemStack off = skeleton.getOffhandItem();
		if (off.getItem() instanceof BowItem) {
			skeleton.setItemSlot(EquipmentSlot.MAINHAND, off);
			skeleton.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
			skeleton.setDropChance(EquipmentSlot.MAINHAND, 0.085F);
		}
		skeleton.getPersistentData().remove(FLAG);
	}

	private static class StanceGoal extends Goal {

		private static final int FLEE = 0;
		private static final int LUNGE = 1;
		private static final int RETREAT = 2;

		private final AbstractSkeleton mob;

		private int phase = FLEE;
		private int phaseTicks = 0;
		private int cooldown = 0;     // ticks until the next leap at the player
		private int repath = 0;       // ticks until the flee route is looked at again
		private int cornered = 0;     // how many times in a row no flee route was found
		private boolean struck = false;
		private boolean hopped = false;

		StanceGoal(AbstractSkeleton mob) {
			this.mob = mob;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		private ServerPlayer target() {
			LivingEntity t = mob.getTarget();
			if (t instanceof ServerPlayer && t.isAlive() && !((ServerPlayer) t).isCreative()
					&& !((ServerPlayer) t).isSpectator()) {
				return (ServerPlayer) t;
			}
			return null;
		}

		@Override
		public boolean canUse() {
			if (!ESMConfig.SkeletonSword.get() || !mob.isAlive() || mob.isPassenger()) {
				return false;
			}
			ServerPlayer t = target();
			if (t == null) {
				return false;
			}
			// Only a skeleton that is holding its bow (and has a free off hand) switches
			if (!(mob.getMainHandItem().getItem() instanceof BowItem) || !mob.getOffhandItem().isEmpty()) {
				return false;
			}
			return mob.distanceTo(t) <= ESMConfig.SkeletonFleeDist.get();
		}

		@Override
		public boolean canContinueToUse() {
			ServerPlayer t = target();
			return ESMConfig.SkeletonSword.get() && mob.isAlive() && !mob.isPassenger() && t != null
					&& mob.distanceTo(t) < ESMConfig.SkeletonFleeUntilDist.get();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void start() {
			phase = FLEE;
			phaseTicks = 0;
			cooldown = 10; // a short moment of running before the first leap
			repath = 0;
			cornered = 0;
			struck = false;
			hopped = false;
			ItemStack bow = mob.getMainHandItem();
			mob.stopUsingItem();
			mob.setItemSlot(EquipmentSlot.OFFHAND, bow);
			mob.setDropChance(EquipmentSlot.OFFHAND, 0.085F);
			mob.setItemSlot(EquipmentSlot.MAINHAND, makeSword());
			mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
			mob.getPersistentData().putBoolean(FLAG, true);
			mob.setAggressive(false);
		}

		@Override
		public void stop() {
			restoreBow(mob);
			mob.setAggressive(false);
			mob.getNavigation().stop();
			mob.setXxa(0.0F);
			mob.setZza(0.0F);
		}

		@Override
		public void tick() {
			LivingEntity t = mob.getTarget();
			if (t == null) {
				return;
			}
			phaseTicks++;
			if (cooldown > 0) {
				cooldown--;
			}
			double dist = mob.distanceTo(t);

			if (phase == LUNGE) {
				tickLunge(t);
				return;
			}
			if (phase == RETREAT) {
				tickRetreat(t, dist);
				return;
			}

			// FLEE: back to the player, running
			mob.setAggressive(false);
			if (dist <= ESMConfig.SkeletonHitAndRunDist.get() && cooldown <= 0 && mob.onGround()) {
				if (startLunge(t)) {
					return;
				}
				cooldown = 8; // no room to leap: try again in a moment
			}
			flee(t);
		}

		// ---------------------------------------------------------------- leap at the player

		private boolean startLunge(LivingEntity t) {
			Vec3 toward = new Vec3(t.getX() - mob.getX(), 0.0D, t.getZ() - mob.getZ());
			double horizontal = toward.length();
			if (horizontal < 1.0E-3D) {
				return false;
			}
			toward = toward.scale(1.0D / horizontal);
			// Land next to the player (about one block short of its centre)
			double travel = Math.max(1.0D, Math.min(horizontal - 0.9D, 4.5D));
			if (!canLeap(toward, travel, false)) {
				return false;
			}
			mob.getNavigation().stop();
			mob.getLookControl().setLookAt(t, 60.0F, 60.0F);
			mob.lookAt(t, 60.0F, 60.0F);
			double speed = Math.max(0.22D, travel / 6.8D);
			mob.setDeltaMovement(toward.x * speed, 0.38D, toward.z * speed);
			mob.hurtMarked = true;
			mob.setAggressive(true);
			phase = LUNGE;
			phaseTicks = 0;
			struck = false;
			return true;
		}

		private void tickLunge(LivingEntity t) {
			// In the air toward the player: face it, and strike as soon as it is within reach
			mob.getLookControl().setLookAt(t, 60.0F, 60.0F);
			mob.lookAt(t, 60.0F, 60.0F);
			mob.setAggressive(true);
			if (!struck && mob.distanceTo(t) <= ESMConfig.SkeletonLungeReach.get() && mob.hasLineOfSight(t)) {
				mob.swing(InteractionHand.MAIN_HAND);
				mob.doHurtTarget(t);
				struck = true;
				startRetreat();
				return;
			}
			// Landed (or took too long) without a hit: back off anyway
			if (phaseTicks > 16 || (phaseTicks > 4 && mob.onGround())) {
				startRetreat();
			}
		}

		// ---------------------------------------------------------------- away again

		private void startRetreat() {
			phase = RETREAT;
			phaseTicks = 0;
			hopped = false;
			int min = ESMConfig.SkeletonLungeCooldownMin.get();
			int max = Math.max(min, ESMConfig.SkeletonLungeCooldownMax.get());
			cooldown = cornered >= 3 ? Math.min(15, min) : min + mob.getRandom().nextInt(max - min + 1);
			mob.setAggressive(false);
		}

		private void tickRetreat(LivingEntity t, double dist) {
			mob.setAggressive(false);
			if (!mob.onGround()) {
				return; // still flying
			}
			// One hop straight back as soon as it stands on the ground again
			if (!hopped) {
				hopped = true;
				if (hopBack(t)) {
					return;
				}
			}
			// Out of the player's reach (3 blocks) with some margin: back to running for the bow range
			if (phaseTicks > 30 || (dist >= 4.6D && phaseTicks > 3)) {
				phase = FLEE;
				phaseTicks = 0;
				repath = 0;
				return;
			}
			flee(t);
		}

		private boolean hopBack(LivingEntity t) {
			Vec3 away = awayFrom(t);
			Vec3 side = new Vec3(-away.z, 0.0D, away.x);
			Vec3 diagA = away.add(side).normalize();
			Vec3 diagB = away.subtract(side).normalize();
			double step = 4.5D;
			for (Vec3 dir : new Vec3[] { away, diagA, diagB }) {
				if (canLeap(dir, step, true)) {
					mob.getNavigation().stop();
					double speed = step / 6.8D;
					mob.setDeltaMovement(dir.x * speed, 0.38D, dir.z * speed);
					mob.hurtMarked = true;
					if (mob.level() instanceof ServerLevel) {
						((ServerLevel) mob.level()).sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY() + 0.1D,
								mob.getZ(), 6, 0.2D, 0.05D, 0.2D, 0.02D);
					}
					return true;
				}
			}
			return false;
		}

		// ---------------------------------------------------------------- running away

		private Vec3 awayFrom(LivingEntity t) {
			Vec3 away = new Vec3(mob.getX() - t.getX(), 0.0D, mob.getZ() - t.getZ());
			if (away.lengthSqr() < 1.0E-4D) {
				double angle = mob.getRandom().nextDouble() * Math.PI * 2.0D;
				return new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
			}
			return away.normalize();
		}

		/** Run away from the player, back to it. Never looks at the player on the way. */
		private void flee(LivingEntity t) {
			if (!mob.onGround()) {
				return;
			}
			if (--repath > 0 && !mob.getNavigation().isDone()) {
				return;
			}
			repath = 8;
			Vec3 away = awayFrom(t);
			double speed = ESMConfig.SkeletonFleeSpeed.get();
			double[] angles = { 0.0D, 0.7D, -0.7D, 1.4D, -1.4D };
			for (double angle : angles) {
				double cos = Math.cos(angle);
				double sin = Math.sin(angle);
				double dx = away.x * cos - away.z * sin;
				double dz = away.x * sin + away.z * cos;
				double x = mob.getX() + dx * 10.0D;
				double z = mob.getZ() + dz * 10.0D;
				Double y = floorY(x, z);
				if (y != null && mob.getNavigation().moveTo(x, y, z, speed)) {
					cornered = 0;
					return;
				}
			}
			// No route anywhere: step back by hand if there is room, and remember that it is cornered
			cornered++;
			manualStepBack(away);
		}

		/** The y of a standable block near the mob's height at (x, z), or null (void, lava, water, no chunk). */
		private Double floorY(double x, double z) {
			Level level = mob.level();
			BlockPos column = BlockPos.containing(x, mob.getY(), z);
			if (!level.hasChunkAt(column)) {
				return null;
			}
			int top = mob.getBlockY() + 3;
			int bottom = mob.getBlockY() - 6;
			for (int y = top; y >= bottom; y--) {
				BlockPos pos = new BlockPos(column.getX(), y, column.getZ());
				BlockState state = level.getBlockState(pos);
				if (!state.getFluidState().isEmpty()) {
					return null;
				}
				if (!state.getCollisionShape(level, pos).isEmpty()) {
					BlockState above = level.getBlockState(pos.above());
					if (above.getCollisionShape(level, pos.above()).isEmpty() && above.getFluidState().isEmpty()) {
						return (double) (y + 1);
					}
					return null;
				}
			}
			return null;
		}

		private void manualStepBack(Vec3 away) {
			if (!mob.onGround() || !canStepBack(away)) {
				return;
			}
			double speed = mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.9D;
			Vec3 motion = mob.getDeltaMovement();
			mob.setDeltaMovement(away.x * speed, motion.y, away.z * speed);
		}

		// Free space behind, and ground (no lava, water or big drop) to stand on
		private boolean canStepBack(Vec3 away) {
			Level level = mob.level();
			if (!level.noCollision(mob, mob.getBoundingBox().move(away.x * 0.6D, 0.0D, away.z * 0.6D))) {
				return false;
			}
			BlockPos probe = BlockPos.containing(mob.getX() + away.x * 1.2D, mob.getY(), mob.getZ() + away.z * 1.2D);
			for (int dy = 0; dy <= 2; dy++) {
				BlockPos pos = probe.below(dy);
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

		/**
		 * Is there room for a leap of this length: free space on the way and solid dry ground where it
		 * would come down? (towardPlayer: the landing may be right next to the player.)
		 */
		private boolean canLeap(Vec3 dir, double dist, boolean away) {
			Level level = mob.level();
			if (!level.noCollision(mob, mob.getBoundingBox().move(dir.x * Math.min(2.0D, dist), 0.1D, dir.z * Math.min(2.0D, dist)))) {
				return false;
			}
			if (away && !level.noCollision(mob, mob.getBoundingBox().move(dir.x * dist, 0.0D, dir.z * dist))) {
				return false;
			}
			BlockPos land = BlockPos.containing(mob.getX() + dir.x * dist, mob.getY(), mob.getZ() + dir.z * dist);
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

		// Iron at least; diamond / netherite and enchantments as the L2Hostility level rises
		private ItemStack makeSword() {
			int level = MobSpeedScaling.getHostilityLevel(mob);
			Item item = Items.IRON_SWORD;
			if (level >= ESMConfig.SkeletonSwordNetheriteLevel.get()) {
				item = Items.NETHERITE_SWORD;
			} else if (level >= ESMConfig.SkeletonSwordDiamondLevel.get()) {
				item = Items.DIAMOND_SWORD;
			}
			ItemStack sword = new ItemStack(item);

			int step = Math.max(1, ESMConfig.SkeletonSwordSharpnessStep.get());
			int sharpness = Math.min(5, level / step);
			if (sharpness > 0) {
				sword.enchant(Enchantments.SHARPNESS, sharpness);
			}
			if (level >= ESMConfig.SkeletonSwordKnockbackLevel.get()) {
				sword.enchant(Enchantments.KNOCKBACK, 1);
			}
			if (level >= ESMConfig.SkeletonSwordFireAspectLevel.get()) {
				sword.enchant(Enchantments.FIRE_ASPECT, 1);
			}
			return sword;
		}
	}
}
