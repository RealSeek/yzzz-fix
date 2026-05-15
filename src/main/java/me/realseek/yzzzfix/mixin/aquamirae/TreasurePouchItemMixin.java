package me.realseek.yzzzfix.mixin.aquamirae;

import com.obscuria.aquamirae.Aquamirae;
import com.obscuria.aquamirae.common.items.TreasurePouchItem;
import com.obscuria.aquamirae.registry.AquamiraeSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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

@Mixin(TreasurePouchItem.class)
public abstract class TreasurePouchItemMixin extends Item {

    public TreasurePouchItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "m_7203_", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUse(Level world, Player player, InteractionHand hand,
                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);

        if (world.isClientSide) {
            player.swing(hand);
            cir.setReturnValue(InteractionResultHolder.pass(stack));
            return;
        }

        ServerLevel serverLevel = (ServerLevel) world;
        serverLevel.playSound(null, player.blockPosition().above(),
                AquamiraeSounds.ITEM_TREASURE_POUCH_OPEN.get(), SoundSource.PLAYERS, 1, 1);

        final List<ItemStack> loot = Aquamirae.SetBuilder.rare();
        if (!loot.isEmpty()) {
            ItemStack reward = loot.get(player.getRandom().nextInt(loot.size())).copy();
            giveItemOrDrop(player, reward);
        }

        final MinecraftServer minecraftServer = player.level().getServer();
        if (minecraftServer != null && player.level() instanceof ServerLevel server) {
            LootParams lootContext = new LootParams.Builder(server)
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .create(LootContextParamSets.GIFT);
            LootTable treasure = minecraftServer.getLootData()
                    .getLootTable(new ResourceLocation(Aquamirae.MODID, "gameplay/treasure_pouch"));
            List<ItemStack> tableLoot = treasure.getRandomItems(lootContext);
            for (ItemStack item : tableLoot) {
                giveItemOrDrop(player, item.copy());
            }
        }

        if (player.getRandom().nextFloat() <= 0.1f) {
            ItemStack map = Aquamirae.getStructureMap(
                    player.getRandom().nextBoolean() ? Aquamirae.SHIP : Aquamirae.OUTPOST,
                    serverLevel, player);
            giveItemOrDrop(player, map);
        }

        stack.shrink(1);
        cir.setReturnValue(InteractionResultHolder.success(stack));
    }

    private static void giveItemOrDrop(Player player, ItemStack item) {
        if (item.isEmpty()) return;
        player.getInventory().add(item);
        if (!item.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(player.level(),
                    player.getX(), player.getEyeY() - 0.3, player.getZ(), item);
            itemEntity.setPickUpDelay(0);
            player.level().addFreshEntity(itemEntity);
        }
    }
}