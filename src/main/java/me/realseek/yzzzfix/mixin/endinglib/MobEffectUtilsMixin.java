package me.realseek.yzzzfix.mixin.endinglib;

import com.mega.endinglib.util.entity.MobEffectUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 修复 EndingLib 的 {@code MobEffectUtils.forceAdd} 在操作不可变 activeEffects 映射时产生异常的问题。
 *
 * <p>当实体的药水效果映射为不可变实现时，{@code Map.put} 会抛出 {@code UnsupportedOperationException}。
 * 该 Mixin 捕获此异常，创建可变副本并通过反射替换原始字段。</p>
 */
@Mixin(value = MobEffectUtils.class, remap = false)
public class MobEffectUtilsMixin {

    @Unique private static final Logger yzzzfix$LOGGER = LogManager.getLogger("YzzzFix/EndingLibFix");
    @Unique private static volatile Field yzzzfix$activeEffectsField;

    @Redirect(method = "forceAdd",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object yzzzfix$safePut(Map<Object, Object> map, Object key, Object value,
                                           LivingEntity entity, MobEffectInstance effectInstance, Entity source) {
        try {
            return map.put(key, value);
        } catch (UnsupportedOperationException e) {
            yzzzfix$LOGGER.debug("Detected immutable activeEffects map on {}, replacing with mutable copy", entity);
            HashMap<MobEffect, MobEffectInstance> mutableMap = new HashMap<>((Map<MobEffect, MobEffectInstance>) (Map<?, ?>) map);
            mutableMap.put((MobEffect) key, (MobEffectInstance) value);
            yzzzfix$setActiveEffects(entity, mutableMap);
            return null;
        }
    }

    @Unique
    private static void yzzzfix$setActiveEffects(LivingEntity entity, Map<MobEffect, MobEffectInstance> map) {
        try {
            if (yzzzfix$activeEffectsField == null) {
                Field field = null;
                for (String name : new String[]{"f_20889_", "activeEffects"}) {
                    try { field = LivingEntity.class.getDeclaredField(name); break; }
                    catch (NoSuchFieldException ignored) {}
                }
                if (field == null) {
                    for (Field f : LivingEntity.class.getDeclaredFields()) {
                        if (f.getType() == Map.class) {
                            f.setAccessible(true);
                            Object val = f.get(entity);
                            if (val instanceof Map<?, ?> m && !m.isEmpty()) {
                                Map.Entry<?, ?> entry = m.entrySet().iterator().next();
                                if (entry.getKey() instanceof MobEffect) { field = f; break; }
                            }
                        }
                    }
                }
                if (field == null) throw new RuntimeException("Cannot find activeEffects field");
                field.setAccessible(true);
                yzzzfix$activeEffectsField = field;
            }
            yzzzfix$activeEffectsField.set(entity, map);
        } catch (Exception ex) {
            yzzzfix$LOGGER.error("Failed to replace activeEffects map", ex);
        }
    }
}
