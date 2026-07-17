package me.realseek.yzzzfix.mixin.enigmaticaddons;

import me.realseek.yzzzfix.module.enigmaticaddons.CelestialArtifactsCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复精灵手环蓄力加速效果导致魔法石英权杖无法正常发射匕首的问题。
 */
@Mixin(targets = "com.xiaoyue.celestial_artifacts.events.CAMiscCuriosHandler", remap = false)
public abstract class CelestialArtifactsUseItemMixin {

    private static final ResourceLocation QUARTZ_SCEPTER = new ResourceLocation("enigmaticaddons", "quartz_scepter");

    @Redirect(
            method = "onStarItemUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingEntityUseItemEvent$Tick;setDuration(I)V"),
            remap = false,
            require = 0
    )
    private static void yzzzfix$skipQuartzScepterUseDurationAcceleration(LivingEntityUseItemEvent.Tick event, int duration) {
        if (yzzzfix$shouldKeepQuartzScepterDuration(event)) {
            return;
        }

        event.setDuration(duration);
    }

    private static boolean yzzzfix$shouldKeepQuartzScepterDuration(LivingEntityUseItemEvent.Tick event) {
        ItemStack stack = event.getItem();
        return QUARTZ_SCEPTER.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))
                && CelestialArtifactsCompat.hasSpiritBracelet(event.getEntity());
    }
}
