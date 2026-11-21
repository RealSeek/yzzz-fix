package me.realseek.yzzzfix.mixin.xtraarrows;

import Tavi007.ElementalCombat.capabilities.attack.AttackData;
import Tavi007.ElementalCombat.capabilities.attack.AttackLayer;
import me.realseek.yzzzfix.module.xtraarrows.LifeStealDamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "Tavi007.ElementalCombat.util.AttackDataHelper", remap = false)
public abstract class ElementalCombatAttackDataMixin {
    @Unique
    private static final ResourceLocation YZZZFIX_BASE_LAYER = new ResourceLocation("minecraft", "base");
    @Unique
    private static final ResourceLocation YZZZFIX_DIRECT_ENTITY_LAYER = new ResourceLocation("minecraft", "direct_entity");

    @Inject(method = "get(Lnet/minecraft/world/damagesource/DamageSource;)LTavi007/ElementalCombat/capabilities/attack/AttackData;",
            at = @At("RETURN"), remap = false)
    private static void yzzzFix$inheritArrowElement(DamageSource source, CallbackInfoReturnable<AttackData> cir) {
        if (source instanceof LifeStealDamageSource lifeSteal && lifeSteal.impactElement() != null) {
            AttackData magicAttack = cir.getReturnValue();
            String style = magicAttack.getStyle();
            // 默认元素会被跳过，移除直接实体层才能防止无属性攻击回退到箭矢的暗属性。
            magicAttack.getLayers().remove(YZZZFIX_DIRECT_ENTITY_LAYER);
            magicAttack.putLayer(YZZZFIX_BASE_LAYER, new AttackLayer(style, lifeSteal.impactElement()));
        }
    }
}
