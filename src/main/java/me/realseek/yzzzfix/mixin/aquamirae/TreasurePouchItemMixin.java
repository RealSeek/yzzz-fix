package me.realseek.yzzzfix.mixin.aquamirae;

import com.obscuria.aquamirae.Aquamirae;
import com.obscuria.aquamirae.registry.AquamiraeSounds;
import me.realseek.yzzzfix.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 修复海灵物语稀有宝藏袋（Treasure Pouch）的多个物品给予问题。
 *
 * <p>原版 {@code TreasurePouchItem#use} 方法存在以下缺陷：
 * <ul>
 *   <li>随机选取奖池物品时存在 off-by-one 错误，最后一个物品永远不会被选中</li>
 *   <li>背包已满时物品直接丢失，未做溢出掉落处理</li>
 *   <li>战利品表（Loot Table）生成的物品未正确给予玩家</li>
 * </ul>
 * </p>
 *
 * <p>本 Mixin 完整替换使用逻辑，修复上述所有问题：
 * <ul>
 *   <li>使用 {@code nextInt(size)} 确保奖池全覆盖</li>
 *   <li>通过 {@link InventoryUtil#giveOrDrop} 安全给予所有物品</li>
 *   <li>正确遍历战利品表结果并逐一给予</li>
 *   <li>保留 10% 概率给予结构地图的原版行为</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "com.obscuria.aquamirae.common.items.TreasurePouchItem", remap = false)
public abstract class TreasurePouchItemMixin {

    @Inject(method = "m_7203_", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$onUse(Level level, Player player, InteractionHand hand,
                               CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        player.swing(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.blockPosition().above(),
                    (SoundEvent) AquamiraeSounds.ITEM_TREASURE_POUCH_OPEN.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);

            List<ItemStack> items = Aquamirae.SetBuilder.rare();
            if (!items.isEmpty()) {
                ItemStack reward = items.get(player.getRandom().nextInt(items.size())).copy();
                InventoryUtil.giveOrDrop(player, reward);
            }

            MinecraftServer server = serverLevel.getServer();
            if (server != null) {
                LootParams lootParams = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withParameter(LootContextParams.ORIGIN, player.position())
                        .create(LootContextParamSets.GIFT);
                LootTable lootTable = server.getLootData()
                        .getLootTable(new ResourceLocation(Aquamirae.MODID, "gameplay/treasure_pouch"));
                lootTable.getRandomItems(lootParams)
                        .forEach(item -> InventoryUtil.giveOrDrop(player, item));
            }

            if (player.getRandom().nextFloat() <= 0.1F) {
                ItemStack map = Aquamirae.getStructureMap(
                        player.getRandom().nextBoolean() ? Aquamirae.SHIP : Aquamirae.OUTPOST,
                        serverLevel, player);
                InventoryUtil.giveOrDrop(player, map);
            }
        }

        stack.shrink(1);
        cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide()));
    }
}
