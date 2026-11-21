package com.esm.nightmare.MobBehavs;

import com.esm.nightmare.ESMConfig;
import com.esm.nightmare.siege.SiegeProtection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Common gate for every NESM mob-driven block break.
 * Explosions do not use this class and therefore remain unaffected.
 */
public class MobDamageBlock {

	/**
	 * Kept for source compatibility with older NESM call sites. New code should
	 * use tryDamageBlock(), which reports whether a real block was destroyed.
	 */
	@Deprecated
	public MobDamageBlock(Entity entity, Block ignoredBlock, BlockPos pos) {
		tryDamageBlock(entity, pos);
	}

	/** Returns true only when a breakable block was actually destroyed. */
	public static boolean tryDamageBlock(Entity entity, BlockPos pos) {
		if (entity == null || pos == null) {
			return false;
		}

		Level level = entity.level();
		if (level.isClientSide || !level.isInWorldBounds(pos) || !level.hasChunkAt(pos)) {
			return false;
		}
		if (!canBreakBlock(entity, level, pos)) {
			return false;
		}

		// Same vanilla destruction path as the original NESM (particles and break sound),
		// but natural blocks and blocks placed by mobs drop nothing.
		BlockState state = level.getBlockState(pos);
		boolean drop = !suppressDrops(level, pos, state);
		boolean destroyed = level.destroyBlock(pos, drop);
		if (destroyed && level instanceof ServerLevel) {
			ServerLevel serverLevel = (ServerLevel) level;
			MobPlacedBlocks.get(serverLevel).remove(MobPlacedBlocks.dimKey(serverLevel), pos);
		}
		return destroyed;
	}

	// ---- drop rules ---------------------------------------------------------

	private static String naturalListCache = null;
	private static List<TagKey<Block>> naturalTags = new ArrayList<>();
	private static Set<ResourceLocation> naturalIds = new HashSet<>();

	/** True if this block should be removed without dropping anything. */
	private static boolean suppressDrops(Level level, BlockPos pos, BlockState state) {
		// Placed by a mob earlier (pillar / bridge cobblestone)
		if (ESMConfig.MobPlacedBlockNoDrop.get() && level instanceof ServerLevel) {
			ServerLevel serverLevel = (ServerLevel) level;
			if (MobPlacedBlocks.get(serverLevel).contains(MobPlacedBlocks.dimKey(serverLevel), pos)) {
				return true;
			}
		}
		// Natural terrain block (type list in the config)
		return ESMConfig.NaturalBlockNoDrop.get() && isNaturalBlock(state);
	}

	private static boolean isNaturalBlock(BlockState state) {
		String text = ESMConfig.NaturalBlockList.get();
		if (!text.equals(naturalListCache)) {
			naturalListCache = text;
			List<TagKey<Block>> tags = new ArrayList<>();
			Set<ResourceLocation> ids = new HashSet<>();
			for (String raw : text.split(",")) {
				String token = raw.trim();
				if (token.isEmpty()) {
					continue;
				}
				boolean isTag = token.startsWith("#");
				String idText = isTag ? token.substring(1) : token;
				ResourceLocation id = ResourceLocation.tryParse(idText.contains(":") ? idText : "minecraft:" + idText);
				if (id == null) {
					continue;
				}
				if (isTag) {
					tags.add(TagKey.create(Registries.BLOCK, id));
				} else {
					ids.add(id);
				}
			}
			naturalTags = tags;
			naturalIds = ids;
		}
		for (TagKey<Block> tag : naturalTags) {
			if (state.is(tag)) {
				return true;
			}
		}
		ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
		return key != null && naturalIds.contains(key);
	}

	/** Shared, side-effect-free validation used before swinging or destroying. */
	public static boolean canBreakBlock(Entity entity, Level level, BlockPos pos) {
		if (entity == null || level == null || pos == null || level.isClientSide) {
			return false;
		}
		if (!level.isInWorldBounds(pos) || !level.hasChunkAt(pos)) {
			return false;
		}

		// Never modify a block in a Monolith Core protected chunk.
		if (SiegeProtection.isProtected(level, pos)) {
			return false;
		}

		BlockState state = level.getBlockState(pos);
		if (state.isAir() || !state.getFluidState().isEmpty()) {
			return false;
		}

		// Negative destroy speed denotes an unbreakable block (e.g. bedrock,
		// barriers and command blocks under normal survival rules).
		if (state.getDestroySpeed(level, pos) < 0.0F) {
			return false;
		}

		return !isBlacklisted(state.getBlock());
	}

	private static boolean isBlacklisted(Block block) {
		ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
		if (key == null) {
			return false;
		}

		String registryId = key.toString().toLowerCase(java.util.Locale.ROOT);
		String path = key.getPath().toLowerCase(java.util.Locale.ROOT);
		String configured = ESMConfig.BlocksEntitiesCannotDigThrough.get();
		if (configured == null || configured.isBlank()) {
			return false;
		}

		// Keep the existing config's substring semantics, but compare against
		// stable registry identifiers rather than translated display names.
		for (String rawToken : configured.split(",")) {
			String token = rawToken.trim().toLowerCase(java.util.Locale.ROOT);
			if (!token.isEmpty() && (registryId.contains(token) || path.contains(token))) {
				return true;
			}
		}
		return false;
	}
}
