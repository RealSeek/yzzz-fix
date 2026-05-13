package me.realseek.yzzzfix.module.lychee_offhand;

import me.realseek.yzzzfix.YzzzFix;
import me.realseek.yzzzfix.module.ModuleRuntimeHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;

/**
 * Lychee 副手交互修复模块的运行时钩子实现。
 *
 * <p>注册高优先级事件监听器，在主手持刷子右键方块时优先检查副手物品是否匹配 Lychee 交互配方，
 * 解决刷子使用逻辑覆盖副手交互的问题。</p>
 */
public final class LycheeOffhandModule implements ModuleRuntimeHooks {

    public static final LycheeOffhandModule INSTANCE = new LycheeOffhandModule();

    private LycheeOffhandModule() {
    }

    @Override
    public void initCommon() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onRightClickBlock);
        YzzzFix.LOGGER.debug("Lychee offhand fix event handler registered.");
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (event.getEntity().isSecondaryUseActive()) {
            return;
        }

        ItemStack mainHandItem = event.getEntity().getMainHandItem();
        if (!mainHandItem.is(Items.BRUSH)) {
            return;
        }

        ItemStack offhandItem = event.getEntity().getOffhandItem();
        if (offhandItem.isEmpty() || event.getEntity().getCooldowns().isOnCooldown(offhandItem.getItem())) {
            return;
        }

        LycheeContext.Builder<LycheeContext> builder = new LycheeContext.Builder<>(event.getLevel());
        builder.withParameter(LycheeLootContextParams.DIRECTION, event.getHitVec().getDirection());

        boolean matched = RecipeTypes.BLOCK_INTERACTING.process(
                event.getEntity(),
                InteractionHand.OFF_HAND,
                event.getPos(),
                event.getHitVec().getLocation(),
                builder
        ).isPresent();

        if (YzzzFix.LOGGER.isDebugEnabled()) {
            Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
            YzzzFix.LOGGER.debug(
                    "lycheefix offhand precheck: side={}, block={}, main={}, off={}, matched={}",
                    event.getLevel().isClientSide ? "client" : "server",
                    block,
                    mainHandItem.getItem(),
                    offhandItem.getItem(),
                    matched
            );
        }

        if (!matched) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
