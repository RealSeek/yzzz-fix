package me.realseek.yzzzfix.module.forbidden_arcanus;

import me.realseek.yzzzfix.YzzzFix;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Forbidden Arcanus 模块的网络通道管理器。
 *
 * <p>注册并管理用于仪式数据同步的网络数据包通道。
 * 通道使用宽松的版本检查策略，确保该 mod 不影响客户端/服务端连接兼容性。</p>
 */
public final class ForbiddenArcanusNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(YzzzFix.MOD_ID, "forbidden_arcanus"),
            () -> PROTOCOL_VERSION,
            remote -> true,
            remote -> true
    );

    private static boolean registered;

    private ForbiddenArcanusNetworkHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        CHANNEL.registerMessage(
                0,
                ForbiddenArcanusSyncRitualsPacket.class,
                ForbiddenArcanusSyncRitualsPacket::encode,
                ForbiddenArcanusSyncRitualsPacket::decode,
                ForbiddenArcanusSyncRitualsPacket::handle
        );
        registered = true;
    }

    public static void sendRituals(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ForbiddenArcanusSyncRitualsPacket(player));
    }
}
