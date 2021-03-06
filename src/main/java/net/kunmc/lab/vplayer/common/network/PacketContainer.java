package net.kunmc.lab.vplayer.common.network;

import net.kunmc.lab.vplayer.VideoPlayer;
import net.kunmc.lab.vplayer.common.data.DataSerializer;
import net.kunmc.lab.vplayer.common.patch.VideoPatch;
import net.kunmc.lab.vplayer.common.patch.VideoPatchOperation;
import net.kunmc.lab.vplayer.common.util.PacketBuffer;

import java.util.List;

public class PacketContainer {

    private final VideoPatchOperation operation;
    private final List<VideoPatch> patches;

    public PacketContainer(VideoPatchOperation operation, List<VideoPatch> patches) {
        this.operation = operation;
        this.patches = patches;
    }

    public VideoPatchOperation getOperation() {
        return operation;
    }

    public List<VideoPatch> getPatches() {
        return patches;
    }

    public static void encode(PacketContainer message, PacketBuffer buf) {
        String string = DataSerializer.encode(message);
        buf.writeString(string);
    }

    public static PacketContainer decode(PacketBuffer buf) {
        String string = buf.readString();
        PacketContainer data = DataSerializer.decode(string, PacketContainer.class);
        if (data == null)
            VideoPlayer.LOGGER.warning("Invalid Packet");
        return data;
    }

}