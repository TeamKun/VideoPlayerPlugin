package net.kunmc.lab.vplayer.common.network;

import net.kunmc.lab.vplayer.VideoPlayer;
import net.kunmc.lab.vplayer.common.data.DataSerializer;
import net.kunmc.lab.vplayer.common.patch.VideoPatch;
import net.kunmc.lab.vplayer.common.patch.VideoPatchOperation;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PacketContainer {

    private static final int maxLength = 32767;

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

    private static void writeVarInt(ByteBuffer buf, int input) {
        while ((input & -128) != 0) {
            buf.put((byte) (input & 127 | 128));
            input >>>= 7;
        }
        buf.put((byte) input);
    }

    private static int readVarInt(ByteBuffer buf) {
        int i = 0;
        int j = 0;

        while (true) {
            byte b0 = buf.get();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }

            if ((b0 & 128) != 128) {
                break;
            }
        }

        return i;
    }

    private static byte[] writeString(String string) {
        byte[] abyte = string.getBytes(StandardCharsets.UTF_8);
        if (abyte.length > maxLength) {
            throw new RuntimeException("String too big (was " + abyte.length + " bytes encoded, max " + maxLength + ")");
        } else {
            ByteBuffer buf = ByteBuffer.allocate(abyte.length + 5);
            writeVarInt(buf, abyte.length);
            buf.put(abyte);
            buf.flip();
            byte[] bytes = new byte[buf.remaining()];
            buf.get(bytes);
            return bytes;
        }
    }

    private static String readString(ByteBuffer buf) {
        int i = readVarInt(buf);
        if (i > maxLength * 4) {
            throw new RuntimeException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")");
        } else if (i < 0) {
            throw new RuntimeException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            byte[] bytes = new byte[i - buf.position()];
            buf.get(bytes, 0, i);
            String s = new String(bytes, StandardCharsets.UTF_8);
            if (s.length() > maxLength) {
                throw new RuntimeException("The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")");
            } else {
                return s;
            }
        }
    }

    public static byte[] encode(PacketContainer message) {
        String string = DataSerializer.encode(message);
        return writeString(string);
    }

    public static PacketContainer decode(byte[] buffer) {
        String string = readString(ByteBuffer.wrap(buffer));
        PacketContainer data = DataSerializer.decode(string, PacketContainer.class);
        if (data == null)
            VideoPlayer.LOGGER.warning("Invalid Packet");
        return data;
    }

}