package me.realseek.yzzzfix.mixin.cy3_core;

import me.realseek.yzzzfix.module.cy3_core.CY3CoreWarMageRingFixBridge;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CY3 战斗法师戒指生命值处理修复的前置 Mixin。
 *
 * <p>该 Mixin 在 {@code LivingEntity#setHealth} 调用中暂存非伤害流程导致的生命值下降，
 * 并临时阻止目标模组逻辑错误地处理该生命值变化。</p>
 *
 * <p>实际生命值会由后置 Mixin（{@link WarMageRingPostMixin}）在更低优先级阶段恢复写入，
 * 从而绕过目标逻辑中的错误判断。</p>
 */
@Mixin(value = LivingEntity.class, priority = 500)
public abstract class WarMageRingPreMixin implements CY3CoreWarMageRingFixBridge {

    @Unique
    private int yzzzFix$hurtDepth;

    @Unique
    private float yzzzFix$savedHealth = Float.NaN;

    @Override
    public float yzzzFix$getSavedHealth() {
        return this.yzzzFix$savedHealth;
    }

    @Override
    public void yzzzFix$setSavedHealth(float value) {
        this.yzzzFix$savedHealth = value;
    }

    @Inject(method = "m_6469_", at = @At("HEAD"), remap = false)
    private void yzzzFix$hurtStart(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        yzzzFix$hurtDepth++;
    }

    @Inject(method = "m_6469_", at = @At("RETURN"), remap = false)
    private void yzzzFix$hurtEnd(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        yzzzFix$hurtDepth--;
    }

    @ModifyArg(
            method = "m_21153_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;m_14036_(FFF)F"),
            index = 0,
            remap = false
    )
    private float yzzzFix$beforeWarMageRing(float newHealth) {
        if (yzzzFix$hurtDepth == 0 && newHealth < ((LivingEntity) (Object) this).getHealth()) {
            yzzzFix$savedHealth = newHealth;
            return Float.MAX_VALUE;
        }
        yzzzFix$savedHealth = Float.NaN;
        return newHealth;
    }
}
