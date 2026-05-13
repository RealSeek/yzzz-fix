package me.realseek.yzzzfix.mixin.celestial_ench;

import com.xiaoyue.celestial_enchantments.content.enchantments.weapon.DestructionCrack;
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
 * 修复 Celestial Enchantments 的 Destruction Crack 附魔等级不生效的问题。
 *
 * <p>原始实现中效果持续时间未乘以附魔等级，导致所有等级效果相同。
 * 该 Mixin 替换原始逻辑，使持续时间正确随附魔等级缩放。</p>
 */
@Mixin(value = DestructionCrack.class, remap = false)
public class DestructionCrackMixin {
    @Inject(method = "onDamageTargetFinal", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$fixDurationScaling(LivingEntity attacker, LivingEntity target, AttackCache cache, int level, CallbackInfo ci) {
        int duration = CEModConfig.COMMON.ench.weapon.destructionCrackDuration.get();
        target.addEffect(new MobEffectInstance(CEEffects.DESTRUCTED.get(), duration * level * 20));
        ci.cancel();
    }
}
