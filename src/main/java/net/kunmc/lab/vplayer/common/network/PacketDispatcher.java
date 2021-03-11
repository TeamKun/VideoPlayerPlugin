package net.kunmc.lab.vplayer.common.network;

import net.kunmc.lab.vplayer.VideoPlayer;
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
        private Function<MSG, byte[]> encoder;

        public SimpleChannel(String channel) {
            this.channel = channel;
        }

        public void registerMessage(Plugin pluginIn, Function<MSG, byte[]> encoderIn, Function<byte[], MSG> decoderIn, BiConsumer<MSG, Player> messageConsumerIn) {
            plugin = pluginIn;
            encoder = encoderIn;

            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channel);
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, (channelIn, playerIn, messageIn) -> {
                if (!channel.equals(channelIn))
                    return;
                MSG msg = decoderIn.apply(messageIn);
                messageConsumerIn.accept(msg, playerIn);
            });
        }

        public void sendTo(MSG messageIn, Player playerIn) {
            playerIn.sendPluginMessage(plugin, channel, encoder.apply(messageIn));
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