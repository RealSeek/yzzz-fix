package me.realseek.yzzzfix.mixin.mokels;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Mokel's Boss Mantyd 攻击模式过程在 Boss 无目标时的空指针崩溃。
 *
 * <p>MCreator 生成的 {@code BossMantydAttackPatternProcedure.execute} 方法假设
 * Boss 实体始终拥有攻击目标，当目标为 null 时会对其调用 {@code getX()} 等方法导致 NPE。</p>
 *
 * <p>本 Mixin 在 execute 方法头部检查实体状态，当传入实体不是 Mob 或没有攻击目标时
 * 直接跳过整个攻击模式逻辑，保持 Boss 正常的空闲行为。</p>
 */
@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "net.mcreator.mokelsbossmantyd.procedures.BossMantydAttackPatternProcedure", remap = false)
public abstract class MokelsBossMantydModMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void yzzzfix$skipAttackPatternWithoutTarget(LevelAccessor world,
                                                               double x, double y, double z,
                                                               Entity entity, CallbackInfo ci) {
        if (!(entity instanceof Mob mob) || mob.getTarget() == null) {
            ci.cancel();
        }
    }
}
