package me.realseek.yzzzfix.mixin.aquamirae;

import com.obscuria.aquamirae.Aquamirae;
import com.obscuria.aquamirae.common.items.PiratePouchItem;
import com.obscuria.aquamirae.registry.AquamiraeSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PiratePouchItem.class)
public abstract class PiratePouchItemMixin extends Item {

    public PiratePouchItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "m_7203_", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUse(Level world, Player entity, InteractionHand hand,
                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = entity.getItemInHand(hand);

        if (world.isClientSide) {
            entity.swing(hand);
            cir.setReturnValue(InteractionResultHolder.pass(stack));
            return;
        }

        ServerLevel serverLevel = (ServerLevel) world;
        serverLevel.playSound(null, entity.blockPosition().above(),
                AquamiraeSounds.ITEM_POUCH_OPEN.get(), SoundSource.PLAYERS, 1, 1);

        final List<ItemStack> loot = Aquamirae.SetBuilder.common();
        if (!loot.isEmpty()) {
            ItemStack reward = loot.get(entity.getRandom().nextInt(loot.size())).copy();
            giveItemOrDrop(entity, reward);
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