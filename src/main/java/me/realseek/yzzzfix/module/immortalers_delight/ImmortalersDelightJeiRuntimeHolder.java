package me.realseek.yzzzfix.module.immortalers_delight;

import mezz.jei.api.runtime.IJeiRuntime;
import org.jetbrains.annotations.Nullable;

/**
 * Immortalers Delight 模块的 JEI 运行时引用持有器。
 *
 * <p>以 volatile 方式存储当前 JEI 运行时实例，供配方注册器在需要时安全访问。</p>
 */
public final class ImmortalersDelightJeiRuntimeHolder {

    private static volatile IJeiRuntime runtime;

    private ImmortalersDelightJeiRuntimeHolder() {
    }

    public static void setRuntime(@Nullable IJeiRuntime runtime) {
        ImmortalersDelightJeiRuntimeHolder.runtime = runtime;
    }

    public static @Nullable IJeiRuntime getRuntime() {
        return runtime;
    }
}
