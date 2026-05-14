package me.realseek.yzzzfix.module.apotheosis_spawner;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 神化刷怪笼 ESP 高亮追踪器。
 *
 * <p>维护一个弱引用集合，记录所有带有红石控制模块的 {@code ApothSpawnerTile} 实例。
 * 当方块实体被 GC 回收后会自动从集合中移除，无需手动清理。</p>
 *
 * <p>提供线程安全的注册/注销接口，以及用于渲染线程的快照获取方法
 * {@link #getSpawnersToRender(Level)}，避免遍历时的并发修改异常。</p>
 */
public final class SpawnerESPTracker {

    private static final Set<BlockEntity> SPAWNERS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private SpawnerESPTracker() {
    }

    public static void register(BlockEntity blockEntity) {
        if (blockEntity != null) {
            SPAWNERS.add(blockEntity);
        }
    }

    public static void unregister(BlockEntity blockEntity) {
        if (blockEntity != null) {
            SPAWNERS.remove(blockEntity);
        }
    }

    public static Collection<BlockEntity> getSpawnersToRender(Level currentLevel) {
        if (currentLevel == null) {
            return Collections.emptyList();
        }

        synchronized (SPAWNERS) {
            if (SPAWNERS.isEmpty()) {
                return Collections.emptyList();
            }

            SPAWNERS.removeIf(be -> be == null || be.isRemoved() || be.getLevel() == null);

            Collection<BlockEntity> valid = new ArrayList<>();
            for (BlockEntity be : SPAWNERS) {
                if (be.getLevel() == currentLevel) {
                    valid.add(be);
                }
            }
            return valid;
        }
    }
}
