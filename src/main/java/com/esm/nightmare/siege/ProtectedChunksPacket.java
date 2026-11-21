package com.esm.nightmare.siege;

import java.util.function.Supplier;

import com.esm.nightmare.siege.client.ProtectionView;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** Server -> client: which chunks hold a Monolith Core (near the player), and the protection radius. */
public class ProtectedChunksPacket {

	private final String dim;
	private final int radius;
	private final long[] chunks;

	public ProtectedChunksPacket(String dim, int radius, long[] chunks) {
		this.dim = dim;
		this.radius = radius;
		this.chunks = chunks;
	}

	public static void encode(ProtectedChunksPacket msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.dim);
		buf.writeVarInt(msg.radius);
		buf.writeVarInt(msg.chunks.length);
		for (long c : msg.chunks) {
			buf.writeLong(c);
		}
	}

	public static ProtectedChunksPacket decode(FriendlyByteBuf buf) {
		String dim = buf.readUtf();
		int radius = buf.readVarInt();
		int n = Math.min(buf.readVarInt(), 4096);
		long[] chunks = new long[n];
		for (int i = 0; i < n; i++) {
			chunks[i] = buf.readLong();
		}
		return new ProtectedChunksPacket(dim, radius, chunks);
	}

	public static void handle(ProtectedChunksPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> ProtectionView.update(msg.dim, msg.radius, msg.chunks)));
		ctx.get().setPacketHandled(true);
	}
}
