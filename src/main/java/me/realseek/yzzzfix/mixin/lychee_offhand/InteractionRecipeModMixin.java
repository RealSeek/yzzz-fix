package me.realseek.yzzzfix.mixin.lychee_offhand;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.interaction.InteractionRecipeMod;

/**
 * 修复 Lychee 交互配方对副手物品处理不完整的问题。
 *
 * <p>当主手持刷子时，原始 {@code useItemOn} 不会检查副手的 Lychee 交互配方。
 * 该 Mixin 在方法入口处优先处理副手交互，匹配成功则直接返回 SUCCESS。</p>
 */
@Mixin(value = InteractionRecipeMod.class, remap = false)
public class InteractionRecipeModMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true, remap = false)
    private static void yzzzfix$processOffhandBeforeBrush(
            Player player,
            Level level,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (player.isSecondaryUseActive() || hand != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!player.getMainHandItem().is(Items.BRUSH)) {
            return;
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (offhandStack.isEmpty() || player.getCooldowns().isOnCooldown(offhandStack.getItem())) {
            return;
        }

        LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(level);
        builder.withParameter(LycheeLootContextParams.DIRECTION, hitResult.getDirection());
        if (RecipeTypes.BLOCK_INTERACTING.process(
                player,
                InteractionHand.OFF_HAND,
                hitResult.getBlockPos(),
                hitResult.getLocation(),
                builder
        ).isPresent()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
