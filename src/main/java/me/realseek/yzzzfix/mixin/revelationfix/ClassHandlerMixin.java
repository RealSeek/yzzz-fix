package me.realseek.yzzzfix.mixin.revelationfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;

/**
 * 修复 RevelationFix 在反射读取类字段时可能因异常导致崩溃的问题。
 *
 * <p>该 Mixin 捕获 {@code getDeclaredFields()} 相关异常，使目标逻辑在遇到不可访问或异常类时
 * 能够安全跳过，而不是中断游戏流程。</p>
 */
@Mixin(targets = "sfiomn.legendarysurvivaloverhaul.common.integration.revelationfix.ClassHandler", remap = false)
public abstract class ClassHandlerMixin {

    @Redirect(method = "*",
            at = @At(value = "INVOKE", target = "Ljava/lang/Class;getDeclaredFields()[Ljava/lang/reflect/Field;"))
    private static Field[] yzzzfix$safeGetDeclaredFields(Class<?> clazz) {
        try {
            return clazz.getDeclaredFields();
        } catch (Throwable t) {
            return new Field[0];
        }
    }
}
