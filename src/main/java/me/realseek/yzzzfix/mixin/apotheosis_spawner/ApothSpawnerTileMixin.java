package me.realseek.yzzzfix.mixin.apotheosis_spawner;

import me.realseek.yzzzfix.module.apotheosis_spawner.SpawnerESPTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        if (((Object) this).getClass().getName().contains("ApothSpawnerTile")) {
            boolean hasRedstoneControl = false;

            if (tag.contains("redstone_control") && tag.getBoolean("redstone_control")) {
                hasRedstoneControl = true;
            } else if (tag.contains("modifier_data")) {
                CompoundTag modData = tag.getCompound("modifier_data");
                if (modData.contains("redstone_control") && modData.getBoolean("redstone_control")) {
                    hasRedstoneControl = true;
                }
            }

            if (hasRedstoneControl) {
                // 使用修改后的同步注册方法
                SpawnerESPTracker.register((BlockEntity) (Object) this);
            }
        }
    }
}