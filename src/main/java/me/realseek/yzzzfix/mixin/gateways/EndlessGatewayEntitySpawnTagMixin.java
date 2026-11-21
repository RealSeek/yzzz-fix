package me.realseek.yzzzfix.mixin.gateways;

import dev.shadowsoffire.gateways.entity.EndlessGatewayEntity;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import me.realseek.yzzzfix.module.gateways.GatewayTrackingService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/** 注册由无尽波次修饰符追加的额外实体。 */
@Pseudo
@Mixin(value = EndlessGatewayEntity.class, remap = false)
public abstract class EndlessGatewayEntitySpawnTagMixin {

    @Inject(method = "startNextWave", at = @At("RETURN"), require = 1, remap = false)
    private void yzzzfix$trackEndlessModifierEntities(CallbackInfo ci) {
        GatewayEntity gateway = (GatewayEntity)(Object)this;
        if (gateway.isRemoved() || !(gateway.level() instanceof ServerLevel level)) {
            return;
        }

        List<LivingEntity> entities = new ArrayList<>(
            ((GatewayEntityAccessor)gateway).getCurrentWaveEntities()
        );
        int wave = gateway.getWave();
        for (LivingEntity entity : entities) {
            if (entity != null && entity.isAlive()) {
                GatewayTrackingService.track(level, gateway.getUUID(), wave, entity);
            }
        }
    }
}
