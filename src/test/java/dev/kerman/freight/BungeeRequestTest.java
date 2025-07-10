package dev.kerman.freight;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.util.List;

import static dev.kerman.freight.BungeeRequest.*;
import static org.junit.jupiter.api.Assertions.*;

@Testable
public final class BungeeRequestTest {

    @Test
    void testReadWriteContinuity() {
        List<BungeeRequest> requests = List.of(
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

        for (BungeeRequest request : requests) {
            assertDoesNotThrow(() -> {
                final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
                buffer.write(ClientPluginMessagePacket.SERIALIZER, request.toClientPacket());
                final ClientPluginMessagePacket packet = ClientPluginMessagePacket.SERIALIZER.read(buffer);
                final BungeeRequest readRequest = BungeeMessage.readRequest(packet.data());
                assertEquals(request.getClass(), readRequest.getClass());
                assertEquals(request, readRequest, "Request should be equal after reading from buffer");
            }, "Failed to read/write request: " + request.getClass().getSimpleName());
        }
    }
}
