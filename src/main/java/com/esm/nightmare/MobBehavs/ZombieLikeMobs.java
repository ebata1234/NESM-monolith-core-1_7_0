package com.esm.nightmare.MobBehavs;

import com.esm.nightmare.ESMConfig;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;

/**
 * Mobs (besides real zombies) that get the zombie behaviour of NESM:
 * digging, pillaring, bridging, TNT, fire, and walking to the player.
 * The list is in the config (zombieLikeMobs), by default piglin and piglin brute.
 */
public final class ZombieLikeMobs {

	private static final MobListUtil LIST = new MobListUtil();

	private ZombieLikeMobs() {
	}

	/** In the config list (real zombies are not counted here; they already work). */
	public static boolean isZombieLike(Entity entity) {
		return !(entity instanceof Zombie) && LIST.contains(ESMConfig.ZombieLikeMobs.get(), entity);
	}

	/** Gets the zombie digging/building behaviour. */
	public static boolean usesZombieBehavior(Entity entity) {
		return entity instanceof Zombie || isZombieLike(entity);
	}
}
