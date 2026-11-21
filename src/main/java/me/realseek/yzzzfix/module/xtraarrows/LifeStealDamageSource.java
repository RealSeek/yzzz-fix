package me.realseek.yzzzfix.module.xtraarrows;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.AbstractArrow;

public final class LifeStealDamageSource extends DamageSource {
    private final String impactElement;

    public LifeStealDamageSource(DamageSource originalSource, AbstractArrow arrow) {
        super(originalSource.typeHolder(), arrow, originalSource.getEntity());
        this.impactElement = arrow instanceof LifeStealArrow lifeStealArrow
                ? lifeStealArrow.yzzzFix$getImpactElement()
                : null;
    }

    public String impactElement() {
        return impactElement;
    }
}
