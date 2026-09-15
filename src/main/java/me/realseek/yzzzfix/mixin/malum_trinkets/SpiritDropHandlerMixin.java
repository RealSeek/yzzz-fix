package me.realseek.yzzzfix.mixin.malum_trinkets;

import com.github.taczmalum.handler.SpiritDropHandler;
import me.realseek.yzzzfix.module.malum.SpiritItemDropService;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

/**
 * 将 Malum Trinkets 额外生成的追踪精魄转换为普通精魄掉落物。
 */
@Mixin(value = SpiritDropHandler.class, remap = false)
public abstract class SpiritDropHandlerMixin {

    @Redirect(
            method = "onLivingDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/sammy/malum/core/handlers/SpiritHarvestHandler;createSpiritEntities(Lnet/minecraft/world/level/Level;Ljava/util/Collection;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/LivingEntity;)V"
            ),
            remap = false
    )
    private static void yzzzfix$dropItems(Level level, Collection<ItemStack> spirits,
                                          Vec3 position, LivingEntity attacker) {
        SpiritItemDropService.dropItems(level, spirits, position, attacker);
    }
}
