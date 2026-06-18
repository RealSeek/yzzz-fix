package me.realseek.yzzzfix.mixin.moonstone;

import com.moonstone.moonstonemod.item.blood.the_prison_of_sin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.SlotContext;

/**
 * 修复罪孽囚笼每 tick 执行伤害递增导致的性能问题。
 * <p>原逻辑每 tick 调用 {@code setDamageValue}，改为每 20 tick（1秒）递增一次。</p>
 */
@Mixin(value = the_prison_of_sin.class, remap = false)
public class ThePrisonOfSinMixin {

    @Inject(method = "curioTick", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$throttleCurioTick(SlotContext slotContext, ItemStack stack, CallbackInfo ci) {
        if (stack.getTag() == null) {
            stack.setTag(new CompoundTag());
        }

        if (slotContext.entity().tickCount % 20 == 0) {
            stack.setDamageValue(stack.getDamageValue() + 1);
        }

        ci.cancel();
    }
}
