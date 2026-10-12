package me.realseek.yzzzfix.mixin.gateways;

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

/** 在服务器范围的追踪数据中注册每一个生成的波次实体。 */
@Pseudo
@Mixin(value = GatewayEntity.class, remap = false)
public abstract class GatewayEntitySpawnTagMixin {

    @Inject(method = "startNextWave", at = @At("RETURN"), require = 1, remap = false)
    private void yzzzfix$trackSpawnedEntities(CallbackInfo ci) {
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
