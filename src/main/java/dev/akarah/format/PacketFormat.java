package dev.akarah.format;

import java.nio.ByteBuffer;

public interface PacketFormat<T> {
    T read(ByteBuffer stream);
    void write(ByteBuffer stream, T value);
    int length(T value);
    String generateJavaType();
}
