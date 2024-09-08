package dev.akarah.protocol;

import dev.akarah.format.PacketFormat;
import dev.akarah.format.PacketFormats;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.nio.ByteBuffer;
import java.util.Optional;

public class PacketSpecification<T> {
    int packetId;
    PacketFormat<T> innerType;

    private PacketSpecification(int id, PacketFormat<T> innerType) {
        this.innerType = innerType;
        this.packetId = id;
    }

    public PacketFormat<T> getFormat() {
        return this.innerType;
    }

    public int getPacketId() {
        return this.packetId;
    }

    /**
     * @return An automatically generated Java-friendly type signature for the packets' internal representation.
     */
    public String javaTypeSignature() {
        return this.innerType.generateJavaType();
    }

    /**
     * Encodes the provided data into a ByteBuffer following this specification.
     * @param value The data to encode
     * @return The data encoded into bytes
     */
    public ByteBuffer encode(T value) {
        var buf = ByteBuffer.allocate(this.innerType.length(value));
        this.innerType.write(buf, value);
        return buf;
    }

    /**
     * Decodes data from a ByteBuffer following this specification.
     * @param buffer The buffer to decode from.
     * @return The decoded data.
     */
    public T decode(ByteBuffer buffer) {
        return this.innerType.read(buffer);
    }

    /**
     * Generates a new empty Packet Specification with a Packet ID.
     * @param packetId The ID of the packet.
     * @return The new empty packet specification.
     */
    public static PacketSpecification<?> ofId(int packetId) {
        return new PacketSpecification<>(packetId, null);
    }

    public<A> PacketSpecification<A> withArguments(
        PacketFormat<A> innerType
    ) {
        return new PacketSpecification<A>(this.packetId, new PacketFormats.NewProtocol<>(innerType));
    }

    public <A, B> PacketSpecification<Pair<A, B>> withArguments(
        PacketFormat<A> first,
        PacketFormat<B> second
    ) {
        return new PacketSpecification<>(this.packetId, new PacketFormats.NewProtocol<>(new PacketFormats.PairFormat<>(first, second)));
    }

    public <A, B, C> PacketSpecification<Triplet<A, B, C>> withArguments(
        PacketFormat<A> first,
        PacketFormat<B> second,
        PacketFormat<C> third
    ) {
        return new PacketSpecification<>(this.packetId, new PacketFormats.NewProtocol<>(
            new PacketFormats.TripletFormat<>(first, second, third)));
    }

    public<A, B, C, D> PacketSpecification<Quartet<A, B, C, D>> withArguments(
        PacketFormat<A> first,
        PacketFormat<B> second,
        PacketFormat<C> third,
        PacketFormat<D> fourth
    ) {
        return new PacketSpecification<>(this.packetId, new PacketFormats.NewProtocol<>(
            new PacketFormats.QuartetFormat<>(first, second, third, fourth)));
    }


}
