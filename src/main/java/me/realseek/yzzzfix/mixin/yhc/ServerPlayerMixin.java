package me.realseek.yzzzfix.mixin.yhc;

import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复灵梦发饰（youkaishomecoming:reimu_hairband）跨维度传送后飞行状态不同步的问题。
 *
 * <p>根因分析（Forge 1.20.1-47.3.22 + Waystones 14.1.5）：</p>
 * <ol>
 *   <li>Waystones 跨维度传送调用 {@code ServerPlayer.teleportTo(ServerLevel, DDDFF)V}</li>
 *   <li>Forge 补丁发送 {@code ClientboundRespawnPacket(flag=3)}，但不发送 {@code ClientboundPlayerAbilitiesPacket}</li>
 *   <li>Flag 3 (KEEP_ALL_PLAYERDATA) 复制 flying 但不复制 mayfly，客户端出现 flying=true, mayfly=false</li>
 *   <li>客户端检测到 flying && !mayfly，自动设置 flying=false 并发送给服务端</li>
 *   <li>FlyingToken.tick() 仅在 mayfly false→true 转换时同步，不会重新同步</li>
 * </ol>
 *
 * <p>修复策略：在传送前捕获飞行状态，传送后延迟 2 tick（等待 FlyingToken 重新应用 mayfly）
 * 恢复飞行状态并强制同步能力到客户端。</p>
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Unique
    private boolean yzzzfix$wasFlying;

    @Inject(method = "m_8999_(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At("HEAD"), remap = false)
    private void yzzzfix$beforeTeleportTo(ServerLevel level, double x, double y, double z, float yRot, float xRot, CallbackInfo ci) {
        yzzzfix$wasFlying = ((ServerPlayer) (Object) this).getAbilities().flying;
    }

    @Inject(method = "m_8999_(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At("RETURN"), remap = false)
    private void yzzzfix$afterTeleportTo(ServerLevel level, double x, double y, double z, float yRot, float xRot, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (self.server == null) return;

        boolean wasFlying = yzzzfix$wasFlying;
        int tick = self.server.getTickCount();

        // tick+2: after FlyingToken entity tick has re-applied mayfly
        self.server.tell(new TickTask(tick + 2, () -> {
            if (self.hasDisconnected()) return;
            if (wasFlying && self.getAbilities().mayfly) {
                self.getAbilities().flying = true;
            }
            self.onUpdateAbilities();
        }));
    }
}
