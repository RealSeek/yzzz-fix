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
 * 修复 {@link AbstractCrockPotDoubleCropBlock} 因传入过时方块状态执行随机刻导致的潜在异常。
 * <p>
 * 双作物方块（如两格高的作物）在执行随机刻时，若该位置方块已被破坏或替换，
 * 过时的 {@link BlockState} 仍可能触发随机刻逻辑，导致类型转换异常或错误的生长判定。
 * <p>
 * 此 Mixin 在 {@code randomTick} 方法入口处进行双重校验：
 * <ul>
 *   <li>校验传入的方块状态是否属于当前方块类型。</li>
 *   <li>校验世界实际存在的方块是否仍属于当前方块类型。</li>
 * </ul>
 * 任一条件不满足则直接取消本次随机刻执行，从而避免崩溃或逻辑错误。
 */
@Mixin(value = AbstractCrockPotDoubleCropBlock.class, remap = false)
public class AbstractCrockPotDoubleCropBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$skipStaleDoubleCropRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        Block self = (Block) (Object) this;
        if (!state.is(self) || !level.getBlockState(pos).is(self)) {
            ci.cancel();
        }
    }
}