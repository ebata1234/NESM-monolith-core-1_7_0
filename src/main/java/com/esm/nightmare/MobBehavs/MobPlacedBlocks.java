package com.esm.nightmare.MobBehavs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Remembers which blocks were placed by mobs (the cobblestone of NESM pillars and bridges),
 * so that they can be broken again without dropping anything.
 * Stored in the world save. Only numbers are kept; no chunk is ever loaded for it.
 * To keep the file small, only the newest MAX_PER_DIM positions per dimension are kept.
 */
public class MobPlacedBlocks extends SavedData {

	private static final String NAME = "nightmareesm_mob_placed_blocks";
	private static final int MAX_PER_DIM = 20000;

	// dimension id -> positions (BlockPos.asLong), oldest first
	private final Map<String, LinkedHashSet<Long>> placed = new HashMap<>();

	public static MobPlacedBlocks get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(MobPlacedBlocks::load,
				MobPlacedBlocks::new, NAME);
	}

	public static String dimKey(ServerLevel level) {
		return level.dimension().location().toString();
	}

	public void add(String dim, BlockPos pos) {
		LinkedHashSet<Long> set = placed.computeIfAbsent(dim, k -> new LinkedHashSet<>());
		long key = pos.asLong();
		set.remove(key);
		set.add(key);
		while (set.size() > MAX_PER_DIM) {
			Iterator<Long> it = set.iterator();
			it.next();
			it.remove();
		}
		setDirty();
	}

	public boolean contains(String dim, BlockPos pos) {
		LinkedHashSet<Long> set = placed.get(dim);
		return set != null && set.contains(pos.asLong());
	}

	public void remove(String dim, BlockPos pos) {
		LinkedHashSet<Long> set = placed.get(dim);
		if (set != null && set.remove(pos.asLong())) {
			setDirty();
		}
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		CompoundTag dims = new CompoundTag();
		for (Map.Entry<String, LinkedHashSet<Long>> e : placed.entrySet()) {
			long[] arr = new long[e.getValue().size()];
			int i = 0;
			for (long l : e.getValue()) {
				arr[i++] = l;
			}
			dims.put(e.getKey(), new LongArrayTag(arr));
		}
		tag.put("dims", dims);
		return tag;
	}

	public static MobPlacedBlocks load(CompoundTag tag) {
		MobPlacedBlocks data = new MobPlacedBlocks();
		CompoundTag dims = tag.getCompound("dims");
		for (String dim : dims.getAllKeys()) {
			LinkedHashSet<Long> set = new LinkedHashSet<>();
			for (long l : dims.getLongArray(dim)) {
				set.add(l);
			}
			data.placed.put(dim, set);
		}
		return data;
	}
}
