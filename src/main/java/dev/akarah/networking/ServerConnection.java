package dev.akarah.networking;

import dev.akarah.format.Packet;
import dev.akarah.protocol.Protocol;
import dev.akarah.util.VarIntStreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

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
        var totalPacketLength = VarIntStreamUtils.readVarLong(inputStream);
        var packetInfo = VarIntStreamUtils.readVarLongWithLength(inputStream);
        var packetId = packetInfo.getValue0();
        var packetIdLength = packetInfo.getValue1();

        var packetLength = totalPacketLength - packetIdLength;
        var buf = ByteBuffer.allocate((int) packetLength);
        for (int i = 0; i < packetLength; i++) {
            buf.put((byte) inputStream.read());
        }
        buf.position(0);

        var dft = this.server.protocol
            .getPacketDefaultFromId(packetId.intValue());

        var pkt = (Packet<?, ?>) dft.fromDataObject(
            dft.specification().decode(buf));

        for (var listener : this.server.packetListeners) {
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
            var packetIdSize = VarIntStreamUtils.writeVarLong(new ByteArrayOutputStream(), pid);
            VarIntStreamUtils.writeVarLong(outputStream, length + packetIdSize);
            VarIntStreamUtils.writeVarLong(outputStream, packetIdSize);
            outputStream.write(buf.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
