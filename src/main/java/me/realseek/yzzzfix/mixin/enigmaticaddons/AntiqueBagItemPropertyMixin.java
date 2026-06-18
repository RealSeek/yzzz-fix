package me.realseek.yzzzfix.mixin.enigmaticaddons;

import auviotre.enigmatic.addon.contents.objects.bookbag.AntiqueBagCapability;
import auviotre.enigmatic.addon.contents.objects.bookbag.IAntiqueBagHandler;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复古旧书袋缺少 {@code has_flower} 物品属性导致模型状态不更新的问题。
 * <p>在 {@code ClientProxy.initItemProperties} 末尾注册自定义属性，
 * 使书袋模型能根据能力数据或 NBT 标签切换显示。</p>
 */
@Mixin(targets = "auviotre.enigmatic.addon.proxy.ClientProxy", remap = false)
public class AntiqueBagItemPropertyMixin {

    @Inject(method = "initItemProperties", at = @At("RETURN"))
    private void yzzzfix$registerHasFlowerProperty(CallbackInfo ci) {
        ItemProperties.register(
                auviotre.enigmatic.addon.registries.EnigmaticAddonItems.ANTIQUE_BAG,
                new ResourceLocation("has_flower"),
                (stack, level, entity, seed) -> yzzzfix$computeHasFlower(stack, entity)
        );
    }

    private static float yzzzfix$computeHasFlower(ItemStack stack, LivingEntity entity) {
        float tagFallback = stack.getOrCreateTag().getBoolean("hasFlower") ? 1.0F : 0.0F;
        if (!(entity instanceof Player player)) return tagFallback;

        try {
            var cap = SuperAddonHandler.getCapability(player, AntiqueBagCapability.INVENTORY);
            if (cap != null && cap.isPresent()) {
                IAntiqueBagHandler handler = cap.orElseThrow(
                        () -> new IllegalArgumentException("Lazy optional must not be empty"));
                if (handler.hasFlower()) return 1.0F;
                if (player.level().isClientSide) return tagFallback;
                return 0.0F;
            }
        } catch (Exception ignored) {
        }
        return tagFallback;
    }
}
