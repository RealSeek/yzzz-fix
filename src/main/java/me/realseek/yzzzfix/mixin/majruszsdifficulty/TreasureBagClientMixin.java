package me.realseek.yzzzfix.mixin.majruszsdifficulty;

import com.majruszsdifficulty.items.TreasureBag;
import com.majruszlibrary.events.OnItemInventoryClicked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 移除宝藏袋在物品栏中右键直接打开的功能。
 *
 * <p>原模组通过 {@link TreasureBag.Client#openInInventory(OnItemInventoryClicked)}
 * 监听 {@link OnItemInventoryClicked} 事件，在玩家于物品栏内右键宝藏袋时直接打开发送网络包。</p>
 *
 * <p>该 Mixin 在 {@code openInInventory} 方法头部注入并取消执行，
 * 从而阻止物品栏右键开袋行为，仅保留手持右键开袋的途径。</p>
 */
@Mixin(TreasureBag.Client.class)
public class TreasureBagClientMixin {

    @Inject(method = "openInInventory", at = @At("HEAD"), cancellable = true, remap = false)
    private static void yzzz$cancelInventoryRightClick(CallbackInfo ci) {
        ci.cancel();
    }
}
