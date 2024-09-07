package dev.akarah.protocol;

import dev.akarah.format.PacketFormat;
import dev.akarah.format.PacketFormats;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.nio.ByteBuffer;
import java.util.Optional;

public class Packet<T> {
    private Packet() {
    }

    int packetId;

    PacketFormat<T> innerType;

    private Packet(int id, PacketFormat<T> innerType) {
        this.innerType = innerType;
        this.packetId = id;
    }

    public PacketFormat<T> getFormat() {
        return this.innerType;
    }

    public int getPacketId() {
        return this.packetId;
    }

    public String javaTypeSignature() {
        return this.innerType.generateJavaType();
    }

    public ByteBuffer encode(T value) {
        var buf = ByteBuffer.allocate(this.innerType.length(value));
        this.innerType.write(buf, value);
        return buf;
    }

    public T decode(ByteBuffer buffer) {
        return this.innerType.read(buffer);
    }

    public Optional<T> tryDecode(ByteBuffer buffer) {
        var pos = buffer.position();
        try {
            return Optional.of(this.innerType.read(buffer));
        } catch (Exception e) {
            buffer.position(pos);
            return Optional.empty();
        }
    }

    public static Packet<?> ofId(int packetId) {
        return new Packet<>(packetId, null);
    }

    public<A> Packet<A> withArguments(
        PacketFormat<A> innerType
    ) {
        return new Packet<A>(this.packetId, new PacketFormats.NewProtocol<>(innerType));
    }

    public <A, B> Packet<Pair<A, B>> withArguments(
        PacketFormat<A> first,
        PacketFormat<B> second
    ) {
        return new Packet<>(this.packetId, new PacketFormats.NewProtocol<>(new PacketFormats.PairFormat<>(first, second)));
    }

    public <A, B, C> Packet<Triplet<A, B, C>> withArguments(
        PacketFormat<A> first,
        PacketFormat<B> second,
        PacketFormat<C> third
    ) {
        return new Packet<>(this.packetId, new PacketFormats.NewProtocol<>(
            new PacketFormats.TripletFormat<>(first, second, third)));
    }

    public<A, B, C, D> Packet<Quartet<A, B, C, D>> withArguments(
        PacketFormat<A> first,
        PacketFormat<B> second,
        PacketFormat<C> third,
        PacketFormat<D> fourth
    ) {
        return new Packet<>(this.packetId, new PacketFormats.NewProtocol<>(
            new PacketFormats.QuartetFormat<>(first, second, third, fourth)));
    }


}
