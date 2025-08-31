package dev.kerman.freight;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;

import static dev.kerman.freight.BungeeResponse.Forward;
import static dev.kerman.freight.BungeeResponse.GetPlayerServer;
import static dev.kerman.freight.BungeeResponse.GetServer;
import static dev.kerman.freight.BungeeResponse.GetServers;
import static dev.kerman.freight.BungeeResponse.IP;
import static dev.kerman.freight.BungeeResponse.IPOther;
import static dev.kerman.freight.BungeeResponse.PlayerCount;
import static dev.kerman.freight.BungeeResponse.PlayerList;
import static dev.kerman.freight.BungeeResponse.ServerIP;
import static dev.kerman.freight.BungeeResponse.UUIDOther;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnvTest
public final class BungeeResponseTest { //TODO bin tests

    static List<BungeeResponse> responses() {
        return List.of(
                new IP("127.0.0.1", 11111),
                new IPOther("playerName", "127.0.0.1", 11111),
                new PlayerCount("testServer", 100),
                new PlayerList("testServer", List.of("player1", "player2")),
                new GetServers(List.of("server1", "server2")),
                new GetServer("testServer"),
                new GetPlayerServer("playerName", "testServer"),
                new BungeeResponse.UUID(UUID.randomUUID()),
                new UUIDOther("playerName", UUID.randomUUID()),
                new ServerIP("testServer", "127.0.0.1", 25565),
                new Forward("testServer", "Forwarded message".getBytes())
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

    @Test
    void testForwardSerialization() {
        // Forward are a bit special as they are missing the type.
        var response = new Forward("testServer", "Forwarded message".getBytes());
        var data = BungeeMessage.writeResponse(response);
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        buffer.write(NetworkBuffer.RAW_BYTES, data);
        var channel = buffer.read(NetworkBuffer.STRING_IO_UTF8);
        assertEquals("testServer", channel, "Channel should be the first value in the framed packet, as expected by the BungeeCord protocol");
        var readResponse = BungeeMessage.readResponse(data);
        assertEquals(response, readResponse, "Forward response should be equal after reading from buffer");
    }

    @Test
    void testType() {
        var response = new IPOther("bob", "127.0.0.1", 65212);
        var data = BungeeMessage.writeResponse(response);
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        buffer.write(NetworkBuffer.RAW_BYTES, data);
        var type = buffer.read(NetworkBuffer.STRING_IO_UTF8);
        assertEquals("IPOther", type, "Type should be 'IPOther' for the IP response");
        var readResponse = BungeeMessage.readResponse(data);
        assertEquals(response, readResponse, "IPOther response should be equal after reading from buffer");
    }

    @Test
    void testLargeForwards() {
        assertDoesNotThrow(() -> {
            new Forward("testServer", new byte[65535]);
        }, "Message format supports 65535 bytes");
        assertThrows(IllegalArgumentException.class, () -> {
            new Forward("testServer", new byte[65535 + 1]);
        }, "Message format supports at most 65535 bytes");
    }
}
