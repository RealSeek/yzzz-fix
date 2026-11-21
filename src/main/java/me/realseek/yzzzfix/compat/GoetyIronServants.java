package me.realseek.yzzzfix.compat;

import com.Polarice3.Goety.api.entities.IOwned;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;

public final class GoetyIronServants {
    public static final ResourceLocation BLIGHT = new ResourceLocation("irons_spellbooks", "blight");

    private static final Set<ResourceLocation> SUMMON_SPELLS = Set.of(
            new ResourceLocation("irons_spellbooks", "raise_dead"),
            new ResourceLocation("irons_spellbooks", "summon_vex"),
            new ResourceLocation("irons_spellbooks", "summon_polar_bear"),
            new ResourceLocation("irons_spellbooks", "summon_horse"),
            new ResourceLocation("irons_spellbooks", "wisp"),
            new ResourceLocation("ias_spellbooks", "venomous_reborn")
    );

    private GoetyIronServants() {
    }

    public static boolean blocksSummon(Entity caster, ResourceLocation spellId) {
        return spellId != null && SUMMON_SPELLS.contains(spellId)
                && caster.level() instanceof ServerLevel level && caster instanceof IOwned servant
                && hasPlayerOwner(servant, level);
    }

    public static boolean hasOnlinePlayerOwner(Entity caster) {
        return caster.level() instanceof ServerLevel && caster instanceof IOwned servant
                && servant.getMasterOwner() instanceof Player;
    }

    private static boolean hasPlayerOwner(IOwned servant, ServerLevel level) {
        LivingEntity owner = servant.getMasterOwner();
        if (owner instanceof Player) {
            return true;
        }

        // 主人离线时只能通过保存的 UUID 确认其是否为玩家。
        UUID ownerId = owner == null ? servant.getOwnerId() : owner instanceof IOwned owned ? owned.getOwnerId() : null;
        return ownerId != null && (
                level.getServer().getPlayerList().getPlayer(ownerId) != null
                || Files.isRegularFile(level.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(ownerId + ".dat"))
        );
    }
}
