package me.realseek.yzzzfix.mixin.immortalers_delight;

import com.renyigesai.immortalers_delight.block.enchantal_cooler.EnchantalCoolerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复魔能冷却器的输出槽、容器槽、燃料槽物品变更不广播到客户端的问题。
 *
 * <p>原代码的 {@code craftItem()} 和 {@code fillFuel()} 直接修改
 * {@code ItemStackHandler}，绕过了容器同步链路。</p>
 *
 * <p>在每次 inventory 修改后注入 {@code setChanged()}，
 * 确保容器正确标记脏数据并触发客户端同步。</p>
 */
@Mixin(value = EnchantalCoolerBlockEntity.class, remap = false)
public class EnchantalCoolerSyncMixin {

    @Inject(method = "craftItem", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/ItemStackHandler;extractItem(IZ)Lnet/minecraft/world/item/ItemStack;"), remap = false)
    private void yzzz$markDirtyAfterExtract(CallbackInfo ci) {
        ((BlockEntity) (Object) this).setChanged();
    }

    @Inject(method = "craftItem", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/ItemStackHandler;setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"), remap = false)
    private void yzzz$markDirtyAfterSet(CallbackInfo ci) {
        ((BlockEntity) (Object) this).setChanged();
    }

    @Inject(method = "craftItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41769_(I)V"), remap = false)
    private void yzzz$markDirtyAfterGrow(CallbackInfo ci) {
        ((BlockEntity) (Object) this).setChanged();
    }

    @Inject(method = "fillFuel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41774_(I)V"), remap = false)
    private void yzzz$markDirtyAfterShrink(CallbackInfo ci) {
        ((BlockEntity) (Object) this).setChanged();
    }
}
