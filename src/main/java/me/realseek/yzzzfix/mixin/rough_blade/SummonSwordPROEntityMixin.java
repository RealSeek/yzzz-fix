package me.realseek.yzzzfix.mixin.rough_blade;

import net.exmo.rough_blade.entity.SummonSwordPROEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 SummonSwordPRO 实体在 {@code faceEntityStandby} 中访问载具时可能出现空指针异常的问题。
 *
 * <p>当召唤剑实体没有载具或载具已失效时，该 Mixin 跳过相关逻辑，避免崩溃。</p>
 */
@Mixin(SummonSwordPROEntity.class)
public abstract class SummonSwordPROEntityMixin extends Entity {

    protected SummonSwordPROEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "faceEntityStandby", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzfix$nullCheckVehicle(CallbackInfo ci) {
        if (this.getVehicle() == null) {
            ci.cancel();
        }
    }
}
