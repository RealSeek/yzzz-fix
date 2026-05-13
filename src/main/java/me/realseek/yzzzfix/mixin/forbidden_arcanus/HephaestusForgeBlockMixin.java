package me.realseek.yzzzfix.mixin.forbidden_arcanus;

import com.stal111.forbidden_arcanus.common.block.HephaestusForgeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Forbidden Arcanus 的 HephaestusForgeBlock 在方块状态变更时可能崩溃的问题。
 *
 * <p>当新方块状态不再是 HephaestusForgeBlock 实例时（如方块被破坏），
 * 原始 {@code onBlockStateChange} 逻辑会产生异常。该 Mixin 在此情况下取消执行。</p>
 */
@Mixin(HephaestusForgeBlock.class)
public abstract class HephaestusForgeBlockMixin {

    @Inject(method = "onBlockStateChange", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzFix$onBlockStateChangeSafe(
            LevelReader level,
            BlockPos pos,
            BlockState oldState,
            BlockState newState,
            CallbackInfo ci
    ) {
        if (!(newState.getBlock() instanceof HephaestusForgeBlock)) {
            ci.cancel();
        }
    }
}
