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
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
    NetworkBuffer.Type<BungeeRequest> SERIALIZER = BungeeProtocol.Type.SERIALIZER
            .unionType(BungeeProtocol.Type::requestSerializer, BungeeProtocol.Type::toType);

    /**
     * Connects the sending audience to a server.
     * @param serverName Server name to connect to. (Defined in your proxy config)
     */
    record Connect(@NotNull String serverName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<Connect> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Connect::serverName,
                Connect::new
        );

        public Connect {
            Check.notNull(serverName, "Server name cannot be null");
        }
    }

    /**
     * Connects a player to a server.
     * @param playerName Player name to connect.
     * @param serverName Server name to connect to. (Defined in your proxy config)
     */
    record ConnectOther(@NotNull String playerName, @NotNull String serverName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<ConnectOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ConnectOther::playerName,
                NetworkBuffer.STRING_IO_UTF8, ConnectOther::serverName,
                ConnectOther::new
        );

        public ConnectOther {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(serverName, "Server name cannot be null");
        }

        /**
         * Creates a ConnectOther request for a player.
         * @param player The player to connect.
         * @param serverName Server name to connect to. (Defined in your proxy config)
         */
        public ConnectOther(@NotNull Pointered player, @NotNull String serverName) {
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
    record IPOther(@NotNull String playerName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<IPOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, IPOther::playerName,
                IPOther::new
        );

        public IPOther {
            Check.notNull(playerName, "Player name cannot be null");
        }

        /**
         * Creates an IPOther request for a player.
         * @param player The player to get the IP address for.
         */
        public IPOther(@NotNull Pointered player) {
            this(player.get(Identity.NAME).orElseThrow());
        }
    }

    /**
     * Requests the player count of a server.
     * @param serverName Player count of that server (Defined in your proxy config)
     */
    record PlayerCount(@NotNull String serverName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<PlayerCount> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerCount::serverName,
                PlayerCount::new
        );

        public PlayerCount {
            Check.notNull(serverName, "Server name cannot be null");
        }

        /**
         * @return A PlayerCount request for all players.
         */
        static @NotNull PlayerCount all() {
            return new PlayerCount(ALL);
        }
    }

    /**
     * Requests the player list of a server.
     * @param serverName Player list of that server (Defined in your proxy config)
     */
    record PlayerList(@NotNull String serverName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<PlayerList> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerList::serverName,
                PlayerList::new
        );

        public PlayerList {
            Check.notNull(serverName, "Server name cannot be null");
        }

        /**
         * @return A PlayerList request for all players.
         */
        static @NotNull PlayerList all() {
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
    record Message(@NotNull String playerName, @NotNull String message) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<Message> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Message::playerName,
                NetworkBuffer.STRING_IO_UTF8, Message::message,
                Message::new
        );

        public Message {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(message, "Message cannot be null");
        }

        /**
         * Creates a Message request for a player.
         * @param player The player to send the message to.
         * @param message The message to send to the player.
         */
        public Message(@NotNull Pointered player, @NotNull String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }

        /**
         * Creates a Message request for a player with basic styling only.
         * @param player The player to send the message to.
         * @param message The message to send to the player as legacy text.
         */
        public Message(@NotNull Pointered player, @NotNull Component message) {
            this(player.get(Identity.NAME).orElseThrow(), LegacyComponentSerializer.legacySection().serialize(message));
        }

        /**
         * Creates a Message request for all players across all servers.
         * @param message The message to send to all players.
         * @return A Message request for all players.
         */
        static @NotNull Message all(@NotNull String message) {
            return new Message(ALL, message);
        }
    }

    /**
     * Sends a raw message to a player. The message is interpreted as proper components.
     * @param playerName The name of the player to send the raw message to.
     * @param message The raw message to send to the player formatted as JSON.
     */
    record MessageRaw(@NotNull String playerName, @NotNull String message) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<MessageRaw> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, MessageRaw::playerName,
                NetworkBuffer.STRING_IO_UTF8, MessageRaw::message,
                MessageRaw::new
        );

        public MessageRaw {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(message, "Message cannot be null");
        }

        /**
         * Creates a MessageRaw request for a player.
         * @param player The player to send the raw message to.
         * @param message The raw message to send to the player formatted as JSON.
         */
        public MessageRaw(@NotNull Pointered player, @NotNull String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }

        /**
         * Creates a MessageRaw request for a player.
         * @param playerName The name of the player to send the raw message to.
         * @param message The raw message to send to the player formatted as JSON.
         */
        public MessageRaw(@NotNull String playerName, @NotNull Component message) {
            this(playerName, GsonComponentSerializer.gson().serialize(message));
        }

        /**
         * Creates a MessageRaw request for a player.
         * @param player The player to send the raw message to.
         * @param message The raw message to send to the player formatted as JSON.
         */
        public MessageRaw(@NotNull Pointered player, @NotNull Component message) {
            this(player.get(Identity.NAME).orElseThrow(), GsonComponentSerializer.gson().serialize(message));
        }

        /**
         * Creates a MessageRaw request for all players across all servers.
         * @param message The message to send to all players.
         * @return A MessageRaw request for all players.
         */
        static @NotNull MessageRaw all(@NotNull String message) {
            return new MessageRaw(ALL, message);
        }

        /**
         * Creates a MessageRaw request for all players across all servers.
         * @param message The message to send to all players.
         * @return A MessageRaw request for all players.
         */
        static @NotNull MessageRaw all(@NotNull Component message) {
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
    record GetPlayerServer(@NotNull String playerName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<GetPlayerServer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, GetPlayerServer::playerName,
                GetPlayerServer::new
        );

        public GetPlayerServer {
            Check.notNull(playerName, "Player name cannot be null");
        }

        public GetPlayerServer(@NotNull Pointered player) {
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
    record UUIDOther(@NotNull String playerName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<UUIDOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, UUIDOther::playerName,
                UUIDOther::new
        );

        public UUIDOther {
            Check.notNull(playerName, "Player name cannot be null");
        }

        public UUIDOther(@NotNull Pointered player) {
            this(player.get(Identity.NAME).orElseThrow());
        }
    }

    /**
     * Requests the IP address and port of a server.
     * @param serverName The name of the server to get the IP and port for. (Defined in your proxy config)
     */
    record ServerIP(@NotNull String serverName) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<ServerIP> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ServerIP::serverName,
                ServerIP::new
        );

        public ServerIP {
            Check.notNull(serverName, "Server name cannot be null");
        }
    }

    /**
     * Kicks a player from the server with a reason.
     * @param playerName The name of the player to kick.
     * @param reason The reason for kicking the player.
     */
    record KickPlayer(@NotNull String playerName, @NotNull String reason) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<KickPlayer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, KickPlayer::playerName,
                NetworkBuffer.STRING_IO_UTF8, KickPlayer::reason,
                KickPlayer::new
        );

        public KickPlayer {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(reason, "Reason cannot be null");
        }

        /**
         * Creates a KickPlayer request for a player.
         * @param player The player to kick.
         * @param message The reason for kicking the player.
         */
        public KickPlayer(@NotNull Pointered player, @NotNull String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }
    }

    /**
     * Kicks a player from the server with a reason.
     * @param playerName The name of the player to kick.
     * @param reason The reason for kicking the player as a JSON string.
     */
    record KickPlayerRaw(@NotNull String playerName, @NotNull String reason) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<KickPlayerRaw> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, KickPlayerRaw::playerName,
                NetworkBuffer.STRING_IO_UTF8, KickPlayerRaw::reason,
                KickPlayerRaw::new
        );

        public KickPlayerRaw {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(reason, "Reason cannot be null");
        }

        /**
         * Creates a KickPlayerRaw request for a player.
         * @param player The player to kick.
         * @param message The reason for kicking the player.
         */
        public KickPlayerRaw(@NotNull Pointered player, @NotNull String message) {
            this(player.get(Identity.NAME).orElseThrow(), message);
        }

        /**
         * Creates a KickPlayerRaw request for a player.
         * @param playerName The name of the player to kick.
         * @param message The reason for kicking the player.
         */
        public KickPlayerRaw(@NotNull String playerName, @NotNull Component message) {
            this(playerName, GsonComponentSerializer.gson().serialize(message));
        }

        /**
         * Creates a KickPlayerRaw request for a player.
         * @param player The player to kick.
         * @param message The reason for kicking the player.
         */
        public KickPlayerRaw(@NotNull Pointered player, @NotNull Component message) {
            this(player.get(Identity.NAME).orElseThrow(), GsonComponentSerializer.gson().serialize(message));
        }
    }

    /**
     * Forwards data to a server or all servers.
     * @param serverName The name of the server to forward the data to. Supports ALL and ONLINE.
     * @param channel The channel to forward the data to.
     * @param data The data to forward.
     */
    record Forward(@NotNull String serverName, @NotNull String channel, byte @NotNull [] data) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<Forward> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Forward::serverName,
                NetworkBuffer.STRING_IO_UTF8, Forward::channel,
                BungeeProtocol.SHORT_BYTE_ARRAY_TYPE, Forward::data,
                Forward::new
        );

        public Forward {
            Check.notNull(serverName, "Server cannot be null");
            Check.notNull(channel, "Channel cannot be null");
            Check.notNull(data, "Data cannot be null");
            Check.argCondition(data.length > 65535, "Data cannot be more than a 65535 in length");
            data = data.clone();
        }

        /**
         * Creates a Forward request for all servers. (excluding the server this request is sent from)
         * @param channel The channel to forward the data to.
         * @param data The data to forward.
         * @return A Forward request for all servers.
         */
        static @NotNull Forward all(@NotNull String channel, byte @NotNull [] data) {
            return new Forward(ALL, channel, data);
        }

        /**
         * Creates a Forward request for all servers. (excluding the server this request is sent from)
         * @param packet The PluginMessagePacket to forward.
         * @return A Forward request for all servers.
         */
        static @NotNull Forward all(@NotNull PluginMessagePacket packet) {
            return all(packet.channel(), packet.data());
        }

        /**
         * Creates a Forward request for all servers. (excluding the server this request is sent from)
         * @param packet The ClientPluginMessagePacket to forward.
         * @return A Forward request for all servers.
         */
        static @NotNull Forward all(@NotNull ClientPluginMessagePacket packet) {
            return all(packet.channel(), packet.data());
        }

        /**
         * Creates a Forward request for the online server. (excluding the server this request is sent from)
         * @param channel The channel to forward the data to.
         * @param data The data to forward.
         * @return A Forward request for the online server.
         */
        static @NotNull Forward online(@NotNull String channel, byte @NotNull [] data) {
            return new Forward(ONLINE, channel, data);
        }

        /**
         * Creates a Forward request for the online server. (excluding the server this request is sent from)
         * @param packet The PluginMessagePacket to forward.
         * @return A Forward request for the online server.
         */
        static @NotNull Forward online(@NotNull PluginMessagePacket packet) {
            return online(packet.channel(), packet.data());
        }

        /**
         * Creates a Forward request for the online server. (excluding the server this request is sent from)
         * @param packet The ClientPluginMessagePacket to forward.
         * @return A Forward request for the online server.
         */
        static @NotNull Forward online(@NotNull ClientPluginMessagePacket packet) {
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
     * @param playerName The name of the player to forward the data to.
     * @param channel The channel to forward the data to.
     * @param data The data to forward.
     */
    record ForwardToPlayer(@NotNull String playerName, @NotNull String channel,
                           byte @NotNull [] data) implements BungeeRequest {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<ForwardToPlayer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ForwardToPlayer::playerName,
                NetworkBuffer.STRING_IO_UTF8, ForwardToPlayer::channel,
                BungeeProtocol.SHORT_BYTE_ARRAY_TYPE, ForwardToPlayer::data,
                ForwardToPlayer::new
        );

        public ForwardToPlayer {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(channel, "Channel cannot be null");
            Check.notNull(data, "Data cannot be null");
            Check.argCondition(data.length > 65535, "Data cannot be more than a 65535 in length");
            data = data.clone();
        }

        /**
         * Creates a ForwardToPlayer request for a specific player.
         * @param player The player to forward the data to.
         * @param channel The channel to forward the data to.
         * @param data The data to forward.
         */
        public ForwardToPlayer(@NotNull Pointered player, @NotNull String channel, byte @NotNull [] data) {
            this(player.get(Identity.NAME).orElseThrow(), channel, data);
        }

        /**
         * Creates a ForwardToPlayer request for a specific player.
         * @param playerName The name of the player to forward the data to.
         * @param message The PluginMessagePacket to forward.
         */
        public ForwardToPlayer(@NotNull String playerName, @NotNull PluginMessagePacket message) {
            this(playerName, message.channel(), message.data());
        }

        /**
         * Creates a ForwardToPlayer request for a specific player.
         * @param player The player to forward the data to.
         * @param message The PluginMessagePacket to forward.
         */
        public ForwardToPlayer(@NotNull Pointered player, @NotNull PluginMessagePacket message) {
            this(player, message.channel(), message.data());
        }

        /**
         * Creates a ForwardToPlayer request for a specific player.
         * @param playerName The name of the player to forward the data to.
         * @param message The ClientPluginMessagePacket to forward.
         */
        public ForwardToPlayer(@NotNull String playerName, @NotNull ClientPluginMessagePacket message) {
            this(playerName, message.channel(), message.data());
        }

        /**
         * Creates a ForwardToPlayer request for a specific player.
         * @param player The player to forward the data to.
         * @param message The ClientPluginMessagePacket to forward.
         */
        public ForwardToPlayer(@NotNull Pointered player, @NotNull ClientPluginMessagePacket message) {
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
