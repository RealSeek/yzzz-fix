package me.realseek.yzzzfix.mixin.crockpot;

import com.sihenzhang.crockpot.block.AbstractCrockPotDoubleCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 防止 CrockPot 的双层作物使用已过时的方块状态执行随机刻。
 *
 * <p>如果催熟后该位置变为空气，原逻辑会在计算生长概率时尝试读取空气的 {@code age} 属性。
 * 如果作物仍在但生长阶段已变化，旧状态也会导致错误的生长判定。
 * 因此仅当传入状态属于当前作物且仍是该位置的实际状态时，才继续执行随机刻。</p>
 */
@Mixin(value = AbstractCrockPotDoubleCropBlock.class, remap = false)
public class AbstractCrockPotDoubleCropBlockMixin {

    // 分别匹配开发环境和 Forge 生产环境中的 randomTick 方法。
    @Inject(method = {"randomTick", "m_213898_"}, at = @At("HEAD"), cancellable = true, require = 1)
    private void yzzzfix$skipStaleDoubleCropRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        Block self = (Block) (Object) this;
        if (!state.is(self) || level.getBlockState(pos) != state) {
            ci.cancel();
        }
    }
}
