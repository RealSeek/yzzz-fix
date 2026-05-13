package me.realseek.yzzzfix.module.cy3_core;

import me.realseek.yzzzfix.YzzzFix;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * CY3 Core 模块的网络通道管理器。
 *
 * <p>注册并管理用于 HPS 状态清理的网络数据包通道。
 * 通道使用宽松的版本检查策略，确保客户端/服务端版本不一致时不会阻止连接。</p>
 */
public final class CY3CoreNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    private static boolean registered;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(YzzzFix.MOD_ID, "cy3_core"),
            () -> PROTOCOL_VERSION,
            remote -> true,
            remote -> true
    );

    private CY3CoreNetworkHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        CHANNEL.registerMessage(
                0,
                CY3CoreClearHPSPacket.class,
                CY3CoreClearHPSPacket::encode,
                CY3CoreClearHPSPacket::decode,
                CY3CoreClearHPSPacket::handle
        );
        registered = true;
    }

    public static void sendClearHPS(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CY3CoreClearHPSPacket());
    }
}
