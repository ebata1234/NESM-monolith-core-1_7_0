package com.esm.nightmare;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class ESMConfig {
	
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	
	//Options
	public static final ForgeConfigSpec.ConfigValue<Boolean> isSiegeModeEnabled;
	public static final ForgeConfigSpec.ConfigValue<Boolean> Entity_Duplication;
	public static final ForgeConfigSpec.ConfigValue<Boolean> isSpecialJockeyMobsAllowed;
	public static final ForgeConfigSpec.ConfigValue<Boolean> isChargedCreeperAllowed;
	public static final ForgeConfigSpec.ConfigValue<Boolean> AngryEntities;
	public static final ForgeConfigSpec.ConfigValue<Boolean> GiveZombiesPickaxe;
	public static final ForgeConfigSpec.ConfigValue<Boolean> GiveZombiesArmor;
	public static final ForgeConfigSpec.ConfigValue<Boolean> GiveSkeletonsArmor;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ZombiesLayTNT;
	public static final ForgeConfigSpec.ConfigValue<Double> ZombieTNTChance;
	public static final ForgeConfigSpec.ConfigValue<Integer> ZombieTNTDistance;
	public static final ForgeConfigSpec.ConfigValue<Boolean> AllowZombieGriefing;
	public static final ForgeConfigSpec.ConfigValue<Boolean> AllowZombieBuilding;	
	public static final ForgeConfigSpec.ConfigValue<Boolean> SpidersShootWebs;
	public static final ForgeConfigSpec.ConfigValue<Boolean> isCreeperBreachingAllowed;
	public static final ForgeConfigSpec.ConfigValue<Boolean> isEntitiesBounceOnWater;
	public static final ForgeConfigSpec.ConfigValue<Boolean> AllowSleeping;
	public static final ForgeConfigSpec.ConfigValue<Boolean> AllowMonsterInfighting;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ZombiesLightFires;
	public static final ForgeConfigSpec.ConfigValue<Boolean> AllowSuperSkeletons;
	public static final ForgeConfigSpec.ConfigValue<Boolean> isMonstersTargetVillagers;
	public static final ForgeConfigSpec.ConfigValue<Boolean> EntitiesNeedPickaxesToBreakBlocks;
	
	public static final ForgeConfigSpec.ConfigValue<Integer> InvadeEveryXDays;	
	public static final ForgeConfigSpec.ConfigValue<Double> SpecialJockeyGenerationChance;
	public static final ForgeConfigSpec.ConfigValue<Integer> DuplicationChance;
	public static final ForgeConfigSpec.ConfigValue<Double> DungeonDuplicationChance;
	public static final ForgeConfigSpec.ConfigValue<Integer> ChargedCreeperChance;
	public static final ForgeConfigSpec.ConfigValue<Integer> AngryEntityChance;
	public static final ForgeConfigSpec.ConfigValue<Integer> CreeperBreachingDistance;
	public static final ForgeConfigSpec.ConfigValue<Integer> CreeperStrikingDistance;
	public static final ForgeConfigSpec.ConfigValue<Integer> CreeperAboveExplodeDistance;
	public static final ForgeConfigSpec.ConfigValue<Integer> CreeperObstructedExplodeTicks;
	public static final ForgeConfigSpec.ConfigValue<Integer> MobMaxLife;
	public static final ForgeConfigSpec.ConfigValue<Double> ChanceOfNuclearCreeper;
	public static final ForgeConfigSpec.ConfigValue<Integer> NuclearCreeperExplosionRadius;
	public static final ForgeConfigSpec.ConfigValue<Integer> NuclearCreeperFuse;
	public static final ForgeConfigSpec.ConfigValue<Integer> ChanceOfPickaxe;
	public static final ForgeConfigSpec.ConfigValue<Double> ChanceOfZombieLightFire;
	public static final ForgeConfigSpec.ConfigValue<Double> ChanceOfSuperSkeleton;
	public static final ForgeConfigSpec.ConfigValue<Integer> EntityDigDelay;
	public static final ForgeConfigSpec.ConfigValue<Integer> EntityBuildDelay;
	public static final ForgeConfigSpec.ConfigValue<Integer> EntityDigXZDistance;
	
	public static final ForgeConfigSpec.ConfigValue<Integer> MonsterAttackDistanceAboveGround;
	public static final ForgeConfigSpec.ConfigValue<Integer> MonsterAttackDistanceUnderground;
	
	public static final ForgeConfigSpec.ConfigValue<Double> SpiderShootWebChance;
	public static final ForgeConfigSpec.ConfigValue<Integer> SpiderShootWebDist;
	
	public static final ForgeConfigSpec.ConfigValue<Integer> MaxDuplicationClones;
	public static final ForgeConfigSpec.ConfigValue<Integer> MinDuplicationClones;
	
	public static final ForgeConfigSpec.ConfigValue<Integer> MaxDungeonDuplicationClones;
	public static final ForgeConfigSpec.ConfigValue<Integer> MinDungeonDuplicationClones;
	
	// Protection Lantern options
	// Napalm creeper options
	public static final ForgeConfigSpec.ConfigValue<Boolean> CreeperNapalm;
	public static final ForgeConfigSpec.ConfigValue<Integer> CreeperNapalmRadius;
	public static final ForgeConfigSpec.ConfigValue<Integer> CreeperNapalmFires;
	public static final ForgeConfigSpec.ConfigValue<Boolean> CreeperNapalmIgnitesEntities;

	public static final ForgeConfigSpec.ConfigValue<Integer> ProtectionLanternRadius;
	public static final ForgeConfigSpec.ConfigValue<Integer> ProtectionLanternDifficulty;

	// Level-based movement speed (L2Hostility)
	public static final ForgeConfigSpec.ConfigValue<Boolean> MobSpeedScaling;
	public static final ForgeConfigSpec.ConfigValue<Double> MobSpeedScalingMaxBonus;
	public static final ForgeConfigSpec.ConfigValue<Integer> MobSpeedScalingMaxLevel;
	public static final ForgeConfigSpec.ConfigValue<String> MobSpeedScalingFastCurveMobs;

	// No drops from natural / mob-placed blocks
	public static final ForgeConfigSpec.ConfigValue<Boolean> NaturalBlockNoDrop;
	public static final ForgeConfigSpec.ConfigValue<String> NaturalBlockList;
	public static final ForgeConfigSpec.ConfigValue<Boolean> MobPlacedBlockNoDrop;

	// Zombie-like mobs (piglins ...)
	public static final ForgeConfigSpec.ConfigValue<String> ZombieLikeMobs;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ZombieLikeNoZombification;

	// Skeleton sword stance
	public static final ForgeConfigSpec.ConfigValue<Boolean> SkeletonSword;
	public static final ForgeConfigSpec.ConfigValue<String> SkeletonSwordMobs;
	public static final ForgeConfigSpec.ConfigValue<Double> SkeletonSwordEnterDist;
	public static final ForgeConfigSpec.ConfigValue<Double> SkeletonSwordExitDist;
	public static final ForgeConfigSpec.ConfigValue<Double> SkeletonSwordRetreatFactor;
	public static final ForgeConfigSpec.ConfigValue<Integer> SkeletonSwordDiamondLevel;
	public static final ForgeConfigSpec.ConfigValue<Integer> SkeletonSwordNetheriteLevel;
	public static final ForgeConfigSpec.ConfigValue<Integer> SkeletonSwordSharpnessStep;
	public static final ForgeConfigSpec.ConfigValue<Integer> SkeletonSwordKnockbackLevel;
	public static final ForgeConfigSpec.ConfigValue<Integer> SkeletonSwordFireAspectLevel;

	// Zombie ender pearl
	public static final ForgeConfigSpec.ConfigValue<Boolean> ZombieEnderPearl;
	public static final ForgeConfigSpec.ConfigValue<String> ZombieEnderPearlMobs;
	public static final ForgeConfigSpec.ConfigValue<Double> ZombieEnderPearlChance;
	public static final ForgeConfigSpec.ConfigValue<Integer> ZombieEnderPearlMinDist;
	public static final ForgeConfigSpec.ConfigValue<Integer> ZombieEnderPearlMaxDist;
	public static final ForgeConfigSpec.ConfigValue<Double> ZombieEnderPearlDropChance;

	// Skeleton dodge
	public static final ForgeConfigSpec.ConfigValue<Boolean> SkeletonEvade;
	public static final ForgeConfigSpec.ConfigValue<String> SkeletonEvadeMobs;
	public static final ForgeConfigSpec.ConfigValue<Double> SkeletonEvadeTriggerDist;
	public static final ForgeConfigSpec.ConfigValue<Integer> SkeletonEvadeChance;
	public static final ForgeConfigSpec.ConfigValue<Double> SkeletonEvadeStepDist;
	public static final ForgeConfigSpec.ConfigValue<Integer> SkeletonEvadeTripleChance;
	public static final ForgeConfigSpec.ConfigValue<Integer> SkeletonEvadeCooldownMin;
	public static final ForgeConfigSpec.ConfigValue<Integer> SkeletonEvadeCooldownMax;

	// v1.7.0: approach (dig / bridge / pillar) toward villagers too
	public static final ForgeConfigSpec.ConfigValue<Boolean> ApproachVillagers;

	// v1.7.0: creeper wall breaching only when a player is near, never into the Monolith Core chunks
	public static final ForgeConfigSpec.ConfigValue<Integer> CreeperBreachPlayerDist;
	public static final ForgeConfigSpec.ConfigValue<Integer> CreeperBreachWallDist;
	public static final ForgeConfigSpec.ConfigValue<Boolean> CreeperBreachRespectsMonolith;

	// v1.7.0: skeleton flee / hit-and-run
	public static final ForgeConfigSpec.ConfigValue<Double> SkeletonFleeDist;
	public static final ForgeConfigSpec.ConfigValue<Double> SkeletonHitAndRunDist;
	public static final ForgeConfigSpec.ConfigValue<Double> SkeletonFleeUntilDist;
	public static final ForgeConfigSpec.ConfigValue<Double> SkeletonFleeSpeed;
	public static final ForgeConfigSpec.ConfigValue<Integer> SkeletonLungeCooldownMin;
	public static final ForgeConfigSpec.ConfigValue<Integer> SkeletonLungeCooldownMax;
	public static final ForgeConfigSpec.ConfigValue<Double> SkeletonLungeReach;

	// Minimum detection range
	public static final ForgeConfigSpec.ConfigValue<Boolean> MobFollowRange;
	public static final ForgeConfigSpec.ConfigValue<Integer> MobFollowRangeMin;

	public static final ForgeConfigSpec.ConfigValue<String> SiegeEntityWhitelist;

//	public static final ForgeConfigSpec.ConfigValue<boolean> UseBlockWhitelist;

	public static final ForgeConfigSpec.ConfigValue<String> BlocksEntitiesCannotDigThrough;
//	public static final ForgeConfigSpec.ConfigValue<String> BlocksEntitiesCanDigThrough;

	public static final ForgeConfigSpec.ConfigValue<String> RideableMobs;
	//public static final ForgeConfigSpec.ConfigValue<List> RideableMobsTest;
	public static final ForgeConfigSpec.ConfigValue<String> JockeyMobs;
	
	
	static
	{
		//Title
		BUILDER.push("Nightmare: Epic Siege Configuration");
		BUILDER.pop();
		
		//Definitions
		isSiegeModeEnabled = BUILDER.comment("Enable Enemies Swarming You").define("enableSiege", true);
		Entity_Duplication = BUILDER.comment("Enable Increased Enemy Presence").define("incEnemies", true);
		isSpecialJockeyMobsAllowed = BUILDER.comment("Should special mobs spawn riding other mobs?").define("isJockies", true);
		isChargedCreeperAllowed = BUILDER.comment("Charged Creepers Spawn Naturally").define("isChargedCreepers", true);
		AngryEntities = BUILDER.comment("Allow Bloodlusting Enemies").define("isBloodlusting", true);
		GiveZombiesPickaxe = BUILDER.comment("Give Zombies Pickaxes and Weapons?").define("isZombiePickaxes", true);
		GiveZombiesArmor = BUILDER.comment("Give Zombies Armor?").define("isZombieArmor", true);
		GiveSkeletonsArmor = BUILDER.comment("Give Skeletons Armor?").define("isSkeletonArmor", true);
		ZombiesLayTNT = BUILDER.comment("Allow Zombies to Lay TNT?").define("isZombieTnt", true);
		ZombieTNTChance = BUILDER.comment("Chance of zombies placing TNT each tick if in distance.").define("zombieTNTChance", 0.25);
		ZombieTNTDistance = BUILDER.comment("How close zombies need to be to player to allow setting TNT.").define("zombieTNTDistance", 2);
		AllowZombieGriefing = BUILDER.comment("Allow Zombies To Destroy Blocks? SETTING IS DEPRECATED AND WILL BE REMOVED IN FUTURE RELEASE. Please set mobGriefing server game rule instead.").define("iszombiegriefing", true);
		AllowZombieBuilding = BUILDER.comment("Allow Zombies To Place Blocks? DEPRECATED - REPLACED BY mobGriefing game rule. Will be removed in future.").define("iszombiebuilding", true);
		SpidersShootWebs = BUILDER.comment("Allow Spiders to Shoot Webs?").define("isSpiderShootWebs", true);
		isCreeperBreachingAllowed = BUILDER.comment("Can Creepers Breach Walls?").define("isCreeperBreaching", true);
		isEntitiesBounceOnWater = BUILDER.comment("Do entities bounce when they touch water? Default = true.").define("isOceanSurfing", true);
		AllowSleeping = BUILDER.comment("Allow Sleeping?").define("allowSleep", false);
		AllowMonsterInfighting = BUILDER.comment("Allow Monster Infighting?").define("allowMonsterInfighting", false);
		ZombiesLightFires = BUILDER.comment("Allow Zombies and Piglins to light flammable things on fire?").define("zombiesLightFires", true);
		AllowSuperSkeletons = BUILDER.comment("Allow Super Skeletons to Spawn in the Game?").define("allowSuperSkeletons", true);
		isMonstersTargetVillagers = BUILDER.comment("Should monsters target villagers?").define("isMonstersTargetVillagers", true);
		EntitiesNeedPickaxesToBreakBlocks = BUILDER.comment("Should only entities holding pickaxes be able to dig?").define("zombiePickaxeDigOnly", false);
		
		InvadeEveryXDays = BUILDER.comment("What days should sieges occur? 1 = default (everyday, constantly). 2 = every other day, 7 every week, and so on.").define("siegeRecurrence", 1);
		SpecialJockeyGenerationChance = BUILDER.comment("Chance of special mob spawning and riding another entity out of 100.").define("jockeyChance", 5.0);
		DuplicationChance = BUILDER.comment("Chance Entity Duplicate on Spawn (Out of 100)").define("dupeChance", 10);
		DungeonDuplicationChance = BUILDER.comment("Chance of entity duplicating when in dungeon (Out of 100)").define("dungeonDupeChance", 50.0);
		ChargedCreeperChance = BUILDER.comment("Chance of Creepers Spawning Charged Out of 100").define("chargedChance", 1);
		AngryEntityChance = BUILDER.comment("Chance of Entities Spawning Bloodlusted Out of 100").define("angryEntityChance", 10);
		CreeperBreachingDistance = BUILDER.comment("Distance at Which Creepers Will Breach if Blocked (in Blocks)").define("breachingDist", 64);
		CreeperStrikingDistance = BUILDER.comment("Creepers Will Explode if This Close to Player (in Blocks)").define("creeperExplodeDist", 6);
		CreeperAboveExplodeDistance = BUILDER.comment("Distance At Which Creepers Breech the Ground (in Blocks)").define("creepersBreechGroundDist", 6);
		CreeperObstructedExplodeTicks = BUILDER.comment("Ticks Until Blocked Creeper Explodes (in Game Ticks)").define("obstructedCreeperExpTicks", 60);
		MobMaxLife = BUILDER.comment("Maximum ticks in an entity's life until they despawn (if their target is the player). 20 Ticks = 1 second.").define("mobMaxLife", 4800);
		NuclearCreeperExplosionRadius = BUILDER.comment("Nuclear Creeper Explosion Radius (Default vanilla is 3").define("nuclearCreeperExplosionRadius", 10);
		NuclearCreeperFuse = BUILDER.comment("How long (in ticks) Nuclear Creeper is lit before it blows up").define("nuclearCreeperFuse",120);
		ChanceOfNuclearCreeper = BUILDER.comment("Chance of Nuclear Creeper out of 100").define("chanceNuclearCreeper", 0.25);
		ChanceOfPickaxe = BUILDER.comment("Chance of Zombies Receiving Special Item or Pickaxe Out of 100").define("pickaxeChance", 20);
		ChanceOfZombieLightFire = BUILDER.comment("Chance of Zombies Lighting Flammable Items on Fire Per Tick Out of 100").define("zombieFireChance", 1.0);
		ChanceOfSuperSkeleton = BUILDER.comment("Chance of More Powerful Skeleton Spawning").define("superSkeletonChance", 1.0);
		EntityDigDelay = BUILDER.comment("Cooldown time for entities breaking blocks in ticks").define("entityDigDelay", 5);
		EntityBuildDelay = BUILDER.comment("Cooldown time for entities placing blocks in ticks").define("entityBuildDelay", 5);
		EntityDigXZDistance = BUILDER.comment("If entity is within this distance for X and Z, it'll start digging to player").define("entityDigDist", 32);
					
		MonsterAttackDistanceAboveGround = BUILDER.comment("Above Ground Monster Attack Distance (in Blocks)").define("groundDist", 90);
		MonsterAttackDistanceUnderground = BUILDER.comment("Below Ground Monster Attack Distance (in Blocks)").define("caveDist", 42);
		
		SpiderShootWebChance = BUILDER.comment("Chance out of 100 for spider shooting webs each tick. Default = 0.5").define("spiderWebChance", 0.5);
		SpiderShootWebDist = BUILDER.comment("Distance at which spider can shoot a web at player. Default = 2 blocks").define("spiderWebDist", 2);
		
		MaxDuplicationClones = BUILDER.comment("Maximum Duplicated Enemies. Recommend Min. 1, Max. 4. Must be greater than min duplication clones and greater than 0.").define("maxDupes", 3);
		MinDuplicationClones = BUILDER.comment("Minimum Duplicated Enemies. Recommend Min. 0, Max. 4. Must be less than max duplication clones, or, will use max dupes value.").define("minDupes", 0);
		
		MaxDungeonDuplicationClones = BUILDER.comment("Maximum Duplicated Enemies FROM A DUNGEON. Recommend Min. 1, Max. 6. Must be greater than min duplication clones and greater than 0.").define("maxDungeonDupes", 6);
		MinDungeonDuplicationClones = BUILDER.comment("Maximum Duplicated Enemies FROM A DUNGEON. Recommend Min. 0, Max. 3. Must be less than max duplication clones, or, will use max dupes value.").define("minDungeonDupes", 1);

		SiegeEntityWhitelist = BUILDER.comment("Entities that will besiege players.").define("siegeWhitelist", "spider,cave spider,creeper,skeleton,witch,"
				+ "ghast,giant,husk,hoglin,pillager,vindicator,illusioner,evoker,ravager,shulker,silverfish,stray,slime,witch,zoglin,"
				+ "zombie,zombie_villager,zombified_piglin,zoglin,drowned,zombie_villager,wither_skeleton,pillager");
		
		BlocksEntitiesCannotDigThrough = BUILDER.comment("Any part of a block name, which defines which blocks that siege monsters CANNOT break to try and get to your position.").define("siegeBreakableBlockBlacklist",
				"_ore,"
				+ "_sign,air,water,lava,_bed,rail,_block,obsidian,torch,fire,spawner,ladder,furnace,level,iron_door,pressure_plate,_button,sugar_cane,"
				+ "juke,soul_,portal,cake,repeater,trapdoor,iron_bars,chain,vine,lily_pad,enchanting,brewing,cauldron,end_,dragon,potted_,skull,_head,"
				+ "anvil,chest,dropper,comparator,hopper,detector,Pillar,terracotta,light,prismarine,book,grass,snow,flower,sapling,bush,stem,dragon,"
				+ "carrots,potatoes,detector,powered,concrete,barrier,carpet,banner,beet,void,shulker,structure,compost,piston,dispenser,fern,pickle,"
				+ "chorus,purpur,beacon,conduit,frame,lectern,smith,ancient,armor,wither,barrel,cutter,hive,lode,respawn,sculk,brick,copper");
		
		RideableMobs = BUILDER.comment("Defines all mobs that can spawn as rideable by spawning monsters. E.g. 'chicken' will cause enemies to spawn riding chickens.").define("rideableMobs",
				"creeper,spider,skeleton,chicken,pig,skeleton_horse,zombie_horse");

		JockeyMobs = BUILDER.comment("Mobs that can spawn riding other mobs.").define("jockeyMobs",
				"creeper,skeleton,zombie,pillager,vindicator,witch,zombified_piglin");
		
		CreeperNapalm = BUILDER.comment("Creeper explosions scatter fire around the blast like napalm.").define("creeperNapalm", true);
		CreeperNapalmRadius = BUILDER.comment("How far from the explosion the napalm fire is scattered (in blocks).").defineInRange("creeperNapalmRadius", 6, 1, 32);
		CreeperNapalmFires = BUILDER.comment("How many fires are scattered per creeper explosion. 0 = none.").defineInRange("creeperNapalmFires", 25, 0, 500);
		CreeperNapalmIgnitesEntities = BUILDER.comment("Entities caught in a creeper explosion are set on fire (5 seconds).").define("creeperNapalmIgnitesEntities", true);

		ProtectionLanternRadius = BUILDER.comment("Protection Lantern range in chunks around the lantern's chunk. 0 = only the chunk the lantern is in (1x1), 1 = 3x3 chunks, 2 = 5x5 chunks.").defineInRange("protectionLanternRadius", 0, 0, 4);
		ProtectionLanternDifficulty = BUILDER.comment("L2Hostility extra difficulty added to every player for each Protection Lantern placed in the world. 0 = disabled.").defineInRange("protectionLanternDifficulty", 5, 0, 1000);

		MobSpeedScaling = BUILDER.comment("Raise mob base movement speed with their L2Hostility level.").define("mobSpeedScaling", true);
		MobSpeedScalingMaxBonus = BUILDER.comment("Speed bonus at the cap level for normal mobs (0.75 = +75%).").defineInRange("mobSpeedScalingMaxBonus", 0.75, 0.0, 10.0);
		MobSpeedScalingMaxLevel = BUILDER.comment("Level at which normal mobs reach the speed cap. The bonus grows linearly up to this level.").defineInRange("mobSpeedScalingMaxLevel", 100, 1, 10000);
		MobSpeedScalingFastCurveMobs = BUILDER.comment("Mobs that use the zombie/skeleton curve instead (+75% @ Lv50, +100% @ Lv100, +150% @ Lv200 cap). Comma separated entity ids.").define("mobSpeedScalingFastCurveMobs", "minecraft:zombie,minecraft:skeleton");

		NaturalBlockNoDrop = BUILDER.comment("Blocks that mobs break and that are on the natural block list drop nothing.").define("naturalBlockNoDrop", true);
		NaturalBlockList = BUILDER.comment("Natural block types (no drops when a mob breaks them). Comma separated: '#namespace:tag' for a block tag, or a block id.")
				.define("naturalBlockList", "#minecraft:base_stone_overworld,#minecraft:base_stone_nether,#minecraft:dirt,#minecraft:sand,#minecraft:terracotta,#forge:ores,#minecraft:nylium,#minecraft:snow,"
						+ "minecraft:gravel,minecraft:clay,minecraft:mud,minecraft:end_stone,minecraft:sandstone,minecraft:red_sandstone,minecraft:ice,minecraft:packed_ice,minecraft:blue_ice,"
						+ "minecraft:soul_sand,minecraft:soul_soil,minecraft:magma_block,minecraft:obsidian");
		MobPlacedBlockNoDrop = BUILDER.comment("Blocks that mobs placed (pillar and bridge cobblestone) drop nothing when a mob breaks them.").define("mobPlacedBlockNoDrop", true);

		ZombieLikeMobs = BUILDER.comment("Mobs that get the zombie behaviour (dig, pillar, bridge, walk to the player and attack). Comma separated entity ids. Zombies already have it.").define("zombieLikeMobs", "minecraft:piglin,minecraft:piglin_brute");
		ZombieLikeNoZombification = BUILDER.comment("Listed piglins do not turn into zombified piglins in the Overworld.").define("zombieLikeNoZombification", true);

		SkeletonSword = BUILDER.comment("Skeletons switch to a sword when the player comes close, and back to the bow when the player is far.").define("skeletonSword", true);
		SkeletonSwordMobs = BUILDER.comment("Mobs that switch to a sword. Comma separated entity ids (add minecraft:stray if you like).").define("skeletonSwordMobs", "minecraft:skeleton");
		SkeletonSwordEnterDist = BUILDER.comment("DEPRECATED since v1.7.0 - not used any more, see skeletonFleeDist.").defineInRange("skeletonSwordEnterDist", 5.0, 1.0, 32.0);
		SkeletonSwordExitDist = BUILDER.comment("DEPRECATED since v1.7.0 - not used any more, see skeletonFleeUntilDist.").defineInRange("skeletonSwordExitDist", 10.0, 2.0, 64.0);
		SkeletonSwordRetreatFactor = BUILDER.comment("DEPRECATED since v1.7.0 - not used any more, see skeletonFleeSpeed.").defineInRange("skeletonSwordRetreatFactor", 0.6, 0.0, 3.0);
		SkeletonSwordDiamondLevel = BUILDER.comment("L2Hostility level from which the sword is a diamond sword (iron below).").defineInRange("skeletonSwordDiamondLevel", 50, 0, 100000);
		SkeletonSwordNetheriteLevel = BUILDER.comment("L2Hostility level from which the sword is a netherite sword.").defineInRange("skeletonSwordNetheriteLevel", 100, 0, 100000);
		SkeletonSwordSharpnessStep = BUILDER.comment("Sharpness = level / this value (at most 5).").defineInRange("skeletonSwordSharpnessStep", 25, 1, 100000);
		SkeletonSwordKnockbackLevel = BUILDER.comment("L2Hostility level from which the sword has Knockback I.").defineInRange("skeletonSwordKnockbackLevel", 60, 0, 100000);
		SkeletonSwordFireAspectLevel = BUILDER.comment("L2Hostility level from which the sword has Fire Aspect I.").defineInRange("skeletonSwordFireAspectLevel", 80, 0, 100000);

		ZombieEnderPearl = BUILDER.comment("Some zombies carry an ender pearl and throw it at the player to close the distance.").define("zombieEnderPearl", true);
		ZombieEnderPearlMobs = BUILDER.comment("Mobs that can carry a pearl. Comma separated entity ids.").define("zombieEnderPearlMobs", "minecraft:zombie");
		ZombieEnderPearlChance = BUILDER.comment("Chance out of 100 that such a mob spawns with a pearl.").defineInRange("zombieEnderPearlChance", 10.0, 0.0, 100.0);
		ZombieEnderPearlMinDist = BUILDER.comment("The pearl is only thrown when the player is at least this far away (blocks).").defineInRange("zombieEnderPearlMinDist", 10, 1, 128);
		ZombieEnderPearlMaxDist = BUILDER.comment("The pearl is only thrown when the player is no farther than this (blocks).").defineInRange("zombieEnderPearlMaxDist", 32, 2, 128);
		ZombieEnderPearlDropChance = BUILDER.comment("Chance (0.0-1.0) that an unused pearl drops when the zombie dies. 0 = never.").defineInRange("zombieEnderPearlDropChance", 0.0, 0.0, 1.0);

		SkeletonEvade = BUILDER.comment("Skeletons sometimes leap away when the player gets too close.").define("skeletonEvade", true);
		SkeletonEvadeMobs = BUILDER.comment("Mobs that dodge. Comma separated entity ids (add minecraft:stray if you like).").define("skeletonEvadeMobs", "minecraft:skeleton");
		SkeletonEvadeTriggerDist = BUILDER.comment("A dodge can start when the player is closer than this (blocks).").defineInRange("skeletonEvadeTriggerDist", 5.0, 1.0, 32.0);
		SkeletonEvadeChance = BUILDER.comment("Chance out of 100, checked twice a second while the player is close and the cooldown is over.").defineInRange("skeletonEvadeChance", 15, 0, 100);
		SkeletonEvadeStepDist = BUILDER.comment("How far one leap carries the skeleton (blocks).").defineInRange("skeletonEvadeStepDist", 5.0, 2.0, 10.0);
		SkeletonEvadeTripleChance = BUILDER.comment("Chance out of 100 that a dodge is 3 leaps in a row. Otherwise it is 1-2 leaps.").defineInRange("skeletonEvadeTripleChance", 10, 0, 100);
		SkeletonEvadeCooldownMin = BUILDER.comment("Minimum cooldown after a dodge (seconds).").defineInRange("skeletonEvadeCooldownMin", 10, 0, 600);
		SkeletonEvadeCooldownMax = BUILDER.comment("Maximum cooldown after a dodge (seconds).").defineInRange("skeletonEvadeCooldownMax", 15, 0, 600);

		ApproachVillagers = BUILDER.comment("Zombies (and zombie-like mobs) also dig, bridge and pillar toward villagers they target, not only toward players.").define("approachVillagers", true);

		CreeperBreachPlayerDist = BUILDER.comment("A creeper only blows a wall on purpose when a (survival) player is within this many blocks. Replaces the old breachingDist (64), which let creepers explode with nobody around.").defineInRange("creeperBreachPlayerDist", 16, 1, 128);
		CreeperBreachWallDist = BUILDER.comment("The wall between the creeper and the player must be within this many blocks of the creeper for it to blast it.").defineInRange("creeperBreachWallDist", 4, 1, 8);
		CreeperBreachRespectsMonolith = BUILDER.comment("A creeper never blows a wall on purpose if the blast could reach a Monolith Core protected chunk.").define("creeperBreachRespectsMonolith", true);

		SkeletonFleeDist = BUILDER.comment("v1.7.0: when the player is within this many blocks the skeleton puts the bow away, takes the sword and runs away with its back to the player (replaces skeletonSwordEnterDist).").defineInRange("skeletonFleeDist", 6.0, 1.0, 32.0);
		SkeletonHitAndRunDist = BUILDER.comment("Only when the player is within this many blocks does the skeleton leap at the player (hit and run). Keep it above the player's reach (3).").defineInRange("skeletonHitAndRunDist", 4.0, 2.0, 16.0);
		SkeletonFleeUntilDist = BUILDER.comment("The skeleton keeps running until it is this far from the player, then takes the bow again (replaces skeletonSwordExitDist).").defineInRange("skeletonFleeUntilDist", 10.0, 3.0, 64.0);
		SkeletonFleeSpeed = BUILDER.comment("Running speed while fleeing, as a multiple of its movement speed attribute (1.0 = walking pace).").defineInRange("skeletonFleeSpeed", 1.6, 0.5, 4.0);
		SkeletonLungeCooldownMin = BUILDER.comment("Minimum ticks between two leaps at the player (20 ticks = 1 second).").defineInRange("skeletonLungeCooldownMin", 30, 0, 1200);
		SkeletonLungeCooldownMax = BUILDER.comment("Maximum ticks between two leaps at the player.").defineInRange("skeletonLungeCooldownMax", 50, 0, 1200);
		SkeletonLungeReach = BUILDER.comment("During the leap the skeleton strikes when it is this close to the player (blocks, centre to centre).").defineInRange("skeletonLungeReach", 2.4, 1.0, 5.0);

		MobFollowRange = BUILDER.comment("Raise the detection range (follow range) of hostile mobs that are below the minimum.").define("mobFollowRange", true);
		MobFollowRangeMin = BUILDER.comment("Minimum detection range in blocks. Mobs that already have more are not changed.").defineInRange("mobFollowRangeMin", 64, 1, 256);

		SPEC = BUILDER.build();

		//TODO: LIST
		// Add monsters with high level enchantments if set in config (default to off)
		// Add monsters with diamond armor (Default to off)
		
		
	}
	
}
