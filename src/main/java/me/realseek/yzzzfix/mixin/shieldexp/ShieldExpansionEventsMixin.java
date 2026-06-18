package me.realseek.yzzzfix.mixin.shieldexp;

import org.infernalstudios.shieldexp.events.ShieldExpansionEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 getShieldValue 对未配置物品返回 null 导致 NPE。
 */
@Mixin(value = ShieldExpansionEvents.class, remap = false)
public class ShieldExpansionEventsMixin {

    @Inject(method = "getShieldValue", at = @At("RETURN"))
    private static void yzzzfix$fixNullReturn(CallbackInfoReturnable<Double> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(0.0D);
        }
    }
}
