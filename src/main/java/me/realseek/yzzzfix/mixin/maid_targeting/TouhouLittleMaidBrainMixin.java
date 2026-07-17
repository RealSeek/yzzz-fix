package me.realseek.yzzzfix.mixin.maid_targeting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * 修复TLM魂符在释放女仆时追加任务列表导致的NBT读取错误无法释放女仆的问题。
 */
@Mixin(targets = "com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.MaidBrain", remap = false)
public abstract class TouhouLittleMaidBrainMixin {

    @ModifyVariable(
            method = "registerWorkGoals",
            at = @At(value = "STORE"),
            ordinal = 0,
            require = 1
    )
    private static List<?> yzzzfix$copyWorkTasksBeforeAppending(List<?> tasks) {
        return new ArrayList<>(tasks);
    }
}
