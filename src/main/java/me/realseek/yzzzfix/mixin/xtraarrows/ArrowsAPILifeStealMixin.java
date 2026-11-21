package me.realseek.yzzzfix.mixin.xtraarrows;

import me.realseek.yzzzfix.module.xtraarrows.LifeStealDamageSource;
import me.realseek.yzzzfix.module.xtraarrows.LifeStealProjectilePenetrationCompat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 修正 Xtra Arrows 吸血箭的附加伤害来源。 */
@Pseudo
@Mixin(targets = "com.morearrows.lists.backend.ArrowsAPI", remap = false)
public class ArrowsAPILifeStealMixin {

    /**
     * 原版把受击者作为直接伤害来源，导致 Champions 触发受击者自己的进攻词缀。
     */
    @Redirect(
        method = "lifeStealOnHitEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            remap = false
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
        LifeStealDamageSource source = new LifeStealDamageSource(originalSource, abstractArrow);
        if (ModList.get().isLoaded("projectile_immune_penetration") && !livingHit.level().isClientSide()) {
            return LifeStealProjectilePenetrationCompat.hurt(livingHit, source, amount);
        }
        return livingHit.hurt(source, amount);
    }
}
