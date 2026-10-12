package me.realseek.yzzzfix.module.gateways;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import me.realseek.yzzzfix.mixin.gateways.GatewayEntityAccessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 服务器范围的 Gateway 波次实体持久化追踪。 */
public final class GatewayTrackingService {

    public static final String GATEWAY_ID = "yzzzfix_gateway_id";
    public static final String WAVE = "yzzzfix_gateway_wave";
    public static final String TOKEN = "yzzzfix_gateway_token";
    public static final String REPLACED = "yzzzfix_gateway_replaced";

    private static final String DATA_NAME = "yzzzfix_gateway_tracking";

    private GatewayTrackingService() {
    }

    public static void track(ServerLevel level, UUID gatewayId, int wave, LivingEntity entity) {
        CompoundTag entityData = entity.getPersistentData();
        Data data = get(level);
        UUID token;
        if (entityData.hasUUID(GATEWAY_ID)
            && gatewayId.equals(entityData.getUUID(GATEWAY_ID))
            && entityData.getInt(WAVE) == wave
            && entityData.hasUUID(TOKEN)) {
            token = entityData.getUUID(TOKEN);
        } else {
            if (hasTrackingTags(entityData)
                && data.isTracked(
                    entityData.getUUID(GATEWAY_ID),
                    entityData.getInt(WAVE),
                    entityData.getUUID(TOKEN)
                )) {
                return;
            }
            token = UUID.randomUUID();
        }

        entityData.putUUID(GATEWAY_ID, gatewayId);
        entityData.putInt(WAVE, wave);
        entityData.putUUID(TOKEN, token);

        data.track(gatewayId, wave, token, entity.getUUID(), level.dimension().location().toString());
    }

    public static boolean consume(ServerLevel level, UUID gatewayId, int wave, UUID token) {
        return get(level).consume(gatewayId, wave, token);
    }

    public static int getRemaining(ServerLevel level, UUID gatewayId, int wave) {
        return get(level).getRemaining(gatewayId, wave);
    }

    public static void removeWave(ServerLevel level, UUID gatewayId, int wave) {
        get(level).removeWave(gatewayId, wave);
    }

    public static void transfer(
            ServerLevel level,
            LivingEntity original,
            LivingEntity replacement
    ) {
        CompoundTag originalData = original.getPersistentData();
        if (!hasTrackingTags(originalData)) {
            return;
        }

        UUID gatewayId = originalData.getUUID(GATEWAY_ID);
        int wave = originalData.getInt(WAVE);
        UUID token = originalData.getUUID(TOKEN);

        CompoundTag replacementData = replacement.getPersistentData();
        replacementData.putUUID(GATEWAY_ID, gatewayId);
        replacementData.putInt(WAVE, wave);
        replacementData.putUUID(TOKEN, token);
        replacementData.remove(REPLACED);
        originalData.putBoolean(REPLACED, true);

        get(level).updateEntity(
            gatewayId,
            wave,
            token,
            replacement.getUUID(),
            replacement.level().dimension().location().toString()
        );
    }

    /** Returns true when a joining entity belongs to a failed gate and must be removed. */
    public static boolean handleJoin(ServerLevel level, LivingEntity entity) {
        CompoundTag entityData = entity.getPersistentData();
        if (!hasTrackingTags(entityData)) {
            return false;
        }

        UUID gatewayId = entityData.getUUID(GATEWAY_ID);
        int wave = entityData.getInt(WAVE);
        UUID token = entityData.getUUID(TOKEN);
        Data data = get(level);

        if (data.isPendingCleanup(gatewayId, wave, token)) {
            data.consume(gatewayId, wave, token);
            return true;
        }

        data.updateEntity(
            gatewayId,
            wave,
            token,
            entity.getUUID(),
            level.dimension().location().toString()
        );
        rebindLoadedGateway(level, gatewayId, entity);
        return false;
    }

    public static void handleFailure(
            ServerLevel level,
            UUID gatewayId,
            boolean removeEntities,
            Collection<LivingEntity> failureEntities
    ) {
        Data data = get(level);
        if (!removeEntities) {
            data.removeGateway(gatewayId);
            return;
        }

        for (LivingEntity entity : failureEntities) {
            CompoundTag entityData = entity.getPersistentData();
            if (entity.getRemovalReason() == Entity.RemovalReason.DISCARDED
                && hasTrackingTags(entityData)
                && gatewayId.equals(entityData.getUUID(GATEWAY_ID))) {
                data.consume(
                    gatewayId,
                    entityData.getInt(WAVE),
                    entityData.getUUID(TOKEN)
                );
            }
        }

        List<CleanupTarget> targets = data.beginCleanup(gatewayId);
        MinecraftServer server = level.getServer();
        for (CleanupTarget target : targets) {
            Entity entity = findLoadedEntity(server, target);
            if (entity == null) {
                continue;
            }

            if (!entity.isRemoved()) {
                entity.discard();
            }
            data.consume(gatewayId, target.wave(), target.token());
        }
    }

    public static boolean hasTrackingTags(CompoundTag tag) {
        return tag.hasUUID(GATEWAY_ID) && tag.contains(WAVE, Tag.TAG_INT) && tag.hasUUID(TOKEN);
    }

    private static Entity findLoadedEntity(MinecraftServer server, CleanupTarget target) {
        ResourceLocation dimensionId = ResourceLocation.tryParse(target.dimension());
        if (dimensionId != null) {
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
            ServerLevel expectedLevel = server.getLevel(dimension);
            if (expectedLevel != null) {
                Entity entity = expectedLevel.getEntity(target.entityId());
                if (entity != null) {
                    return entity;
                }
            }
        }

        for (ServerLevel candidate : server.getAllLevels()) {
            Entity entity = candidate.getEntity(target.entityId());
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    private static void rebindLoadedGateway(ServerLevel entityLevel, UUID gatewayId, LivingEntity entity) {
        for (ServerLevel candidate : entityLevel.getServer().getAllLevels()) {
            Entity loaded = candidate.getEntity(gatewayId);
            if (!(loaded instanceof GatewayEntity gateway) || gateway.level() != entityLevel) {
                continue;
            }

            var currentEntities = ((GatewayEntityAccessor)gateway).getCurrentWaveEntities();
            currentEntities.removeIf(previous ->
                previous != entity && previous.getUUID().equals(entity.getUUID())
            );
            currentEntities.add(entity);
            return;
        }
    }

    private static Data get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
            Data::load,
            Data::new,
            DATA_NAME
        );
    }

    private record CleanupTarget(int wave, UUID token, UUID entityId, String dimension) {
    }

    private static final class Data extends SavedData {

        private final Map<UUID, GatewayRecord> gateways = new HashMap<>();

        private static Data load(CompoundTag root) {
            Data data = new Data();
            ListTag gatewaysTag = root.getList("gateways", Tag.TAG_COMPOUND);
            for (int i = 0; i < gatewaysTag.size(); i++) {
                CompoundTag gatewayTag = gatewaysTag.getCompound(i);
                if (!gatewayTag.hasUUID("id")) {
                    continue;
                }

                GatewayRecord gateway = new GatewayRecord();
                gateway.pendingCleanup = gatewayTag.getBoolean("pending_cleanup");
                ListTag wavesTag = gatewayTag.getList("waves", Tag.TAG_COMPOUND);
                for (int waveIndex = 0; waveIndex < wavesTag.size(); waveIndex++) {
                    CompoundTag waveTag = wavesTag.getCompound(waveIndex);
                    int waveNumber = waveTag.getInt("wave");
                    WaveRecord wave = new WaveRecord();
                    ListTag entitiesTag = waveTag.getList("entities", Tag.TAG_COMPOUND);
                    for (int entityIndex = 0; entityIndex < entitiesTag.size(); entityIndex++) {
                        CompoundTag entityTag = entitiesTag.getCompound(entityIndex);
                        if (!entityTag.hasUUID("token") || !entityTag.hasUUID("entity")) {
                            continue;
                        }
                        wave.entities.put(
                            entityTag.getUUID("token"),
                            new TrackedEntity(entityTag.getUUID("entity"), entityTag.getString("dimension"))
                        );
                    }
                    if (!wave.entities.isEmpty()) {
                        gateway.waves.put(waveNumber, wave);
                    }
                }
                if (!gateway.waves.isEmpty()) {
                    data.gateways.put(gatewayTag.getUUID("id"), gateway);
                }
            }
            return data;
        }

        private void track(
                UUID gatewayId,
                int waveNumber,
                UUID token,
                UUID entityId,
                String dimension
        ) {
            GatewayRecord gateway = gateways.computeIfAbsent(gatewayId, ignored -> new GatewayRecord());
            if (gateway.pendingCleanup) {
                return;
            }

            WaveRecord wave = gateway.waves.computeIfAbsent(waveNumber, ignored -> new WaveRecord());
            TrackedEntity previous = wave.entities.put(token, new TrackedEntity(entityId, dimension));
            if (previous == null || !previous.entityId.equals(entityId) || !previous.dimension.equals(dimension)) {
                setDirty();
            }
        }

        private boolean consume(UUID gatewayId, int waveNumber, UUID token) {
            GatewayRecord gateway = gateways.get(gatewayId);
            if (gateway == null) {
                return false;
            }
            WaveRecord wave = gateway.waves.get(waveNumber);
            if (wave == null || wave.entities.remove(token) == null) {
                return false;
            }

            if (wave.entities.isEmpty()) {
                gateway.waves.remove(waveNumber);
            }
            if (gateway.waves.isEmpty()) {
                gateways.remove(gatewayId);
            }
            setDirty();
            return true;
        }

        private int getRemaining(UUID gatewayId, int waveNumber) {
            GatewayRecord gateway = gateways.get(gatewayId);
            if (gateway == null) {
                return 0;
            }
            WaveRecord wave = gateway.waves.get(waveNumber);
            return wave == null ? 0 : wave.entities.size();
        }

        private boolean isTracked(UUID gatewayId, int waveNumber, UUID token) {
            GatewayRecord gateway = gateways.get(gatewayId);
            if (gateway == null) {
                return false;
            }
            WaveRecord wave = gateway.waves.get(waveNumber);
            return wave != null && wave.entities.containsKey(token);
        }

        private void removeWave(UUID gatewayId, int waveNumber) {
            GatewayRecord gateway = gateways.get(gatewayId);
            if (gateway == null || gateway.waves.remove(waveNumber) == null) {
                return;
            }
            if (gateway.waves.isEmpty()) {
                gateways.remove(gatewayId);
            }
            setDirty();
        }

        private void removeGateway(UUID gatewayId) {
            if (gateways.remove(gatewayId) != null) {
                setDirty();
            }
        }

        private void updateEntity(
                UUID gatewayId,
                int waveNumber,
                UUID token,
                UUID entityId,
                String dimension
        ) {
            GatewayRecord gateway = gateways.get(gatewayId);
            if (gateway == null) {
                return;
            }
            WaveRecord wave = gateway.waves.get(waveNumber);
            if (wave == null || !wave.entities.containsKey(token)) {
                return;
            }

            TrackedEntity updated = new TrackedEntity(entityId, dimension);
            TrackedEntity previous = wave.entities.put(token, updated);
            if (!updated.equals(previous)) {
                setDirty();
            }
        }

        private boolean isPendingCleanup(UUID gatewayId, int waveNumber, UUID token) {
            GatewayRecord gateway = gateways.get(gatewayId);
            if (gateway == null || !gateway.pendingCleanup) {
                return false;
            }
            WaveRecord wave = gateway.waves.get(waveNumber);
            return wave != null && wave.entities.containsKey(token);
        }

        private List<CleanupTarget> beginCleanup(UUID gatewayId) {
            GatewayRecord gateway = gateways.get(gatewayId);
            if (gateway == null) {
                return List.of();
            }

            gateway.pendingCleanup = true;
            setDirty();
            List<CleanupTarget> targets = new ArrayList<>();
            for (Map.Entry<Integer, WaveRecord> waveEntry : gateway.waves.entrySet()) {
                for (Map.Entry<UUID, TrackedEntity> entityEntry : waveEntry.getValue().entities.entrySet()) {
                    TrackedEntity entity = entityEntry.getValue();
                    targets.add(new CleanupTarget(
                        waveEntry.getKey(),
                        entityEntry.getKey(),
                        entity.entityId,
                        entity.dimension
                    ));
                }
            }
            return targets;
        }

        @Override
        public CompoundTag save(CompoundTag root) {
            ListTag gatewaysTag = new ListTag();
            for (Map.Entry<UUID, GatewayRecord> gatewayEntry : gateways.entrySet()) {
                CompoundTag gatewayTag = new CompoundTag();
                gatewayTag.putUUID("id", gatewayEntry.getKey());
                gatewayTag.putBoolean("pending_cleanup", gatewayEntry.getValue().pendingCleanup);

                ListTag wavesTag = new ListTag();
                for (Map.Entry<Integer, WaveRecord> waveEntry : gatewayEntry.getValue().waves.entrySet()) {
                    CompoundTag waveTag = new CompoundTag();
                    waveTag.putInt("wave", waveEntry.getKey());

                    ListTag entitiesTag = new ListTag();
                    for (Map.Entry<UUID, TrackedEntity> entityEntry : waveEntry.getValue().entities.entrySet()) {
                        CompoundTag entityTag = new CompoundTag();
                        entityTag.putUUID("token", entityEntry.getKey());
                        entityTag.putUUID("entity", entityEntry.getValue().entityId);
                        entityTag.putString("dimension", entityEntry.getValue().dimension);
                        entitiesTag.add(entityTag);
                    }
                    waveTag.put("entities", entitiesTag);
                    wavesTag.add(waveTag);
                }
                gatewayTag.put("waves", wavesTag);
                gatewaysTag.add(gatewayTag);
            }
            root.put("gateways", gatewaysTag);
            return root;
        }
    }

    private static final class GatewayRecord {
        private final Map<Integer, WaveRecord> waves = new HashMap<>();
        private boolean pendingCleanup;
    }

    private static final class WaveRecord {
        private final Map<UUID, TrackedEntity> entities = new HashMap<>();
    }

    private record TrackedEntity(UUID entityId, String dimension) {
    }
}
