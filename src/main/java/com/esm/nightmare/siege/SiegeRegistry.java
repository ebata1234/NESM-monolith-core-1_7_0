package com.esm.nightmare.siege;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SiegeRegistry {

	public static final String MOD_ID = "nightmareesm";

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

	public static final RegistryObject<Block> MONOLITH_CORE = BLOCKS.register("monolith_core",
			MonolithCoreBlock::new);

	public static final RegistryObject<Item> MONOLITH_CORE_ITEM = ITEMS.register("monolith_core",
			() -> new MonolithCoreItem(MONOLITH_CORE.get()));

	public static final RegistryObject<Item> MONOLITH_GOGGLES = ITEMS.register("monolith_goggles",
			MonolithGogglesItem::new);

	private SiegeRegistry() {
	}

	public static void register(IEventBus modEventBus) {
		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
		modEventBus.addListener(SiegeRegistry::addToCreativeTab);
	}

	private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			event.accept(MONOLITH_CORE_ITEM.get());
			event.accept(MONOLITH_GOGGLES.get());
		}
	}
}
