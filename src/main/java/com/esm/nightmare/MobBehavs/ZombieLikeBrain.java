package com.esm.nightmare.MobBehavs;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.esm.nightmare.ESMConfig;

/**
 * Piglins decide who to attack with their own "brain", not with the normal target goals.
 * NESM sets the normal target (Mob.setTarget), so for piglins in the zombie-like list we
 * copy it into the brain. They then walk up to and attack the player like zombies do,
 * even if the player wears gold.
 */
public class ZombieLikeBrain {

	@SubscribeEvent
	public void onTick(LivingEvent.LivingTickEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof AbstractPiglin) || entity.level().isClientSide() || !entity.isAlive()) {
			return;
		}
		AbstractPiglin piglin = (AbstractPiglin) entity;
		if (!ZombieLikeMobs.isZombieLike(piglin)) {
			return;
		}
		LivingEntity target = piglin.getTarget();
		// A survival player, or (v1.7.0) a villager
		if (!ApproachTarget.isValid(target)) {
			return;
		}
		piglin.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
	}

	// Piglins turn into zombified piglins in the Overworld; keep them as they are
	@SubscribeEvent
	public void onJoin(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide() || !(event.getEntity() instanceof AbstractPiglin)) {
			return;
		}
		AbstractPiglin piglin = (AbstractPiglin) event.getEntity();
		if (ESMConfig.ZombieLikeNoZombification.get() && ZombieLikeMobs.isZombieLike(piglin)) {
			piglin.setImmuneToZombification(true);
		}
	}
}
