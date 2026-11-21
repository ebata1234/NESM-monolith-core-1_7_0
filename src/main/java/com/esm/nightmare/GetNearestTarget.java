package com.esm.nightmare;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.server.ServerLifecycleHooks;

public class GetNearestTarget {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public Entity Get(Entity Attacker) {

		// Current Minecraft server
		MinecraftServer Curr_Server = ServerLifecycleHooks.getCurrentServer();
		if (Curr_Server == null) {
			return null;
		}

		PlayerList Player_List = Curr_Server.getPlayerList();
		if (Player_List.getPlayerCount() > 0) {
			return GetNearestEntity(Player_List, Attacker);
		}
		return null;
	}

	// The closest valid target (player or villager)
	Entity GetNearestEntity(PlayerList Player_List, Entity Attacker) {

		Entity Winning_Target = null;
		float Winning_Dist = Float.MAX_VALUE;

		for (Entity Potential : GetNearbyEntities(Player_List, Attacker)) {
			if (isEntitySurvivalModeOrMob(Potential)) {
				float Dist = Attacker.distanceTo(Potential);
				if (Dist < Winning_Dist) {
					Winning_Dist = Dist;
					Winning_Target = Potential;
				}
			}
		}
		return Winning_Target;
	}

	// If targeting villagers is enabled, then, include them in the list.
	java.util.List<Entity> GetNearbyEntities(PlayerList Player_List, Entity Attacker) {

		java.util.List<Entity> Compiled_Entities = new ArrayList<Entity>();
		java.util.List<ServerPlayer> All_Players = Player_List.getPlayers();
		int PlayerCount = Player_List.getPlayerCount();

		// Get players
		for (int i = 0; i < PlayerCount; i++) {
			ServerPlayer Plyr = All_Players.get(i);
			// Same dimension only, and no creative / spectator players
			if (!Plyr.isCreative() && !Plyr.isSpectator() && Plyr.isAlive() && Plyr.level() == Attacker.level()) {
				Compiled_Entities.add((Entity) Plyr);
			}
		}

		// If should target villagers, then, include those as well.
		// v1.7.0: no line of sight is needed and the whole detection range (follow range) is searched,
		// so a villager behind a wall (or farther than 16 blocks away) is noticed just like a player.
		if (ESMConfig.isMonstersTargetVillagers.get()) {

			double Range = 64.0D;
			if (Attacker instanceof LivingEntity) {
				Range = ((LivingEntity) Attacker).getAttributeValue(Attributes.FOLLOW_RANGE);
			}
			Range = Math.max(16.0D, Math.min(Range, 128.0D));

			TargetingConditions Tgt = TargetingConditions.forCombat().ignoreLineOfSight().range(Range);

			java.util.List<Villager> All_Nearby_Entities = Attacker.level().getNearbyEntities(
					Villager.class, Tgt, (LivingEntity) Attacker,
					Attacker.getBoundingBox().inflate(Range, Math.min(Range, 32.0D), Range));

			for (int i = 0; i < All_Nearby_Entities.size(); i++) {
				Villager Curr_Villager = All_Nearby_Entities.get(i);
				Entity Entity_Villager = (Entity) Curr_Villager;
				Compiled_Entities.add(Entity_Villager);
			}
		}

		//LOGGER.info("Compiled Ents = " + Compiled_Entities.size());

		return Compiled_Entities;

	}

	
	boolean isEntitySurvivalModeOrMob(Entity Entity_Class) {

		if (Entity_Class instanceof ServerPlayer) {
			ServerPlayer Server_Player = (ServerPlayer) Entity_Class;
			return !Server_Player.isCreative() && !Server_Player.isSpectator();
		} else {
			return true;
		}

	}
}
