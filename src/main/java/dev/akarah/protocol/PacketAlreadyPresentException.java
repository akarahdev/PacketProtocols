package dev.akarah.protocol;

public class PacketAlreadyPresentException extends RuntimeException {
    public PacketAlreadyPresentException(int packetId) {
        super("Protocol already has packet of ID #" + packetId);
    }
}
