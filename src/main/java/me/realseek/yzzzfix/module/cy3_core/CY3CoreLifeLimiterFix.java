package me.realseek.yzzzfix.module.cy3_core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 修复 CY3 Life Limiter 相关生命值同步状态残留的问题。
 *
 * <p>该类通过反射访问目标模组中的 {@code LifeLimiter.HPS} 集合，
 * 并在玩家离开世界、登出或状态变化时清理残留 UUID，同时主动触发生命值同步。</p>
 *
 * <p>当目标字段无法解析时，该修复会自动失效并记录错误日志，以避免影响游戏启动。</p>
 */
public final class CY3CoreLifeLimiterFix {

    private static final Logger LOGGER = LogManager.getLogger("CY3CoreLifeLimiterFix");

    @SuppressWarnings("unchecked")
    private final Set<UUID> hps;
    private final Set<UUID> syncedPlayers = ConcurrentHashMap.newKeySet();

    @SuppressWarnings("unchecked")
    public CY3CoreLifeLimiterFix() {
        Set<UUID> resolved = null;
        try {
            Class<?> clazz = Class.forName("org.heike233.chapterofyuusha3.comm.compat.curios.item.LifeLimiter");
            Field field = clazz.getField("HPS");
            Object value = field.get(null);
            if (value instanceof Set<?> set) {
                resolved = (Set<UUID>) set;
            } else {
                LOGGER.error("CY3 LifeLimiter.HPS has unexpected type: {}",
                        value == null ? "null" : value.getClass().getName());
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to resolve CY3 LifeLimiter.HPS", exception);
        }
        this.hps = resolved;
    }

    public boolean isAvailable() {
        return hps != null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UUID uuid = player.getUUID();
        if (hps.remove(uuid)) {
            syncedPlayers.remove(uuid);
            CY3CoreNetworkHandler.sendClearHPS(player);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        UUID uuid = player.getUUID();
        if (hps.contains(uuid)) {
            if (syncedPlayers.add(uuid)) {
                player.setHealth(player.getHealth());
            }
        } else if (syncedPlayers.remove(uuid)) {
            player.setHealth(player.getHealth());
            CY3CoreNetworkHandler.sendClearHPS(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        syncedPlayers.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        syncedPlayers.clear();
    }
}
