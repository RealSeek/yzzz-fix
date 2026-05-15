package me.realseek.yzzzfix.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 背包物品操作工具类。
 *
 * <p>提供安全的物品给予方法，当玩家背包已满时自动将物品掉落到世界中，
 * 避免因背包空间不足导致物品丢失。</p>
 */
public final class InventoryUtil {

    private InventoryUtil() {
    }

    /**
     * 将物品给予玩家，若背包已满则掉落在玩家脚下。
     *
     * <p>该方法会先尝试通过 {@link Player#addItem(ItemStack)} 将物品放入背包，
     * 若背包无法容纳则调用 {@link Player#drop(ItemStack, boolean)} 将物品作为实体掉落。
     * 掉落的物品不会有投掷速度，会直接出现在玩家位置。</p>
     *
     * @param player 目标玩家
     * @param stack  要给予的物品，传入后不应再使用（可能被修改）
     */
    public static void giveOrDrop(Player player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        ItemStack toGive = stack.copy();
        if (!player.addItem(toGive)) {
            player.drop(toGive, false);
        }
    }
}
