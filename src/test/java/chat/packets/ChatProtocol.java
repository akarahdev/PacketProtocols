package chat.packets;

import dev.akarah.protocol.Protocol;

public class ChatProtocol {
    public static Protocol PROTOCOL = Protocol.empty()
        .version(1)
        .registerPacket(new ChatMessagePacket(null, null));
}
