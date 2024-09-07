package dev.akarah.protocol;

import dev.akarah.format.Packet;

import java.util.HashMap;

/**
 * Represents a series of valid packet formats.
 */
public class Protocol {
    private final HashMap<Integer, Packet<?, ? extends Packet<?, ?>>> packets = new HashMap<>();
    private int protocolVersion = 0;

    private Protocol() {
    }

    /**
     * Creates a new empty protocol.
     *
     * @return A new protocol with nothing registered.
     */
    public static Protocol empty() {
        return new Protocol();
    }

    /**
     * @param protocolVersion The desired version of the protocol.
     * @return The same protocol with the version set.
     */
    public Protocol version(int protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    /**
     * @return The version of this Protocol.
     */
    public int version() {
        return this.protocolVersion;
    }

    /**
     * Registers a new packet to be understood by the protocol.
     *
     * @param packet The new packet to add.
     * @return The protocol with the new packet registered.
     * @throws PacketAlreadyPresentException If the protocol already has a packet of this ID.
     */
    public Protocol registerPacket(Packet<?, ? extends Packet<?, ?>> packet) {
        if (this.packets.containsKey(packet.specification().getPacketId())) {
            throw new PacketAlreadyPresentException(packet.specification().getPacketId());
        }

        this.packets.put(packet.specification().getPacketId(), packet);
        return this;
    }
}
