package me.realseek.yzzzfix.mixin.sunspirit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 修复太阳之灵水晶实体存活时间过长的问题。
 * <p>将水晶寿命从 300 tick（15秒）缩减为 100 tick（5秒）。</p>
 */
@Mixin(targets = "com.aetherteam.aether.entity.projectile.crystal.AbstractCrystal", remap = false)
public abstract class AbstractCrystalMixin {

    @ModifyConstant(constant = @Constant(intValue = 300), method = "getLifeSpan", remap = false)
    private int yzzzfix$reduceLifespan(int original) {
        return 100;
    }
}
