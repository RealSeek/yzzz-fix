package me.realseek.yzzzfix.mixin.arkane_domains;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Arkane Domains 模组的 UniccProcedure 在方块放置时将红石矿石替换为结构空位的问题。
 *
 * <p>该 MCreator 模组监听 {@code BlockEvent.EntityPlaceEvent}，当检测到放置的方块为红石矿石时，
 * 会将该位置及其上方 9 格全部替换为 {@code minecraft:structure_void}，导致玩家无法正常放置红石矿石。</p>
 *
 * <p>修复方式：在内部 {@code execute} 方法入口处直接取消执行，
 * 阻止该过程对红石矿石的错误替换行为。注入 execute 而非 onBlockPlace 以避免引用 Forge 事件类导致类加载冲突。</p>
 */
@Mixin(targets = "net.mcreator.arkanedomains.procedures.UniccProcedure", remap = false)
public abstract class UniccProcedureMixin {

    @Inject(method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDD)V", at = @At("HEAD"), cancellable = true)
    private static void yzzzFix$cancelRedstoneOreReplacement(CallbackInfo ci) {
        ci.cancel();
    }
}
