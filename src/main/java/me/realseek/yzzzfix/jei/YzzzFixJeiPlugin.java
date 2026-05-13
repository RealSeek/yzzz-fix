package me.realseek.yzzzfix.jei;

import me.realseek.yzzzfix.YzzzFix;
import me.realseek.yzzzfix.module.ModuleRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Yzzz Fix 的 JEI 集成入口。
 *
 * <p>该类在 JEI 生命周期中接收运行时对象，并将配方转移处理器注册等事件分发给各个启用的修复模块。
 * 这样可以在保持模块化结构的同时，集中管理与 JEI 相关的兼容逻辑。</p>
 */
@JeiPlugin
public final class YzzzFixJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(YzzzFix.MOD_ID, "main");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        ModuleRegistry.onJeiRuntimeAvailable(jeiRuntime);
    }

    @Override
    public void onRuntimeUnavailable() {
        ModuleRegistry.onJeiRuntimeUnavailable();
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        ModuleRegistry.registerRecipeTransferHandlers(registration);
    }
}
