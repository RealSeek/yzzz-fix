package me.realseek.yzzzfix.mixin.depthcrawler;

import net.mcreator.depthcrawler.procedures.DeepvenomEffectStartedappliedProcedure;
import net.mcreator.depthcrawler.procedures.DeepvenomEffectExpiresProcedure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Deepvenom 药水效果的游戏模式切换漏洞。
 *
 * <p>原效果通过延迟任务恢复生存模式，存在失效风险。
 * 本 Mixin 移除延迟任务，并在效果过期时强制将冒险模式玩家切回生存。
 * 确保无论何种原因导致效果结束，玩家都不会永久卡在冒险模式。</p>
 */
@Mixin(value = {DeepvenomEffectStartedappliedProcedure.class, DeepvenomEffectExpiresProcedure.class}, remap = false)
public abstract class DeepvenomMixin {

    // 1.取消 60 秒延迟任务
    @Redirect(method = "execute",
            at = @At(value = "INVOKE",
                    target = "Lnet/mcreator/depthcrawler/DepthcrawlerMod;queueServerWork(ILjava/lang/Runnable;)V"),
            remap = false)
    private static void yzzzfix$cancelQueuedTask(int time, Runnable task) {
        //不执行操作，避免延迟恢复
    }

    //2.效果过期时强制恢复生存模式
    @Inject(method = "execute", at = @At("TAIL"), remap = false)
    private static void yzzzfix$forceRestoreSurvival(Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player) {
            if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
                player.setGameMode(GameType.SURVIVAL);
            }
        }
    }
}