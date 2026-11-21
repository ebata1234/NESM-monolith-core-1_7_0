package com.esm.nightmare.siege;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Remembers where every Protection Lantern is, per dimension.
 * It is stored in the world save, so it survives restarts.
 * It never loads or force-loads chunks: it only stores numbers.
 */
public class LanternData extends SavedData {

	private static final String NAME = "nightmareesm_protection_lanterns";

	// dimension id -> positions of lanterns (BlockPos.asLong)
	private final Map<String, Set<Long>> lanterns = new HashMap<>();

	// dimension id -> (chunk position -> number of lanterns in that chunk)
	private final Map<String, Map<Long, Integer>> chunkCounts = new HashMap<>();

	// Always stored in the overworld's data storage so all dimensions share one file
	public static LanternData get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(LanternData::load, LanternData::new,
				NAME);
	}

	public static String dimKey(ServerLevel level) {
		return level.dimension().location().toString();
	}

	public void add(String dim, BlockPos pos) {
		Set<Long> set = lanterns.computeIfAbsent(dim, k -> new HashSet<>());
		if (set.add(pos.asLong())) {
			chunkCounts.computeIfAbsent(dim, k -> new HashMap<>()).merge(ChunkPos.asLong(pos), 1, Integer::sum);
			setDirty();
		}
	}

	public void remove(String dim, BlockPos pos) {
		Set<Long> set = lanterns.get(dim);
		if (set != null && set.remove(pos.asLong())) {
			Map<Long, Integer> counts = chunkCounts.get(dim);
			if (counts != null) {
				long chunk = ChunkPos.asLong(pos);
				Integer n = counts.get(chunk);
				if (n != null) {
					if (n <= 1) {
						counts.remove(chunk);
					} else {
						counts.put(chunk, n - 1);
					}
				}
			}
			setDirty();
		}
	}

	// Is this block position inside a chunk that a lantern protects?
	// radius = 0 -> only the lantern's own chunk, 1 -> 3x3 chunks, ...
	public boolean isProtected(String dim, BlockPos pos, int radius) {
		Map<Long, Integer> counts = chunkCounts.get(dim);
		if (counts == null || counts.isEmpty()) {
			return false;
		}
		int cx = pos.getX() >> 4;
		int cz = pos.getZ() >> 4;
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (counts.containsKey(ChunkPos.asLong(cx + dx, cz + dz))) {
					return true;
				}
			}
		}
		return false;
	}

	// Chunks that contain a lantern, within 'range' chunks of (cx, cz). Used by the goggles display.
	public long[] lanternChunksNear(String dim, int cx, int cz, int range) {
		Map<Long, Integer> counts = chunkCounts.get(dim);
		if (counts == null || counts.isEmpty()) {
			return new long[0];
		}
		java.util.List<Long> out = new java.util.ArrayList<>();
		for (long key : counts.keySet()) {
			if (Math.abs(ChunkPos.getX(key) - cx) <= range && Math.abs(ChunkPos.getZ(key) - cz) <= range) {
				out.add(key);
				if (out.size() >= 2048) {
					break;
				}
			}
		}
		long[] arr = new long[out.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = out.get(i);
		}
		return arr;
	}

	// Total number of lanterns in all dimensions
	public int totalCount() {
		int total = 0;
		for (Set<Long> set : lanterns.values()) {
			total += set.size();
		}
		return total;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag list = new ListTag();
		for (Map.Entry<String, Set<Long>> e : lanterns.entrySet()) {
			if (e.getValue().isEmpty()) {
				continue;
			}
			CompoundTag c = new CompoundTag();
			c.putString("dim", e.getKey());
			long[] arr = new long[e.getValue().size()];
			int i = 0;
			for (long l : e.getValue()) {
				arr[i++] = l;
			}
			c.putLongArray("positions", arr);
			list.add(c);
		}
		tag.put("dims", list);
		return tag;
	}

	public static LanternData load(CompoundTag tag) {
		LanternData data = new LanternData();
		ListTag list = tag.getList("dims", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag c = list.getCompound(i);
			String dim = c.getString("dim");
			for (long l : c.getLongArray("positions")) {
				// add() also rebuilds the per-chunk counts
				data.add(dim, BlockPos.of(l));
			}
		}
		// Loading must not mark the data as changed
		data.setDirty(false);
		return data;
	}
}
