package me.realseek.yzzzfix.mixin.goety;

import com.Polarice3.goety_cataclysm.common.entities.projectiles.DeathLaserBeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * 修复 DeathLaserBeam 的 caster 字段为 null 时 updateWithCaster() 抛出 NPE。
 */
@Mixin(value = DeathLaserBeam.class, remap = false)
public class DeathLaserBeamMixin {

    @Unique
    private static Field yzzzfix$casterField;

    @Inject(method = "updateWithCaster", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$cancelIfNoCaster(CallbackInfo ci) {
        try {
            if (yzzzfix$casterField == null) {
                yzzzfix$casterField = DeathLaserBeam.class.getDeclaredField("caster");
                yzzzfix$casterField.setAccessible(true);
            }
            if (yzzzfix$casterField.get(this) == null) {
                ci.cancel();
            }
        } catch (Exception ignored) {
        }
    }
}
