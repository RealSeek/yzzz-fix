package me.realseek.yzzzfix.module.apotheosis_spawner;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

public class SpawnerESPTracker {

    public static final Set<BlockEntity> SPAWNERS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    public static void register(BlockEntity be) {
        if (be != null) {
            SPAWNERS.add(be);
        }
    }
    public static Set<BlockEntity> getSpawnersToRender(Level currentLevel) {
        if (SPAWNERS.isEmpty()) return Collections.emptySet();

        Set<BlockEntity> valid = new HashSet<>();
        synchronized (SPAWNERS) {
            for (BlockEntity be : SPAWNERS) {
                if (be != null && !be.isRemoved() && be.getLevel() == currentLevel) {
                    valid.add(be);
                }
            }
        }
        return valid;
    }
}