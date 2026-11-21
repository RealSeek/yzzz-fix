package me.realseek.yzzzfix.mixin.goety_iron;

import com.k1sak1.goety_iron.common.entities.ally.GIronDeadKing;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.entity.mobs.goals.WarlockAttackGoal;
import me.realseek.yzzzfix.compat.GoetyIronServants;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.k1sak1.goety_iron.common.entities.ai.DeadKingServantGoal", remap = false)
public abstract class DeadKingCombatGoalMixin extends WarlockAttackGoal {
    @Shadow int meleeAnimTimer;

    protected DeadKingCombatGoalMixin(IMagicEntity caster, double speed, int minInterval, int maxInterval, float range) {
        super(caster, speed, minInterval, maxInterval, range);
    }

    @Inject(method = "m_8036_", at = @At("HEAD"), cancellable = true, require = 1)
    private void yzzzfix$allowPursuit(CallbackInfoReturnable<Boolean> cir) {
        if (!GoetyIronServants.hasOnlinePlayerOwner(mob)) {
            return;
        }
        LivingEntity current = mob.getTarget();
        if (!yzzzfix$isValidTarget(current)) {
            cir.setReturnValue(false);
            return;
        }
        target = current;
        cir.setReturnValue(true);
    }

    @Inject(method = "m_8045_", at = @At("HEAD"), cancellable = true, require = 1)
    private void yzzzfix$stopOnInvalidTarget(CallbackInfoReturnable<Boolean> cir) {
        if (GoetyIronServants.hasOnlinePlayerOwner(mob) && !yzzzfix$isValidTarget(mob.getTarget())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "doMovement", at = @At("HEAD"), cancellable = true, require = 1)
    private void yzzzfix$pathAroundCover(double distanceSquared, CallbackInfo ci) {
        if (!hasLineOfSight && GoetyIronServants.hasOnlinePlayerOwner(mob)) {
            if (mob.tickCount % 10 == 0) {
                mob.getNavigation().moveTo(target, speedModifier);
            }
            ci.cancel();
        }
    }

    @Inject(method = "handleAttackLogic", at = @At("HEAD"), cancellable = true, require = 1)
    private void yzzzfix$holdFireBehindCover(double distanceSquared, CallbackInfo ci) {
        if (!hasLineOfSight && meleeAnimTimer < 0 && GoetyIronServants.hasOnlinePlayerOwner(mob)) {
            if (spellCastingMob.isCasting()) {
                spellCastingMob.cancelCast();
            }
            ci.cancel();
        }
    }

    @Inject(method = "m_8041_", at = @At("TAIL"), require = 1)
    private void yzzzfix$clearMeleeState(CallbackInfo ci) {
        if (mob instanceof GIronDeadKing king && GoetyIronServants.hasOnlinePlayerOwner(mob)) {
            king.isMeleeing = false;
        }
    }

    @Override
    protected void resetAttackTimer(double distanceSquared) {
        if (GoetyIronServants.hasOnlinePlayerOwner(mob)) {
            float distanceFactor = Mth.clamp((float) Math.sqrt(distanceSquared) / attackRadius, 0F, 1F);
            attackTime = spellCastingMob.isCasting() ? 4 : Mth.floor(Mth.lerp(distanceFactor, 25F, 40F));
        } else {
            super.resetAttackTimer(distanceSquared);
        }
    }

    @Override
    protected void doSpellAction() {
        super.doSpellAction();
        if (GoetyIronServants.hasOnlinePlayerOwner(mob) && !spellCastingMob.isCasting()) {
            attackTime = Math.min(attackTime, 5);
        }
    }

    @Unique
    private boolean yzzzfix$isValidTarget(LivingEntity current) {
        return current != null && current.isAlive() && mob.canAttack(current) && !mob.isAlliedTo(current);
    }
}
