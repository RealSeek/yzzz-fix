package me.realseek.yzzzfix.mixin.cy3_core;

import me.realseek.yzzzfix.module.cy3_core.CY3CoreWarMageRingFixBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * CY3 战斗法师戒指生命值处理修复的后置 Mixin。
 *
 * <p>该 Mixin 与前置 Mixin（{@link WarMageRingPreMixin}）配合使用，
 * 在目标逻辑执行后恢复先前暂存的真实生命值，
 * 确保非伤害流程中的生命值变化不会被战斗法师戒指逻辑错误拦截。</p>
 */
@Mixin(value = net.minecraft.world.entity.LivingEntity.class, priority = 1500)
public abstract class WarMageRingPostMixin {

    @ModifyArg(
            method = "m_21153_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;m_14036_(FFF)F"),
            index = 0,
            remap = false
    )
    private float yzzzFix$afterWarMageRing(float newHealth) {
        CY3CoreWarMageRingFixBridge self = (CY3CoreWarMageRingFixBridge) this;
        float saved = self.yzzzFix$getSavedHealth();
        if (!Float.isNaN(saved)) {
            self.yzzzFix$setSavedHealth(Float.NaN);
            return saved;
        }
        return newHealth;
    }
}
