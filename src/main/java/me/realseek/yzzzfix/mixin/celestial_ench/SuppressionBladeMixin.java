package me.realseek.yzzzfix.mixin.celestial_ench;

import com.xiaoyue.celestial_enchantments.content.enchantments.weapon.SuppressionBlade;
import com.xiaoyue.celestial_enchantments.data.CEModConfig;
import com.xiaoyue.celestial_enchantments.register.CEEffects;
import dev.xkmc.l2damagetracker.contents.attack.AttackCache;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Celestial Enchantments 的 Suppression Blade 附魔等级不生效的问题。
 *
 * <p>原始实现中压制效果持续时间未乘以附魔等级，导致所有等级效果相同。
 * 该 Mixin 替换原始逻辑，使压制持续时间正确随附魔等级缩放。</p>
 */
@Mixin(value = SuppressionBlade.class, remap = false)
public class SuppressionBladeMixin {
    @Inject(method = "onDamageTargetFinal", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$fixDurationScaling(LivingEntity attacker, LivingEntity target, AttackCache cache, int level, CallbackInfo ci) {
        int duration = CEModConfig.COMMON.ench.weapon.suppressionBladeEffectDuration.get();
        target.addEffect(new MobEffectInstance(CEEffects.SUPPRESSED.get(), duration * level * 20));
        ci.cancel();
    }
}
