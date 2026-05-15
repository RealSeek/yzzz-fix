package me.realseek.yzzzfix.mixin.aquamirae;

import com.obscuria.aquamirae.Aquamirae;
import com.obscuria.aquamirae.registry.AquamiraeSounds;
import me.realseek.yzzzfix.util.InventoryUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 修复海灵物语普通宝藏袋（Pirate Pouch）在背包满时不给物品的问题。
 *
 * <p>原版 {@code PiratePouchItem#use} 方法在将奖励物品放入玩家背包时，
 * 未处理背包已满的情况，导致物品直接丢失。此外随机选取逻辑存在 off-by-one 错误，
 * 使得奖池中最后一个物品永远不会被选中。</p>
 *
 * <p>本 Mixin 在方法头部拦截并完整替换使用逻辑：
 * <ul>
 *   <li>使用 {@code nextInt(size)} 确保奖池中所有物品均有机会被选中</li>
 *   <li>通过 {@link InventoryUtil#giveOrDrop} 安全给予物品，背包满时自动掉落</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "com.obscuria.aquamirae.common.items.PiratePouchItem", remap = false)
public abstract class PiratePouchItemMixin {

    @Inject(method = "m_7203_", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$onUse(Level level, Player player, InteractionHand hand,
                               CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        player.swing(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.blockPosition().above(),
                    (SoundEvent) AquamiraeSounds.ITEM_POUCH_OPEN.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);

            List<ItemStack> items = Aquamirae.SetBuilder.common();
            if (!items.isEmpty()) {
                ItemStack reward = items.get(player.getRandom().nextInt(items.size())).copy();
                InventoryUtil.giveOrDrop(player, reward);
            }
        }

        stack.shrink(1);
        cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide()));
    }
}
