package me.realseek.yzzzfix.mixin.apotheosis_spawner;

import com.bobmowzie.mowziesmobs.server.entity.MowzieEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.lang.reflect.Field;

/**
 * 修复 Mowzie's Mobs 与神化（Apotheosis）刷怪笼的兼容性崩溃。
 *
 * <p>原始实现中，{@code MowzieEntity.spawnPredicate} 方法会强行将传入的 {@code LevelAccessor}
 * 转换为 {@code ServerLevel}。当神化刷怪笼进行模拟生成测试时，会传入虚构的 {@code LyingLevel}，
 * 从而导致 {@link ClassCastException} 并引起服务器崩溃。</p>
 *
 * <p>本 Mixin 拦截方法参数。当检测到传入的世界是 {@code LyingLevel} 时，
 * 采用反射（结合 volatile 和双重检查锁缓存 Field）提取其内部包裹的真实 {@code LevelAccessor}，
 * 并将其替换给原方法，从而保证强转成功，同时完美保留神化刷怪笼的强化属性。</p>
 */
@SuppressWarnings({"UnresolvedMixinReference", "unchecked"})
@Mixin(targets = "com.bobmowzie.mowziesmobs.server.entity.MowzieEntity", remap = false)
public class MowzieEntityMixin {

    @Unique
    private static final Logger pathfinderplus$LOGGER = LogManager.getLogger("PathfinderPlusFix");

    @Unique
    private static final Object pathfinderplus$FIELD_INIT_LOCK = new Object();

    @Unique
    private static volatile Field pathfinderplus$wrappedLevelField;

    @ModifyVariable(
            method = "spawnPredicate",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false // 目标是第三方 Mod 的方法，必须设为 false 防止混淆器报错
    )
    private static LevelAccessor pathfinderplus$fixLyingLevelCast(LevelAccessor level) {
        if (level == null) {
            return null;
        }

        // 仅当传入的世界是神化的模拟世界时才进行介入 (同样避免直接引用 LyingLevel.class)
        if (level.getClass().getName().endsWith("LyingLevel")) {
            try {
                pathfinderplus$initFieldsIfNeeded(level.getClass());

                Field field = pathfinderplus$wrappedLevelField;
                if (field != null) {
                    Object realLevel = field.get(level);

                    // 只要挖出来的真实世界是 ServerLevel，就替换过去
                    if (realLevel instanceof ServerLevel) {
                        return (ServerLevel) realLevel;
                    }
                }
            } catch (Exception e) {
                pathfinderplus$LOGGER.warn("[PathfinderPlus] Apotheosis LyingLevel reflection failed, falling back to original logic.", e);
            }
        }

        return level; // 正常情况，原样放行
    }

    @Unique
    private static void pathfinderplus$initFieldsIfNeeded(Class<?> lyingLevelClass) {
        if (pathfinderplus$wrappedLevelField != null) {
            return;
        }
        synchronized (pathfinderplus$FIELD_INIT_LOCK) {
            if (pathfinderplus$wrappedLevelField != null) {
                return;
            }
            try {
                Field targetField = null;
                // 动态遍历寻找类型为 LevelAccessor（包裹真实世界）的字段
                for (Field f : lyingLevelClass.getDeclaredFields()) {
                    if (LevelAccessor.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        targetField = f;
                        break;
                    }
                }
                pathfinderplus$wrappedLevelField = targetField;
            } catch (Exception e) {
                pathfinderplus$LOGGER.warn("[PathfinderPlus] Failed to initialize LyingLevel reflection field.", e);
            }
        }
    }
}