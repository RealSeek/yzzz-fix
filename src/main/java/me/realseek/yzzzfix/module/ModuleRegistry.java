package me.realseek.yzzzfix.module;

import me.realseek.yzzzfix.YzzzFix;
import me.realseek.yzzzfix.YzzzFixConfig;
import me.realseek.yzzzfix.module.eidolon_jei.EidolonJeiModule;
import me.realseek.yzzzfix.module.forbidden_arcanus.ForbiddenArcanusModule;
import me.realseek.yzzzfix.module.immortalers_delight.ImmortalersDelightModule;
import me.realseek.yzzzfix.module.cy3_core.CY3CoreModule;
import me.realseek.yzzzfix.module.lychee_offhand.LycheeOffhandModule;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * Yzzz Fix 的修复模块注册表。
 *
 * <p>该类集中声明所有可用修复模块，并提供模块查询、默认配置生成、
 * 生命周期分发以及 JEI 集成分发能力。</p>
 *
 * <p>每个模块的启用状态由配置文件控制，同时还会根据所需模组是否加载来决定是否执行运行时钩子。</p>
 */
public final class ModuleRegistry {

    private static final String MIXIN_ROOT = "me.realseek.yzzzfix.mixin.";
    private static final List<ModuleDefinition> MODULES = List.of(
            module(
                    "forbidden_arcanus",
                    "Forbidden Arcanus Fix",
                    "forbidden_arcanus_fix",
                    List.of("forbidden_arcanus"),
                    List.of(
                            "com.stal111.forbidden_arcanus.common.block.HephaestusForgeBlock",
                            "com.stal111.forbidden_arcanus.common.integration.ForbiddenArcanusJEIPlugin"
                    ),
                    ForbiddenArcanusModule.INSTANCE
            ),
            module(
                    "immortalers_delight",
                    "Immortalers Delight Fix",
                    "immortalers_delight_fix",
                    List.of("immortalers_delight"),
                    List.of(
                            "com.renyigesai.immortalers_delight.recipe.EnchantalCoolerRecipe$Serializer",
                            "com.renyigesai.immortalers_delight.recipe.JEIImmortalersDelightPlugin"
                    ),
                    ImmortalersDelightModule.INSTANCE
            ),
            module("irons_spellbooks", "Iron's Spellbooks Fix", "irons_spellbooks_fix", List.of("irons_spellbooks")),
            module("revelationfix", "RevelationFix Fix", "revelationfix_fix", List.of("revelationfix")),
            module("mokels", "Mokels Fix", "mokels_fix", List.of()),
            module(
                    "cy3_core",
                    "CY3 Core Fix",
                    "cy3_core_fix",
                    List.of("chapter_of_yuusha_3_core"),
                    List.of(
                            "org.heike233.chapterofyuusha3.comm.compat.curios.item.LifeLimiter",
                            "com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid",
                            "org.heike233.chapterofyuusha3.api.mixin.touhoulittlemaid.MaidAbilityHelper"
                    ),
                    CY3CoreModule.INSTANCE
            ),
            module("aetherworks", "Aetherworks Fix", "aetherworks_fix", List.of()),
            module("apotheosis_gateway", "Apotheosis Gateway Fix", "apotheosis_gateway_fix", List.of("apotheosis")),
            module("apotheosis_spawner", "Apothic Spawners Fix", "apotheosis_spawner_fix", List.of("apotheosis")),
            module("reliquary_lyssa", "Reliquary Lyssa Fix", "reliquary_lyssa_fix", List.of("reliquary")),
            module("eidolon_jei", "Eidolon JEI Fix", "eidolon_jei_fix", List.of("eidolon"), List.of(), EidolonJeiModule.INSTANCE),
            module("null_blockstate", "Null BlockState Fix", "null_blockstate_fix", List.of(), List.of(), ModuleRuntimeHooks.NOOP),
            module("ritual_manager_npe", "Ritual Manager NPE Fix", "ritual_manager_npe_fix", List.of("forbidden_arcanus")),
            module("rough_blade", "Rough Blade Fix", "rough_blade_fix", List.of("rough_blade")),
            module("illager_additions", "Illager Additions Fix", "illager_additions_fix", List.of("illager_additions")),
            module("enigmatic_totem", "Enigmatic Totem Fix", "enigmatic_totem_fix", List.of("enigmaticlegacy")),
            module("yhc", "YHC Fix", "yhc_fix", List.of()),
            module("celestial_ench", "Celestial Ench Fix", "celestial_ench_fix", List.of()),
            module("endinglib", "EndingLib Fix", "endinglib_fix", List.of("endinglib")),
            module("eidolon_hearts", "Eidolon Hearts Fix", "eidolon_hearts_fix", List.of("eidolon")),
            module(
                    "celestial_forge",
                    "Celestial Forge Fix",
                    "celestial_forge_fix",
                    List.of(),
                    List.of("com.xiaoyue.celestial_forge.utils.ModifierUtils"),
                    ModuleRuntimeHooks.NOOP
            ),
            module(
                    "lychee_offhand",
                    "Lychee Offhand Fix",
                    "lychee_offhand_fix",
                    List.of("lychee"),
                    List.of("snownee.lychee.interaction.InteractionRecipeMod"),
                    LycheeOffhandModule.INSTANCE
            )
    );

    private ModuleRegistry() {
    }

    public static List<ModuleDefinition> modules() {
        return MODULES;
    }

    public static LinkedHashMap<String, Boolean> defaultConfigValues() {
        LinkedHashMap<String, Boolean> defaults = new LinkedHashMap<>();
        for (ModuleDefinition module : MODULES) {
            defaults.put(module.configKey(), true);
        }
        return defaults;
    }

    public static Optional<ModuleDefinition> findByMixinClassName(String mixinClassName) {
        for (ModuleDefinition module : MODULES) {
            if (module.matchesMixin(mixinClassName)) {
                return Optional.of(module);
            }
        }
        return Optional.empty();
    }

    public static long countEnabledByConfig() {
        return MODULES.stream()
                .filter(module -> YzzzFixConfig.isEnabled(module.configKey()))
                .count();
    }

    public static void initCommon() {
        for (ModuleDefinition module : MODULES) {
            if (!YzzzFixConfig.isEnabled(module.configKey())) {
                YzzzFix.LOGGER.info("Module {} disabled by config.", module.id());
                continue;
            }
            if (!module.isRuntimeAvailable()) {
                YzzzFix.LOGGER.debug("Module {} runtime requirements not satisfied, skipping for now.", module.id());
                continue;
            }
            module.runtimeHooks().initCommon();
        }
    }

    public static void initClient() {
        for (ModuleDefinition module : MODULES) {
            if (isActive(module)) {
                module.runtimeHooks().initClient();
            }
        }
    }

    public static void onJeiRuntimeAvailable(IJeiRuntime jeiRuntime) {
        for (ModuleDefinition module : MODULES) {
            if (isActive(module)) {
                module.runtimeHooks().onJeiRuntimeAvailable(jeiRuntime);
            }
        }
    }

    public static void onJeiRuntimeUnavailable() {
        for (ModuleDefinition module : MODULES) {
            if (isActive(module)) {
                module.runtimeHooks().onJeiRuntimeUnavailable();
            }
        }
    }

    public static void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        for (ModuleDefinition module : MODULES) {
            if (isActive(module)) {
                module.runtimeHooks().registerRecipeTransferHandlers(registration);
            }
        }
    }

    private static boolean isActive(ModuleDefinition module) {
        return YzzzFixConfig.isEnabled(module.configKey()) && module.isRuntimeAvailable();
    }

    private static ModuleDefinition module(String packageName, String displayName, String configKey, List<String> requiredModIds) {
        return module(packageName, displayName, configKey, requiredModIds, List.of(), ModuleRuntimeHooks.NOOP);
    }

    private static ModuleDefinition module(
            String packageName,
            String displayName,
            String configKey,
            List<String> requiredModIds,
            List<String> requiredClassNames,
            ModuleRuntimeHooks runtimeHooks
    ) {
        return new ModuleDefinition(
                packageName,
                displayName,
                configKey,
                requiredModIds,
                requiredClassNames,
                List.of(MIXIN_ROOT + packageName + "."),
                runtimeHooks
        );
    }
}
