package dev.kerman.freight;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.Objects;

/**
 * BungeeCord request interface.
 * <p>
 * This interface holds all requests data structures for the BungeeCord protocol.
 * It includes the request types, serializers, and data types.
 * <p>
 * You can construct requests using {@link BungeeRequest} and send them to the server with {@link #send(Audience)}.
 */
public sealed interface BungeeRequest extends BungeeMessage {
    @ApiStatus.Experimental
    NetworkBuffer.Type<BungeeRequest> SERIALIZER = BungeeProtocol.Type.REQUEST_SERIALIZER
            .unionType(BungeeProtocol.Type::requestSerializer, BungeeProtocol.Type::toType);

    /**
     * Connects the sending audience to a server.
     * @param serverName Server name to connect to. (Defined in your proxy config)
     */
    record Connect(String serverName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<Connect> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Connect::serverName,
                Connect::new
        );

        public Connect {
            Objects.requireNonNull(serverName, "Server name cannot be null");
        }
    }

    /**
     * Connects a player to a server.
     * @param playerName Player name to connect.
     * @param serverName Server name to connect to. (Defined in your proxy config)
     */
    record ConnectOther(String playerName, String serverName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<ConnectOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ConnectOther::playerName,
                NetworkBuffer.STRING_IO_UTF8, ConnectOther::serverName,
                ConnectOther::new
        );

        public ConnectOther {
            Objects.requireNonNull(playerName, "Player name cannot be null");
            Objects.requireNonNull(serverName, "Server name cannot be null");
        }

        /**
         * Creates a ConnectOther request for a player.
         * @param player The player to connect.
         * @param serverName Server name to connect to. (Defined in your proxy config)
         */
        public ConnectOther(Pointered player, String serverName) {
            this(player.get(Identity.NAME).orElseThrow(), serverName);
        }
    }

    /**
     * Requests the IP address of the sending audience.
     */
    record IP() implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<IP> SERIALIZER = NetworkBufferTemplate.template(IP::new);
    }

    /**
     * Requests the IP address of a player.
     * @param playerName The name
     */
    record IPOther(String playerName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<IPOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, IPOther::playerName,
                IPOther::new
        );

        public IPOther {
            Objects.requireNonNull(playerName, "Player name cannot be null");
        }

        /**
         * Creates an IPOther request for a player.
         * @param player The player to get the IP address for.
         */
        public IPOther(Pointered player) {
            this(player.get(Identity.NAME).orElseThrow());
        }
    }

    /**
     * Requests the player count of a server.
     * <br>
     * You can get player count responses for all servers sending a {@link #all()} request.
     * @param serverName Player count of that server (Defined in your proxy config) or ALL for all defined servers.
     */
    record PlayerCount(String serverName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<PlayerCount> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerCount::serverName,
                PlayerCount::new
        );

        public PlayerCount {
            Objects.requireNonNull(serverName, "Server name cannot be null");
        }

        /**
         * A {@link PlayerCount} request for all players.
         * <br>
         * Expect multiple responses if there is more than one server with players on it.
         * @return A {@link PlayerCount} request for all players.
         */
        @Contract(pure = true)
        static PlayerCount all() {
            return new PlayerCount(ALL);
        }
    }

    /**
     * Requests the player list of a server.
     * <br>
     * You can get player list responses for all servers sending a {@link #all()} request.
     * @param serverName Player list of that server (Defined in your proxy config)
     */
    record PlayerList(String serverName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<PlayerList> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerList::serverName,
                PlayerList::new
        );

        public PlayerList {
            Objects.requireNonNull(serverName, "Server name cannot be null");
        }

        /**
         * A {@link PlayerList} request for all players.
         * <br>
         * Expect multiple responses if there is more than one server with players on it.
         * @return A {@link PlayerList} request for all players.
         */
        @Contract(pure = true)
        static PlayerList all() {
            return new PlayerList(ALL);
        }
    }

    /**
     * Requests the list of all servers. (As defined in your proxy config)
     */
    record GetServers() implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<GetServers> SERIALIZER = NetworkBufferTemplate.template(GetServers::new);
    }

    /**
     * Sends a message to a player.
     * @param playerName The name of the player to send the message to.
     * @param message The message to send to the player.
     */
    record Message(String playerName, String message) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<Message> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Message::playerName,
                NetworkBuffer.STRING_IO_UTF8, Message::message,
                Message::new
        );

        public Message {
            Objects.requireNonNull(playerName, "Player name cannot be null");
            Objects.requireNonNull(message, "Message cannot be null");
        }

        /**
         * Creates a Message request for a player.
         * @param player The player to send the message to.
         * @param message The message to send to the player.
         */
        public Message(Pointered player, String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }

        /**
         * Creates a Message request for a player with basic styling only.
         * @param player The player to send the message to.
         * @param message The message to send to the player as legacy text.
         */
        public Message(Pointered player, Component message) {
            this(player.get(Identity.NAME).orElseThrow(), LegacyComponentSerializer.legacySection().serialize(message));
        }

        /**
         * Creates a Message request for all players across all servers.
         * @param message The message to send to all players.
         * @return A Message request for all players.
         */
        @Contract(pure = true)
        static Message all(String message) {
            return new Message(ALL, message);
        }
    }

    /**
     * Sends a raw message to a player. The message is interpreted as proper components.
     * @param playerName The name of the player to send the raw message to.
     * @param message The raw message to send to the player formatted as JSON.
     */
    record MessageRaw(String playerName, String message) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<MessageRaw> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, MessageRaw::playerName,
                NetworkBuffer.STRING_IO_UTF8, MessageRaw::message,
                MessageRaw::new
        );

        public MessageRaw {
            Objects.requireNonNull(playerName, "Player name cannot be null");
            Objects.requireNonNull(message, "Message cannot be null");
        }

        /**
         * Creates a MessageRaw request for a player.
         * @param player The player to send the raw message to.
         * @param message The raw message to send to the player formatted as JSON.
         */
        public MessageRaw(Pointered player, String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }

        /**
         * Creates a MessageRaw request for a player.
         * @param playerName The name of the player to send the raw message to.
         * @param message The raw message to send to the player formatted as JSON.
         */
        public MessageRaw(String playerName, Component message) {
            this(playerName, GsonComponentSerializer.gson().serialize(message));
        }

        /**
         * Creates a MessageRaw request for a player.
         * @param player The player to send the raw message to.
         * @param message The raw message to send to the player formatted as JSON.
         */
        public MessageRaw(Pointered player, Component message) {
            this(player.get(Identity.NAME).orElseThrow(), GsonComponentSerializer.gson().serialize(message));
        }

        /**
         * Creates a MessageRaw request for all players across all servers.
         * @param message The message to send to all players.
         * @return A MessageRaw request for all players.
         */
        @Contract(pure = true)
        static MessageRaw all(String message) {
            return new MessageRaw(ALL, message);
        }

        /**
         * Creates a MessageRaw request for all players across all servers.
         * @param message The message to send to all players.
         * @return A MessageRaw request for all players.
         */
        @Contract(pure = true)
        static MessageRaw all(Component message) {
            return new MessageRaw(ALL, message);
        }
    }

    /**
     * Requests the server the sending audience is connected to.
     */
    record GetServer() implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<GetServer> SERIALIZER = NetworkBufferTemplate.template(GetServer::new);
    }

    /**
     * Requests the server a player is connected to.
     * @param playerName The name of the player to get the server for.
     */
    record GetPlayerServer(String playerName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<GetPlayerServer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, GetPlayerServer::playerName,
                GetPlayerServer::new
        );

        public GetPlayerServer {
            Objects.requireNonNull(playerName, "Player name cannot be null");
        }

        public GetPlayerServer(Pointered player) {
            this(player.get(Identity.NAME).orElseThrow());
        }
    }

    /**
     * Requests the UUID of the sending audience.
     */
    record UUID() implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<UUID> SERIALIZER = NetworkBufferTemplate.template(UUID::new);
    }

    /**
     * Requests the UUID of a player.
     * @param playerName The name of the player to get the UUID for.
     */
    record UUIDOther(String playerName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<UUIDOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, UUIDOther::playerName,
                UUIDOther::new
        );

        public UUIDOther {
            Objects.requireNonNull(playerName, "Player name cannot be null");
        }

        public UUIDOther(Pointered player) {
            this(player.get(Identity.NAME).orElseThrow());
        }
    }

    /**
     * Requests the IP address and port of a server.
     * @param serverName The name of the server to get the IP and port for. (Defined in your proxy config)
     */
    record ServerIP(String serverName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<ServerIP> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ServerIP::serverName,
                ServerIP::new
        );

        public ServerIP {
            Objects.requireNonNull(serverName, "Server name cannot be null");
        }
    }

    /**
     * Kicks a player from the server with a reason.
     * @param playerName The name of the player to kick.
     * @param reason The reason for kicking the player.
     */
    record KickPlayer(String playerName, String reason) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<KickPlayer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, KickPlayer::playerName,
                NetworkBuffer.STRING_IO_UTF8, KickPlayer::reason,
                KickPlayer::new
        );

        public KickPlayer {
            Objects.requireNonNull(playerName, "Player name cannot be null");
            Objects.requireNonNull(reason, "Reason cannot be null");
        }

        /**
         * Creates a KickPlayer request for a player.
         * @param player The player to kick.
         * @param message The reason for kicking the player.
         */
        public KickPlayer(Pointered player, String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }
    }

    /**
     * Kicks a player from the server with a reason.
     * @param playerName The name of the player to kick.
     * @param reason The reason for kicking the player as a JSON string.
     */
    record KickPlayerRaw(String playerName, String reason) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<KickPlayerRaw> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, KickPlayerRaw::playerName,
                NetworkBuffer.STRING_IO_UTF8, KickPlayerRaw::reason,
                KickPlayerRaw::new
        );

        public KickPlayerRaw {
            Objects.requireNonNull(playerName, "Player name cannot be null");
            Objects.requireNonNull(reason, "Reason cannot be null");
        }

        /**
         * Creates a KickPlayerRaw request for a player.
         * @param player The player to kick.
         * @param message The reason for kicking the player.
         */
        public KickPlayerRaw(Pointered player, String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }

        /**
         * Creates a KickPlayerRaw request for a player.
         * @param playerName The name of the player to kick.
         * @param message The reason for kicking the player.
         */
        public KickPlayerRaw(String playerName, Component message) {
            this(playerName, GsonComponentSerializer.gson().serialize(message));
        }

        /**
         * Creates a KickPlayerRaw request for a player.
         * @param player The player to kick.
         * @param message The reason for kicking the player.
         */
        public KickPlayerRaw(Pointered player, Component message) {
            this(player.get(Identity.NAME).orElseThrow(), GsonComponentSerializer.gson().serialize(message));
        }
    }

    /**
     * Forwards data to a server or all servers. (Always excluding the current one)
     * <br>
     * Forward it to a specific player using {@link ForwardToPlayer},
     * use {@link #all(PluginMessagePacket)} to send a single message to all servers,
     * or {@link #online(PluginMessagePacket)} to send to online servers
     * @param serverName The name of the server to forward the data to. Supports ALL and ONLINE.
     * @param channel The channel to forward the data to.
     * @param data The data to forward.
     */
    record Forward(String serverName, String channel, byte[] data) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<Forward> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Forward::serverName,
                NetworkBuffer.STRING_IO_UTF8, Forward::channel,
                BungeeProtocol.SHORT_BYTE_ARRAY_TYPE, Forward::data,
                Forward::new
        );

        public Forward {
            Objects.requireNonNull(serverName, "Server cannot be null");
            Objects.requireNonNull(channel, "Channel cannot be null");
            Objects.requireNonNull(data, "Data cannot be null");
            if (data.length > 65535) // Check length before cloning
                throw new IllegalArgumentException("Data cannot be more than 65535 in length");
            data = data.clone();
        }

        /**
         * Creates a {@link Forward} request for all servers. (excluding the server this request is sent from)
         * @param channel The channel to forward the data to.
         * @param data The data to forward.
         * @return A {@link Forward} request for all servers.
         */
        @Contract(pure = true)
        static Forward all(String channel, byte[] data) {
            return new Forward(ALL, channel, data);
        }

        /**
         * Creates a {@link Forward} request for all servers. (excluding the server this request is sent from)
         * @param packet The PluginMessagePacket to forward.
         * @return A {@link Forward} request for all servers.
         */
        @Contract(pure = true)
        static Forward all(PluginMessagePacket packet) {
            return all(packet.channel(), packet.data());
        }

        /**
         * Creates a {@link Forward} request for all servers. (excluding the server this request is sent from)
         * @param packet The ClientPluginMessagePacket to forward.
         * @return A {@link Forward} request for all servers.
         */
        @Contract(pure = true)
        static Forward all(ClientPluginMessagePacket packet) {
            return all(packet.channel(), packet.data());
        }

        /**
         * Creates a {@link Forward} request for the online server. (excluding the server this request is sent from)
         * @param channel The channel to forward the data to.
         * @param data The data to forward.
         * @return A {@link Forward} request for the online server.
         */
        @Contract(pure = true)
        static Forward online(String channel, byte[] data) {
            return new Forward(ONLINE, channel, data);
        }

        /**
         * Creates a {@link Forward} request for the online server. (excluding the server this request is sent from)
         * @param packet The PluginMessagePacket to forward.
         * @return A {@link Forward} request for the online server.
         */
        @Contract(pure = true)
        static Forward online(PluginMessagePacket packet) {
            return online(packet.channel(), packet.data());
        }

        /**
         * Creates a {@link Forward} request for the online server. (excluding the server this request is sent from)
         * @param packet The ClientPluginMessagePacket to forward.
         * @return A {@link Forward} request for the online server.
         */
        @Contract(pure = true)
        static Forward online(ClientPluginMessagePacket packet) {
            return online(packet.channel(), packet.data());
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Forward(String server1, String channel1, byte[] data1))) return false;
            return serverName().equals(server1) && channel().equals(channel1) && Arrays.equals(data(), data1);
        }

        @Override
        public int hashCode() {
            int result = serverName().hashCode();
            result = 31 * result + channel().hashCode();
            result = 31 * result + Arrays.hashCode(data());
            return result;
        }
    }

    /**
     * Forwards data to a specific player.
     * <br>
     * Note: The response for this is the same as {@link Forward} as BungeeCord provides no way to differ the requests.
     * @param playerName The name of the player to forward the data to.
     * @param channel The channel to forward the data to.
     * @param data The data to forward.
     */
    record ForwardToPlayer(String playerName, String channel,
                           byte[] data) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<ForwardToPlayer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ForwardToPlayer::playerName,
                NetworkBuffer.STRING_IO_UTF8, ForwardToPlayer::channel,
                BungeeProtocol.SHORT_BYTE_ARRAY_TYPE, ForwardToPlayer::data,
                ForwardToPlayer::new
        );

        public ForwardToPlayer {
            Objects.requireNonNull(playerName, "Player name cannot be null");
            Objects.requireNonNull(channel, "Channel cannot be null");
            Objects.requireNonNull(data, "Data cannot be null");
            if (data.length > 65535) // Check length before cloning
                throw new IllegalArgumentException("Data cannot be more than 65535 in length");
            data = data.clone();
        }

        /**
         * Creates a ForwardToPlayer request for a specific player.
         * @param player The player to forward the data to.
         * @param channel The channel to forward the data to.
         * @param data The data to forward.
         */
        public ForwardToPlayer(Pointered player, String channel, byte[] data) {
            this(player.get(Identity.NAME).orElseThrow(), channel, data);
        }

        /**
         * Creates a ForwardToPlayer request for a specific player.
         * @param playerName The name of the player to forward the data to.
         * @param message The PluginMessagePacket to forward.
         */
        public ForwardToPlayer(String playerName, PluginMessagePacket message) {
            this(playerName, message.channel(), message.data());
        }

        /**
         * Creates a ForwardToPlayer request for a specific player.
         * @param player The player to forward the data to.
         * @param message The PluginMessagePacket to forward.
         */
        public ForwardToPlayer(Pointered player, PluginMessagePacket message) {
            this(player, message.channel(), message.data());
        }

        /**
         * Creates a ForwardToPlayer request for a specific player.
         * @param playerName The name of the player to forward the data to.
         * @param message The ClientPluginMessagePacket to forward.
         */
        public ForwardToPlayer(String playerName, ClientPluginMessagePacket message) {
            this(playerName, message.channel(), message.data());
        }

        /**
         * Creates a ForwardToPlayer request for a specific player.
         * @param player The player to forward the data to.
         * @param message The ClientPluginMessagePacket to forward.
         */
        public ForwardToPlayer(Pointered player, ClientPluginMessagePacket message) {
            this(player, message.channel(), message.data());
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof ForwardToPlayer(String playerName1, String channel1, byte[] data1))) return false;
            return playerName().equals(playerName1) && channel().equals(channel1) && Arrays.equals(data(), data1);
        }

        @Override
        public int hashCode() {
            int result = playerName().hashCode();
            result = 31 * result + channel().hashCode();
            result = 31 * result + Arrays.hashCode(data());
            return result;
        }
    }
}
