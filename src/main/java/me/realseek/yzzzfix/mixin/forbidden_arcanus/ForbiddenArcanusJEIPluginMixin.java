package me.realseek.yzzzfix.mixin.forbidden_arcanus;

import me.realseek.yzzzfix.YzzzFix;
import com.stal111.forbidden_arcanus.common.integration.ForbiddenArcanusJEIPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 取消 Forbidden Arcanus 原始的 JEI 配方注册逻辑。
 *
 * <p>原始注册在 JEI 加载时执行，此时仪式数据可能尚未从服务端同步到客户端。
 * 该 Mixin 取消原始注册，改由 {@code ForbiddenArcanusRecipeRegistrar} 在数据就绪后动态注册。</p>
 */
@Mixin(ForbiddenArcanusJEIPlugin.class)
public abstract class ForbiddenArcanusJEIPluginMixin {

    @Inject(method = "registerRecipes", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzFix$cancelOriginalRegistration(IRecipeRegistration registration, CallbackInfo ci) {
        ci.cancel();
        YzzzFix.LOGGER.debug("Cancelled original Forbidden Arcanus JEI registration; recipes will be synced dynamically.");
    }
}
