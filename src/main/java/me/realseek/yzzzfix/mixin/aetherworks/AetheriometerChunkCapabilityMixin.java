package me.realseek.yzzzfix.mixin.aetherworks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.network.PacketDistributor;
import net.sirplop.aetherworks.capabilities.AetheriometerChunkCapability;
import net.sirplop.aetherworks.network.MessageSyncAetheriometer;
import net.sirplop.aetherworks.network.PacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Aetherworks 在区块监听事件中可能访问未加载区块的问题。
 *
 * <p>原始逻辑在 {@code ChunkWatchEvent.Watch} 中直接获取区块数据，
 * 但此时区块可能尚未完全加载。该 Mixin 替换原始处理逻辑，
 * 使用 {@code getChunkNow} 安全获取区块，为 null 时跳过同步。</p>
 */
@Mixin(targets = "net.sirplop.aetherworks.capabilities.AetheriometerChunkCapability$EventHandler", remap = false)
public abstract class AetheriometerChunkCapabilityMixin {

    @Inject(method = "chunkWatch", at = @At("HEAD"), cancellable = true, remap = false)
    private static void yzzzfix$safeChunkWatch(ChunkWatchEvent.Watch event, CallbackInfo ci) {
        ci.cancel();
        ServerPlayer player = event.getPlayer();
        ServerLevel level = event.getLevel();
        ChunkPos pos = event.getPos();
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
        if (chunk == null) return;
        AetheriometerChunkCapability.getData(chunk).ifPresent(cap -> {
            int data = cap.getData();
            PacketHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new MessageSyncAetheriometer(pos, data)
            );
        });
    }
}
