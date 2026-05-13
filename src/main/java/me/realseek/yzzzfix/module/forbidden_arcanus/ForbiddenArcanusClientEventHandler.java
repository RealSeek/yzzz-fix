package me.realseek.yzzzfix.module.forbidden_arcanus;

import me.realseek.yzzzfix.YzzzFix;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Forbidden Arcanus 模块的客户端事件处理器。
 *
 * <p>监听配方更新事件以重试 JEI 配方注册，并在玩家登出时清理仪式缓存。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ForbiddenArcanusClientEventHandler {

    private ForbiddenArcanusClientEventHandler() {
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        YzzzFix.LOGGER.debug("Recipes updated on client, retrying Forbidden Arcanus JEI registration.");
        ForbiddenArcanusRecipeRegistrar.tryRegister();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ForbiddenArcanusClientRitualCache.clear();
        YzzzFix.LOGGER.debug("Cleared Forbidden Arcanus ritual cache on logout.");
    }
}
