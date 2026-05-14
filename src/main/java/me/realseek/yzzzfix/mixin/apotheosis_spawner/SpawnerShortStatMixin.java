package me.realseek.yzzzfix.mixin.apotheosis_spawner;

import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Function;
/**
 * 纯 Inject 拦截：完全放弃 Shadow，利用反射强取父类变量
 * 修复了恼鬼刷怪笼最低0变成20的问题
 */
@SuppressWarnings({"UnresolvedMixinReference", "unchecked", "rawtypes"})
@Mixin(targets = "dev.shadowsoffire.apotheosis.spawn.modifiers.SpawnerStats$ShortStat", remap = false)
public abstract class SpawnerShortStatMixin {

    private static Field yzzzFix$getterField = null;
    private static Field yzzzFix$setterField = null;


    @Inject(
            method = "apply(Ljava/lang/Short;Ljava/lang/Short;Ljava/lang/Short;Ldev/shadowsoffire/apotheosis/spawn/spawner/ApothSpawnerTile;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void yzzzFix$fixZeroStatClamping(Short value, Short min, Short max, ApothSpawnerTile spawner, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (yzzzFix$getterField == null || yzzzFix$setterField == null) {
                Class<?> baseClass = this.getClass().getSuperclass(); // 获取父类 Base
                for (Field f : baseClass.getDeclaredFields()) {
                    if (f.getType() == Function.class) {
                        f.setAccessible(true);
                        yzzzFix$getterField = f;
                    } else if (f.getType() == BiConsumer.class) {
                        f.setAccessible(true);
                        yzzzFix$setterField = f;
                    }
                }
            }

            if (yzzzFix$getterField == null || yzzzFix$setterField == null) {
                return;
            }

            Function getter = (Function) yzzzFix$getterField.get(this);
            BiConsumer setter = (BiConsumer) yzzzFix$setterField.get(this);
            short old = ((Short) getter.apply(spawner)).shortValue();

            int effectiveMin = Math.min((int) old, (int) min);

            short newValue = (short) Mth.clamp(old + value, effectiveMin, (int) max);
            setter.accept(spawner, newValue);
            short currentAfter = ((Short) getter.apply(spawner)).shortValue();
            cir.setReturnValue(old != currentAfter);

        } catch (Exception e) {
            System.out.println("[YzzzFix] 神化刷怪笼 Mixin 反射异常，放行原版逻辑: " + e.getMessage());
            e.printStackTrace();
        }
    }
}