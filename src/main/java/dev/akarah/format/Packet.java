package dev.akarah.format;

import dev.akarah.protocol.PacketSpecification;
import dev.akarah.protocol.Protocol;

/**
 * An abstract class representing a specification and proper data representation of a packet.
 * @param <T> The internal representation of the packets' data.
 * @param <Self> This must be the exact same type as the class. E.g if you are making a `UsernamePacket`,
 *              this should also be the same `UsernamePacket`.
 */
public abstract class Packet<T, Self> {
    /**
     * @return The specification for this packet.
     */
    public abstract PacketSpecification<T> specification();

    /**
     * @param formattedData The internal data to convert from.
     * @return This packet with the provided data. This must return a new instance of the packet.
     */
    public abstract Self fromData(T formattedData);

    /**
     * @return The packet converted to the internal data format.
     */
    public abstract T toFormat();

    public Self fromDataObject(Object formattedData) {
        return this.fromData((T) formattedData);
    }
}
