package me.realseek.yzzzfix.mixin.gateways;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 阻止 GatewayEntity 在波次实体所在区块未加载时误判波次完成。
 */
@Mixin(GatewayEntity.class)
public class GatewayEntityMixin {

    private static final String ALIVE_UUIDS_KEY = "yzzz_active_wave_uuids";

    @Unique
    private final Set<UUID> yzzz$pendingUuids = new HashSet<>();

    @Final
    @Shadow(remap = false)
    Set<LivingEntity> currentWaveEntities;

    @Final
    @Shadow(remap = false)
    Set<UUID> unresolvedWaveEntities;

    @Inject(method = "m_8119_", at = @At(value = "INVOKE", target = "Ldev/shadowsoffire/gateways/entity/GatewayEntity;completeWave()V"), cancellable = true, remap = false)
    private void yzzz$preventUnloadedEntitiesAdvancingWave(CallbackInfo ci) {
        yzzz$refreshTrackedEntities();
        if (!yzzz$pendingUuids.isEmpty()) {
            unresolvedWaveEntities.addAll(yzzz$pendingUuids);
        }
        if (!unresolvedWaveEntities.isEmpty() || !yzzz$pendingUuids.isEmpty()) {
            ci.cancel();
        }
    }

    @Inject(method = "m_8119_", at = @At("RETURN"), remap = false)
    private void yzzz$cleanupPendingUuids(CallbackInfo ci) {
        yzzz$refreshTrackedEntities();
    }

    @Inject(method = "m_7380_", at = @At("RETURN"), remap = false)
    private void yzzz$saveActiveWaveUuids(net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
        yzzz$refreshTrackedEntities();
        Set<UUID> tracked = new HashSet<>(yzzz$pendingUuids);
        tracked.addAll(unresolvedWaveEntities);
        for (LivingEntity entity : currentWaveEntities) {
            if (entity.deathTime == 0) {
                tracked.add(entity.getUUID());
            }
        }

        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (UUID uuid : tracked) {
            net.minecraft.nbt.CompoundTag entry = new net.minecraft.nbt.CompoundTag();
            entry.putUUID("uuid", uuid);
            list.add(entry);
        }
        if (!list.isEmpty()) {
            tag.put(ALIVE_UUIDS_KEY, list);
        }
    }

    @Inject(method = "m_7378_", at = @At("RETURN"), remap = false)
    private void yzzz$loadActiveWaveUuids(net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
        yzzz$pendingUuids.clear();
        if (tag.contains(ALIVE_UUIDS_KEY)) {
            net.minecraft.nbt.ListTag list = tag.getList(ALIVE_UUIDS_KEY, 10);
            for (int i = 0; i < list.size(); i++) {
                net.minecraft.nbt.CompoundTag entry = list.getCompound(i);
                UUID uuid = entry.getUUID("uuid");
                unresolvedWaveEntities.add(uuid);
                yzzz$pendingUuids.add(uuid);
            }
        }
    }

    @Inject(method = "onFailure", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzz$preventFailureOnNonDeadEntities(Collection<LivingEntity> entities, GatewayEntity.FailureReason reason, CallbackInfo ci) {
        yzzz$refreshTrackedEntities();
        if (!yzzz$pendingUuids.isEmpty() || !unresolvedWaveEntities.isEmpty()) {
            unresolvedWaveEntities.addAll(yzzz$pendingUuids);
            ci.cancel();
        }
    }

    @Unique
    private void yzzz$refreshTrackedEntities() {
        for (LivingEntity entity : currentWaveEntities) {
            UUID uuid = entity.getUUID();
            if (entity.deathTime > 0) {
                yzzz$pendingUuids.remove(uuid);
                unresolvedWaveEntities.remove(uuid);
            } else if (entity.isAlive()) {
                yzzz$pendingUuids.remove(uuid);
            } else {
                yzzz$pendingUuids.add(uuid);
                unresolvedWaveEntities.add(uuid);
            }
        }
    }
}
