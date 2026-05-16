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
 *
 * <p>注意：必须 target BlockEntity 并在运行时检查类名，因为 Mixin 无法注入子类未 override 的继承方法。
 * 同时需要同时指定 MCP 名和 SRG 名以兼容开发环境和生产环境。</p>
 */
@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = BlockEntity.class, remap = false)
public class ApothSpawnerTileMixin {

    @Inject(
            method = {
                    "load(Lnet/minecraft/nbt/CompoundTag;)V",
                    "m_142466_(Lnet/minecraft/nbt/CompoundTag;)V"
            },
            at = @At("TAIL")
    )
    private void yzzzFix$trackSpawner(CompoundTag tag, CallbackInfo ci) {
        if (((Object) this).getClass().getName().contains("ApothSpawnerTile")
                && yzzzFix$hasRedstoneControl(tag)) {
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
