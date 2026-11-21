package com.esm.nightmare.MobBehavs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.esm.nightmare.ESMConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Napalm creepers: when a creeper explodes, fire is scattered around the blast.
 *
 * The explosion itself is not changed. When a creeper explosion is detected,
 * we remember where it happened and, at the end of the same server tick
 * (after the crater has been made), place fire on the ground around it.
 */
public class CreeperNapalm {

	private static class Pending {
		final ServerLevel level;
		final Vec3 center;

		Pending(ServerLevel level, Vec3 center) {
			this.level = level;
			this.center = center;
		}
	}

	private final List<Pending> pending = new ArrayList<>();
	private final Random random = new Random();

	@SubscribeEvent
	public void onDetonate(ExplosionEvent.Detonate event) {
		if (!ESMConfig.CreeperNapalm.get()) {
			return;
		}
		Level level = event.getLevel();
		if (!(level instanceof ServerLevel)) {
			return;
		}
		Explosion explosion = event.getExplosion();
		if (!(explosion.getIndirectSourceEntity() instanceof Creeper)) {
			return;
		}

		// Everything caught in the blast starts burning
		if (ESMConfig.CreeperNapalmIgnitesEntities.get()) {
			for (Entity entity : event.getAffectedEntities()) {
				if (entity instanceof LivingEntity) {
					entity.setSecondsOnFire(5);
				}
			}
		}

		pending.add(new Pending((ServerLevel) level, explosion.getPosition()));
	}

	// The crater exists by the end of the tick, so fire can be placed now
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || pending.isEmpty()) {
			return;
		}
		List<Pending> todo = new ArrayList<>(pending);
		pending.clear();
		for (Pending p : todo) {
			spreadFire(p);
		}
	}

	private void spreadFire(Pending p) {
		ServerLevel level = p.level;

		// Respect the mobGriefing game rule, like the crater does
		if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
			return;
		}

		int radius = ESMConfig.CreeperNapalmRadius.get();
		int attempts = ESMConfig.CreeperNapalmFires.get();
		BlockPos center = BlockPos.containing(p.center);

		for (int i = 0; i < attempts; i++) {
			// Random spot inside a circle around the blast
			double angle = random.nextDouble() * Math.PI * 2.0;
			double dist = radius * Math.sqrt(random.nextDouble());
			int x = center.getX() + (int) Math.round(Math.cos(angle) * dist);
			int z = center.getZ() + (int) Math.round(Math.sin(angle) * dist);

			// Walk down from above until we find air that can hold fire
			for (int y = center.getY() + radius; y >= center.getY() - radius; y--) {
				BlockPos pos = new BlockPos(x, y, z);
				if (!level.hasChunkAt(pos)) {
					break;
				}
				if (level.isEmptyBlock(pos) && BaseFireBlock.canBePlacedAt(level, pos, Direction.DOWN)) {
					level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
					break;
				}
			}
		}
	}
}
