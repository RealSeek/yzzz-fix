package me.realseek.yzzzfix.mixin.majruszsdifficulty;

import com.majruszsdifficulty.items.TreasureBag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 在服务端拒绝物品栏内右键宝藏袋操作 */
@Mixin(value = TreasureBag.class, remap = false)
public abstract class TreasureBagServerMixin {
    @Inject(
            method = "openInInventory(Lcom/majruszsdifficulty/items/TreasureBag$RightClickAction;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private static void yzzz$cancelInventoryRightClick(
            TreasureBag.RightClickAction action,
            ServerPlayer player,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}
