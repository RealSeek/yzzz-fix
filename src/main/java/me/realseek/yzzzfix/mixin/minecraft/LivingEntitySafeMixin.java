package me.realseek.yzzzfix.mixin.minecraft;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Map;

/**
 * 彻底移除 LivingEntity.collectEquipmentChanges() 的执行，
 * 避免因其他模组 Mixin 冲突导致的 ConcurrentModificationException 崩溃。
 * <p>该方法仅用于立即同步装备变化，取消后装备状态仍会通过正常网络同步更新。</p>
 */
@Mixin(value = LivingEntity.class, remap = false)
public abstract class LivingEntitySafeMixin {

    @Inject(method = "m_21319_", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$cancelCollectEquipmentChanges(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        cir.setReturnValue(Collections.emptyMap());
    }
}