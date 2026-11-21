package me.realseek.yzzzfix.mixin.gateways;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import me.realseek.yzzzfix.module.gateways.GatewayTrackingService;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;

/** 使用按波次持久化的令牌，同时保持原有的完成流程不变。 */
@Pseudo
@Mixin(value = GatewayEntity.class, remap = false)
public abstract class GatewayEntityWaveCompletionMixin extends Entity {

    @Unique
    private static final Logger YZZZFIX_LOGGER = LogManager.getLogger("YzzzFix/Gateways");

    public GatewayEntityWaveCompletionMixin() {
        super(null, null);
    }

    @Shadow @Final
    public static EntityDataAccessor<Integer> ENEMIES;

    @Shadow
    protected abstract int getWave();

    @Inject(
        method = "m_8119_",
        at = @At(
            value = "INVOKE",
            target = "Ldev/shadowsoffire/gateways/entity/GatewayEntity;isWaveActive()Z",
            ordinal = 1
        ),
        require = 1,
        remap = true
    )
    private void yzzzfix$syncDisplayedEnemyCount(CallbackInfo ci) {
        GatewayEntity gateway = (GatewayEntity)(Object)this;
        if (gateway.level() instanceof ServerLevel level) {
            this.getEntityData().set(
                ENEMIES,
                GatewayTrackingService.getRemaining(level, gateway.getUUID(), this.getWave())
            );
        }
    }

    @Redirect(
        method = "m_8119_",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;isEmpty()Z",
            remap = false
        ),
        require = 1,
        remap = true
    )
    private boolean yzzzfix$isCurrentWaveComplete(List<?> enemies) {
        GatewayEntity gateway = (GatewayEntity)(Object)this;
        return gateway.level() instanceof ServerLevel level
            && GatewayTrackingService.getRemaining(level, gateway.getUUID(), this.getWave()) == 0;
    }

    @Inject(
        method = "m_8119_",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Set;clear()V",
            ordinal = 1,
            shift = At.Shift.AFTER,
            remap = false
        ),
        require = 1,
        remap = true
    )
    private void yzzzfix$removeCompletedWave(CallbackInfo ci) {
        GatewayEntity gateway = (GatewayEntity)(Object)this;
        if (gateway.level() instanceof ServerLevel level) {
            GatewayTrackingService.removeWave(level, gateway.getUUID(), this.getWave());
        }
    }

    @Inject(method = "onFailure", at = @At("HEAD"), require = 1, remap = false)
    private void yzzzfix$recordFailureCleanup(
            Collection<LivingEntity> entities,
            GatewayEntity.FailureReason reason,
            CallbackInfo ci
    ) {
        GatewayEntity gateway = (GatewayEntity)(Object)this;
        if (gateway.level() instanceof ServerLevel level) {
            Boolean removeOnFailure = yzzzfix$getRemoveOnFailure(gateway);
            if (removeOnFailure == null) {
                YZZZFIX_LOGGER.error(
                    "Unable to read removeOnFailure for gateway {}; unloaded wave entities cannot be scheduled for cleanup.",
                    gateway.getUUID()
                );
                return;
            }
            GatewayTrackingService.handleFailure(
                level,
                gateway.getUUID(),
                removeOnFailure,
                entities
            );
        }
    }

    @Unique
    private static Boolean yzzzfix$getRemoveOnFailure(GatewayEntity gateway) {
        try {
            Object gatewayDefinition = gateway.getClass().getMethod("getGateway").invoke(gateway);
            Object rules = gatewayDefinition.getClass().getMethod("rules").invoke(gatewayDefinition);
            return (Boolean) rules.getClass().getMethod("removeOnFailure").invoke(rules);
        } catch (ReflectiveOperationException | ClassCastException exception) {
            YZZZFIX_LOGGER.error("Failed to inspect Gateway failure rules.", exception);
            return null;
        }
    }
}
