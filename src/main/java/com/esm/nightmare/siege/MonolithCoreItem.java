package com.esm.nightmare.siege;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** The Monolith Core block item. Only difference: its name is shown in aqua. */
public class MonolithCoreItem extends BlockItem {

	public MonolithCoreItem(Block block) {
		super(block, new Item.Properties());
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId()).withStyle(ChatFormatting.AQUA);
	}
}
