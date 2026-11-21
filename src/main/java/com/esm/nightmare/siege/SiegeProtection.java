package com.esm.nightmare.siege;

import com.esm.nightmare.ESMConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * The one question every NESM block-changing behaviour asks:
 * "Am I allowed to change the block at this position?"
 *
 * It looks at the block that would be changed, not at where the mob stands.
 * Explosions never ask this, so creepers and TNT still blow things up.
 */
public final class SiegeProtection {

	private SiegeProtection() {
	}

	public static boolean isProtected(Level level, BlockPos pos) {
		if (!(level instanceof ServerLevel)) {
			return false;
		}
		ServerLevel serverLevel = (ServerLevel) level;
		int radius = ESMConfig.ProtectionLanternRadius.get();
		return LanternData.get(serverLevel).isProtected(LanternData.dimKey(serverLevel), pos, radius);
	}
}
