package me.realseek.yzzzfix.module.cy3_core;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * CY3 Core 模块的 HPS 清理网络数据包。
 *
 * <p>该数据包由服务端发送给客户端，通知客户端清除本地玩家在 {@code LifeLimiter.HPS} 中的状态。
 * 数据包本身不携带任何数据，仅作为触发信号。</p>
 */
public final class CY3CoreClearHPSPacket {

    public static void encode(CY3CoreClearHPSPacket message, FriendlyByteBuf buf) {
    }

    public static CY3CoreClearHPSPacket decode(FriendlyByteBuf buf) {
        return new CY3CoreClearHPSPacket();
    }

    public static void handle(CY3CoreClearHPSPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CY3CoreClientHPSCleaner::clearHPS);
        });
        context.setPacketHandled(true);
    }
}
