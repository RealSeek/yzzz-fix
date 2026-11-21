package me.realseek.yzzzfix.mixin.reliquary;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 新版 Reliquary 可能在同一次催熟中先施用骨粉，再对施用前的方块状态执行随机刻。
 * 随机刻前重新读取世界状态。若仍是同一种作物，则使用最新状态，否则跳过本次随机刻。
 */
@Pseudo
@Mixin(targets = "reliquary.block.FertileLilyPadBlock", remap = false)
public class ModernFertileLilyPadBlockMixin {

    @Group(name = "yzzzfix$refreshCropTick", min = 1, max = 1)
    @Redirect(method = "tickCropBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private void yzzzfix$refreshCropTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        yzzzfix$tickCurrentCrop(state, level, pos, random);
    }

    @Group(name = "yzzzfix$refreshCropTick", min = 1, max = 1)
    @Redirect(method = "tickCropBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;m_222972_(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"))
    private void yzzzfix$refreshCropTickSrg(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        yzzzfix$tickCurrentCrop(state, level, pos, random);
    }

    @Unique
    private static void yzzzfix$tickCurrentCrop(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState current = level.getBlockState(pos);
        if (current.is(state.getBlock())) {
            current.randomTick(level, pos, random);
        }
    }
}
