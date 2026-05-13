package me.realseek.yzzzfix.mixin.celestial_ench;

import com.xiaoyue.celestial_enchantments.content.enchantments.armor.LifeShield;
import com.xiaoyue.celestial_enchantments.data.CEModConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Celestial Enchantments 的 Life Shield 附魔等级不生效的问题。
 *
 * <p>原始实现中护盾上限和转化比例未乘以附魔等级，导致所有等级效果相同。
 * 该 Mixin 替换原始逻辑，使生命护盾效果正确随附魔等级缩放。</p>
 */
@Mixin(value = LifeShield.class, remap = false)
public class LifeShieldMixin {
    @Inject(method = "onLivingHeal", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$fixAbsorptionScaling(LivingHealEvent event, LivingEntity entity, int level, CallbackInfo ci) {
        double factor = CEModConfig.COMMON.ench.armor.lifeShieldPercentage.get();
        float absorption = entity.getAbsorptionAmount();
        float threshold = entity.getMaxHealth() * (float) (factor * level);
        if (absorption > threshold) { ci.cancel(); return; }
        float healAmount = event.getAmount() * (float) (factor * level);
        entity.setAbsorptionAmount(Math.min(healAmount + absorption, threshold));
        ci.cancel();
    }
}
