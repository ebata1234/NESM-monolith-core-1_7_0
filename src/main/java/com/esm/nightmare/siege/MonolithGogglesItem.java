package com.esm.nightmare.siege;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * Helmet. While it is worn, the chunks that Monolith Cores protect are drawn
 * as outlines in the world (client side, see siege.client.ProtectionView).
 */
public class MonolithGogglesItem extends ArmorItem {

	public MonolithGogglesItem() {
		super(GogglesMaterial.INSTANCE, ArmorItem.Type.HELMET, new Item.Properties());
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.nightmareesm.monolith_goggles.desc").withStyle(ChatFormatting.GRAY));
	}

	private enum GogglesMaterial implements ArmorMaterial {
		INSTANCE;

		@Override
		public int getDurabilityForType(ArmorItem.Type type) {
			return 165;
		}

		@Override
		public int getDefenseForType(ArmorItem.Type type) {
			return 1;
		}

		@Override
		public int getEnchantmentValue() {
			return 12;
		}

		@Override
		public SoundEvent getEquipSound() {
			return SoundEvents.ARMOR_EQUIP_GENERIC;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.of(Items.GLASS_PANE);
		}

		// Worn texture: assets/nightmareesm/textures/models/armor/monolith_goggles_layer_1.png
		@Override
		public String getName() {
			return "nightmareesm:monolith_goggles";
		}

		@Override
		public float getToughness() {
			return 0.0F;
		}

		@Override
		public float getKnockbackResistance() {
			return 0.0F;
		}
	}
}
