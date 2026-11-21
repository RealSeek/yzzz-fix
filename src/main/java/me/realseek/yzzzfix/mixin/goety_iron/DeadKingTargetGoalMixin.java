package me.realseek.yzzzfix.mixin.goety_iron;

import com.Polarice3.Goety.api.entities.IOwned;
import com.k1sak1.goety_iron.common.entities.ally.GIronDeadKing;
import me.realseek.yzzzfix.compat.GoetyIronServants;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.k1sak1.goety_iron.common.entities.ally.GIronDeadKing$GIronDeadKingTargetGoal", remap = false)
public abstract class DeadKingTargetGoalMixin extends TargetGoal {
    @Shadow @Final private GIronDeadKing deadKing;
    @Shadow private LivingEntity target;

    @Unique
    private long yzzzfix$lastVisibleTick;

    protected DeadKingTargetGoalMixin(Mob mob, boolean mustSee, boolean mustReach) {
        super(mob, mustSee, mustReach);
    }

    @Inject(method = "m_8036_", at = @At("HEAD"), cancellable = true, require = 1)
    private void yzzzfix$selectVisibleTarget(CallbackInfoReturnable<Boolean> cir) {
        if (!GoetyIronServants.hasOnlinePlayerOwner(deadKing)) {
            return;
        }

        LivingEntity owner = deadKing.getTrueOwner();
        LivingEntity nearest = null;
        double nearestDistance = getFollowDistance() * getFollowDistance();
        for (LivingEntity candidate : deadKing.level().getEntitiesOfClass(
                LivingEntity.class, deadKing.getBoundingBox().inflate(16, 8, 16))) {
            double distance = deadKing.distanceToSqr(candidate);
            if (distance <= nearestDistance && candidate != owner && yzzzfix$isValidTarget(candidate)
                    && yzzzfix$isHostile(candidate, owner) && deadKing.getSensing().hasLineOfSight(candidate)) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        target = nearest;
        yzzzfix$lastVisibleTick = deadKing.level().getGameTime();
        cir.setReturnValue(nearest != null);
    }

    @Override
    public boolean canContinueToUse() {
        if (GoetyIronServants.hasOnlinePlayerOwner(deadKing)) {
            LivingEntity current = deadKing.getTarget();
            if (current == null || !yzzzfix$isValidTarget(current)) {
                return false;
            }
            long now = deadKing.level().getGameTime();
            if (deadKing.getSensing().hasLineOfSight(current)) {
                yzzzfix$lastVisibleTick = now;
            } else if (now - yzzzfix$lastVisibleTick >= 30) {
                return false;
            }
        }
        return super.canContinueToUse();
    }

    @Unique
    private boolean yzzzfix$isValidTarget(LivingEntity candidate) {
        return candidate != deadKing && candidate.isAlive()
                && deadKing.canAttack(candidate) && !deadKing.isAlliedTo(candidate);
    }

    @Unique
    private boolean yzzzfix$isHostile(LivingEntity candidate, LivingEntity owner) {
        if (candidate.getLastHurtByMob() == deadKing
                || (owner != null && candidate.getLastHurtByMob() == owner)) {
            return true;
        }
        return candidate instanceof IOwned owned ? owned.isHostile() : candidate instanceof Enemy;
    }
}
