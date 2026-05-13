package me.realseek.yzzzfix.module.immortalers_delight;

import me.realseek.yzzzfix.YzzzFix;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Immortalers Delight 模块的客户端事件处理器。
 *
 * <p>监听配方更新事件以重试 JEI 配方注册，并在玩家登出时重置注册状态。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ImmortalersDelightClientEventHandler {

    private ImmortalersDelightClientEventHandler() {
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        YzzzFix.LOGGER.debug("Recipes updated on client, retrying Immortalers Delight JEI registration.");
        ImmortalersDelightRecipeRegistrar.tryRegister();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ImmortalersDelightRecipeRegistrar.reset();
        YzzzFix.LOGGER.debug("Reset Immortalers Delight JEI registration state on logout.");
    }
}
