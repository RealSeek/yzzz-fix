package me.realseek.yzzzfix.module.enigmaticaddons;

import me.realseek.yzzzfix.module.ModuleRuntimeHooks;
import net.minecraftforge.common.MinecraftForge;

/**
 * Enigmatic Addons 修复模块的运行时钩子。
 * <p>负责注册古旧书袋能力数据持久化的事件监听器。</p>
 */
public final class EnigmaticAddonsModule implements ModuleRuntimeHooks {

    public static final EnigmaticAddonsModule INSTANCE = new EnigmaticAddonsModule();

    private EnigmaticAddonsModule() {
    }

    @Override
    public void initCommon() {
        MinecraftForge.EVENT_BUS.register(AntiqueBagFix.class);
    }
}
