package net.kunmc.lab.vplayer.server.network;

import net.kunmc.lab.vplayer.ProxyServer;
import net.kunmc.lab.vplayer.common.network.PacketContainer;
import net.kunmc.lab.vplayer.common.network.PacketDispatcher;
import net.kunmc.lab.vplayer.server.patch.VideoPatchRecieveEventServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PacketDispatcherServer {
    public static void register(Plugin plugin) {
        PacketDispatcher.registerServer(plugin, PacketDispatcherServer::handle);
    }

    public static void handle(PacketContainer message, Player player) {
        if (message == null || message.getOperation() == null)
            return;

        ProxyServer.getServer().getPluginManager().callEvent(new VideoPatchRecieveEventServer(message.getOperation(), message.getPatches(), player));
    }

    public static void send(Player network, PacketContainer packet) {
        PacketDispatcher.channel.sendTo(packet, network);
    }
}
