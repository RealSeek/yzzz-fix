package me.realseek.yzzzfix.mixin.cy3_core;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.heike233.chapterofyuusha3.api.mixin.touhoulittlemaid.MaidAbilityHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * 修复女仆实体生命值缓存未随 {@code setHealth} 同步的问题。
 *
 * <p>CY3 模组通过 {@code MaidAbilityHelper} 维护了一份生命值缓存。
 * 当 {@code setHealth} 被调用后，该 Mixin 在服务端同步更新缓存值，
 * 避免后续逻辑使用过期的生命值数据。</p>
 */
@Mixin(value = EntityMaid.class, priority = 1500)
public abstract class EntityMaidMixin extends TamableAnimal {

    protected EntityMaidMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "m_21153_", at = @At("RETURN"), remap = false)
    private void yzzzFix$syncCacheAfterSetHealth(float health, CallbackInfo ci) {
        if (this.level().isClientSide()) {
            return;
        }
        float actualHealth = this.getHealth();
        ((MaidAbilityHelper) this).cy3$setHealth(actualHealth);
    }

    @Inject(method = "m_6153_", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzFix$triggerDieOnAttributeDeath(CallbackInfo ci) {
        if (this.level().isClientSide()) {
            return;
        }

        float health = this.getHealth();
        MaidAbilityHelper helper = (MaidAbilityHelper) this;
        int invulTick = helper.cy3$getInvulTick();

        if (health <= 0 && invulTick <= 0 && this.deathTime == 0) {
            helper.cy3$setHealth(0.0F);
            this.die(this.damageSources().generic());

            float revivedHealth = this.getHealth();
            if (revivedHealth > 0 || helper.cy3$getInvulTick() > 0) {
                this.dead = false;
                this.deathTime = 0;
                ci.cancel();
            }
        }
    }
}
