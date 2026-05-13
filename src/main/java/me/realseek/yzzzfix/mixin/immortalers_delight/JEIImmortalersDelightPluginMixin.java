package me.realseek.yzzzfix.mixin.immortalers_delight;

import com.renyigesai.immortalers_delight.recipe.JEIImmortalersDelightPlugin;
import me.realseek.yzzzfix.YzzzFix;
import mezz.jei.api.registration.IRecipeRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 取消 Immortalers Delight 原始的 JEI 配方注册逻辑。
 *
 * <p>原始注册可能在配方数据尚未就绪时执行。该 Mixin 取消原始注册，
 * 改由 {@code ImmortalersDelightRecipeRegistrar} 在数据就绪后动态注册。</p>
 */
@Mixin(JEIImmortalersDelightPlugin.class)
public abstract class JEIImmortalersDelightPluginMixin {

    @Inject(method = "registerRecipes", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzFix$cancelRegisterRecipes(IRecipeRegistration registration, CallbackInfo ci) {
        ci.cancel();
        YzzzFix.LOGGER.debug("Cancelled original Immortalers Delight JEI registration; recipes will be registered dynamically.");
    }
}
