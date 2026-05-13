package me.realseek.yzzzfix.module.forbidden_arcanus;

import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.Ritual;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.CreateItemResult;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.UpgradeTierResult;
import com.stal111.forbidden_arcanus.common.integration.ApplyModifierRecipeMaker;
import com.stal111.forbidden_arcanus.common.integration.ForbiddenArcanusJEIPlugin;
import com.stal111.forbidden_arcanus.core.init.ModRecipeTypes;
import me.realseek.yzzzfix.YzzzFix;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.List;

/**
 * Forbidden Arcanus 仪式配方的动态 JEI 注册器。
 *
 * <p>在仪式数据从服务端同步到客户端且 JEI 运行时可用后，将仪式配方按类型
 * （锻造/升级/Clibano/修饰符）分别注册到 JEI 配方管理器中。</p>
 */
public final class ForbiddenArcanusRecipeRegistrar {

    private ForbiddenArcanusRecipeRegistrar() {
    }

    public static synchronized void tryRegister() {
        if (ForbiddenArcanusClientRitualCache.isRecipesRegistered()) {
            return;
        }

        IJeiRuntime runtime = ForbiddenArcanusJeiRuntimeHolder.getRuntime();
        if (runtime == null) {
            return;
        }

        if (!ForbiddenArcanusClientRitualCache.hasRituals()) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        IRecipeManager recipeManager = runtime.getRecipeManager();
        List<Ritual> rituals = ForbiddenArcanusClientRitualCache.getRituals();
        if (rituals == null) {
            return;
        }

        List<Ritual> smithing = rituals.stream()
                .filter(ritual -> ritual.result() instanceof CreateItemResult)
                .toList();
        List<Ritual> upgrading = rituals.stream()
                .filter(ritual -> ritual.result() instanceof UpgradeTierResult)
                .toList();

        recipeManager.addRecipes(ForbiddenArcanusJEIPlugin.HEPHAESTUS_SMITHING, smithing);
        recipeManager.addRecipes(ForbiddenArcanusJEIPlugin.HEPHAESTUS_FORGE_UPGRADING, upgrading);

        try {
            var modifierRecipes = ApplyModifierRecipeMaker.getRecipes();
            if (!modifierRecipes.isEmpty()) {
                recipeManager.addRecipes(RecipeTypes.SMITHING, modifierRecipes);
            }
        } catch (Exception exception) {
            YzzzFix.LOGGER.error("Failed to register Forbidden Arcanus ApplyModifier recipes.", exception);
        }

        try {
            var clibanoRecipes = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CLIBANO_COMBUSTION.get());
            if (!clibanoRecipes.isEmpty()) {
                recipeManager.addRecipes(ForbiddenArcanusJEIPlugin.CLIBANO_COMBUSTION, clibanoRecipes);
            }
        } catch (Exception exception) {
            YzzzFix.LOGGER.error("Failed to register Forbidden Arcanus Clibano recipes.", exception);
        }

        ForbiddenArcanusClientRitualCache.markRegistered();
        YzzzFix.LOGGER.info(
                "Registered Forbidden Arcanus rituals into JEI: total={}, smithing={}, upgrading={}.",
                rituals.size(),
                smithing.size(),
                upgrading.size()
        );
    }
}
