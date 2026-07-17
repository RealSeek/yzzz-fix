package me.realseek.yzzzfix.module.enigmaticaddons;

import auviotre.enigmatic.addon.contents.items.ArtificialFlower;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * 修复魔法石英花在玩家重生后自动关闭导致不会提供正面效果的问题。
 */
public final class MagicQuartzFlowerBindingService {

    private static final String FLOWER_ENABLE_UUID = "FlowerEnableUUID";
    private static final String FLOWER_ENABLE = "FlowerEnable";
    private static final Logger LOGGER = LogUtils.getLogger();

    private MagicQuartzFlowerBindingService() {
    }

    public static void copyDeathBinding(Player oldPlayer, Player newPlayer) {
        CompoundTag oldData = oldPlayer.getPersistentData();
        if (oldData.hasUUID(FLOWER_ENABLE_UUID)) {
            newPlayer.getPersistentData().putUUID(FLOWER_ENABLE_UUID, oldData.getUUID(FLOWER_ENABLE_UUID));
        }
    }

    public static void restoreMissingBindingFromInventory(Player player) {
        if (!(player instanceof ServerPlayer) || player.getPersistentData().hasUUID(FLOWER_ENABLE_UUID)) {
            return;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (restoreMissingBindingFromStack(player, stack)) {
                return;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (restoreMissingBindingFromStack(player, stack)) {
                return;
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (restoreMissingBindingFromStack(player, stack)) {
                return;
            }
        }
    }

    private static boolean restoreMissingBindingFromStack(Player player, ItemStack stack) {
        if (!isEnabledMagicQuartzFlower(stack)) {
            return false;
        }

        UUID flowerUuid = ArtificialFlower.Helper.getFlowerUUID(stack);
        if (flowerUuid == null) {
            return false;
        }

        player.getPersistentData().putUUID(FLOWER_ENABLE_UUID, flowerUuid);
        LOGGER.debug("Restored Magic Quartz Flower binding for {}", player.getScoreboardName());
        return true;
    }

    private static boolean isEnabledMagicQuartzFlower(ItemStack stack) {
        if (!stack.is(EnigmaticAddonItems.ARTIFICIAL_FLOWER)) {
            return false;
        }

        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(FLOWER_ENABLE);
    }
}
