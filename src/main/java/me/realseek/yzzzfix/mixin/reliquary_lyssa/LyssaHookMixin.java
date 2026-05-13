package me.realseek.yzzzfix.mixin.reliquary_lyssa;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import reliquary.entities.LyssaHook;
import reliquary.items.RodOfLyssaItem;

/**
 * 修复 Lyssa Hook 实体销毁后物品侧仍保留旧实体 ID 的问题。
 *
 * <p>该 Mixin 在钩爪实体移除时遍历玩家背包，清理所有关联的 Rod of Lyssa 物品中的
 * hookEntityId 标签，避免后续逻辑继续引用已经不存在的实体。</p>
 */
@Mixin(LyssaHook.class)
public abstract class LyssaHookMixin {

    @Inject(method = "m_142687_", at = @At("HEAD"), remap = false)
    private void yzzzfix$onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        LyssaHook self = (LyssaHook) (Object) this;
        Player player = self.getFishingPlayerOptional().orElse(null);
        if (player == null) return;
        int myId = self.getId();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof RodOfLyssaItem) {
                if (RodOfLyssaItem.getHookEntityId(stack) == myId) {
                    stack.getOrCreateTag().putInt("hookEntityId", 0);
                }
            }
        }
    }
}
