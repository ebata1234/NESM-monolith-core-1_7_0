package com.esm.nightmare.MobBehavs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * When a player breaks or places a block on a spot that was recorded as "placed by a mob",
 * the record is dropped, so player-built blocks are never treated as mob-placed.
 */
public class MobPlacedBlocksEvents {

	@SubscribeEvent
	public void onBreak(BlockEvent.BreakEvent event) {
		if (event.getPlayer() != null && event.getPlayer().level() instanceof ServerLevel) {
			ServerLevel level = (ServerLevel) event.getPlayer().level();
			MobPlacedBlocks.get(level).remove(MobPlacedBlocks.dimKey(level), event.getPos());
		}
	}

	@SubscribeEvent
	public void onPlace(BlockEvent.EntityPlaceEvent event) {
		if (event.getEntity() instanceof Player && event.getEntity().level() instanceof ServerLevel) {
			ServerLevel level = (ServerLevel) event.getEntity().level();
			MobPlacedBlocks.get(level).remove(MobPlacedBlocks.dimKey(level), event.getPos());
		}
	}
}
