package net.kunmc.lab.vplayer.common.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PacketBuffer {
    public static final int maxLength = 32767;
    private final ByteBuffer buf;

    private PacketBuffer(ByteBuffer backingBuffer) {
        buf = backingBuffer;
    }

    public PacketBuffer(int length) {
        this(ByteBuffer.allocate(length));
    }

    public PacketBuffer() {
        this(maxLength);
    }

    public void fromBytes(byte[] bytes) {
        buf.clear();
        buf.put(bytes);
        buf.flip();
    }

    public byte[] toBytes() {
        buf.flip();
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        buf.clear();
        return bytes;
    }

    public void writeByte(int input) {
        buf.put((byte) input);
    }

    public byte readByte() {
        return buf.get();
    }

    public void writeVarInt(int input) {
        while ((input & -128) != 0) {
            buf.put((byte) (input & 127 | 128));
            input >>>= 7;
        }
        buf.put((byte) input);
    }

    public int readVarInt() {
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

    public void writeString(String string) {
        byte[] abyte = string.getBytes(StandardCharsets.UTF_8);
        if (abyte.length > maxLength) {
            throw new RuntimeException("String too big (was " + abyte.length + " bytes encoded, max " + maxLength + ")");
        } else {
            writeVarInt(abyte.length);
            buf.put(abyte);
        }
    }

    public String readString() {
        int i = readVarInt();
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
}
