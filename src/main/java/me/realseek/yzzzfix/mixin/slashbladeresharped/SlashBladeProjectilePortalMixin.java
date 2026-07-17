package me.realseek.yzzzfix.mixin.slashbladeresharped;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 阻止拔刀剑投射物进入传送门，避免异常的 NBT 同步错误崩溃。
 */
@Mixin(Entity.class)
public abstract class SlashBladeProjectilePortalMixin {

    private static final String SLASHBLADE_PROJECTILE_CLASS = "mods.flammpfeil.slashblade.entity.Projectile";

    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$discardSlashBladeProjectileBeforeDimensionChange(ServerLevel destination, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir) {
        Entity entity = (Entity) (Object) this;
        if (!yzzzfix$isSlashBladeProjectile(entity)) {
            return;
        }

        entity.discard();
        cir.setReturnValue(null);
    }

    private static boolean yzzzfix$isSlashBladeProjectile(Entity entity) {
        Class<?> type = entity.getClass();
        while (type != null) {
            if (SLASHBLADE_PROJECTILE_CLASS.equals(type.getName())) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }
}
