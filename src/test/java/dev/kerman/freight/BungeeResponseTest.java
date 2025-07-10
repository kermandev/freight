package dev.kerman.freight;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.util.List;
import java.util.UUID;

import static dev.kerman.freight.BungeeResponse.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testable
public final class BungeeResponseTest {

    @Test
    public void testReadWriteContinuity() {
        final List<BungeeResponse> responses = List.of(
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

        for (BungeeResponse response : responses) {
            assertDoesNotThrow(() -> {
                final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
                buffer.write(PluginMessagePacket.SERIALIZER, response.toPacket());
                final PluginMessagePacket packet = PluginMessagePacket.SERIALIZER.read(buffer);
                final BungeeResponse responseRequest = BungeeMessage.readResponse(packet.data());
                assertEquals(response.getClass(), responseRequest.getClass());
                assertEquals(response, responseRequest, "Request should be equal after reading from buffer");
            }, "Failed to read/write request: " + response.getClass().getSimpleName());
        }
    }
}
