package me.realseek.yzzzfix.mixin.lychee_offhand;

import me.realseek.yzzzfix.YzzzFix;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;

/**
 * 修复 Lychee 交互配方在刷子持续使用期间无法触发副手交互的问题。
 *
 * <p>该 Mixin 在刷子的 {@code onUseTick} 中每隔一定 tick 重新检查副手物品是否匹配
 * Lychee 方块交互配方，确保持续使用刷子时副手交互仍能正常触发。</p>
 */
@Mixin(BrushItem.class)
public class BrushItemMixin {

    @Inject(method = "m_5929_", at = @At("RETURN"), remap = false)
    private void yzzzfix$retryOffhandWhileHoldingUse(
            Level level,
            LivingEntity livingEntity,
            ItemStack stack,
            int remainingUseDuration,
            CallbackInfo ci
    ) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) {
            return;
        }
        if (player.getUsedItemHand() != InteractionHand.MAIN_HAND || player.isSecondaryUseActive()) {
            return;
        }

        int useTicks = ((BrushItem) (Object) this).getUseDuration(stack) - remainingUseDuration + 1;
        if (useTicks % 10 != 5) {
            return;
        }

        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
                livingEntity,
                entity -> !entity.isSpectator() && entity.isPickable(),
                Math.sqrt(ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE) - 1.0D
        );
        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return;
        }

        BlockState blockState = level.getBlockState(blockHitResult.getBlockPos());
        if (blockState.getBlock() instanceof BrushableBlock) {
            return;
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (offhandStack.isEmpty() || player.getCooldowns().isOnCooldown(offhandStack.getItem())) {
            return;
        }

        LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(level);
        builder.withParameter(LycheeLootContextParams.DIRECTION, blockHitResult.getDirection());
        if (RecipeTypes.BLOCK_INTERACTING.process(
                player,
                InteractionHand.OFF_HAND,
                blockHitResult.getBlockPos(),
                blockHitResult.getLocation(),
                builder
        ).isPresent() && YzzzFix.LOGGER.isDebugEnabled()) {
            YzzzFix.LOGGER.debug(
                    "lycheefix brush loop retrigger: block={}, off={}",
                    blockState.getBlock(),
                    offhandStack.getItem()
            );
        }
    }
}
