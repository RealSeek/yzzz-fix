package me.realseek.yzzzfix.module;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;

import java.util.List;

/**
 * 描述一个可独立启用的修复模块。
 *
 * <p>模块定义包含模块标识、显示名称、配置键、依赖模组、依赖类名、
 * 该模块拥有的 Mixin 包前缀以及运行时生命周期钩子。</p>
 *
 * <p>Mixin 插件会根据 {@link #mixinPackagePrefixes()} 判断某个 Mixin 是否属于该模块，
 * 并结合配置与运行环境决定是否应用对应 Mixin。</p>
 */
public record ModuleDefinition(
        String id,
        String displayName,
        String configKey,
        List<String> requiredModIds,
        List<String> requiredClassNames,
        List<String> mixinPackagePrefixes,
        ModuleRuntimeHooks runtimeHooks
) {

    public ModuleDefinition {
        requiredModIds = List.copyOf(requiredModIds);
        requiredClassNames = List.copyOf(requiredClassNames);
        mixinPackagePrefixes = List.copyOf(mixinPackagePrefixes);
        runtimeHooks = runtimeHooks == null ? ModuleRuntimeHooks.NOOP : runtimeHooks;
    }

    public boolean matchesMixin(String mixinClassName) {
        for (String prefix : mixinPackagePrefixes) {
            if (mixinClassName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRuntimeAvailable() {
        ModList modList = ModList.get();
        if (modList != null) {
            for (String modId : requiredModIds) {
                if (!modList.isLoaded(modId)) {
                    return false;
                }
            }
            return true;
        }
        LoadingModList loadingModList = LoadingModList.get();
        if (loadingModList != null) {
            for (String modId : requiredModIds) {
                if (loadingModList.getModFileById(modId) == null) {
                    return false;
                }
            }
            return true;
        }
        return !requiredModIds.isEmpty() ? false : true;
    }
}
