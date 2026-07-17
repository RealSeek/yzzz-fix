package me.realseek.yzzzfix.mixin.inventoryprofilesnext;

import org.anti_ad.mc.ipnext.event.CutterCraftingHandlerBase;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 清理 IPN 关闭连续合成后遗留的切石机续作状态，避免槽位点击被持续拦截。
 */
@Mixin(value = CutterCraftingHandlerBase.class, remap = false)
public abstract class CutterCraftingHandlerBaseMixin {

    @Shadow
    private boolean skipTick;

    @Shadow
    private boolean isCraftClick;

    @Shadow
    private boolean stillCrafting;

    @Shadow
    private boolean isRefillTick;

    @Shadow
    private int lastRecipe;

    @Shadow
    private int recipe;

    @Shadow
    private boolean isNewScreen;

    @Shadow
    private AbstractContainerScreen<?> currentScreen;

    @Shadow
    private AbstractContainerMenu currentContainer;

    @Shadow
    private boolean getEnabled() {
        throw new AssertionError();
    }

    @Inject(method = "onTickBase", at = @At("HEAD"))
    private void yzzzfix$clearDisabledState(CallbackInfo ci) {
        if (!getEnabled()) {
            yzzzfix$resetState();
        }
    }

    @Inject(method = "onCraftedSink", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$ignoreCraftAfterDisabled(CallbackInfo ci) {
        if (!getEnabled()) {
            yzzzfix$resetState();
            ci.cancel();
        }
    }

    private void yzzzfix$resetState() {
        skipTick = false;
        isCraftClick = false;
        stillCrafting = false;
        isRefillTick = false;
        lastRecipe = -1;
        recipe = -1;
        isNewScreen = true;
        currentScreen = null;
        currentContainer = null;
    }
}
