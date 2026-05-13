package me.realseek.yzzzfix.module.forbidden_arcanus;

import me.realseek.yzzzfix.YzzzFix;
import me.realseek.yzzzfix.module.ModuleRuntimeHooks;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;

/**
 * Forbidden Arcanus 修复模块的运行时钩子实现。
 *
 * <p>负责注册网络通道、common/client 事件处理器，以及在 JEI 运行时可用时触发仪式配方的动态注册。</p>
 */
public final class ForbiddenArcanusModule implements ModuleRuntimeHooks {

    public static final ForbiddenArcanusModule INSTANCE = new ForbiddenArcanusModule();

    private ForbiddenArcanusModule() {
    }

    @Override
    public void initCommon() {
        ForbiddenArcanusNetworkHandler.register();
        MinecraftForge.EVENT_BUS.register(new ForbiddenArcanusCommonEvents());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> MinecraftForge.EVENT_BUS.register(ForbiddenArcanusClientEventHandler.class));
        YzzzFix.LOGGER.info("Forbidden Arcanus module initialized.");
    }

    @Override
    public void onJeiRuntimeAvailable(IJeiRuntime jeiRuntime) {
        ForbiddenArcanusJeiRuntimeHolder.setRuntime(jeiRuntime);
        ForbiddenArcanusRecipeRegistrar.tryRegister();
    }

    @Override
    public void onJeiRuntimeUnavailable() {
        ForbiddenArcanusJeiRuntimeHolder.setRuntime(null);
        ForbiddenArcanusClientRitualCache.markUnregistered();
    }
}
