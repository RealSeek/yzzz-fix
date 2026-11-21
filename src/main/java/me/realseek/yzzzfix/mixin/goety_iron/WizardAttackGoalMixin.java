package me.realseek.yzzzfix.mixin.goety_iron;

import com.k1sak1.goety_iron.common.entities.ai.DeadKingServantGoal;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import me.realseek.yzzzfix.compat.GoetyIronServants;
import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal", remap = false)
public abstract class WizardAttackGoalMixin {
    @Shadow @Final protected PathfinderMob mob;
    @Shadow protected int attackTime;
    @Shadow protected AbstractSpell singleUseSpell;

    @Inject(
            method = "m_8037_",
            at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/entity/mobs/goals/WizardAttackGoal;handleAttackLogic(D)V"),
            require = 1
    )
    private void yzzzfix$startDeadKingAttackTimer(CallbackInfo ci) {
        if (attackTime < 0 && (Object) this instanceof DeadKingServantGoal
                && GoetyIronServants.hasOnlinePlayerOwner(mob)) {
            attackTime = 12;
        }
    }

    @Redirect(
            method = "doSpellAction",
            at = @At(value = "FIELD", target = "Lio/redspace/ironsspellbooks/entity/mobs/goals/WizardAttackGoal;singleUseSpell:Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;", ordinal = 0),
            require = 1
    )
    private AbstractSpell yzzzfix$skipBlockedSingleUseSpell(WizardAttackGoal goal) {
        if (goal instanceof DeadKingServantGoal && singleUseSpell != SpellRegistry.none()
                && GoetyIronServants.blocksSummon(mob, singleUseSpell.getSpellResource())) {
            return SpellRegistry.none();
        }
        return singleUseSpell;
    }
}
