package me.realseek.yzzzfix.mixin.celestial_ench;

import com.xiaoyue.celestial_enchantments.content.enchantments.armor.TraumaAbsorption;
import com.xiaoyue.celestial_enchantments.data.CEModConfig;
import dev.xkmc.l2damagetracker.contents.attack.AttackCache;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Celestial Enchantments 的 Trauma Absorption 附魔等级不生效的问题。
 *
 * <p>原始实现中治疗量未乘以附魔等级，导致所有等级回复相同。
 * 该 Mixin 替换原始逻辑，使创伤吸收回复量正确随附魔等级缩放。</p>
 */
@Mixin(value = TraumaAbsorption.class, remap = false)
public class TraumaAbsorptionMixin {
    @Inject(method = "onDamagedFinal", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$fixHealScaling(LivingEntity entity, AttackCache cache, int level, CallbackInfo ci) {
        double heal = CEModConfig.COMMON.ench.armor.traumaAbsorptionHeal.get();
        entity.heal((entity.getMaxHealth() - entity.getHealth()) * (float) (heal * level));
        ci.cancel();
    }
}
