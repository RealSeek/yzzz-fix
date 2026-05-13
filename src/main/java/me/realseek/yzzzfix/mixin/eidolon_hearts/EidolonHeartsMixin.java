package me.realseek.yzzzfix.mixin.eidolon_hearts;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 禁用 Eidolon 破损的心形覆盖层渲染逻辑。
 *
 * <p>该 Mixin 取消目标覆盖层的 render 方法，避免客户端界面渲染异常或崩溃。</p>
 */
@Mixin(targets = "elucent.eidolon.client.ClientRegistry$EidolonHearts")
public abstract class EidolonHeartsMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzfix$cancelOverlay(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height, CallbackInfo ci) {
        ci.cancel();
    }
}
