package com.esm.nightmare.MobBehavs;

import com.esm.nightmare.ESMConfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * A few zombies spawn carrying an ender pearl (in the off hand). When their
 * target is far enough away but in sight, they throw it at the player and
 * teleport to where it lands (vanilla pearl behaviour for a non-player owner).
 * Each zombie has one pearl and throws it once.
 */
public class ZombieEnderPearl {

	private static final String ROLLED = "nesm_pearl_rolled";

	private final MobListUtil mobList = new MobListUtil();

	// Decide once, when the mob first spawns
	@SubscribeEvent
	public void onSpawn(MobSpawnEvent.FinalizeSpawn event) {
		Mob mob = event.getEntity();
		if (!ESMConfig.ZombieEnderPearl.get() || mob.level().isClientSide()) {
			return;
		}
		if (!mobList.contains(ESMConfig.ZombieEnderPearlMobs.get(), mob)) {
			return;
		}
		CompoundTag data = mob.getPersistentData();
		if (data.getBoolean(ROLLED)) {
			return;
		}
		data.putBoolean(ROLLED, true);

		if (mob.getRandom().nextDouble() * 100.0 < ESMConfig.ZombieEnderPearlChance.get()) {
			mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ENDER_PEARL));
			mob.setDropChance(EquipmentSlot.OFFHAND, ESMConfig.ZombieEnderPearlDropChance.get().floatValue());
		}
	}

	@SubscribeEvent
	public void onTick(LivingEvent.LivingTickEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Mob) || entity.level().isClientSide() || !entity.isAlive()) {
			return;
		}
		Mob mob = (Mob) entity;
		// Twice a second, spread out by entity id
		if ((mob.tickCount + mob.getId()) % 10 != 0) {
			return;
		}
		if (!mob.getOffhandItem().is(Items.ENDER_PEARL) || mob.isPassenger()) {
			return;
		}
		LivingEntity target = mob.getTarget();
		if (!(target instanceof ServerPlayer)) {
			return;
		}
		ServerPlayer player = (ServerPlayer) target;
		if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
			return;
		}
		double dist = mob.distanceTo(player);
		if (dist < ESMConfig.ZombieEnderPearlMinDist.get() || dist > ESMConfig.ZombieEnderPearlMaxDist.get()) {
			return;
		}
		if (!mob.hasLineOfSight(player)) {
			return;
		}
		throwPearl(mob, player);
	}

	private void throwPearl(Mob mob, ServerPlayer player) {
		ServerLevel level = (ServerLevel) mob.level();

		ThrownEnderpearl pearl = new ThrownEnderpearl(level, mob);
		double dx = player.getX() - mob.getX();
		double dz = player.getZ() - mob.getZ();
		double horizontal = Math.sqrt(dx * dx + dz * dz);
		double dy = player.getY(0.5D) - pearl.getY();
		// A thrown pearl drops as it flies: aim a little higher the farther away the player is
		double lift = 0.0065D * horizontal * horizontal;
		pearl.shoot(dx, dy + lift, dz, 1.6F, 1.0F);
		level.addFreshEntity(pearl);

		level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.HOSTILE,
				1.0F, 0.4F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
		mob.swing(InteractionHand.OFF_HAND);

		// The pearl is used up
		mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
	}
}
