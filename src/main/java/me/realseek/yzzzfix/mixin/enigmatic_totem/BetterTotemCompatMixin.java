package me.realseek.yzzzfix.mixin.enigmatic_totem;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.items.TotemOfMalice;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.packets.clients.PacketEvilCage;
import auviotre.enigmatic.addon.packets.clients.PacketMaliceTotem;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.cerbon.better_totem_of_undying.utils.BTUUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 修复 Totem of Malice 与 Better Totem of Undying 的兼容问题。
 *
 * <p>当 Better Totem 的 {@code canSaveFromDeath} 返回 false 时，该 Mixin 额外检查
 * Totem of Malice 是否可以触发，实现恶意图腾的完整救命逻辑（AOE 伤害、击退、满血恢复）。</p>
 */
@Mixin(value = BTUUtils.class, remap = false)
public class BetterTotemCompatMixin {

    @Inject(method = "canSaveFromDeath", at = @At("RETURN"), cancellable = true)
    private static void yzzzfix$checkTotemOfMalice(LivingEntity entity, DamageSource source,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (player.getCooldowns().isOnCooldown(Items.TOTEM_OF_UNDYING)) return;
        if (BTUUtils.isDimensionBlacklisted(entity.level())) return;
        if (BTUUtils.isStructureBlacklisted(entity.blockPosition(), (ServerLevel) entity.level())) return;
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (!TotemOfMalice.isEnable(player, Boolean.FALSE)) return;

        ItemStack stack = ItemStack.EMPTY;
        ItemStack validTotem = TotemOfMalice.getValidTotem(player);
        if (!validTotem.isEmpty()) {
            stack = validTotem.copy();
            TotemOfMalice.hurtAndBreak(validTotem, player);
        } else if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.TOTEM_OF_MALICE)) {
            ItemStack curioStack = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.TOTEM_OF_MALICE);
            if (TotemOfMalice.isPowerful(curioStack)) {
                stack = curioStack.copy();
                TotemOfMalice.hurtAndBreak(curioStack, player);
            }
        }
        if (stack.isEmpty()) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack invStack = inv.getItem(i);
                if (!invStack.isEmpty() && invStack.is(EnigmaticAddonItems.TOTEM_OF_MALICE)
                        && TotemOfMalice.isPowerful(invStack)) {
                    stack = invStack.copy();
                    TotemOfMalice.hurtAndBreak(invStack, player);
                    break;
                }
            }
        }
        if (stack.isEmpty()) return;

        player.awardStat(Stats.ITEM_USED.get(EnigmaticAddonItems.TOTEM_OF_MALICE), 1);
        CriteriaTriggers.USED_TOTEM.trigger(player, stack);

        PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(
                entity.getX(), entity.getY(), entity.getZ(), 64.0, entity.level().dimension());
        EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> tp),
                new PacketMaliceTotem(entity.getX(), entity.getY(), entity.getZ()));

        float aoeDamage = entity.getMaxHealth() * (1.5f + SuperpositionHandler.getCurseAmount(stack) * 0.5f);
        List<LivingEntity> nearby = entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(8.0));
        for (LivingEntity target : nearby) {
            if (target == player) continue;
            Vec3 delta = target.position().subtract(entity.position()).normalize().scale(0.5);
            float modifier = Math.min(1.0f, 0.8f / target.distanceTo(entity));
            Vec3 horizontal = new Vec3(delta.x, 0, delta.z).normalize().scale(modifier);
            target.push(horizontal.x, target.onGround() ? 1.2f * modifier : 0.0, horizontal.z);
            target.hurt(SuperAddonHandler.damageSource(target, EnigmaticAddonDamageTypes.EVIL_CURSE, player), aoeDamage);
            target.invulnerableTime = 0;
            EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> tp),
                    new PacketEvilCage(target.getX(), target.getY(), target.getZ(), target.getBbWidth() / 2.0, target.getBbHeight(), 0));
        }

        entity.setHealth(entity.getMaxHealth());
        entity.removeAllEffects();
        cir.setReturnValue(true);
    }
}
