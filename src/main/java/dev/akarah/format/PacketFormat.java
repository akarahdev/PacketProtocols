package dev.akarah.format;

import dev.akarah.util.UnsafeFunction;

import java.nio.ByteBuffer;

public interface PacketFormat<T> {
    T read(ByteBuffer stream);
    void write(ByteBuffer stream, T value);
    int length(T value);
    String generateJavaType();

    default PacketFormat<T> comment(String comment) {
        return this;
    }

    @UnsafeFunction
    default int lengthFromObject(Object value) {
        return this.length((T) value);
    }

    @UnsafeFunction
    default void writeFromObject(ByteBuffer stream, Object value) {
        this.write(stream, (T) value);
    }
}
