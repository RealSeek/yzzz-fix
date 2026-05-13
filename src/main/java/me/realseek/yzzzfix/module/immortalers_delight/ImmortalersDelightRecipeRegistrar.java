package me.realseek.yzzzfix.module.immortalers_delight;

import com.renyigesai.immortalers_delight.recipe.EnchantalCoolerRecipe;
import com.renyigesai.immortalers_delight.recipe.JEIImmortalersDelightPlugin;
import me.realseek.yzzzfix.YzzzFix;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.List;

/**
 * Immortalers Delight Enchantal Cooler 配方的动态 JEI 注册器。
 *
 * <p>在 JEI 运行时可用且客户端世界已加载后，从配方管理器获取 Enchantal Cooler 配方并注册到 JEI。
 * 注册状态在玩家登出时重置，以便重新进入世界时能再次注册。</p>
 */
public final class ImmortalersDelightRecipeRegistrar {

    private static volatile boolean registered;

    private ImmortalersDelightRecipeRegistrar() {
    }

    public static synchronized void tryRegister() {
        if (registered) {
            return;
        }

        IJeiRuntime runtime = ImmortalersDelightJeiRuntimeHolder.getRuntime();
        if (runtime == null) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        IRecipeManager recipeManager = runtime.getRecipeManager();
        try {
            List<EnchantalCoolerRecipe> recipes = level.getRecipeManager()
                    .getAllRecipesFor(EnchantalCoolerRecipe.Type.INSTANCE);
            recipeManager.addRecipes(JEIImmortalersDelightPlugin.ENCHANTAL_COOLER_TYPE, recipes);
            registered = true;
            YzzzFix.LOGGER.info("Registered {} Immortalers Delight Enchantal Cooler recipes in JEI.", recipes.size());
        } catch (Exception exception) {
            YzzzFix.LOGGER.error("Failed to register Immortalers Delight recipes into JEI.", exception);
        }
    }

    public static void reset() {
        registered = false;
    }
}
