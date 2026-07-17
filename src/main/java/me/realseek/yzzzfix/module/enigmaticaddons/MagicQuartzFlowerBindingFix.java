package me.realseek.yzzzfix.module.enigmaticaddons;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 修复魔法石英花在玩家重生后自动关闭导致不会提供正面效果的问题。
 */
public final class MagicQuartzFlowerBindingFix {

    private MagicQuartzFlowerBindingFix() {
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }
        MagicQuartzFlowerBindingService.copyDeathBinding(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        MagicQuartzFlowerBindingService.restoreMissingBindingFromInventory(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        MagicQuartzFlowerBindingService.restoreMissingBindingFromInventory(event.getEntity());
    }
}
