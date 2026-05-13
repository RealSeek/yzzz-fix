package me.realseek.yzzzfix.module.forbidden_arcanus;

import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.Ritual;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Forbidden Arcanus 仪式数据的客户端缓存。
 *
 * <p>存储从服务端同步的仪式列表，并跟踪 JEI 配方是否已注册。
 * 在玩家登出或 JEI 运行时不可用时清理缓存状态。</p>
 */
public final class ForbiddenArcanusClientRitualCache {

    private static volatile List<Ritual> cachedRituals = null;
    private static volatile boolean recipesRegistered = false;

    private ForbiddenArcanusClientRitualCache() {
    }

    public static void setRituals(List<Ritual> rituals) {
        cachedRituals = new CopyOnWriteArrayList<>(rituals);
        recipesRegistered = false;
    }

    public static @Nullable List<Ritual> getRituals() {
        return cachedRituals;
    }

    public static boolean hasRituals() {
        return cachedRituals != null && !cachedRituals.isEmpty();
    }

    public static boolean isRecipesRegistered() {
        return recipesRegistered;
    }

    public static void markRegistered() {
        recipesRegistered = true;
    }

    public static void markUnregistered() {
        recipesRegistered = false;
    }

    public static void clear() {
        cachedRituals = null;
        recipesRegistered = false;
    }
}
