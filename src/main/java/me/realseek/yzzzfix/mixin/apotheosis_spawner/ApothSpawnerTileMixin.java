package me.realseek.yzzzfix.mixin.apotheosis_spawner;

import me.realseek.yzzzfix.module.apotheosis_spawner.SpawnerESPTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 追踪带有红石控制模块的神化刷怪笼，用于客户端 ESP 高亮渲染。
 *
 * <p>在 {@code ApothSpawnerTile} 加载 NBT 数据时，检测是否包含 redstone_control 标记，
 * 若存在则将该方块实体注册到 {@link SpawnerESPTracker} 中，供渲染端使用。</p>
 */
@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile", remap = false)
public abstract class ApothSpawnerTileMixin {

    @Inject(method = "load(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void yzzzFix$trackSpawner(CompoundTag tag, CallbackInfo ci) {
        if (yzzzFix$hasRedstoneControl(tag)) {
            SpawnerESPTracker.register((BlockEntity) (Object) this);
        }
    }

    @Unique
    private static boolean yzzzFix$hasRedstoneControl(CompoundTag tag) {
        if (tag.getBoolean("redstone_control")) {
            return true;
        }
        if (!tag.contains("modifier_data")) {
            return false;
        }
        return tag.getCompound("modifier_data").getBoolean("redstone_control");
    }
}
