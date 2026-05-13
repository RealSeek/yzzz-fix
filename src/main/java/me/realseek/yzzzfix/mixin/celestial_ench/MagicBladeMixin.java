package me.realseek.yzzzfix.mixin.celestial_ench;

import com.xiaoyue.celestial_core.data.CCDamageTypes;
import com.xiaoyue.celestial_enchantments.content.enchantments.weapon.MagicBlade;
import com.xiaoyue.celestial_enchantments.data.CEModConfig;
import dev.xkmc.l2damagetracker.contents.attack.AttackCache;
import dev.xkmc.l2library.init.events.GeneralEventHandler;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Celestial Enchantments 的 Magic Blade 附魔等级不生效的问题。
 *
 * <p>原始实现中伤害倍率未乘以附魔等级，导致所有等级伤害相同。
 * 该 Mixin 替换原始逻辑，使额外魔法伤害正确随附魔等级缩放。</p>
 */
@Mixin(value = MagicBlade.class, remap = false)
public class MagicBladeMixin {
    @Inject(method = "onDamageTargetFinal", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$fixDamageScaling(LivingEntity attacker, LivingEntity target, AttackCache cache, int level, CallbackInfo ci) {
        double atk = CEModConfig.COMMON.ench.weapon.magicBladeDamage.get();
        GeneralEventHandler.schedule(() -> {
            target.hurt(CCDamageTypes.magic(attacker), cache.getDamageDealt() * (float) (atk * level));
        });
        ci.cancel();
    }
}
