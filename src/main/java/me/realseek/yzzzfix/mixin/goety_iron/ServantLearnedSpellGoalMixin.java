package me.realseek.yzzzfix.mixin.goety_iron;

import com.k1sak1.goety_iron.common.entities.ally.GIronDeadKing;
import com.k1sak1.goety_iron.common.entities.ally.LearnedSpellData;
import com.k1sak1.goety_iron.common.entities.ally.ServantSpellManager;
import com.k1sak1.goety_iron.config.GIAdditionalConfig;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import me.realseek.yzzzfix.compat.GoetyIronServants;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "com.k1sak1.goety_iron.common.entities.ai.ServantLearnedSpellGoal", remap = false)
public abstract class ServantLearnedSpellGoalMixin extends WizardAttackGoal {
    @Unique
    private ServantSpellManager yzzzfix$spellManager;

    protected ServantLearnedSpellGoalMixin(IMagicEntity caster, double speed, int minInterval, int maxInterval) {
        super(caster, speed, minInterval, maxInterval);
    }

    @Redirect(
            method = {"m_8036_", "doSpellAction"},
            at = @At(value = "INVOKE", target = "Lcom/k1sak1/goety_iron/common/entities/ally/ServantSpellManager;getReadySpells()Ljava/util/List;"),
            require = 2
    )
    private List<LearnedSpellData> yzzzfix$filterReadySpells(ServantSpellManager manager) {
        if (!yzzzfix$isPlayerDeadKing()) {
            return manager.getReadySpells();
        }
        yzzzfix$spellManager = manager;
        return yzzzfix$readySpells(manager);
    }

    @Inject(method = "m_8036_", at = @At("HEAD"), cancellable = true, require = 1)
    private void yzzzfix$checkTargetBeforeStarting(CallbackInfoReturnable<Boolean> cir) {
        if (yzzzfix$isPlayerDeadKing() && !yzzzfix$validCombatTarget(mob.getTarget())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "m_8045_", at = @At("RETURN"), cancellable = true, require = 1)
    private void yzzzfix$yieldWhenNoSpells(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && yzzzfix$isPlayerDeadKing()
                && (yzzzfix$spellManager == null || yzzzfix$readySpells(yzzzfix$spellManager).isEmpty()
                || mob.getTarget() != target)) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(
            method = "doSpellAction",
            at = @At(value = "INVOKE", target = "Lcom/k1sak1/goety_iron/common/entities/ally/ServantSpellManager;resetSpellCooldown(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;)V"),
            require = 1
    )
    private void yzzzfix$cooldownOnlyAfterCast(ServantSpellManager manager, AbstractSpell spell) {
        if (!yzzzfix$isPlayerDeadKing() || spellCastingMob.isCasting()) {
            manager.resetSpellCooldown(spell);
        } else {
            attackTime = 5;
        }
    }

    @Unique
    private List<LearnedSpellData> yzzzfix$readySpells(ServantSpellManager manager) {
        List<LearnedSpellData> spells = manager.getReadySpells();
        List<? extends String> banned = GIAdditionalConfig.BannedLearnedSpells.get();
        spells.removeIf(entry -> {
            AbstractSpell spell = entry.getSpell();
            return banned.contains(spell.getSpellId())
                    || GoetyIronServants.blocksSummon(mob, spell.getSpellResource())
                    || !yzzzfix$canCastAtTarget(entry, spell);
        });
        return spells;
    }

    @Unique
    private boolean yzzzfix$canCastAtTarget(LearnedSpellData entry, AbstractSpell spell) {
        LivingEntity current = mob.getTarget();
        return yzzzfix$validCombatTarget(current) && !spell.shouldAIStopCasting(entry.getSpellLevel(), mob, current);
    }

    @Unique
    private boolean yzzzfix$isPlayerDeadKing() {
        return mob instanceof GIronDeadKing && GoetyIronServants.hasOnlinePlayerOwner(mob);
    }

    @Unique
    private boolean yzzzfix$validCombatTarget(LivingEntity current) {
        return current != null && current.isAlive() && mob.canAttack(current)
                && !mob.isAlliedTo(current) && mob.getSensing().hasLineOfSight(current);
    }
}
