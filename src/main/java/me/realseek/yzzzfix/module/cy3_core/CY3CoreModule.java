package me.realseek.yzzzfix.module.cy3_core;

import me.realseek.yzzzfix.YzzzFix;
import me.realseek.yzzzfix.module.ModuleRuntimeHooks;
import net.minecraftforge.common.MinecraftForge;

/**
 * CY3 Core 修复模块的运行时钩子实现。
 *
 * <p>负责注册网络通道并初始化 LifeLimiter 生命值同步修复。
 * 当目标模组的 {@code LifeLimiter.HPS} 字段可用时，注册事件监听器进行状态清理。</p>
 */
public final class CY3CoreModule implements ModuleRuntimeHooks {

    public static final CY3CoreModule INSTANCE = new CY3CoreModule();

    private CY3CoreModule() {
    }

    @Override
    public void initCommon() {
        CY3CoreNetworkHandler.register();

        CY3CoreLifeLimiterFix fix = new CY3CoreLifeLimiterFix();
        if (fix.isAvailable()) {
            MinecraftForge.EVENT_BUS.register(fix);
            YzzzFix.LOGGER.info("CY3 Core module initialized with LifeLimiter fixes active.");
        } else {
            YzzzFix.LOGGER.warn("CY3 Core module initialized, but LifeLimiter.HPS was not found.");
        }
    }

    @Override
    public void initClient() {
    }
}
