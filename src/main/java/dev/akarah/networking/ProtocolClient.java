package dev.akarah.networking;

import dev.akarah.format.Packet;
import dev.akarah.protocol.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProtocolClient {
    public final List<Consumer<Packet<?, ?>>> packetListeners = new ArrayList<>();
    public final Socket socket = new Socket();
    public final Protocol protocol;
    public final InputStream inputStream;
    public final OutputStream outputStream;

    private ProtocolClient(Protocol protocol, SocketAddress address) throws IOException {
        this.protocol = protocol;
        this.socket.connect(address);
        this.inputStream = this.socket.getInputStream();
        this.outputStream = this.socket.getOutputStream();
    }

    public static ProtocolClient of(Protocol protocol, SocketAddress address) {
        try {
            return new ProtocolClient(protocol, address);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ProtocolClient withPacketListener(Consumer<Packet<?, ?>> listener) {
        this.packetListeners.add(listener);
        return this;
    }

    public ProtocolClient connectTo(SocketAddress address) {
        try {
            socket.connect(address);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public ProtocolClient start() {
        Thread.ofVirtual().start(() -> {
            try {
                while (true) {
                    int packetId = (inputStream.read() << 24)
                        | (inputStream.read() << 16)
                        | (inputStream.read() << 8)
                        | inputStream.read();
                    int packetLength = (inputStream.read() << 24)
                        | (inputStream.read() << 16)
                        | (inputStream.read() << 8)
                        | inputStream.read();
                    var buf = ByteBuffer.allocate(packetLength);
                    for (int i = 0; i < packetLength; i++) {
                        buf.put((byte) inputStream.read());
                    }
                    var dft = this.protocol
                        .getPacketDefaultFromId(packetId);

                    var pkt = (Packet<?, ?>) dft.fromDataObject(
                        dft.specification().decode(buf));

                    for (var listener : this.packetListeners) {
                        listener.accept(pkt);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return this;
    }

    public void sendPacket(Packet<?, ?> packet) {
        var length = packet.specification().getFormat().lengthFromObject(packet.toFormat());
        var buf = ByteBuffer.allocate(length);
        packet.specification().getFormat().writeFromObject(
            buf,
            packet.toFormat()
        );

        var pid = packet.specification().getPacketId();
        try {
            this.outputStream.write((pid >> 24) & 0xFF);
            this.outputStream.write((pid >> 16) & 0xFF);
            this.outputStream.write((pid >> 8) & 0xFF);
            this.outputStream.write(pid & 0xFF);

            this.outputStream.write((length >> 24) & 0xFF);
            this.outputStream.write((length >> 16) & 0xFF);
            this.outputStream.write((length >> 8) & 0xFF);
            this.outputStream.write(length & 0xFF);

            this.outputStream.write(buf.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
