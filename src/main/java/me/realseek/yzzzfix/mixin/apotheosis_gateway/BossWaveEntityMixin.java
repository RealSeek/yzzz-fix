package me.realseek.yzzzfix.mixin.apotheosis_gateway;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 修复 Apotheosis Gateway 的 BossWave 实体在 Boss 数据缺失时产生空指针异常的问题。
 *
 * <p>当 bossId 存在但对应的 Boss 对象为 null 时，{@code getDescription()} 会崩溃。
 * 该 Mixin 在此情况下返回一个安全的默认描述文本。</p>
 */
@Mixin(targets = "dev.shadowsoffire.apotheosis.adventure.compat.GatewaysCompat$BossWaveEntity", remap = false)
public class BossWaveEntityMixin {

    @Shadow private Optional<?> bossId;
    @Shadow private Supplier<?> boss;

    @Inject(method = "getDescription", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzfix$fixNullBossDescription(CallbackInfoReturnable<MutableComponent> cir) {
        if (!bossId.isEmpty() && boss.get() == null) {
            cir.setReturnValue(Component.translatable("misc.apotheosis.boss",
                    Component.translatable("misc.apotheosis.random")));
        }
    }
}
