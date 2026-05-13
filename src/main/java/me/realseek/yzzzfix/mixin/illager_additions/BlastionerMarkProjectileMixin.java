package me.realseek.yzzzfix.mixin.illager_additions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Blastioner Mark 投射物命中非生物实体时的类型转换问题。
 *
 * <p>该 Mixin 在处理命中目标前确认目标是否为 {@code LivingEntity}，
 * 对非生物实体直接跳过相关效果逻辑，避免 ClassCastException。</p>
 */
@Mixin(targets = "com.pikachu.mod.illager_more.entities.projectile.BlastionerMarkProjectile")
public class BlastionerMarkProjectileMixin {

    @Inject(method = "m_5790_", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzfix$skipNonLivingEntity(EntityHitResult result, CallbackInfo ci) {
        if (!(result.getEntity() instanceof LivingEntity)) {
            ci.cancel();
        }
    }
}
