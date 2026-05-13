package me.realseek.yzzzfix.module.forbidden_arcanus;

import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.Ritual;
import com.stal111.forbidden_arcanus.core.registry.FARegistries;
import me.realseek.yzzzfix.YzzzFix;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Forbidden Arcanus 仪式数据同步网络数据包。
 *
 * <p>服务端在玩家登录后将所有已注册的仪式通过该数据包发送给客户端，
 * 客户端收到后缓存数据并触发 JEI 配方注册。</p>
 */
public final class ForbiddenArcanusSyncRitualsPacket {

    private final List<Ritual> rituals;

    public ForbiddenArcanusSyncRitualsPacket(ServerPlayer player) {
        Registry<Ritual> registry = player.level().registryAccess().registryOrThrow(FARegistries.RITUAL);
        this.rituals = registry.stream().toList();
    }

    private ForbiddenArcanusSyncRitualsPacket(List<Ritual> rituals) {
        this.rituals = rituals;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.rituals.size());
        for (Ritual ritual : this.rituals) {
            buf.writeJsonWithCodec(Ritual.NETWORK_CODEC, ritual);
        }
    }

    public static ForbiddenArcanusSyncRitualsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Ritual> rituals = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            rituals.add(buf.readJsonWithCodec(Ritual.NETWORK_CODEC));
        }
        return new ForbiddenArcanusSyncRitualsPacket(rituals);
    }

    public static void handle(ForbiddenArcanusSyncRitualsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ForbiddenArcanusClientRitualCache.setRituals(packet.rituals);
            YzzzFix.LOGGER.info("Received {} Forbidden Arcanus rituals from server.", packet.rituals.size());
            ForbiddenArcanusRecipeRegistrar.tryRegister();
        });
        context.setPacketHandled(true);
    }
}
