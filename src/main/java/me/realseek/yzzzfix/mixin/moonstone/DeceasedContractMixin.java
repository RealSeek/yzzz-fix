package me.realseek.yzzzfix.mixin.moonstone;

import com.moonstone.moonstonemod.init.Items;
import com.moonstone.moonstonemod.item.decorated.deceased_contract;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复死灵契约生成血球追踪实体的问题。
 *
 * <p>原逻辑在击杀触发时有概率生成 {@code blood} 实体追踪玩家，玩家背包满或实体追踪失败时
 * 容易出现吞物品和额外实体开销。这里只替换血球实体的生成方式，改为直接在触发玩家脚下
 * 生成血球物品实体；死灵契约原本的概率、冷却、僵尸和巨人召唤逻辑保持不变。</p>
 */
@Mixin(value = deceased_contract.class, remap = false)
public abstract class DeceasedContractMixin {

    @Redirect(
            method = "Did",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z", ordinal = 2),
            remap = false
    )
    private static boolean yzzzfix$spawnBloodItemAtPlayer(Level level, Entity entity) {
        if (entity instanceof Projectile projectile && projectile.getOwner() instanceof Player player) {
            ItemEntity itemEntity = new ItemEntity(
                    player.level(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    new ItemStack(Items.blood.get())
            );
            itemEntity.setPickUpDelay(0);
            return player.level().addFreshEntity(itemEntity);
        }
        return level.addFreshEntity(entity);
    }
}
