package me.realseek.yzzzfix.mixin.null_blockstate;

import me.realseek.yzzzfix.YzzzFix;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复第三方模组向 {@code Level.setBlock} 传入 null BlockState 时导致崩溃的问题。
 *
 * <p>该 Mixin 在方法入口处检查 BlockState 参数，为 null 时记录警告日志并返回 false，
 * 避免后续逻辑产生 NullPointerException。</p>
 */
@Mixin(Level.class)
public abstract class LevelMixin {

    @Inject(method = "m_6933_", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzFix$nullBlockStateCheck(
            BlockPos pos,
            BlockState state,
            int flags,
            int recursionLeft,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (state == null) {
            YzzzFix.LOGGER.warn("Blocked null BlockState at Level.setBlock({}) to prevent a crash.", pos);
            cir.setReturnValue(false);
        }
    }
}
