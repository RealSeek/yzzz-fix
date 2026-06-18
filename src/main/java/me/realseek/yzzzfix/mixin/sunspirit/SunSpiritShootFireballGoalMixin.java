package me.realseek.yzzzfix.mixin.sunspirit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 修复太阳之灵火球射击频率过高的问题。
 * <p>将初始发射间隔从 55 延长到 275，射击间隔从 15 延长到 90，
 * 初始批量从 3 减为 1，常规批量从 4 减为 2。</p>
 */
@Mixin(targets = "com.aetherteam.aether.entity.monster.dungeon.boss.SunSpirit$ShootFireballGoal", remap = false)
public abstract class SunSpiritShootFireballGoalMixin {

    @ModifyConstant(constant = @Constant(floatValue = 55.0F), method = "<init>", remap = false)
    private float yzzzfix$increaseInitialInterval(float original) {
        return original * 5.0F;
    }

    @ModifyConstant(constant = @Constant(floatValue = 15.0F), method = "m_8056_", remap = false)
    private float yzzzfix$increaseShotInterval(float original) {
        return original * 6.0F;
    }

    @ModifyConstant(constant = @Constant(intValue = 3), method = "<init>", remap = false)
    private int yzzzfix$reduceInitialBatch(int original) {
        return 1;
    }

    @ModifyConstant(constant = @Constant(intValue = 4), method = "m_8056_", remap = false)
    private int yzzzfix$reduceBatchSize(int original) {
        return 2;
    }
}
