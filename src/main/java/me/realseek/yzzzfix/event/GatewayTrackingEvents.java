package me.realseek.yzzzfix.event;

import me.realseek.yzzzfix.YzzzFixConfig;
import me.realseek.yzzzfix.module.gateways.GatewayTrackingService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "yzzz_fix")
public final class GatewayTrackingEvents {

    private GatewayTrackingEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!isEnabled() || entity.level().isClientSide()) {
            return;
        }

        CompoundTag data = entity.getPersistentData();
        if (data.getBoolean(GatewayTrackingService.REPLACED)
            || !GatewayTrackingService.hasTrackingTags(data)) {
            return;
        }

        UUID gatewayId = data.getUUID(GatewayTrackingService.GATEWAY_ID);
        int wave = data.getInt(GatewayTrackingService.WAVE);
        UUID token = data.getUUID(GatewayTrackingService.TOKEN);
        GatewayTrackingService.consume((ServerLevel) entity.level(), gatewayId, wave, token);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityConverted(LivingConversionEvent.Post event) {
        LivingEntity original = event.getEntity();
        if (!isEnabled() || original.level().isClientSide()) {
            return;
        }

        GatewayTrackingService.transfer(
            (ServerLevel) original.level(),
            original,
            event.getOutcome()
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!isEnabled()
            || event.getLevel().isClientSide()
            || !(event.getEntity() instanceof LivingEntity living)) {
            return;
        }

        if (GatewayTrackingService.handleJoin((ServerLevel) event.getLevel(), living)) {
            living.discard();
            event.setCanceled(true);
        }
    }

    private static boolean isEnabled() {
        return ModList.get().isLoaded("gateways")
            && YzzzFixConfig.isEnabled("gateways_entity_unload_fix");
    }
}
