package dev.akarah.protocol;

import dev.akarah.format.PacketFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a series of valid packet formats.
 */
public class Protocol {
    public List<PacketFormat<?>> packetFormats = new ArrayList<>();

    private Protocol() {}

    public static Protocol empty() {
        return new Protocol();
    }
}
