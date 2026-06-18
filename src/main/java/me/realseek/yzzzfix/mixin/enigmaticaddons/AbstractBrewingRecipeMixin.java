package me.realseek.yzzzfix.mixin.enigmaticaddons;

import com.aizistral.enigmaticlegacy.brewing.AbstractBrewingRecipe;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 修复 AbstractBrewingRecipe 构造方法中 HashMap 键存在但值为 null 导致的 NPE。
 */
@Mixin(value = AbstractBrewingRecipe.class, remap = false)
public abstract class AbstractBrewingRecipeMixin {

    @Unique
    private static final Logger yzzzfix$LOG = LogManager.getLogger("YzzzFix:EnigmaticAddons");

    @SuppressWarnings("unchecked")
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/HashMap;get(Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            )
    )
    private Object yzzzfix$fixNullList(HashMap<Object, Object> map, Object key) {
        Object result = map.get(key);
        if (key instanceof ResourceLocation id && result == null && map.containsKey(id)) {
            yzzzfix$LOG.error("Corrupted recipeMap entry: {}", key,
                    new IllegalStateException("recipeMap contains null value"));
            List<AbstractBrewingRecipe> list = new ArrayList<>();
            map.put(id, list);
            return list;
        }
        return result;
    }
}
