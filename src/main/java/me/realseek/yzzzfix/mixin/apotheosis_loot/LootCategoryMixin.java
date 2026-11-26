package me.realseek.yzzzfix.mixin.apotheosis_loot;

import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让「物品类自身实现了 {@link Equipable} 的骷髅方块物品」重新进入神化（Apotheosis）的战利品类型判定。
 *
 * <p>神化 {@code LootCategory#armorSlot} 会无条件排除两类方块物品：雕刻南瓜，以及
 * {@link AbstractSkullBlock} 的方块物品。对原版骷髅头来说这个排除是有意的（它们是可复制/装饰性的方块，
 * 神化不希望它们进入词缀、重铸和宝石系统）；但突变生物（Mutant Monsters）的「突变骷髅头」是真正的头盔，
 * 它的物品类 {@code ArmorBlockItem} 自己实现了 {@link Equipable} 且装备槽为头盔，也被一并排除了，导致：</p>
 * <ul>
 *   <li>{@code LootCategory.forItem} 返回 {@code NONE} → 不会生成神化词缀；</li>
 *   <li>{@code SocketHelper#getGemsImpl} / {@code AddSocketsRecipe} 看到 {@code NONE}
 *       直接返回空 → 无法获得宝石槽位。</li>
 * </ul>
 *
 * <p>注意区分：原版骷髅头<b>也能</b>戴在头部，那是 {@link Equipable#get(ItemStack)} 读取了
 * {@link AbstractSkullBlock} 方块实现的 {@link Equipable}，物品类本身并没有实现。所以这里的区分条件是
 * 「物品类本身是否实现 {@link Equipable}」：突变骷髅头的物品类实现了，原版骷髅头的没有。</p>
 *
 * <p>本注入在 {@code forItem} 返回 {@code NONE} 之后兜底：只有物品类本身实现 {@link Equipable}
 * 且装备槽位为头盔时才改判为 {@link LootCategory#HELMET}。同时尊重
 * {@link AdventureConfig#TYPE_OVERRIDES}：只要配置里显式声明了该物品（包括显式设为 {@code none}），
 * 就保持配置结果，不做改判，避免重新放开词缀/重铸/打孔。</p>
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
        // 物品类本身实现 Equipable 才是真的可穿戴头盔；原版骷髅头只有方块实现了 Equipable。
        if (!(item instanceof Equipable equipable) || equipable.getEquipmentSlot() != EquipmentSlot.HEAD) {
            return;
        }
        // 显式配置优先：Equipment Type Overrides 里写了的物品（含 "none"）保持配置结果。
        if (yzzzfix$hasTypeOverride(item)) {
            return;
        }
        cir.setReturnValue(LootCategory.HELMET);
    }

    private static boolean yzzzfix$hasTypeOverride(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return id != null && AdventureConfig.TYPE_OVERRIDES.containsKey(id);
    }
}
