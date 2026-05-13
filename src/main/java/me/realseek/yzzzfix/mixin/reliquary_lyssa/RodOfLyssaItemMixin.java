package me.realseek.yzzzfix.mixin.reliquary_lyssa;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import reliquary.entities.LyssaHook;
import reliquary.items.RodOfLyssaItem;

/**
 * 修复 Rod of Lyssa 使用过期钩爪实体 ID 的问题。
 *
 * <p>该 Mixin 在物品使用前验证 hookEntityId 对应的实体是否仍为有效的 LyssaHook，
 * 如果实体已失效则清除 ID，避免后续逻辑引用错误实体。</p>
 */
@Mixin(RodOfLyssaItem.class)
public abstract class RodOfLyssaItemMixin {

    @Inject(method = "m_7203_", at = @At("HEAD"), remap = false)
    private void yzzzfix$validateHookEntityId(Level level, Player player, InteractionHand hand,
                                               CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        int hookEntityId = RodOfLyssaItem.getHookEntityId(stack);
        if (hookEntityId != 0) {
            Entity entity = level.getEntity(hookEntityId);
            if (!(entity instanceof LyssaHook)) {
                stack.getOrCreateTag().putInt("hookEntityId", 0);
            }
        }
    }
}
