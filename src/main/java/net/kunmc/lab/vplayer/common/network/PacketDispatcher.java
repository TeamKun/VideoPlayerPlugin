package net.kunmc.lab.vplayer.common.network;

import net.kunmc.lab.vplayer.VideoPlayer;
import net.kunmc.lab.vplayer.common.util.PacketBuffer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class PacketDispatcher {

    public static final String PROTOCOL_VERSION = "VP01";

    public static final String channelId = VideoPlayer.MODID + ":patch";
    public static final SimpleChannel<PacketContainer> channel = new SimpleChannel<>(channelId);

    public static class SimpleChannel<MSG> {
        private final String channel;
        private Plugin plugin;
        private BiConsumer<MSG, PacketBuffer> encoder;
        private final PacketBuffer buf = new PacketBuffer();

        public SimpleChannel(String channel) {
            this.channel = channel;
        }

        public void registerMessage(Plugin pluginIn, BiConsumer<MSG, PacketBuffer> encoderIn, Function<PacketBuffer, MSG> decoderIn, BiConsumer<MSG, Player> messageConsumerIn) {
            plugin = pluginIn;
            encoder = encoderIn;

            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channel);
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, (channelIn, playerIn, messageIn) -> {
                if (!channel.equals(channelIn))
                    return;
                buf.fromBytes(messageIn);
                byte codec = buf.readByte();
                MSG msg = decoderIn.apply(buf);
                messageConsumerIn.accept(msg, playerIn);
                buf.clear();
            });
        }

        public void sendTo(MSG messageIn, Player playerIn) {
            buf.clear();
            buf.writeByte(0);
            encoder.accept(messageIn, buf);
            playerIn.sendPluginMessage(plugin, channel, buf.toBytes());
        }
    }

    public static void registerServer(Plugin plugin, BiConsumer<PacketContainer, Player> handler) {
        channel.registerMessage(
                plugin,
                PacketContainer::encode,
                PacketContainer::decode,
                handler
        );
    }

}