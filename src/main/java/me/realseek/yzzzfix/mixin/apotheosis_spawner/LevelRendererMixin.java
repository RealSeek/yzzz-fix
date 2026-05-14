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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 在世界渲染末尾为带有红石控制的神化刷怪笼绘制黄色呼吸灯高亮框。
 *
 * <p>通过 {@link SpawnerESPTracker#getSpawnersToRender} 获取当前维度内需要渲染的刷怪笼快照，
 * 批量绘制半透明实心方块和线框轮廓，使用正弦函数实现呼吸灯透明度动画效果。</p>
 *
 * <p>渲染状态通过 try/finally 保护，确保异常时不会污染后续渲染管线。</p>
 */
@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = LevelRenderer.class, remap = false)
public abstract class LevelRendererMixin {

    @Unique
    private static final double yzzzFix$MAX_RENDER_DIST_SQR = 64.0 * 64.0;

    @Inject(method = {"renderLevel", "m_109599_"}, at = @At("TAIL"))
    private void yzzzFix$renderHologramESP(PoseStack poseStack, float partialTick,
                                           long finishNanoTime, boolean renderBlockOutline,
                                           Camera camera, GameRenderer gameRenderer,
                                           LightTexture lightTexture, Matrix4f projectionMatrix,
                                           CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Collection<BlockEntity> spawners = SpawnerESPTracker.getSpawnersToRender(mc.level);
        if (spawners.isEmpty()) return;

        Vec3 cameraPos = camera.getPosition();
        List<AABB> boxes = new ArrayList<>();

        for (BlockEntity be : spawners) {
            if (be.getPersistentData().getBoolean("YzzzSpawnerDisabled")) continue;
            BlockPos pos = be.getBlockPos();
            if (pos.distToCenterSqr(cameraPos) > yzzzFix$MAX_RENDER_DIST_SQR) continue;
            boxes.add(new AABB(pos).move(-cameraPos.x, -cameraPos.y, -cameraPos.z));
        }

        if (boxes.isEmpty()) return;

        float time = (System.currentTimeMillis() % 2000L) / 2000.0f;
        float alpha = 0.2f + 0.15f * (float) Math.sin(time * Math.PI * 2.0);

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        try {
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (AABB box : boxes) {
                yzzzFix$drawSolidBox(buffer, matrix, box, 1.0f, 0.8f, 0.0f, alpha);
            }
            tesselator.end();

            RenderSystem.lineWidth(2.5f);
            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
            for (AABB box : boxes) {
                yzzzFix$drawEdgeLines(buffer, matrix, box, 1.0f, 0.8f, 0.0f, 1.0f);
            }
            tesselator.end();
        } finally {
            RenderSystem.lineWidth(1.0f);
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    @Unique
    private static void yzzzFix$drawSolidBox(BufferBuilder builder, Matrix4f matrix,
                                             AABB aabb, float r, float g, float b, float a) {
        float minX = (float) aabb.minX, minY = (float) aabb.minY, minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX, maxY = (float) aabb.maxY, maxZ = (float) aabb.maxZ;

        yzzzFix$quad(builder, matrix, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        yzzzFix$quad(builder, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, r, g, b, a);
        yzzzFix$quad(builder, matrix, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, a);
        yzzzFix$quad(builder, matrix, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        yzzzFix$quad(builder, matrix, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
        yzzzFix$quad(builder, matrix, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, a);
    }

    @Unique
    private static void yzzzFix$quad(BufferBuilder builder, Matrix4f matrix,
                                     float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float x3, float y3, float z3,
                                     float x4, float y4, float z4,
                                     float r, float g, float b, float a) {
        builder.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x3, y3, z3).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x4, y4, z4).color(r, g, b, a).endVertex();
    }

    @Unique
    private static void yzzzFix$drawEdgeLines(BufferBuilder builder, Matrix4f matrix,
                                              AABB aabb, float r, float g, float b, float a) {
        float minX = (float) aabb.minX, minY = (float) aabb.minY, minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX, maxY = (float) aabb.maxY, maxZ = (float) aabb.maxZ;

        yzzzFix$line(builder, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        yzzzFix$line(builder, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        yzzzFix$line(builder, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        yzzzFix$line(builder, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        yzzzFix$line(builder, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        yzzzFix$line(builder, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        yzzzFix$line(builder, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        yzzzFix$line(builder, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        yzzzFix$line(builder, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        yzzzFix$line(builder, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        yzzzFix$line(builder, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        yzzzFix$line(builder, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    @Unique
    private static void yzzzFix$line(BufferBuilder builder, Matrix4f matrix,
                                     float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float r, float g, float b, float a) {
        builder.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
    }
}
