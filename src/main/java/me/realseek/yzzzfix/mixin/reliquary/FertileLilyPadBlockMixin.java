package me.realseek.yzzzfix.mixin.reliquary;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "reliquary.blocks.FertileLilyPadBlock", remap = false)
public class FertileLilyPadBlockMixin {

    /**
     * 为旧版 Reliquary 的作物筛选补充空气检查，保留原有的草方块和双层植物过滤规则。
     */
    @Inject(method = "isAllowedCropBlock", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$rejectAir(Block cropBlock, CallbackInfoReturnable<Boolean> cir) {
        if (cropBlock.defaultBlockState().isAir()) {
            cir.setReturnValue(false);
        }
    }

    // 催熟前核对方块状态，避免用已被替换或已生长的旧状态执行随机刻。
    @Inject(method = "tickCropBlock", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$verifyBeforeTick(ServerLevel world, BlockPos cropPos, BlockState cropState,
                                          Block cropBlock, double distance, CallbackInfo ci) {
        if (world.getBlockState(cropPos) != cropState) {
            ci.cancel();
        }
    }
}
