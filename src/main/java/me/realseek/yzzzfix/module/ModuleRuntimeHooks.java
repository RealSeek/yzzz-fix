package me.realseek.yzzzfix.module;

import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

/**
 * 修复模块的运行时生命周期钩子接口。
 *
 * <p>模块可按需实现 common 初始化、client 初始化、JEI 运行时回调以及配方转移处理器注册等逻辑。
 * 未实现的方法默认为空操作，便于仅包含 Mixin 修复的模块复用同一套模块注册机制。</p>
 */
public interface ModuleRuntimeHooks {

    ModuleRuntimeHooks NOOP = new ModuleRuntimeHooks() {
    };

    default void initCommon() {
    }

    default void initClient() {
    }

    default void onJeiRuntimeAvailable(IJeiRuntime jeiRuntime) {
    }

    default void onJeiRuntimeUnavailable() {
    }

    default void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    }
}
