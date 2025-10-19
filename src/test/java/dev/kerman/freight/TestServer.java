package dev.kerman.freight;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;

import java.util.Arrays;
import java.util.Set;

public class TestServer {

    static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: TestServer <bungeecord/bungeegaurd/velocity/online/offline>");
            return;
        }

        Auth auth = switch (args[0]) {
            case "bungeecord" -> new Auth.Bungee();
            case "bungeegaurd" -> new Auth.Bungee(Set.of("nosecret"));
            case "velocity" -> new Auth.Velocity("nosecret");
            case "online" -> new Auth.Online();
            case "offline" -> new Auth.Offline();
            default -> throw new IllegalStateException("Unexpected value: " + args[0]);
        };

        MinecraftServer minecraftserver = MinecraftServer.init(auth);

        var instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer();
        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));
        instanceContainer.setChunkSupplier(LightingChunk::new);

        var eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.getPlayer().setRespawnPoint(new Pos(0, 42, 0));
            event.setSpawningInstance(instanceContainer);
        });

        eventHandler.addListener(PlayerPluginMessageEvent.class, event -> {

            System.out.println(Arrays.toString(event.getMessage()));
            var response = BungeeMessage.readResponse(event);
            if (response == null) return;
            System.out.printf("Received response: `%s`%n", response);
        });

        var commandHandler = MinecraftServer.getCommandManager();
        commandHandler.register(new CommandTest());
        minecraftserver.start("localhost", 25565);
    }


    static final class CommandTest extends Command {
        public CommandTest() {
            super("test");

            setCondition(Conditions::playerOnly);
            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.Message(sender, "Hello from the proxy!");
                sendMessage(sender, packet);
            }, ArgumentType.Literal("message"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.MessageRaw(sender, "{\"text\":\"Raw JSON message!\"}");
                sendMessage(sender, packet);
            }, ArgumentType.Literal("messageraw"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.Connect("lobby");
                sendMessage(sender, packet);
            }, ArgumentType.Literal("connect"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.ConnectOther(sender, "lobby");
                sendMessage(sender, packet);
            }, ArgumentType.Literal("connectother"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.IP();
                sendMessage(sender, packet);
            }, ArgumentType.Literal("ip"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.IPOther(sender);
                sendMessage(sender, packet);
            }, ArgumentType.Literal("ipother"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.PlayerCount("lobby");
                sendMessage(sender, packet);
            }, ArgumentType.Literal("playercount"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.PlayerList("lobby");
                sendMessage(sender, packet);
            }, ArgumentType.Literal("playerlist"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.GetServers();
                sendMessage(sender, packet);
            }, ArgumentType.Literal("getservers"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.GetServer();
                sendMessage(sender, packet);
            }, ArgumentType.Literal("getserver"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.GetPlayerServer(sender);
                sendMessage(sender, packet);
            }, ArgumentType.Literal("getplayerserver"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.UUID();
                sendMessage(sender, packet);
            }, ArgumentType.Literal("uuid"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.UUIDOther(sender);
                sendMessage(sender, packet);
            }, ArgumentType.Literal("uuidother"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.ServerIP("lobby");
                sendMessage(sender, packet);
            }, ArgumentType.Literal("serverip"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.KickPlayer(sender, "Test kick reason");
                sendMessage(sender, packet);
            }, ArgumentType.Literal("kickplayer"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.KickPlayerRaw(sender, "{\"text\":\"Kicked!\"}");
                sendMessage(sender, packet);
            }, ArgumentType.Literal("kickplayerraw"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.Forward("lobby", "test:channel", new byte[]{1, 2, 3});
                sendMessage(sender, packet);
            }, ArgumentType.Literal("forward"));

            addSyntax((sender, args) -> {
                var packet = new BungeeRequest.ForwardToPlayer(sender, "test:channel", new byte[]{4, 5, 6});
                sendMessage(sender, packet);
            }, ArgumentType.Literal("forwardtoplayer"));
        }

        void sendMessage(CommandSender sender, BungeeMessage message) {
            if (!(sender instanceof Player player)) {
                throw new IllegalStateException("Command can only be executed by a player");
            }
            message.send(player);
        }
    }
}
