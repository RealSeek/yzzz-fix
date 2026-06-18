package me.realseek.yzzzfix.mixin.cy3_core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.heike233.chapterofyuusha3.comm.compat.curios.item.TaskExpert;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 修复 TaskExpert 在 FTB Quests 数据未就绪时读取任务数量导致 NPE 崩溃。
 * <p>通过反射检查 {@code ClientQuestFile} 状态，未就绪时返回 0 并在 tooltip 显示加载提示。</p>
 */
@Mixin(value = TaskExpert.class, remap = false)
public class TaskExpertMixin {

    @Unique
    private static boolean yzzzfix$questDataChecked;
    @Unique
    private static boolean yzzzfix$questDataReady;

    @Unique
    private static boolean yzzzfix$isQuestDataReady() {
        if (yzzzfix$questDataChecked) {
            return yzzzfix$questDataReady;
        }
        yzzzfix$questDataChecked = true;
        try {
            Class<?> clientQuestFile = Class.forName("dev.ftb.mods.ftbquests.client.ClientQuestFile");
            Field instanceField = clientQuestFile.getDeclaredField("INSTANCE");
            Object file = instanceField.get(null);
            if (file == null) return false;
            if (!(boolean) clientQuestFile.getMethod("exists").invoke(null)) return false;
            Field teamDataField = clientQuestFile.getDeclaredField("selfTeamData");
            Object teamData = teamDataField.get(file);
            if (teamData == null) return false;
            yzzzfix$questDataReady = !(boolean) teamData.getClass().getMethod("isLocked").invoke(teamData);
        } catch (Exception e) {
            yzzzfix$questDataReady = true;
        }
        return yzzzfix$questDataReady;
    }

    @Inject(method = "getClientQuestCount", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$safeQuestCount(CallbackInfoReturnable<Integer> cir) {
        if (!yzzzfix$isQuestDataReady()) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
    private void yzzzfix$safeTooltip(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        if (!yzzzfix$isQuestDataReady()) {
            tooltip.add(Component.translatable("tooltip.crashmodfix.ftbquests.loading")
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("tooltip.crashmodfix.ftbquests.wait")
                    .withStyle(ChatFormatting.GRAY));
            ci.cancel();
        }
    }
}
