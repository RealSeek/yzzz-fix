package me.realseek.yzzzfix.mixin.cy3_core;

import net.minecraft.world.damagesource.DamageType;
import org.heike233.chapterofyuusha3.comm.compat.curios.item.adaptation.BasicAdaptation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 CY3 BasicAdaptation 在 handleTick 中读取 {@code DamageType#msgId()} 时可能出现空值的问题。
 *
 * <p>AdvancedDamage 将 lastDamageType 初始化为 null。当 tickCount 归零时，
 * handleTick 直接调用 {@code lastDamageType.msgId()} 而没有空值检查，
 * 导致玩家从未受伤或伤害记录被清除时产生 NullPointerException。</p>
 *
 * <p>该 Mixin 重定向 {@code DamageType.msgId()} 调用，在实例为 null 时返回空字符串。</p>
 *
 * <p>注意：{@code @Mixin} 使用 remap=false（目标为 mod 类），
 * 但 {@code @At} target 使用 remap=true（{@code DamageType.msgId()} 是 Minecraft 方法）。</p>
 */
@Mixin(value = BasicAdaptation.class, remap = false)
public abstract class BasicAdaptationMixin {

    @Redirect(
            method = "handleTick",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/damagesource/DamageType;msgId()Ljava/lang/String;",
                     remap = true),
            require = 0
    )
    private String yzzzFix$nullCheckLastDamageType(DamageType damageType) {
        if (damageType == null) {
            return "";
        }
        return damageType.msgId();
    }
}
