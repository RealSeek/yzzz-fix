package me.realseek.yzzzfix.mixin.sunspirit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 修复太阳之灵召唤火焰仆从频率过高的问题。
 * <p>将召唤间隔的上下界均扩大为原来的 3 倍。</p>
 */
@Mixin(targets = "com.aetherteam.aether.entity.monster.dungeon.boss.SunSpirit$SummonFireGoal", remap = false)
public abstract class SunSpiritSummonFireGoalMixin {

    @ModifyConstant(constant = @Constant(intValue = 10), method = "<init>", remap = false)
    private int yzzzfix$increaseSummonBaseInit(int original) {
        return original * 3;
    }

    @ModifyConstant(constant = @Constant(intValue = 40), method = "<init>", remap = false)
    private int yzzzfix$increaseSummonBoundInit(int original) {
        return original * 3;
    }

    @ModifyConstant(constant = @Constant(intValue = 10), method = "m_8056_", remap = false)
    private int yzzzfix$increaseSummonBaseTick(int original) {
        return original * 3;
    }

    @ModifyConstant(constant = @Constant(intValue = 40), method = "m_8056_", remap = false)
    private int yzzzfix$increaseSummonBoundTick(int original) {
        return original * 3;
    }
}
