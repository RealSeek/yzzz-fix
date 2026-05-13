package me.realseek.yzzzfix.mixin;

import me.realseek.yzzzfix.YzzzFixConfig;
import me.realseek.yzzzfix.module.ModuleDefinition;
import me.realseek.yzzzfix.module.ModuleRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Yzzz Fix 的 Mixin 配置插件。
 *
 * <p>该插件在 Mixin 应用前根据模块配置、依赖模组和依赖类是否存在来动态决定
 * 某个 Mixin 是否应当应用，从而避免在目标模组缺失或版本不兼容时产生类加载错误。</p>
 *
 * <p>插件还会记录每个模块中 Mixin 的应用与跳过情况，便于在日志中诊断当前环境下的修复启用状态。</p>
 */
public final class YzzzFixMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("YzzzFixMixinPlugin");

    // Per-module mixin application tracking: module id -> [appliedList, skippedList]
    private static final Map<String, MixinStats> moduleStats = new LinkedHashMap<>();

    @Override
    public void onLoad(String mixinPackage) {
        YzzzFixConfig.ensureLoaded();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        ModuleDefinition module = ModuleRegistry.findByMixinClassName(mixinClassName).orElse(null);
        if (module == null) {
            return true;
        }

        String shortName = shortMixinName(mixinClassName);

        if (!YzzzFixConfig.isEnabled(module.configKey())) {
            LOGGER.debug("Skipping mixin {} because module {} is disabled in config.", mixinClassName, module.id());
            recordSkipped(module.id(), shortName, "disabled in config");
            return false;
        }

        if (!module.isRuntimeAvailable()) {
            LOGGER.debug("Skipping mixin {} because required mod for module {} is missing.", mixinClassName, module.id());
            recordSkipped(module.id(), shortName, "missing required mod");
            return false;
        }

        for (String requiredClassName : module.requiredClassNames()) {
            if (!classExists(requiredClassName)) {
                LOGGER.debug("Skipping mixin {} because required class {} is missing.", mixinClassName, requiredClassName);
                recordSkipped(module.id(), shortName, "missing dependency: " + requiredClassName);
                return false;
            }
        }

        // Target class existence is NOT checked here — with defaultRequire=0 in the
        // mixin config, Mixin itself gracefully skips unresolvable targets. Checking
        // via MixinService bytecodeProvider this early can give false negatives for
        // fat/shadow jars and non-standard mod packaging.

        recordApplied(module.id(), shortName);
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    // ── Mixin stats tracking ──

    private static synchronized void recordApplied(String moduleId, String mixinName) {
        moduleStats.computeIfAbsent(moduleId, k -> new MixinStats()).applied.add(mixinName);
    }

    private static synchronized void recordSkipped(String moduleId, String mixinName, String reason) {
        moduleStats.computeIfAbsent(moduleId, k -> new MixinStats()).skipped.add(mixinName + " (" + reason + ")");
    }

    private static String shortMixinName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        return lastDot >= 0 ? fullClassName.substring(lastDot + 1) : fullClassName;
    }

    /**
     * Logs a summary of mixin application results per module.
     * Call this after mixin loading is complete (e.g. during mod construction).
     */
    public static void logSummary() {
        if (moduleStats.isEmpty()) {
            return;
        }

        int totalApplied = 0;
        int totalSkipped = 0;

        LOGGER.info("========== Mixin Application Summary ==========");
        for (Map.Entry<String, MixinStats> entry : moduleStats.entrySet()) {
            MixinStats stats = entry.getValue();
            int applied = stats.applied.size();
            int skipped = stats.skipped.size();
            totalApplied += applied;
            totalSkipped += skipped;

            if (skipped == 0) {
                LOGGER.info("  [{}] {} applied, 0 skipped", entry.getKey(), applied);
            } else if (applied == 0) {
                LOGGER.warn("  [{}] 0 applied, {} skipped (all inactive)", entry.getKey(), skipped);
            } else {
                LOGGER.warn("  [{}] {} applied, {} skipped", entry.getKey(), applied, skipped);
            }
            for (String detail : stats.skipped) {
                LOGGER.warn("    - SKIPPED: {}", detail);
            }
        }
        LOGGER.info("  Total: {} applied, {} skipped", totalApplied, totalSkipped);
        LOGGER.info("================================================");
    }

    private static final class MixinStats {
        final List<String> applied = new ArrayList<>();
        final List<String> skipped = new ArrayList<>();
    }

    private static boolean classExists(String className) {
        try {
            MixinService.getService().getBytecodeProvider().getClassNode(className.replace('.', '/'));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
