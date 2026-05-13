package me.realseek.yzzzfix;

import me.realseek.yzzzfix.mixin.YzzzFixMixinPlugin;
import me.realseek.yzzzfix.module.ModuleRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Yzzz Fix 的 Forge 模组入口类。
 *
 * <p>该类负责在模组构造阶段完成全局初始化工作，包括客户端/服务端显示兼容性声明、
 * JSON 配置加载、Mixin 应用摘要输出以及各修复模块的 common/client 生命周期初始化。</p>
 *
 * <p>本模组定位为模块化 bug 修复集合，每个修复项由 {@link ModuleRegistry}
 * 统一注册，并可通过配置文件单独启用或禁用。</p>
 */
@Mod(YzzzFix.MOD_ID)
public final class YzzzFix {

    public static final String MOD_ID = "yzzz_fix";
    public static final String MOD_NAME = "Yzzz Fix";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public YzzzFix() {
        registerDisplayTest();
        YzzzFixConfig.ensureLoaded();
        YzzzFixMixinPlugin.logSummary();
        ModuleRegistry.initCommon();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ModuleRegistry::initClient);

        LOGGER.info("{} initialized with {}/{} modules enabled by config.",
                MOD_NAME,
                ModuleRegistry.countEnabledByConfig(),
                ModuleRegistry.modules().size());
    }

    @SuppressWarnings("removal")
    private static void registerDisplayTest() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> NetworkConstants.IGNORESERVERONLY,
                        (remoteVersion, isServer) -> true
                ));
    }
}
