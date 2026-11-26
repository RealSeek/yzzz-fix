package me.realseek.yzzzfix.mixin.mowzies_blowgun;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 将莫伊泽生物（Mowzie's Mobs）吹筒发射吹标时的出手速度倍率从 1.1 提升到 3.0。
 *
 * <p>目标方法 {@code ItemBlowgun#releaseUsing}（生产环境 SRG 名 {@code m_5551_}）在
 * 服务端生成 {@code EntityDart} 时调用
 * {@code AbstractArrow#shootFromRotation(Entity, float, float, float, float, float)}，
 * 其中第五个参数（初速度）直接写成 {@code getArrowVelocity(charge) * 1.1F}。
 * 满蓄力时 {@code getArrowVelocity} 返回 1.0，因此吹标初速度上限仅为 1.1，
 * 远低于原版弓满蓄力的 3.0。</p>
 *
 * <p>这里只替换该方法里唯一的那一个 {@code 1.1F} 常量，把吹标初速度上限提高到 3.0，
 * 与原版弓满蓄力一致；吹筒的蓄力速度、散布（1.0）、附魔和弹药消耗逻辑均不变。
 * 由于原版箭矢伤害按出手速度计算，提高初速度同时也会提高吹标伤害，这是该调整的预期结果。</p>
 */
@Mixin(targets = "com.bobmowzie.mowziesmobs.server.item.ItemBlowgun", remap = false)
public abstract class ItemBlowgunMixin {

    @ModifyConstant(
            method = {"releaseUsing", "m_5551_"},
            constant = @Constant(floatValue = 1.1F),
            remap = false
    )
    private float yzzzfix$raiseDartSpeed(float original) {
        return 3.0F;
    }
}
