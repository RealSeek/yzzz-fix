package me.realseek.yzzzfix.mixin.xtraarrows;

import me.realseek.yzzzfix.module.xtraarrows.LifeStealArrow;
import me.realseek.yzzzfix.module.xtraarrows.LifeStealElementalCombatCompat;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
        "com.morearrows.specialarrowentities.lifesteal.DiamondLifeStealArrowEntity",
        "com.morearrows.specialarrowentities.lifesteal.FlintLifeStealArrowEntity",
        "com.morearrows.specialarrowentities.lifesteal.GoldenLifeStealArrowEntity",
        "com.morearrows.specialarrowentities.lifesteal.IronLifeStealArrowEntity",
        "com.morearrows.specialarrowentities.lifesteal.NetheriteLifeStealArrowEntity"
}, remap = false)
public abstract class LifeStealArrowElementMixin implements LifeStealArrow {
    @Unique
    private String yzzzFix$impactElement;

    @Inject(method = "m_5790_(Lnet/minecraft/world/phys/EntityHitResult;)V", at = @At("HEAD"), remap = false)
    private void yzzzFix$rememberImpactElement(EntityHitResult hit, CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        // 本体命中可能移除箭矢并使元素能力失效，附加伤害必须用命中前的元素。
        if (!arrow.level().isClientSide && ModList.get().isLoaded("elementalcombat")) {
            yzzzFix$impactElement = LifeStealElementalCombatCompat.getImpactElement(arrow);
        }
    }

    @Override
    public String yzzzFix$getImpactElement() {
        return yzzzFix$impactElement;
    }
}
