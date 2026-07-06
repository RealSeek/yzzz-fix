package me.realseek.yzzzfix.mixin.refinedstorage;

import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPattern;
import com.refinedmods.refinedstorage.api.autocrafting.ICraftingPatternProvider;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.api.util.IComparer;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import com.refinedmods.refinedstorage.integration.jei.IngredientTracker;
import com.refinedmods.refinedstorage.item.PatternItem;
import com.refinedmods.refinedstorage.util.ItemStackKey;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.*;

/**
 * 对 JEI 填充做条件式 NBT 匹配。
 *
 * <p>配方候选带 NBT 时精确匹配；候选不带 NBT 时允许同物品不同 NBT 参与普通配方。</p>
 */
@Mixin(value = IngredientTracker.class, remap = false)
public abstract class IngredientTrackerMixin {

    @Shadow
    private Map<ItemStackKey, Integer> storedItems;
    @Shadow
    private Map<ItemStackKey, Integer> patternItems;
    @Shadow
    private Map<ItemStackKey, UUID> craftableItems;

    @Unique
    private static boolean yzzzfix$matches(ItemStack a, ItemStack b) {
        return API.instance().getComparer().isEqual(a, b, yzzzfix$comparisonFlags(a));
    }

    @Unique
    private static ItemStackKey yzzzfix$keyWithoutNbt(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setTag(null);
        return new ItemStackKey(copy);
    }

    @Unique
    private static int yzzzfix$comparisonFlags(ItemStack recipeStack) {
        return yzzzfix$requiresNbt(recipeStack) ? IComparer.COMPARE_NBT : 0;
    }

    @Unique
    private static boolean yzzzfix$requiresNbt(ItemStack stack) {
        return stack != null && stack.hasTag() && stack.getTag() != null && !stack.getTag().isEmpty();
    }

    @Unique
    private static void yzzzfix$mergeAvailable(Map<ItemStackKey, Integer> map, ItemStack stack, int count) {
        map.merge(new ItemStackKey(stack.copy()), count, Integer::sum);
        if (yzzzfix$requiresNbt(stack)) {
            map.merge(yzzzfix$keyWithoutNbt(stack), count, Integer::sum);
        }
    }

    /**
     * @reason 同时保存精确 key 和无 NBT fallback key，兼顾 NBT 配方和普通配方。
     */
    @Overwrite
    public void addStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (stack.getItem() instanceof ICraftingPatternProvider) {
            ICraftingPattern pattern = PatternItem.fromCache(Minecraft.getInstance().level, stack);
            if (pattern.isValid()) {
                for (ItemStack outputStack : pattern.getOutputs()) {
                    yzzzfix$mergeAvailable(patternItems, outputStack, 1);
                }
            }
        } else {
            yzzzfix$mergeAvailable(storedItems, stack, stack.getCount());
        }
    }

    /**
     * @reason 遍历 Map 并使用无 NBT 比较替代 ItemStackKey 的 Map.get() 查找。
     */
    @Overwrite
    public ItemStack findBestMatch(GridContainerMenu gridContainer, Player player, List<ItemStack> list) {
        ItemStack resultStack = ItemStack.EMPTY;
        int count = 0;

        for (ItemStack listStack : list) {
            // check crafting matrix
            if (gridContainer.getGrid().getGridType().equals(GridType.CRAFTING)) {
                CraftingContainer craftingMatrix = gridContainer.getGrid().getCraftingMatrix();
                if (craftingMatrix != null) {
                    for (int matrixSlot = 0; matrixSlot < craftingMatrix.getContainerSize(); matrixSlot++) {
                        ItemStack stackInSlot = craftingMatrix.getItem(matrixSlot);
                        if (yzzzfix$matches(listStack, stackInSlot) && stackInSlot.getCount() > count) {
                            count = stackInSlot.getCount();
                            resultStack = stackInSlot;
                        }
                    }
                }
            }

            // check inventory
            for (int inventorySlot = 0; inventorySlot < player.getInventory().getContainerSize(); inventorySlot++) {
                ItemStack stackInSlot = player.getInventory().getItem(inventorySlot);
                if (yzzzfix$matches(listStack, stackInSlot) && stackInSlot.getCount() > count) {
                    count = stackInSlot.getCount();
                    resultStack = stackInSlot;
                }
            }

            // check storage — iterate with NBT-free comparison
            for (var entry : storedItems.entrySet()) {
                if (yzzzfix$matches(listStack, entry.getKey().getStack()) && entry.getValue() > count) {
                    resultStack = listStack;
                    count = entry.getValue();
                }
            }
        }

        // check craftable / pattern items
        if (count == 0) {
            for (ItemStack itemStack : list) {
                boolean found = false;
                for (ItemStackKey key : craftableItems.keySet()) {
                    if (yzzzfix$matches(itemStack, key.getStack())) {
                        resultStack = itemStack;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    for (ItemStackKey key : patternItems.keySet()) {
                        if (yzzzfix$matches(itemStack, key.getStack())) {
                            resultStack = itemStack;
                            found = true;
                            break;
                        }
                    }
                }
                if (found) {
                    break;
                }
            }
        }

        return resultStack;
    }

    /**
     * @reason 根据配方候选是否带 NBT 决定是否启用精确 NBT 比较。
     */
    @Redirect(
            method = "checkStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/refinedmods/refinedstorage/api/util/IComparer;isEqual"
                            + "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Z"
            ),
            remap = false
    )
    private boolean yzzzfix$redirectIsEqual(IComparer comparer, ItemStack a, ItemStack b, int flags) {
        return comparer.isEqual(a, b, yzzzfix$comparisonFlags(a));
    }
}
