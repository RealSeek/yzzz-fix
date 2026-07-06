package me.realseek.yzzzfix.mixin.refinedstorage;

import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.IComparer;
import com.refinedmods.refinedstorage.apiimpl.network.grid.CraftingGridBehavior;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 只在配方候选不带 NBT 时放宽 onRecipeTransfer 的 NBT 匹配。
 *
 * <p>配方候选带 NBT 时保留精确匹配，避免拿错特殊物品。</p>
 */
@Mixin(value = CraftingGridBehavior.class, remap = false)
public class CraftingGridBehaviorMixin {

    @Redirect(
            method = "onRecipeTransfer",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/refinedmods/refinedstorage/api/network/INetwork;extractItem"
                            + "(Lnet/minecraft/world/item/ItemStack;IILcom/refinedmods/refinedstorage/api/util/Action;)"
                            + "Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false
    )
    private ItemStack yzzzfix$redirectExtract(
            INetwork network,
            ItemStack stack, int size, int flags, Action action
    ) {
        return network.extractItem(stack, size, yzzzfix$recipeFlags(stack, flags), action);
    }

    @Redirect(
            method = "onRecipeTransfer",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/refinedmods/refinedstorage/api/util/IComparer;isEqual"
                            + "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Z"
            ),
            remap = false
    )
    private boolean yzzzfix$redirectIsEqual(
            com.refinedmods.refinedstorage.api.util.IComparer comparer,
            ItemStack a, ItemStack b, int flags
    ) {
        return comparer.isEqual(a, b, yzzzfix$recipeFlags(a, flags));
    }

    @Unique
    private static int yzzzfix$recipeFlags(ItemStack recipeStack, int flags) {
        return yzzzfix$requiresNbt(recipeStack) ? flags | IComparer.COMPARE_NBT : flags & ~IComparer.COMPARE_NBT;
    }

    @Unique
    private static boolean yzzzfix$requiresNbt(ItemStack stack) {
        return stack != null && stack.hasTag() && stack.getTag() != null && !stack.getTag().isEmpty();
    }
}
