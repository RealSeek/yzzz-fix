package me.realseek.yzzzfix.mixin.apotheosis_spawner;

import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 修复神化（Apotheosis）刷怪笼 ShortStat 的 clamp 逻辑缺陷。
 *
 * <p>原始实现中，当恼鬼刷怪笼的 spawnDelay 最小值为 0 时，apply 方法会将其错误地
 * clamp 到默认最小值 20，导致刷怪间隔无法低于 20 tick。</p>
 *
 * <p>本 Mixin 通过反射获取父类 {@code Stat} 的 getter/setter 字段，重新实现 clamp 逻辑：
 * 使用 {@code Math.min(currentValue, min)} 作为有效最小值，允许数值低于参数指定的 min。</p>
 *
 * <p>反射初始化采用 volatile + double-checked locking 保证线程安全。</p>
 */
@SuppressWarnings({"UnresolvedMixinReference", "unchecked"})
@Mixin(targets = "dev.shadowsoffire.apotheosis.spawn.modifiers.SpawnerStats$ShortStat", remap = false)
public abstract class SpawnerShortStatMixin {

    @Unique
    private static final Logger yzzzFix$LOGGER = LogManager.getLogger("YzzzFix");

    @Unique
    private static volatile Field yzzzFix$getterField;

    @Unique
    private static volatile Field yzzzFix$setterField;

    @Inject(
            method = "apply(Ljava/lang/Short;Ljava/lang/Short;Ljava/lang/Short;Ldev/shadowsoffire/apotheosis/spawn/spawner/ApothSpawnerTile;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void yzzzFix$fixZeroStatClamping(Short value, Short min, Short max,
                                             ApothSpawnerTile spawner,
                                             CallbackInfoReturnable<Boolean> cir) {
        try {
            yzzzFix$initFieldsIfNeeded();

            Field getterField = yzzzFix$getterField;
            Field setterField = yzzzFix$setterField;
            if (getterField == null || setterField == null) {
                return;
            }

            Function<ApothSpawnerTile, Short> getter =
                    (Function<ApothSpawnerTile, Short>) getterField.get(this);
            BiConsumer<ApothSpawnerTile, Short> setter =
                    (BiConsumer<ApothSpawnerTile, Short>) setterField.get(this);

            short oldValue = getter.apply(spawner);
            int effectiveMin = Math.min((int) oldValue, (int) min);
            short newValue = (short) Mth.clamp(oldValue + value, effectiveMin, (int) max);

            setter.accept(spawner, newValue);
            short currentValue = getter.apply(spawner);
            cir.setReturnValue(oldValue != currentValue);

        } catch (ReflectiveOperationException | RuntimeException e) {
            yzzzFix$LOGGER.warn("[YzzzFix] Apotheosis spawner ShortStat mixin reflection failed, falling back to original logic.", e);
        }
    }

    @Unique
    private static void yzzzFix$initFieldsIfNeeded() throws ReflectiveOperationException {
        if (yzzzFix$getterField != null && yzzzFix$setterField != null) {
            return;
        }
        synchronized (SpawnerShortStatMixin.class) {
            if (yzzzFix$getterField != null && yzzzFix$setterField != null) {
                return;
            }
            Class<?> statClass = Class.forName(
                    "dev.shadowsoffire.apotheosis.spawn.modifiers.SpawnerStats$Stat");
            Field getter = null;
            Field setter = null;
            for (Field f : statClass.getDeclaredFields()) {
                if (f.getType() == Function.class) {
                    f.setAccessible(true);
                    getter = f;
                } else if (f.getType() == BiConsumer.class) {
                    f.setAccessible(true);
                    setter = f;
                }
            }
            yzzzFix$getterField = getter;
            yzzzFix$setterField = setter;
        }
    }
}
