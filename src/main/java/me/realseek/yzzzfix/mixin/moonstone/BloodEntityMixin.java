package me.realseek.yzzzfix.mixin.moonstone;

import com.moonstone.moonstonemod.entity.blood;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复血球实体（blood）碰到佩戴死灵契约玩家时误用 setHealth 的问题。
 * <p>原代码会错误地将玩家生命值设为 15%，本 Mixin 将其改为正确的 heal 恢复。</p>
 */
@Mixin(value = blood.class, remap = false)
public abstract class BloodEntityMixin {

    @Redirect(method = "m_6123_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_5634_(F)V"),
            remap = false)
    private void yzzzfix$redirectSetHealthToHeal(LivingEntity entity, float health) {
        entity.heal(entity.getMaxHealth() * 0.15f);
    }
}