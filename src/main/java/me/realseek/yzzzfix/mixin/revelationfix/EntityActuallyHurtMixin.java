package me.realseek.yzzzfix.mixin.revelationfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 阻止 RevelationFix 在实体受伤逻辑中调用 {@code System.exit(-1)}。
 *
 * <p>目标模组的异常处理可能直接终止 JVM。该 Mixin 拦截相关调用，
 * 避免单个逻辑错误导致客户端或服务器进程被强制关闭。</p>
 */
@Mixin(targets = "sfiomn.legendarysurvivaloverhaul.common.integration.revelationfix.events.EntityActuallyHurt", remap = false)
public abstract class EntityActuallyHurtMixin {

    @Redirect(method = "*",
            at = @At(value = "INVOKE", target = "Ljava/lang/System;exit(I)V"))
    private static void yzzzfix$preventSystemExit(int status) {
        // Prevent RevelationFix from calling System.exit(-1) on errors
    }
}
