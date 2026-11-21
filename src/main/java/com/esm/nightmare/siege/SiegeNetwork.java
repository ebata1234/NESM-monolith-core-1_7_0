package com.esm.nightmare.siege;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class SiegeNetwork {

	private static final String PROTOCOL = "1";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(SiegeRegistry.MOD_ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

	private SiegeNetwork() {
	}

	public static void init() {
		CHANNEL.registerMessage(0, ProtectedChunksPacket.class, ProtectedChunksPacket::encode,
				ProtectedChunksPacket::decode, ProtectedChunksPacket::handle);
	}
}
