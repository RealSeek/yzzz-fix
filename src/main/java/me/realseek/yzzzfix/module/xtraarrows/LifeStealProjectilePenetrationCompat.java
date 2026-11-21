package me.realseek.yzzzfix.module.xtraarrows;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.heike233.projectile_immune_penetration.utils.ArrowHitHelper;

public final class LifeStealProjectilePenetrationCompat {
    private LifeStealProjectilePenetrationCompat() {}

    public static boolean hurt(LivingEntity target, LifeStealDamageSource source, float amount) {
        // 复用突破模组的伤害结算，避免附伤再触发一次箭矢附魔击退。
        return ArrowHitHelper.redirectHurt(target, source, amount, (victim, damage, hitSource, attacker) ->
                ArrowHitHelper.forceProjectileDamage(victim, damage, hitSource, attacker, Vec3.ZERO, 0));
    }
}
