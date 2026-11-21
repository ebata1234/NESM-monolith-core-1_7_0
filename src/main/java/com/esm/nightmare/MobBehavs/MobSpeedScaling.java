package com.esm.nightmare.MobBehavs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.esm.nightmare.ESMConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Raises the base movement speed of L2Hostility mobs according to their level.
 *
 * Normal mobs:      +0% at level 0, rising linearly to +75% at level 100 (the cap).
 * Zombie/Skeleton:  smooth curve through +75% @ 50, +100% @ 100, +150% @ 200 (the cap).
 *
 * L2Hostility is a soft dependency: it is read by reflection, so this mod
 * still loads (and does nothing here) when L2Hostility is not installed.
 * The bonus is a transient MULTIPLY_BASE modifier, so it is rebuilt from the
 * mob's level and never saved.
 */
public class MobSpeedScaling {

	private static final UUID MODIFIER_ID = UUID.fromString("c1d4f6a2-8b37-4e51-9a0c-3d2e7f5b9a64");

	// Smooth curve (monotone cubic) through these points: level -> bonus
	private static final double[] X = { 0, 50, 100, 200 };
	private static final double[] Y = { 0.0, 0.75, 1.00, 1.50 };
	private static final double[] D = tangents(X, Y);

	// L2Hostility access, resolved once
	private static boolean resolved = false;
	private static boolean available = false;
	private static Object holder;
	private static Method holderGet;
	private static Method capGetLevel;

	private String cachedList = null;
	private Set<ResourceLocation> fastMobs = new HashSet<>();

	@SubscribeEvent
	public void onLivingTick(LivingEvent.LivingTickEvent event) {
		Entity e = event.getEntity();
		if (!(e instanceof Mob) || !(e instanceof Enemy) || e.level().isClientSide()) {
			return;
		}
		// Once a second per mob, spread out by entity id
		if ((e.tickCount + e.getId()) % 20 != 0) {
			return;
		}
		Mob mob = (Mob) e;
		AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}

		double bonus = 0.0;
		if (ESMConfig.MobSpeedScaling.get()) {
			int level = getHostilityLevel(mob);
			if (level > 0) {
				bonus = bonusFor(mob, level);
			}
		}
		apply(speed, bonus);
	}

	private double bonusFor(Mob mob, int level) {
		if (isFastCurveMob(mob)) {
			return curve(level);
		}
		int maxLevel = Math.max(1, ESMConfig.MobSpeedScalingMaxLevel.get());
		double max = ESMConfig.MobSpeedScalingMaxBonus.get();
		return max * Math.min(level, maxLevel) / (double) maxLevel;
	}

	private boolean isFastCurveMob(Mob mob) {
		String list = ESMConfig.MobSpeedScalingFastCurveMobs.get();
		if (!list.equals(cachedList)) {
			cachedList = list;
			fastMobs = new HashSet<>();
			for (String s : list.split(",")) {
				s = s.trim();
				if (s.isEmpty()) {
					continue;
				}
				ResourceLocation id = ResourceLocation.tryParse(s.contains(":") ? s : "minecraft:" + s);
				if (id != null) {
					fastMobs.add(id);
				}
			}
		}
		ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
		return key != null && fastMobs.contains(key);
	}

	private static void apply(AttributeInstance speed, double bonus) {
		AttributeModifier old = speed.getModifier(MODIFIER_ID);
		if (old != null && Math.abs(old.getAmount() - bonus) < 1.0e-6) {
			return;
		}
		if (old != null) {
			speed.removeModifier(MODIFIER_ID);
		}
		if (bonus > 0) {
			speed.addTransientModifier(new AttributeModifier(MODIFIER_ID, "NESM level speed", bonus,
					AttributeModifier.Operation.MULTIPLY_BASE));
		}
	}

	// ---- curve -------------------------------------------------------------

	// Zombie/skeleton bonus (0.75 = +75%) at a given L2Hostility level. Capped at the last point.
	public static double curve(double level) {
		if (level <= X[0]) {
			return Y[0];
		}
		if (level >= X[X.length - 1]) {
			return Y[Y.length - 1];
		}
		int i = 0;
		while (level > X[i + 1]) {
			i++;
		}
		double h = X[i + 1] - X[i];
		double t = (level - X[i]) / h;
		double t2 = t * t;
		double t3 = t2 * t;
		return (2 * t3 - 3 * t2 + 1) * Y[i] + (t3 - 2 * t2 + t) * h * D[i] + (-2 * t3 + 3 * t2) * Y[i + 1]
				+ (t3 - t2) * h * D[i + 1];
	}

	// Monotone cubic (Fritsch-Carlson / PCHIP) tangents: passes through every point, never overshoots
	private static double[] tangents(double[] x, double[] y) {
		int n = x.length;
		double[] h = new double[n - 1];
		double[] s = new double[n - 1];
		for (int i = 0; i < n - 1; i++) {
			h[i] = x[i + 1] - x[i];
			s[i] = (y[i + 1] - y[i]) / h[i];
		}
		double[] d = new double[n];
		for (int i = 1; i < n - 1; i++) {
			if (s[i - 1] * s[i] <= 0) {
				d[i] = 0;
			} else {
				double w1 = 2 * h[i] + h[i - 1];
				double w2 = h[i] + 2 * h[i - 1];
				d[i] = (w1 + w2) / (w1 / s[i - 1] + w2 / s[i]);
			}
		}
		d[0] = endTangent(h[0], h[1], s[0], s[1]);
		d[n - 1] = endTangent(h[n - 2], h[n - 3], s[n - 2], s[n - 3]);
		return d;
	}

	private static double endTangent(double h0, double h1, double s0, double s1) {
		double d = ((2 * h0 + h1) * s0 - h0 * s1) / (h0 + h1);
		if (d * s0 <= 0) {
			return 0;
		}
		if (s0 * s1 <= 0 && Math.abs(d) > 3 * Math.abs(s0)) {
			return 3 * s0;
		}
		return d;
	}

	// ---- L2Hostility (reflection) -----------------------------------------

	public static int getHostilityLevel(Mob mob) {
		if (!resolved) {
			resolve();
		}
		if (!available) {
			return 0;
		}
		try {
			Object cap = holderGet.invoke(holder, mob);
			if (cap == null) {
				return 0;
			}
			Object level = capGetLevel.invoke(cap);
			return level instanceof Integer ? (Integer) level : 0;
		} catch (Throwable t) {
			// Mob without the capability (or it is not ready yet): treat as level 0
			return 0;
		}
	}

	private static synchronized void resolve() {
		if (resolved) {
			return;
		}
		resolved = true;
		try {
			Class<?> capClass = Class.forName("dev.xkmc.l2hostility.content.capability.mob.MobTraitCap");
			Field f = capClass.getField("HOLDER");
			holder = f.get(null);
			for (Method m : holder.getClass().getMethods()) {
				if (m.getName().equals("get") && m.getParameterCount() == 1) {
					holderGet = m;
					break;
				}
			}
			capGetLevel = capClass.getMethod("getLevel");
			available = holder != null && holderGet != null;
		} catch (Throwable t) {
			available = false;
		}
	}
}
