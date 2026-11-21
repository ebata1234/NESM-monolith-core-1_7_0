package com.esm.nightmare.MobBehavs;

import com.esm.nightmare.ESMConfig;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Hostile mobs whose default detection range (follow range) is below the
 * configured minimum (64 blocks) get raised to that minimum. Mobs that already
 * have 64 or more are left alone.
 */
public class MobFollowRange {

	@SubscribeEvent
	public void onJoin(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide() || !ESMConfig.MobFollowRange.get()) {
			return;
		}
		if (!(event.getEntity() instanceof Mob) || !(event.getEntity() instanceof Enemy)) {
			return;
		}
		Mob mob = (Mob) event.getEntity();
		AttributeInstance range = mob.getAttribute(Attributes.FOLLOW_RANGE);
		if (range == null) {
			return;
		}
		double min = ESMConfig.MobFollowRangeMin.get();
		if (range.getBaseValue() < min) {
			range.setBaseValue(min);
		}
	}
}
