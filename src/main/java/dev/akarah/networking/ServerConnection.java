package dev.akarah.networking;

import dev.akarah.format.Packet;
import dev.akarah.protocol.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ServerConnection {
    private final OutputStream outputStream;
    public final Socket socket;
    private final InputStream inputStream;
    private final ProtocolServer server;

    protected ServerConnection(Socket socket, ProtocolServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        Thread.ofVirtual().start(this::start);
    }

    public void start() {
        try {
            for(var listener : this.server.connectionListeners) {
                listener.accept(this);
            }
            while(this.socket.isConnected()) {
                this.receivingLoop();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receivingLoop() throws IOException {
        int packetId = (inputStream.read() << 24)
            | (inputStream.read() << 16)
            | (inputStream.read() << 8)
            | inputStream.read();
        int packetLength = (inputStream.read() << 24)
            | (inputStream.read() << 16)
            | (inputStream.read() << 8)
            | inputStream.read();
        System.out.println("pid: " + packetId + " length: " + packetLength);
        var buf = ByteBuffer.allocate(packetLength);
        for(int i = 0; i < packetLength; i++) {
            buf.put((byte) inputStream.read());
        }
        buf.position(0);
        var dft = this.server.protocol
            .getPacketDefaultFromId(packetId);

        var pkt = (Packet<?, ?>) dft.fromDataObject(
            dft.specification().decode(buf));

        for(var listener : this.server.packetListeners) {
            listener.accept(pkt);
        }
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
