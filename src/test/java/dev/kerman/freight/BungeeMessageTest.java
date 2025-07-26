package dev.kerman.freight;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

@EnvTest
public class BungeeMessageTest {

    @Test
    void testSingle(Env env) {
        var instance = env.createFlatInstance();
        var message = new BungeeRequest.IP(); // packet doesnt matter.
        var connection1 = env.createConnection();
        var connection2 = env.createConnection();
        var connection3 = env.createConnection();
        var player1 = connection1.connect(instance, new Pos(0, 0, 0));
        var player2 = connection2.connect(instance, new Pos(0, 0, 0));
        var player3 = connection3.connect(instance, new Pos(0, 0, 0));
        var tracker1 = connection1.trackIncoming(PluginMessagePacket.class);
        var tracker2 = connection2.trackIncoming(PluginMessagePacket.class);
        var tracker3 = connection3.trackIncoming(PluginMessagePacket.class);
        message.sendSingle(List.of(player1, player2, player3));
        var packets1 = tracker1.collect();
        var packets2 = tracker2.collect();
        var packets3 = tracker3.collect();
        Assertions.assertEquals(1, packets1.size() + packets2.size() + packets3.size(), "Only one packet should of been sent across all players");
    }

    @Test
    void testMultiple(Env env) {
        var instance = env.createFlatInstance();
        var message = new BungeeRequest.IP(); // packet doesnt matter.
        var connection1 = env.createConnection();
        var connection2 = env.createConnection();
        var connection3 = env.createConnection();
        var player1 = connection1.connect(instance, new Pos(0, 0, 0));
        var player2 = connection2.connect(instance, new Pos(0, 0, 0));
        var player3 = connection3.connect(instance, new Pos(0, 0, 0));
        var tracker1 = connection1.trackIncoming(PluginMessagePacket.class);
        var tracker2 = connection2.trackIncoming(PluginMessagePacket.class);
        var tracker3 = connection3.trackIncoming(PluginMessagePacket.class);
        message.send(Audience.audience(player1, player2, player3));
        var packets1 = tracker1.collect();
        var packets2 = tracker2.collect();
        var packets3 = tracker3.collect();
        Assertions.assertEquals(3, packets1.size() + packets2.size() + packets3.size(), "One packet should of been sent across all players");
    }

    @Test
    void determineType() {
        final byte[] message = new byte[]{0, 15, 71, 101, 116, 80, 108, 97, 121, 101, 114, 83, 101, 114, 118, 101, 114, 0, 10, 84, 104, 101, 77, 111, 100, 101, 57, 49, 49}; // Request.GetPlayerServer
        final var response = BungeeMessage.readRequest(message);
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertInstanceOf(BungeeRequest.GetPlayerServer.class, response, "Response should be of type GetPlayerServer");
    }
}
