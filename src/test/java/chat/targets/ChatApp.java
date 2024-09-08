package chat.targets;

import chat.packets.ChatMessagePacket;
import chat.packets.ChatProtocol;
import dev.akarah.format.Packet;
import dev.akarah.networking.ProtocolClient;
import dev.akarah.networking.ProtocolServer;
import dev.akarah.networking.ServerConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Timer;

public class ChatApp {
    public static void main(String[] args) throws IOException {
        if(args[0].equals("--server")) {
            ProtocolServer.withProtocol(ChatProtocol.PROTOCOL)
                .withPort(2000)
                .withConnectionListener(ServerFunctions::listenForConnection)
                .withPacketListener(ServerFunctions::echoMessage)
                .start();
        } else if(args[0].equals("--client")) {
            var client = ProtocolClient.of(ChatProtocol.PROTOCOL, new InetSocketAddress("localhost", 2000))
                .withPacketListener(ClientFunctions::echoMessage)
                .start();
            Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(100);
                    client.sendPacket(new ChatMessagePacket(
                        "Guest",
                        "I connected!"
                    ));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            while(true) {
                var reader = new BufferedReader(new InputStreamReader(System.in));
                var line = reader.readLine();
                client.sendPacket(new ChatMessagePacket(
                    "Guest",
                    line
                ));
            }
        }
    }

    public static class ServerFunctions {
        public static void listenForConnection(ServerConnection connection) {
            System.out.println("Connection established from " + connection.socket.getInetAddress());
        }

        public static void echoMessage(Packet<?, ?> packet) {
            if(packet instanceof ChatMessagePacket chatMessagePacket) {
                System.out.println("[ECHO] " + chatMessagePacket.username + " > " + chatMessagePacket.message);
            }
        }
    }

    public static class ClientFunctions {
        public static void echoMessage(Packet<?, ?> packet) {
            if(packet instanceof ChatMessagePacket chatMessagePacket) {
                System.out.println("[INC] " + chatMessagePacket.username + " > " + chatMessagePacket.message);
            }
        }
    }
}
