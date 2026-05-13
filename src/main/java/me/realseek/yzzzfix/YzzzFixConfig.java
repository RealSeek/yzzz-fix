package me.realseek.yzzzfix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.realseek.yzzzfix.module.ModuleRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Yzzz Fix 的 JSON 配置管理器。
 *
 * <p>配置文件位于 {@code config/yzzz-fix.json}。该类负责在首次访问时懒加载配置，
 * 在配置缺失、格式错误或缺少模块键时自动使用默认值补全并重写配置文件。</p>
 *
 * <p>配置快照以不可变语义对外提供，运行期间可通过 {@link #reload()} 重新读取配置。
 * 每个模块的启用状态由 {@link ModuleRegistry} 中定义的配置键决定。</p>
 */
public final class YzzzFixConfig {

    private static final Logger LOGGER = LogManager.getLogger("YzzzFixConfig");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final Path CONFIG_PATH = Paths.get("config", "yzzz-fix.json");

    private static volatile ConfigSnapshot snapshot;

    private YzzzFixConfig() {
    }

    public static void ensureLoaded() {
        getSnapshot();
    }

    public static boolean isEnabled(String configKey) {
        return getSnapshot().moduleStates().getOrDefault(configKey, true);
    }

    public static synchronized void reload() {
        snapshot = loadOrCreate();
    }

    static synchronized ConfigSnapshot getSnapshot() {
        if (snapshot == null) {
            snapshot = loadOrCreate();
        }
        return snapshot;
    }

    private static ConfigSnapshot loadOrCreate() {
        LinkedHashMap<String, Boolean> defaults = ModuleRegistry.defaultConfigValues();
        JsonObject root = null;
        boolean shouldRewrite = false;

        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (parsed != null && parsed.isJsonObject()) {
                    root = parsed.getAsJsonObject();
                } else {
                    shouldRewrite = true;
                    LOGGER.warn("Config file {} is not a JSON object, regenerating with defaults.", CONFIG_PATH);
                }
            } catch (Exception exception) {
                shouldRewrite = true;
                LOGGER.warn("Failed to read config file {}, regenerating with defaults.", CONFIG_PATH, exception);
            }
        } else {
            shouldRewrite = true;
        }

        if (root == null) {
            root = buildDefaultRoot(defaults);
        }

        LinkedHashMap<String, Boolean> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, Boolean> entry : defaults.entrySet()) {
            String key = entry.getKey();
            boolean value = entry.getValue();
            if (root.has(key) && root.get(key).isJsonPrimitive() && root.get(key).getAsJsonPrimitive().isBoolean()) {
                value = root.get(key).getAsBoolean();
            } else {
                shouldRewrite = true;
            }
            resolved.put(key, value);
        }

        if (!"1".equals(readString(root, "config_version"))) {
            shouldRewrite = true;
        }

        ConfigSnapshot loadedSnapshot = new ConfigSnapshot(resolved);
        if (shouldRewrite) {
            writeSnapshot(loadedSnapshot);
        }
        return loadedSnapshot;
    }

    private static JsonObject buildDefaultRoot(Map<String, Boolean> defaults) {
        JsonObject root = new JsonObject();
        root.addProperty("config_version", "1");
        root.addProperty("_comment", "yzzz-fix 配置文件 - 设为 false 可禁用对应修复，修改后需重启游戏");
        for (Map.Entry<String, Boolean> entry : defaults.entrySet()) {
            root.addProperty(entry.getKey(), entry.getValue());
        }
        return root;
    }

    private static String readString(JsonObject root, String key) {
        if (!root.has(key)) {
            return null;
        }
        JsonElement value = root.get(key);
        return value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? value.getAsString()
                : null;
    }

    private static void writeSnapshot(ConfigSnapshot snapshot) {
        try {
            Path parent = CONFIG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(buildDefaultRoot(snapshot.moduleStates()), writer);
            }
        } catch (IOException exception) {
            LOGGER.error("Failed to write config file {}", CONFIG_PATH, exception);
        }
    }

    public record ConfigSnapshot(LinkedHashMap<String, Boolean> moduleStates) {
    }
}
