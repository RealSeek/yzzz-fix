package me.realseek.yzzzfix.module.immortalers_delight;

import me.realseek.yzzzfix.YzzzFix;
import me.realseek.yzzzfix.module.ModuleRuntimeHooks;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;

/**
 * Immortalers Delight 修复模块的运行时钩子实现。
 *
 * <p>负责注册客户端事件处理器，以及在 JEI 运行时可用时触发 Enchantal Cooler 配方的动态注册。</p>
 */
public final class ImmortalersDelightModule implements ModuleRuntimeHooks {

    public static final ImmortalersDelightModule INSTANCE = new ImmortalersDelightModule();

    private ImmortalersDelightModule() {
    }

    @Override
    public void initCommon() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> MinecraftForge.EVENT_BUS.register(ImmortalersDelightClientEventHandler.class));
        YzzzFix.LOGGER.info("Immortalers Delight module initialized.");
    }

    @Override
    public void onJeiRuntimeAvailable(IJeiRuntime jeiRuntime) {
        ImmortalersDelightJeiRuntimeHolder.setRuntime(jeiRuntime);
        ImmortalersDelightRecipeRegistrar.tryRegister();
    }

    @Override
    public void onJeiRuntimeUnavailable() {
        ImmortalersDelightJeiRuntimeHolder.setRuntime(null);
        ImmortalersDelightRecipeRegistrar.reset();
    }
}
