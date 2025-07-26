package dev.kerman.freight;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static dev.kerman.freight.BungeeRequest.Connect;
import static dev.kerman.freight.BungeeRequest.ConnectOther;
import static dev.kerman.freight.BungeeRequest.Forward;
import static dev.kerman.freight.BungeeRequest.ForwardToPlayer;
import static dev.kerman.freight.BungeeRequest.GetPlayerServer;
import static dev.kerman.freight.BungeeRequest.GetServer;
import static dev.kerman.freight.BungeeRequest.GetServers;
import static dev.kerman.freight.BungeeRequest.IP;
import static dev.kerman.freight.BungeeRequest.IPOther;
import static dev.kerman.freight.BungeeRequest.KickPlayer;
import static dev.kerman.freight.BungeeRequest.KickPlayerRaw;
import static dev.kerman.freight.BungeeRequest.Message;
import static dev.kerman.freight.BungeeRequest.MessageRaw;
import static dev.kerman.freight.BungeeRequest.PlayerCount;
import static dev.kerman.freight.BungeeRequest.PlayerList;
import static dev.kerman.freight.BungeeRequest.ServerIP;
import static dev.kerman.freight.BungeeRequest.UUID;
import static dev.kerman.freight.BungeeRequest.UUIDOther;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public final class BungeeRequestTest { //TODO bin tests
    static List<BungeeRequest> requests() {
        return List.of(
                new Connect("testServer"),
                new ConnectOther("playerName", "testServer"),
                new IP(),
                new IPOther("playerName"),
                new PlayerCount("testServer"),
                new PlayerList("testServer"),
                new GetServers(),
                new Message("Player", "Hello, World!"),
                new MessageRaw("player", "Hello, World!"),
                new GetServer(),
                new GetPlayerServer("playerName"),
                new UUID(),
                new UUIDOther("testplayer"),
                new ServerIP("testServer"),
                new KickPlayer("playerName", "You have been kicked!"),
                new KickPlayerRaw("playerName", Component.text("YOU WERE KICKED!!!")),
                new Forward("testServer", "test", "Forwarded message".getBytes()),
                new ForwardToPlayer("testServer", "test", "Forwarded message".getBytes())
        );
    }

    @ParameterizedTest
    @MethodSource("requests")
    void testReadWriteContinuity(BungeeRequest request) {
        assertDoesNotThrow(() -> {
            final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
            buffer.write(ClientPluginMessagePacket.SERIALIZER, request.toClientPacket());
            final ClientPluginMessagePacket packet = ClientPluginMessagePacket.SERIALIZER.read(buffer);
            final BungeeRequest readRequest = BungeeMessage.readRequest(packet.data());
            assertEquals(request.getClass(), readRequest.getClass());
            assertEquals(request, readRequest, "Request should be equal after reading from buffer");
        }, "Failed to read/write request: " + request.getClass().getSimpleName());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requests")
    void envFullTest(BungeeRequest message, Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 64, 0));
        var tracker = connection.trackIncoming(PluginMessagePacket.class);
        message.send(player);
        tracker.assertSingle((packet) -> {
            var request = BungeeMessage.readRequest(packet.data());
            Assertions.assertNotNull(request);
            Assertions.assertEquals(message, request, "Request should be equal after reading from packet");
        });
    }
}
