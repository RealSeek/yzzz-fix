package me.realseek.yzzzfix.mixin.ritual_manager_npe;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.RitualManager;

/**
 * 修复 Forbidden Arcanus 的 RitualManager 在客户端保存时可能出现空指针异常的问题。
 *
 * <p>当 {@code ServerLifecycleHooks.getCurrentServer()} 返回 null 时（即客户端环境），
 * 该 Mixin 阻止保存逻辑执行，确保仪式数据保存仅在服务端有效上下文中进行。</p>
 */
@Mixin(value = RitualManager.class, priority = 500)
public class RitualManagerSaveFixMixin {

    @Inject(method = "save", at = @At("HEAD"), cancellable = true, remap = false)
    private void yzzzfix$preventClientNpe(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            cir.setReturnValue(tag);
        }
    }
}
