package me.realseek.yzzzfix.mixin.apotheosis_loot;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让「可穿戴的骷髅方块物品」重新进入神化（Apotheosis）的战利品类型判定。
 *
 * <p>神化 {@code LootCategory#armorSlot} 会无条件排除两类方块物品：雕刻南瓜，以及
 * {@link AbstractSkullBlock} 的方块物品。后者本来只是为了排除原版骷髅头（原版骷髅头
 * 不能穿戴，不应该算头盔），但像突变生物（Mutant Monsters）的「突变骷髅头」这种
 * <b>方块物品 + 可穿戴头盔</b> 的组合也被一并排除了，导致：</p>
 * <ul>
 *   <li>{@code LootCategory.forItem} 返回 {@code NONE} → 不会生成神化词缀；</li>
 *   <li>{@code SocketHelper#getGemsImpl} / {@code AddSocketsRecipe} 看到 {@code NONE}
 *       直接返回空 → 无法获得宝石槽位。</li>
 * </ul>
 *
 * <p>本注入在 {@code forItem} 返回 {@code NONE} 之后兜底：只有当物品同时满足
 * 「{@link BlockItem} 且方块是 {@link AbstractSkullBlock}」与「实现了 {@link Equipable}
 * 且装备槽位为头盔」时才改判为 {@link LootCategory#HELMET}。原版骷髅头不是
 * {@link Equipable}，雕刻南瓜的方块类型也不是 {@link AbstractSkullBlock}，因此两者
 * 行为完全不变；也不会覆盖配置文件里显式写死的类型覆盖（那些不为 {@code NONE}）。</p>
 */
@Mixin(value = LootCategory.class, remap = false)
public abstract class LootCategoryMixin {

    @Inject(method = "forItem", at = @At("RETURN"), cancellable = true)
    private static void yzzzfix$wearableSkullIsHelmet(ItemStack stack, CallbackInfoReturnable<LootCategory> cir) {
        if (cir.getReturnValue() != LootCategory.NONE || stack == null || stack.isEmpty()) {
            return;
        }
        Item item = stack.getItem();
        if (!(item instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof AbstractSkullBlock)) {
            return;
        }
        if (!(item instanceof Equipable equipable) || equipable.getEquipmentSlot() != EquipmentSlot.HEAD) {
            return;
        }
        cir.setReturnValue(LootCategory.HELMET);
    }
}
