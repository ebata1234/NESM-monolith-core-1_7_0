package com.esm.nightmare.siege;

import java.util.UUID;

import com.esm.nightmare.ESMConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Adds (number of lanterns x 5) to every player's L2Hostility "extra_difficulty"
 * attribute. L2Hostility adds that attribute to its own player difficulty,
 * so we only need to hand out a modifier. If L2Hostility is not installed,
 * the attribute does not exist and nothing happens.
 */
public class LanternDifficulty {

	private static final ResourceLocation ATTRIBUTE_ID = new ResourceLocation("l2hostility", "extra_difficulty");

	private static final UUID MODIFIER_ID = UUID.fromString("5b0a7c3e-2f4d-4d6a-9a38-0f1c7e8b6d21");

	// Every second: also covers logins, respawns and players that join later
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server != null && server.getTickCount() % 20 == 0) {
			refresh(server);
		}
	}

	// Recalculate right away (called when a lantern is placed or removed)
	public static void refresh(MinecraftServer server) {
		Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(ATTRIBUTE_ID);
		if (attribute == null) {
			return;
		}
		int total = LanternData.get(server.overworld()).totalCount();
		double amount = (double) total * ESMConfig.ProtectionLanternDifficulty.get();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			apply(player, attribute, amount);
		}
	}

	private static void apply(ServerPlayer player, Attribute attribute, double amount) {
		AttributeInstance instance = player.getAttribute(attribute);
		if (instance == null) {
			return;
		}
		AttributeModifier old = instance.getModifier(MODIFIER_ID);
		if (old != null && old.getAmount() == amount) {
			return;
		}
		if (old != null) {
			instance.removeModifier(MODIFIER_ID);
		}
		if (amount > 0) {
			// Transient: not saved with the player, always rebuilt from the lantern count
			instance.addTransientModifier(new AttributeModifier(MODIFIER_ID, "NESM protection lantern difficulty",
					amount, AttributeModifier.Operation.ADDITION));
		}
	}
}
