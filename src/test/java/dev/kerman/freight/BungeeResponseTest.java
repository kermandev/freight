package dev.kerman.freight;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;

import static dev.kerman.freight.BungeeResponse.Connect;
import static dev.kerman.freight.BungeeResponse.ConnectOther;
import static dev.kerman.freight.BungeeResponse.Forward;
import static dev.kerman.freight.BungeeResponse.ForwardToPlayer;
import static dev.kerman.freight.BungeeResponse.GetPlayerServer;
import static dev.kerman.freight.BungeeResponse.GetServer;
import static dev.kerman.freight.BungeeResponse.GetServers;
import static dev.kerman.freight.BungeeResponse.IP;
import static dev.kerman.freight.BungeeResponse.IPOther;
import static dev.kerman.freight.BungeeResponse.KickPlayer;
import static dev.kerman.freight.BungeeResponse.KickPlayerRaw;
import static dev.kerman.freight.BungeeResponse.Message;
import static dev.kerman.freight.BungeeResponse.MessageRaw;
import static dev.kerman.freight.BungeeResponse.PlayerCount;
import static dev.kerman.freight.BungeeResponse.PlayerList;
import static dev.kerman.freight.BungeeResponse.ServerIP;
import static dev.kerman.freight.BungeeResponse.UUIDOther;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnvTest
public final class BungeeResponseTest { //TODO bin tests

    static List<BungeeResponse> responses() {
        return List.of(
                new Connect(),
                new ConnectOther(),
                new IP("127.0.0.1", 11111),
                new IPOther("playerName", "127.0.0.1", 11111),
                new PlayerCount("testServer", 100),
                new PlayerList("testServer", List.of("player1", "player2")),
                new GetServers(List.of("server1", "server2")),
                new Message(),
                new MessageRaw(),
                new GetServer("testServer"),
                new GetPlayerServer("playerName", "testServer"),
                new BungeeResponse.UUID(UUID.randomUUID()),
                new UUIDOther("playerName", UUID.randomUUID()),
                new ServerIP("testServer", "127.0.0.1", 25565),
                new KickPlayer(),
                new KickPlayerRaw(),
                new Forward("testServer", "Forwarded message".getBytes()),
                new ForwardToPlayer("testServer", "Forwarded messag2e".getBytes())
        );
    }

    @ParameterizedTest
    @MethodSource("responses")
    void testReadWriteContinuity(BungeeResponse response) {
        assertDoesNotThrow(() -> {
            final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
            buffer.write(PluginMessagePacket.SERIALIZER, response.toPacket());
            final PluginMessagePacket packet = PluginMessagePacket.SERIALIZER.read(buffer);
            final BungeeResponse responseRequest = BungeeMessage.readResponse(packet.data());
            assertEquals(response.getClass(), responseRequest.getClass());
            assertEquals(response, responseRequest, "Request should be equal after reading from buffer");
        }, "Failed to read/write request: " + response.getClass().getSimpleName());
    }

    @ParameterizedTest
    @MethodSource("responses")
    void envFullTest(BungeeResponse response, Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 64, 0));
        var listener = env.listen(PlayerPluginMessageEvent.class);
        player.addPacketToQueue(response.toClientPacket());
        listener.followup(event -> {
            var readResponse = BungeeMessage.readResponse(event);
            assertNotNull(readResponse);
            assertEquals(response, readResponse, "Response should be equal after reading from event");
        });
    }
}
