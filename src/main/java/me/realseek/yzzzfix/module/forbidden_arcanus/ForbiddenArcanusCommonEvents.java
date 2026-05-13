package me.realseek.yzzzfix.module.forbidden_arcanus;

import me.realseek.yzzzfix.YzzzFix;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Forbidden Arcanus 模块的 common 事件处理器。
 *
 * <p>在玩家登录后延迟发送仪式数据同步包，确保客户端在接收数据时已完成初始化。</p>
 */
public final class ForbiddenArcanusCommonEvents {

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.getServer();
            if (server != null) {
                server.execute(() -> scheduleDelayed(server, serverPlayer, 20));
            }
        }
    }

    private static void scheduleDelayed(MinecraftServer server, ServerPlayer player, int ticks) {
        if (ticks <= 0) {
            ForbiddenArcanusNetworkHandler.sendRituals(player);
            YzzzFix.LOGGER.debug("Sent Forbidden Arcanus ritual sync to {}", player.getName().getString());
            return;
        }
        server.execute(() -> scheduleDelayed(server, player, ticks - 1));
    }
}
