package me.realseek.yzzzfix.module.eidolon_jei;

import me.realseek.yzzzfix.YzzzFix;
import me.realseek.yzzzfix.module.ModuleRuntimeHooks;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.world.inventory.MenuType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Eidolon JEI 修复模块的运行时钩子实现。
 *
 * <p>通过反射注册 Eidolon Worktable 的 JEI 配方转移处理器，
 * 使玩家可以在 JEI 界面中直接将配方材料转移到工作台。</p>
 */
public final class EidolonJeiModule implements ModuleRuntimeHooks {

    public static final EidolonJeiModule INSTANCE = new EidolonJeiModule();

    private EidolonJeiModule() {
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        try {
            Class<?> worktableContainerClass = Class.forName("elucent.eidolon.gui.WorktableContainer");
            MenuType<?> worktableMenuType = (MenuType<?>) invokeRegistryGet(
                    "elucent.eidolon.registries.Registry",
                    "WORKTABLE_CONTAINER"
            );
            RecipeType<?> worktableRecipeType = (RecipeType<?>) readStaticField(
                    "elucent.eidolon.gui.jei.JEIRegistry",
                    "WORKTABLE_CATEGORY"
            );

            registration.addRecipeTransferHandler(
                    (Class) worktableContainerClass,
                    worktableMenuType,
                    worktableRecipeType,
                    1,
                    13,
                    14,
                    36
            );
            YzzzFix.LOGGER.info("Registered Eidolon worktable JEI transfer handler.");
        } catch (Exception exception) {
            YzzzFix.LOGGER.error("Failed to register Eidolon JEI transfer handler.", exception);
        }
    }

    private static Object invokeRegistryGet(String ownerClassName, String fieldName) throws Exception {
        Object registryObject = readStaticField(ownerClassName, fieldName);
        Method getMethod = registryObject.getClass().getMethod("get");
        return getMethod.invoke(registryObject);
    }

    private static Object readStaticField(String ownerClassName, String fieldName) throws Exception {
        Class<?> ownerClass = Class.forName(ownerClassName);
        Field field = ownerClass.getField(fieldName);
        return field.get(null);
    }
}
