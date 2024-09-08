package dev.akarah.util;

import org.javatuples.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VarIntStreamUtils {
    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    public static long readVarLong(InputStream stream) throws IOException {
        long value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = (byte) stream.read();
            value |= (long) (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;
        }

        return value;
    }

    public static Pair<Long, Integer> readVarLongWithLength(InputStream stream) throws IOException {
        long value = 0;
        int position = 0;
        byte currentByte;

        int length = 0;
        while (true) {
            length += 1;
            currentByte = (byte) stream.read();
            value |= (long) (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;
        }

        return new Pair<>(value, length);
    }

    public static int writeVarLong(OutputStream stream, long value) throws IOException {
        int length = 0;
        while (true) {
            length++;
            if ((value & ~((long) SEGMENT_BITS)) == 0) {
                stream.write((byte) value);
                return length;
            }

            stream.write((byte) (value & SEGMENT_BITS) | CONTINUE_BIT);

            value >>>= 7;
        }
    }
}
