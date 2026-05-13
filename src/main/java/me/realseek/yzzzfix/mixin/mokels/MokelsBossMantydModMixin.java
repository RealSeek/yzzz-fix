package me.realseek.yzzzfix.mixin.mokels;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Queue;

/**
 * 修复 Mokel's Boss Mantyd 在排队任务中可能出现的空指针异常。
 *
 * <p>该 Mixin 为目标逻辑增加异常保护，避免异步或延迟执行任务中的空对象访问导致游戏崩溃。</p>
 */
@Mixin(targets = "mokels.boss.mantydmod.mantydmod", remap = false)
public abstract class MokelsBossMantydModMixin {

    @Unique
    private static final Logger yzzzfix$LOGGER = LogManager.getLogger("YzzzFix/MokelsFix");

    @Redirect(method = "m_8119_",
            at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;"),
            remap = true)
    private Object yzzzfix$safeQueuePoll(Queue<?> queue) {
        Object work = queue.poll();
        if (work instanceof Runnable runnable) {
            try {
                runnable.run();
            } catch (NullPointerException e) {
                yzzzfix$LOGGER.debug("Caught NPE in queued work: {}", e.getMessage());
            }
            return null; // Already executed
        }
        return work;
    }
}
