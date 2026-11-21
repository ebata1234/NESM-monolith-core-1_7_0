package com.esm.nightmare.MobBehavs;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;

/** Reads a comma separated list of entity ids from the config (cached until the text changes). */
public class MobListUtil {

	private String cachedText = null;
	private Set<ResourceLocation> ids = new HashSet<>();

	public boolean contains(String configText, Entity entity) {
		if (!configText.equals(cachedText)) {
			cachedText = configText;
			ids = new HashSet<>();
			for (String s : configText.split(",")) {
				s = s.trim();
				if (s.isEmpty()) {
					continue;
				}
				ResourceLocation id = ResourceLocation.tryParse(s.contains(":") ? s : "minecraft:" + s);
				if (id != null) {
					ids.add(id);
				}
			}
		}
		ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
		return key != null && ids.contains(key);
	}
}
