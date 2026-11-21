package com.esm.nightmare.siege;

import com.esm.nightmare.ESMConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Once a second, sends the nearby Monolith Core chunks to every player who is
 * wearing the goggles. Players without goggles get nothing.
 */
public class GogglesSync {

	// Chunks around the player that are sent (the client only draws what is close anyway)
	private static final int RANGE = 8;

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null || server.getTickCount() % 20 != 0) {
			return;
		}
		int radius = ESMConfig.ProtectionLanternRadius.get();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof MonolithGogglesItem)) {
				continue;
			}
			ServerLevel level = (ServerLevel) player.level();
			String dim = LanternData.dimKey(level);
			long[] chunks = LanternData.get(level).lanternChunksNear(dim, player.blockPosition().getX() >> 4,
					player.blockPosition().getZ() >> 4, RANGE + radius);
			SiegeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
					new ProtectedChunksPacket(dim, radius, chunks));
		}
	}
}
