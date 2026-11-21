package me.realseek.yzzzfix.module.xtraarrows;

import Tavi007.ElementalCombat.api.AttackDataAPI;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;

public final class LifeStealElementalCombatCompat {
    private LifeStealElementalCombatCompat() {}

    public static String getImpactElement(AbstractArrow arrow) {
        Entity owner = arrow.getOwner();
        DamageSource impact = arrow.damageSources().arrow(arrow, owner == null ? arrow : owner);
        return AttackDataAPI.getFullDataAsLayer(impact).getElement();
    }
}
