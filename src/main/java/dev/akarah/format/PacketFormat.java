package dev.akarah.format;

import java.nio.ByteBuffer;

public interface PacketFormat<T> {
    T read(ByteBuffer stream);
    void write(ByteBuffer stream, T value);
    int length(T value);
    String generateJavaType();

    default PacketFormat<T> comment(String comment) {
        return this;
    }

    default int lengthFromObject(Object value) {
        return this.length((T) value);
    }

    default void writeFromObject(ByteBuffer stream, Object value) {
        this.write(stream, (T) value);
    }
}
