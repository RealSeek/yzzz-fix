package me.realseek.yzzzfix.mixin.sunspirit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复太阳之灵 Boss 技能无冷却导致的平衡性问题。
 * <p>为蒸发、燃烧实体、冰晶检查和火焰仆从召唤分别添加冷却时间，
 * 并将蒸发半径从 9 缩减为 5。</p>
 */
@Mixin(targets = "com.aetherteam.aether.entity.monster.dungeon.boss.SunSpirit", remap = false)
public abstract class SunSpiritMixin {

    @Unique
    private int yzzzfix$tickCount;

    @Unique
    private int yzzzfix$evaporateCooldown;

    @Unique
    private int yzzzfix$burnCooldown;

    @Unique
    private int yzzzfix$iceCrystalCooldown;

    @Unique
    private int yzzzfix$minionSpawnCooldown;

    @ModifyConstant(constant = @Constant(intValue = 9), method = "<init>", remap = false)
    private int yzzzfix$reduceEvaporateRadius(int original) {
        return 5;
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void yzzzfix$initCooldowns(CallbackInfo ci) {
        this.yzzzfix$evaporateCooldown = 0;
        this.yzzzfix$burnCooldown = 5;
        this.yzzzfix$iceCrystalCooldown = 10;
        this.yzzzfix$minionSpawnCooldown = 0;
    }

    @Inject(method = "m_8119_", at = @At("HEAD"), remap = false)
    private void yzzzfix$tick(CallbackInfo ci) {
        this.yzzzfix$tickCount++;
    }

    @Inject(method = "evaporate", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzfix$cooldownEvaporate(CallbackInfo ci) {
        if (this.yzzzfix$evaporateCooldown > 0) {
            this.yzzzfix$evaporateCooldown--;
            ci.cancel();
            return;
        }
        this.yzzzfix$evaporateCooldown = 60;
    }

    @Inject(method = "burnEntities", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzfix$cooldownBurnEntities(CallbackInfo ci) {
        if (this.yzzzfix$burnCooldown > 0) {
            this.yzzzfix$burnCooldown--;
            ci.cancel();
            return;
        }
        this.yzzzfix$burnCooldown = 20;
    }

    @Inject(method = "checkIceCrystals", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzfix$cooldownIceCrystals(CallbackInfo ci) {
        if (this.yzzzfix$iceCrystalCooldown > 0) {
            this.yzzzfix$iceCrystalCooldown--;
            ci.cancel();
            return;
        }
        this.yzzzfix$iceCrystalCooldown = 20;
    }

    @Inject(method = "m_6469_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;m_7967_(Lnet/minecraft/world/entity/Entity;)Z"),
            cancellable = true, remap = false)
    private void yzzzfix$limitFireMinions(CallbackInfoReturnable<Boolean> ci) {
        if (this.yzzzfix$minionSpawnCooldown > 0) {
            ci.cancel();
            return;
        }
        this.yzzzfix$minionSpawnCooldown = 100;
    }
}
