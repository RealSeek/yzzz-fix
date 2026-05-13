package me.realseek.yzzzfix.mixin.irons_spellbooks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 Iron's Spells 'n Spellbooks 潜伏者戒指冷却时间没有正确受到冷却缩减属性影响的问题。
 *
 * <p>原始逻辑中 tooltip 使用 {@code 300 * (2.0 - softCapFormula(cdr))} 计算冷却时间，
 * 但实际冷却在 {@code ServerPlayerEvents.onLivingTakeDamage} 中硬编码为 300 ticks (15s)。</p>
 *
 * <p>该 Mixin 在处理生物受伤事件时捕获攻击者，并在添加物品冷却时根据攻击者的
 * {@code irons_spellbooks:cooldown_reduction} 属性重新计算冷却时间。
 * 冷却缩减使用软上限公式处理，避免高数值属性导致冷却时间异常。</p>
 */
@Mixin(targets = "io.redspace.ironsspellbooks.player.ServerPlayerEvents")
public class LurkerRingCooldownMixin {

    @Unique
    private static final ResourceLocation yzzzfix$CDR_KEY = new ResourceLocation("irons_spellbooks", "cooldown_reduction");

    @Unique
    private static final ThreadLocal<Player> yzzzfix$currentAttacker = new ThreadLocal<>();

    @Inject(method = "onLivingTakeDamage", at = @At("HEAD"), remap = false)
    private static void yzzzfix$captureAttacker(LivingDamageEvent event, CallbackInfo ci) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Player player) {
            yzzzfix$currentAttacker.set(player);
        } else {
            yzzzfix$currentAttacker.remove();
        }
    }

    @Inject(method = "onLivingTakeDamage", at = @At("RETURN"), remap = false)
    private static void yzzzfix$clearAttacker(LivingDamageEvent event, CallbackInfo ci) {
        yzzzfix$currentAttacker.remove();
    }

    @Redirect(
            method = "onLivingTakeDamage",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemCooldowns;m_41524_(Lnet/minecraft/world/item/Item;I)V"),
            remap = false
    )
    private static void yzzzfix$fixLurkerRingCooldown(ItemCooldowns cooldowns, Item item, int originalTicks) {
        Player player = yzzzfix$currentAttacker.get();
        if (player != null) {
            Attribute cdrAttribute = ForgeRegistries.ATTRIBUTES.getValue(yzzzfix$CDR_KEY);
            if (cdrAttribute != null) {
                AttributeInstance inst = player.getAttribute(cdrAttribute);
                double cdr = inst != null ? inst.getValue() : 1.0;
                int adjusted = Math.max((int) (300.0 * (2.0 - yzzzfix$softCapFormula(cdr))), 0);
                cooldowns.addCooldown(item, adjusted);
                return;
            }
        }
        cooldowns.addCooldown(item, originalTicks);
    }

    /**
     * 软上限公式，复现自 io.redspace.ironsspellbooks.api.util.Utils.softCapFormula
     */
    @Unique
    private static double yzzzfix$softCapFormula(double value) {
        if (value <= 1.75) {
            return value;
        }
        return 1.0 / (-16.0 * (value - 1.5)) + 2.0;
    }
}
