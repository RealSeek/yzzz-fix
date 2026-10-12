package me.realseek.yzzzfix.mixin.mutant_monsters;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 Mutant Monsters 的 HurtByNearestTargetGoal 在 canContinueToUse() 中
 * 对 getLastHurtByMob() 返回的 null 直接调用 distanceToSqr()
 * 导致的 NullPointerException。
 *
 * <p>使用 @Redirect 拦截 Mob.distanceToSqr(Entity) 的调用，
 * 当目标为 null 时返回 Double.MAX_VALUE，使比较逻辑自然跳过 null 目标，
 * 从而避免崩溃。</p>
 */
@Mixin(targets = "fuzs.mutantmonsters.world.entity.ai.goal.HurtByNearestTargetGoal", remap = false)
public class HurtByNearestTargetGoalMixin {

    @Redirect(
            method = "m_8045_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;m_20280_(Lnet/minecraft/world/entity/Entity;)D"
            ),
            remap = false
    )
    private double yzzz$nullSafeDistanceToSqr(Mob self, Entity other) {
        return other == null ? Double.MAX_VALUE : self.distanceToSqr(other);
    }
}
