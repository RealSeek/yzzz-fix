package me.realseek.yzzzfix.module.enigmaticaddons;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 修复精灵手环蓄力加速效果导致魔法石英权杖无法正常发射匕首的问题。
 */
public final class CelestialArtifactsCompat {

    private static final ResourceLocation SPIRIT_BRACELET = new ResourceLocation("celestial_artifacts", "spirit_bracelet");
    private static final ConcurrentMap<ResourceLocation, Item> ITEM_CACHE = new ConcurrentHashMap<>();

    private CelestialArtifactsCompat() {
    }

    public static boolean hasSpiritBracelet(LivingEntity entity) {
        Item item = getCelestialItem(SPIRIT_BRACELET);
        return item != Items.AIR && SuperpositionHandler.hasCurio(entity, item);
    }

    private static Item getCelestialItem(ResourceLocation itemId) {
        return ITEM_CACHE.computeIfAbsent(itemId, CelestialArtifactsCompat::resolveCelestialItem);
    }

    private static Item resolveCelestialItem(ResourceLocation itemId) {
        Item registryItem = ForgeRegistries.ITEMS.getValue(itemId);
        if (registryItem != null && registryItem != Items.AIR) {
            return registryItem;
        }

        try {
            Class<?> caItemsClass = Class.forName("com.xiaoyue.celestial_artifacts.register.CAItems");
            Field field = caItemsClass.getField("SPIRIT_BRACELET");
            Object value = field.get(null);
            Method getMethod = value.getClass().getMethod("get");
            Object item = getMethod.invoke(value);
            if (item instanceof Item resolvedItem) {
                return resolvedItem;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }

        return Items.AIR;
    }
}
