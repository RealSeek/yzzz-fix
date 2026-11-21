package me.realseek.yzzzfix.mixin.goety_iron;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.goals.SpellBarrageGoal;
import me.realseek.yzzzfix.compat.GoetyIronServants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.k1sak1.goety_iron.common.entities.ally.GIronDeadKing$GIronDeadKingBarrageGoal", remap = false)
public abstract class DeadKingBarrageGoalMixin extends SpellBarrageGoal {
    protected DeadKingBarrageGoalMixin(IMagicEntity caster, AbstractSpell spell, int minLevel, int maxLevel,
                                       int minInterval, int maxInterval, int count) {
        super(caster, spell, minLevel, maxLevel, minInterval, maxInterval, count);
    }

    @Inject(method = "m_8036_", at = @At("HEAD"), cancellable = true, require = 1)
    private void yzzzfix$canUseBarrage(CallbackInfoReturnable<Boolean> cir) {
        if (mob.getTarget() == null) {
            return;
        }
        if (GoetyIronServants.blocksSummon(mob, spell.getSpellResource())
                || (GoetyIronServants.hasOnlinePlayerOwner(mob)
                && (!mob.getTarget().isAlive() || !mob.getSensing().hasLineOfSight(mob.getTarget())))) {
            cir.setReturnValue(false);
        }
    }
}
