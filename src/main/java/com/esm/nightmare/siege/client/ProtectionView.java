package com.esm.nightmare.siege.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.esm.nightmare.siege.MonolithGogglesItem;
import com.esm.nightmare.siege.SiegeRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client only. While the goggles are worn, draws the outline of the protected
 * chunks: translucent pillars at the corners and rings every 4 blocks.
 * Only the outer border of the whole protected area is drawn.
 */
@Mod.EventBusSubscriber(modid = SiegeRegistry.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ProtectionView {

	private static String dim = "";
	// Border segments in block coordinates: {x1, z1, x2, z2}
	private static List<int[]> edges = new ArrayList<>();

	private static final double DRAW_DISTANCE = 96.0;
	private static final float R = 0.35F, G = 1.0F, B = 1.0F;

	private ProtectionView() {
	}

	// Called from the network packet (client main thread)
	public static void update(String newDim, int radius, long[] lanternChunks) {
		dim = newDim;

		// Every chunk the lanterns protect
		Set<Long> protectedChunks = new HashSet<>();
		for (long c : lanternChunks) {
			int cx = ChunkPos.getX(c);
			int cz = ChunkPos.getZ(c);
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					protectedChunks.add(ChunkPos.asLong(cx + dx, cz + dz));
				}
			}
		}

		// Keep only the sides that face an unprotected chunk
		List<int[]> list = new ArrayList<>();
		for (long c : protectedChunks) {
			int cx = ChunkPos.getX(c);
			int cz = ChunkPos.getZ(c);
			int x0 = cx << 4;
			int z0 = cz << 4;
			int x1 = x0 + 16;
			int z1 = z0 + 16;
			if (!protectedChunks.contains(ChunkPos.asLong(cx, cz - 1))) {
				list.add(new int[] { x0, z0, x1, z0 });
			}
			if (!protectedChunks.contains(ChunkPos.asLong(cx, cz + 1))) {
				list.add(new int[] { x0, z1, x1, z1 });
			}
			if (!protectedChunks.contains(ChunkPos.asLong(cx - 1, cz))) {
				list.add(new int[] { x0, z0, x0, z1 });
			}
			if (!protectedChunks.contains(ChunkPos.asLong(cx + 1, cz))) {
				list.add(new int[] { x1, z0, x1, z1 });
			}
		}
		edges = list;
	}

	@SubscribeEvent
	public static void onRender(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		ClientLevel level = mc.level;
		List<int[]> list = edges;
		if (player == null || level == null || list.isEmpty()) {
			return;
		}
		if (!(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof MonolithGogglesItem)) {
			return;
		}
		if (!level.dimension().location().toString().equals(dim)) {
			return;
		}

		PoseStack poseStack = event.getPoseStack();
		Vec3 cam = event.getCamera().getPosition();
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		VertexConsumer vc = buffers.getBuffer(RenderType.lines());

		poseStack.pushPose();
		poseStack.translate(-cam.x, -cam.y, -cam.z);
		PoseStack.Pose pose = poseStack.last();

		int yBase = Mth.floor(cam.y / 4.0) * 4 - 24;
		int yTop = yBase + 64;
		double maxSq = DRAW_DISTANCE * DRAW_DISTANCE;

		for (int[] e : list) {
			double mx = (e[0] + e[2]) * 0.5 - cam.x;
			double mz = (e[1] + e[3]) * 0.5 - cam.z;
			if (mx * mx + mz * mz > maxSq) {
				continue;
			}
			boolean alongX = e[1] == e[3];

			// Pillars: strong at the corners, faint in between
			for (int i = 0; i <= 16; i += 4) {
				double x = alongX ? e[0] + i : e[0];
				double z = alongX ? e[1] : e[1] + i;
				float a = (i == 0 || i == 16) ? 0.9F : 0.3F;
				line(vc, pose, x, yBase, z, x, yTop, z, a);
			}
			// Rings
			for (int y = yBase; y <= yTop; y += 4) {
				line(vc, pose, e[0], y, e[1], e[2], y, e[3], 0.4F);
			}
		}

		poseStack.popPose();
		buffers.endBatch(RenderType.lines());
	}

	private static void line(VertexConsumer vc, PoseStack.Pose pose, double x1, double y1, double z1, double x2,
			double y2, double z2, float alpha) {
		float dx = (float) (x2 - x1);
		float dy = (float) (y2 - y1);
		float dz = (float) (z2 - z1);
		float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
		if (len == 0.0F) {
			return;
		}
		dx /= len;
		dy /= len;
		dz /= len;
		vc.vertex(pose.pose(), (float) x1, (float) y1, (float) z1).color(R, G, B, alpha)
				.normal(pose.normal(), dx, dy, dz).endVertex();
		vc.vertex(pose.pose(), (float) x2, (float) y2, (float) z2).color(R, G, B, alpha)
				.normal(pose.normal(), dx, dy, dz).endVertex();
	}
}
