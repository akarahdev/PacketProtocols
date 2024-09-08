package dev.akarah.networking;

import dev.akarah.format.Packet;
import dev.akarah.protocol.Protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProtocolServer {
    Protocol protocol;
    int port = 80;

    public List<Consumer<ServerConnection>> connectionListeners = new ArrayList<>();
    public List<Consumer<Packet<?, ?>>> packetListeners = new ArrayList<>();

    private ProtocolServer(Protocol protocol) {
        this.protocol = protocol;
    }

    public static ProtocolServer withProtocol(Protocol protocol) {
        return new ProtocolServer(protocol);
    }

    public ProtocolServer withPort(int port) {
        this.port = port;
        return this;
    }

    public ProtocolServer withConnectionListener(Consumer<ServerConnection> listener) {
        this.connectionListeners.add(listener);
        return this;
    }

    public ProtocolServer withPacketListener(Consumer<Packet<?, ?>> listener) {
        this.packetListeners.add(listener);
        return this;
    }

    public void start() {
        try(var server = new ServerSocket(port)) {
            while(true) {
                var client = server.accept();
                new ServerConnection(client, this);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
