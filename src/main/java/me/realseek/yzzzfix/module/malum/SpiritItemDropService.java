package me.realseek.yzzzfix.module.malum;

import com.sammy.malum.core.handlers.SpiritHarvestHandler;
import com.sammy.malum.registry.common.SoundRegistry;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

import java.util.Collection;
import java.util.UUID;

/**
 * 将 Malum 常规收获精魄转换为可拾取的普通物品实体。
 */
public final class SpiritItemDropService {

    private static final String SPIRIT_DROP_MARKER = "yzzz_fix:malum_spirit_drop";

    private SpiritItemDropService() {
    }

    public static void dropItems(Level level, Collection<ItemStack> spirits, Vec3 position, LivingEntity attacker) {
        if (level.isClientSide || spirits.isEmpty()) {
            return;
        }

        level.playSound(null, position.x, position.y, position.z,
                SoundRegistry.SOUL_SHATTER.get(), SoundSource.PLAYERS,
                1.0F, 0.7F + level.random.nextFloat() * 0.4F);

        Player player = resolvePlayer(attacker, level);
        if (player != null && ModList.get().isLoaded("malstone")) {
            for (ItemStack stack : spirits) {
                if (!stack.isEmpty()) {
                    SpiritHarvestHandler.pickupSpirit(player, stack.copy());
                }
            }
            return;
        }

        Vec3 dropPosition = player != null ? player.position() : position;

        for (ItemStack stack : spirits) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemEntity item = new ItemEntity(level,
                    dropPosition.x, dropPosition.y, dropPosition.z, stack.copy());
            item.getPersistentData().putBoolean(SPIRIT_DROP_MARKER, true);
            item.setPickUpDelay(0);
            level.addFreshEntity(item);
        }
    }

    public static boolean isSpiritDrop(ItemEntity item) {
        return item.getPersistentData().getBoolean(SPIRIT_DROP_MARKER);
    }

    private static Player resolvePlayer(LivingEntity attacker, Level level) {
        if (attacker instanceof Player player) {
            return player;
        }
        if (attacker instanceof TamableAnimal tamable && tamable.getOwner() instanceof Player player) {
            return player;
        }
        if (attacker instanceof OwnableEntity ownable) {
            UUID ownerUuid = ownable.getOwnerUUID();
            if (ownerUuid != null) {
                return level.getPlayerByUUID(ownerUuid);
            }
        }
        return null;
    }
}
