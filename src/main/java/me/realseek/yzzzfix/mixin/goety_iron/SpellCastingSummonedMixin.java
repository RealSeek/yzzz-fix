package me.realseek.yzzzfix.mixin.goety_iron;

import com.k1sak1.goety_iron.common.entities.ally.GIronDeadKing;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import me.realseek.yzzzfix.compat.GoetyIronServants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
        "com.k1sak1.goety_iron.common.entities.ally.SpellCastingSummoned",
        "com.k1sak1.goety_iron.common.entities.ally.GIronAncientKnight"
}, remap = false)
public abstract class SpellCastingSummonedMixin {

    @Unique
    private static final ResourceLocation yzzzfix$SPELL_REGISTRY = new ResourceLocation("irons_spellbooks", "spells");

    @Inject(
            method = "initiateCastSpell(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;I)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void yzzzfix$blockPlayerServantSummoning(@Coerce Object spell, int spellLevel, CallbackInfo ci) {
        IForgeRegistry<Object> spells = RegistryManager.ACTIVE.getRegistry(yzzzfix$SPELL_REGISTRY);
        if (spells != null && GoetyIronServants.blocksSummon((Entity) (Object) this, spells.getKey(spell))) {
            ci.cancel();
        }
    }

    @ModifyArg(
            method = "initiateCastSpell",
            at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;initiateCast(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;IILio/redspace/ironsspellbooks/api/spells/CastSource;Ljava/lang/String;)V"),
            index = 2,
            require = 1
    )
    private int yzzzfix$shortenDeadKingBlight(AbstractSpell spell, int spellLevel, int duration,
                                             CastSource source, String castingItem) {
        if ((Object) this instanceof GIronDeadKing king && GoetyIronServants.hasOnlinePlayerOwner(king)
                && GoetyIronServants.BLIGHT.equals(spell.getSpellResource())) {
            return Math.min(32, duration);
        }
        return duration;
    }
}
