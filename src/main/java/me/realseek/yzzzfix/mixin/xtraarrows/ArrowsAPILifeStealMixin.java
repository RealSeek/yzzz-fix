package me.realseek.yzzzfix.mixin.xtraarrows;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 修复XtraArrows 吸血箭来源问题. */
@Pseudo
@Mixin(targets = "com.morearrows.lists.backend.ArrowsAPI", remap = false)
public class ArrowsAPILifeStealMixin {

    /**
     * XtraArrows 将受伤实体作为直接实体传递给 indirectMagic。
     * 保留原始伤害类型和攻击实体，但将箭矢标识为直接实体，
     * 以便伤害造成实体能够与 Champions 及其他伤害来源实体正常工作。
     */
    @Redirect(
        method = "lifeStealOnHitEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            remap = true
        ),
        remap = false
    )
    private static boolean fixLifeStealDamageSource(
        LivingEntity livingHit,
        DamageSource originalSource,
        float amount,
        AbstractArrow abstractArrow,
        EntityHitResult entityHitResult
    ) {
        Entity directEntity = abstractArrow;
        Entity causingEntity = originalSource.getEntity();
        DamageSource correctedSource = new DamageSource(
            originalSource.typeHolder(),
            directEntity,
            causingEntity
        );
        return livingHit.hurt(correctedSource, amount);
    }
}
