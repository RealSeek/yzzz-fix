package me.realseek.yzzzfix.module.cy3_core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

/**
 * 客户端侧 CY3 LifeLimiter HPS 集合清理工具。
 *
 * <p>当服务端发送清理数据包时，该类通过反射移除本地玩家在 {@code LifeLimiter.HPS} 中的 UUID，
 * 确保客户端状态与服务端保持同步。</p>
 */
public final class CY3CoreClientHPSCleaner {

    private static final Logger LOGGER = LogManager.getLogger("CY3CoreClientHPSCleaner");

    private CY3CoreClientHPSCleaner() {
    }

    @SuppressWarnings("unchecked")
    public static void clearHPS() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        try {
            Class<?> clazz = Class.forName("org.heike233.chapterofyuusha3.comm.compat.curios.item.LifeLimiter");
            Field field = clazz.getField("HPS");
            Set<UUID> hps = (Set<UUID>) field.get(null);
            hps.remove(player.getUUID());
        } catch (Exception exception) {
            LOGGER.debug("Failed to clear CY3 LifeLimiter.HPS on client.", exception);
        }
    }
}
