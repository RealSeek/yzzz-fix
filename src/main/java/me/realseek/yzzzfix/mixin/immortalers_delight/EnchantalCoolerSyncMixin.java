package me.realseek.yzzzfix.mixin.immortalers_delight;

import com.renyigesai.immortalers_delight.block.enchantal_cooler.EnchantalCoolerBlockEntity;
import me.realseek.yzzzfix.module.immortalers_delight.EnchantalCoolerInventoryHandler;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 补全魔凝机未经过 {@link net.minecraftforge.items.ItemStackHandler} 回调的同步入口，并抑制
 * 没有可消耗燃料时的无效逐 tick 方块更新。
 */
@Mixin(value = EnchantalCoolerBlockEntity.class, remap = false)
public class EnchantalCoolerSyncMixin {

    @Redirect(
            method = "craftTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/renyigesai/immortalers_delight/block/enchantal_cooler/EnchantalCoolerBlockEntity;fuelSupplement()Z"
            ),
            remap = false
    )
    private static boolean yzzzfix$onlyBroadcastForActualFuel(EnchantalCoolerBlockEntity blockEntity) {
        ItemStack fuel = blockEntity.getInventory().getStackInSlot(blockEntity.FUEL_SLOT);
        return blockEntity.residualDye < 3 && blockEntity.isFuel(fuel);
    }

    @Inject(method = "m_7407_", at = @At("RETURN"), remap = false)
    private void yzzzfix$notifyAfterContainerRemoval(
            int slot,
            int amount,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (!cir.getReturnValue().isEmpty()) {
            EnchantalCoolerInventoryHandler.notifyOwner((EnchantalCoolerBlockEntity) (Object) this);
        }
    }
}
