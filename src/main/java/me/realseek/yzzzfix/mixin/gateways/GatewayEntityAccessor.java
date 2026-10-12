package me.realseek.yzzzfix.mixin.gateways;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

/** 访问由 GatewayEntity 维护的实时实体集合。 */
@Pseudo
@Mixin(value = GatewayEntity.class, remap = false)
public interface GatewayEntityAccessor {

    @Accessor("currentWaveEntities")
    Set<LivingEntity> getCurrentWaveEntities();
}
