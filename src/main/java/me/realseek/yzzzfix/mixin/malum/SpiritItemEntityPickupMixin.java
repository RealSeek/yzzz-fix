package me.realseek.yzzzfix.mixin.malum;

import com.sammy.malum.core.handlers.SpiritHarvestHandler;
import me.realseek.yzzzfix.module.malum.SpiritItemDropService;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让普通精魄掉落物在拾取时继续经过 Malum 及附属模组的拾取处理器。
 */
@Mixin(ItemEntity.class)
public abstract class SpiritItemEntityPickupMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$handleSpiritPickup(Player player, CallbackInfo ci) {
        ItemEntity item = (ItemEntity) (Object) this;
        if (item.level().isClientSide
                || !ModList.get().isLoaded("malstone")
                || !SpiritItemDropService.isSpiritDrop(item)) {
            return;
        }

        ItemStack stack = item.getItem();
        if (!stack.isEmpty()) {
            SpiritHarvestHandler.pickupSpirit(player, stack.copy());
        }
        item.discard();
        ci.cancel();
    }
}
