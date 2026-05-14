package me.realseek.yzzzfix.mixin.apotheosis_spawner;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.realseek.yzzzfix.module.apotheosis_spawner.SpawnerESPTracker;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = LevelRenderer.class, remap = false)
public class LevelRendererMixin {

    @Inject(method = {"renderLevel", "m_109599_"}, at = @At("TAIL"))
    private void yzzzFix$renderHologramESP(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Vec3 cameraPos = camera.getPosition();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        Matrix4f matrix4f = poseStack.last().pose();

        for (BlockEntity be : SpawnerESPTracker.SPAWNERS) {
            if (be == null || be.isRemoved() || be.getLevel() != mc.level) continue;

            // 判断是否被禁用。如果 isDisabled 为真，跳过渲染（灯灭）
            if (be.getPersistentData().getBoolean("YzzzSpawnerDisabled")) continue;

            BlockPos pos = be.getBlockPos();
            if (pos.distToCenterSqr(cameraPos) > 64 * 64) continue;

            AABB aabb = new AABB(pos).move(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            float time = (System.currentTimeMillis() % 2000) / 2000.0f;
            float alpha = 0.2f + 0.15f * (float)Math.sin(time * Math.PI * 2);
            float r = 1.0f, g = 0.8f, b = 0.0f;

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            yzzzFix$drawSolidBox(bufferBuilder, matrix4f, aabb, r, g, b, alpha);
            tesselator.end();

            RenderSystem.lineWidth(2.5f);
            bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
            yzzzFix$drawEdgeLines(bufferBuilder, matrix4f, aabb, r, g, b, 1.0f);
            tesselator.end();
        }

        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void yzzzFix$drawSolidBox(BufferBuilder builder, Matrix4f matrix, AABB aabb, float r, float g, float b, float a) {
        float minX = (float)aabb.minX, minY = (float)aabb.minY, minZ = (float)aabb.minZ;
        float maxX = (float)aabb.maxX, maxY = (float)aabb.maxY, maxZ = (float)aabb.maxZ;

        builder.vertex(matrix, minX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).endVertex();

        builder.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).endVertex();

        builder.vertex(matrix, minX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).endVertex();

        builder.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).endVertex();

        builder.vertex(matrix, minX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).endVertex();

        builder.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        builder.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).endVertex();
    }

    private void yzzzFix$drawEdgeLines(BufferBuilder builder, Matrix4f matrix, AABB aabb, float r, float g, float b, float a) {
        float minX = (float)aabb.minX, minY = (float)aabb.minY, minZ = (float)aabb.minZ;
        float maxX = (float)aabb.maxX, maxY = (float)aabb.maxY, maxZ = (float)aabb.maxZ;

        addLine(builder, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        addLine(builder, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        addLine(builder, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        addLine(builder, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);
        addLine(builder, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(builder, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(builder, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        addLine(builder, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
        addLine(builder, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        addLine(builder, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(builder, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(builder, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private void addLine(BufferBuilder builder, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        builder.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
    }
}