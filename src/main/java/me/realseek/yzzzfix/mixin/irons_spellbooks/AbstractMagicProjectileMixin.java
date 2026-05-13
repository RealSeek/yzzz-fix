package me.realseek.yzzzfix.mixin.irons_spellbooks;

import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Iron's Spells 'n Spellbooks 魔法弹射物在未加载区块中进行命中检测时导致服务端卡死的问题。
 *
 * <p>当魔法弹射物（如连锁闪电）执行命中检测时，会通过 {@code ProjectileUtil} 进行射线追踪，
 * 进而调用 {@code ServerChunkCache.getChunkBlocking} 同步加载未加载的区块，阻塞主线程。</p>
 *
 * <p>修复策略：在命中检测前检查当前区块和运动目标区块是否已加载。
 * 当前区块未加载则丢弃实体；目标区块未加载则跳过本 tick 的检测（下一 tick 重试）。</p>
 */
@Mixin(targets = "io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile")
public abstract class AbstractMagicProjectileMixin extends Projectile {

    protected AbstractMagicProjectileMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Inject(method = "handleHitDetection", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzfix$skipHitDetectionIfChunkUnloaded(CallbackInfo ci) {
        Level level = this.level();
        if (level.isClientSide()) {
            return;
        }

        // If entity's own chunk is not loaded, it shouldn't be ticking - discard it
        int curChunkX = SectionPos.blockToSectionCoord(Mth.floor(this.getX()));
        int curChunkZ = SectionPos.blockToSectionCoord(Mth.floor(this.getZ()));
        if (!level.hasChunk(curChunkX, curChunkZ)) {
            this.discard();
            ci.cancel();
            return;
        }

        // Check destination chunk (where the ray trace would end)
        // If unloaded, skip hit detection this tick to avoid synchronous chunk loading
        Vec3 movement = this.getDeltaMovement();
        double endX = this.getX() + movement.x;
        double endZ = this.getZ() + movement.z;
        int endChunkX = SectionPos.blockToSectionCoord(Mth.floor(endX));
        int endChunkZ = SectionPos.blockToSectionCoord(Mth.floor(endZ));
        if (!level.hasChunk(endChunkX, endChunkZ)) {
            ci.cancel();
        }
    }
}
