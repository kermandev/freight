package dev.kerman.freight;

import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * BungeeCord response interface.
 * <p>
 * This interface holds all response data structures for the BungeeCord protocol.
 * It includes the request types, serializers, and data types.
 * <p>
 * The most common use cases are to deserialize the response from a {@link NetworkBuffer} or {@code byte[]}.
 * To do this, you can use the {@link BungeeMessage#readResponse(NetworkBuffer)} method.
 * There is also a shorthand version for events {@link BungeeMessage#readResponse(PlayerPluginMessageEvent)}
 */
public sealed interface BungeeResponse extends BungeeMessage {
    @ApiStatus.Experimental
    NetworkBuffer.Type<BungeeResponse> SERIALIZER = BungeeProtocol.Type.SERIALIZER
            .unionType(BungeeProtocol.Type::responseSerializer, BungeeProtocol.Type::toType);

    /**
     * Unused by default; potential response to a {@link BungeeRequest.Connect} request.
     */
    record Connect() implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<Connect> SERIALIZER = NetworkBufferTemplate.template(Connect::new);
    }

    /**
     * Unused by default; potential response to a {@link BungeeRequest.ConnectOther} request.
     */
    record ConnectOther() implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<ConnectOther> SERIALIZER = NetworkBufferTemplate.template(ConnectOther::new);
    }

    /**
     * A response containing the IP address and port of the server.
     * @param ip the IP address of the server
     * @param port the port of the server, must be non-negative and less than or equal to 65535
     */
    record IP(@NotNull String ip, int port) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<IP> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, IP::ip,
                NetworkBuffer.INT, IP::port,
                IP::new
        );

        public IP {
            Check.notNull(ip, "IP cannot be null");
            Check.argCondition(port < 0 || port > 65535, "Port must be non negative and less than or equal to 65535");
        }
    }

    /**
     * A response containing the player name, IP address, and port of another player.
     * @param playerName the name of the player
     * @param ip the IP address of the player
     * @param port the port of the player, must be non-negative and less than or equal to 65535
     */
    record IPOther(@NotNull String playerName, @NotNull String ip, int port) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<IPOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, IPOther::playerName,
                NetworkBuffer.STRING_IO_UTF8, IPOther::ip,
                NetworkBuffer.INT, IPOther::port,
                IPOther::new
        );

        public IPOther {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(ip, "IP cannot be null");
            Check.argCondition(port <= 0 || port > 65535, "Port must be greater than 0 and less than or equal to 65535");
        }
    }

    /**
     * A response containing the server name and the number of players online.
     * @param serverName the name of the server
     * @param playerCount the number of players online, must be greater than 0
     */
    record PlayerCount(@NotNull String serverName, int playerCount) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<PlayerCount> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerCount::serverName,
                NetworkBuffer.INT, PlayerCount::playerCount,
                PlayerCount::new
        );

        public PlayerCount {
            Check.notNull(serverName, "Server name cannot be null");
            Check.argCondition(playerCount <= 0, "Player count must be greater than 0");
        }
    }

    /**
     * A response containing the server name and a list of player names.
     * @param serverName the name of the server
     * @param playerNameList the list of player names, must not be null or empty
     */
    record PlayerList(@NotNull String serverName, @NotNull List<String> playerNameList) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<PlayerList> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, PlayerList::serverName,
                BungeeProtocol.CSV_TYPE, PlayerList::playerNameList,
                PlayerList::new
        );

        public PlayerList {
            Check.notNull(serverName, "Server name cannot be null");
            Check.notNull(playerNameList, "Player name list cannot be null");
            playerNameList = List.copyOf(playerNameList);
        }
    }

    /**
     * A response containing a list of server names.
     * @param serverNames the list of server names (As defined in the proxy config)
     */
    record GetServers(@NotNull List<String> serverNames) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<GetServers> SERIALIZER = NetworkBufferTemplate.template(
                BungeeProtocol.CSV_TYPE, GetServers::serverNames,
                GetServers::new
        );

        public GetServers {
            Check.notNull(serverNames, "Server names cannot be null");
            serverNames = List.copyOf(serverNames);
        }
    }

    /**
     * Unused by default; potential response to a {@link BungeeRequest.Message} request.
     */
    record Message() implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<Message> SERIALIZER = NetworkBufferTemplate.template(Message::new);
    }

    /**
     * Unused by default; potential response to a {@link BungeeRequest.MessageRaw} request.
     */
    record MessageRaw() implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<MessageRaw> SERIALIZER = NetworkBufferTemplate.template(MessageRaw::new);
    }

    /**
     * A response containing the server name.
     * @param serverName the name of the server
     */
    record GetServer(@NotNull String serverName) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<GetServer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, GetServer::serverName,
                GetServer::new
        );

        public GetServer {
            Check.notNull(serverName, "Server name cannot be null");
        }
    }

    /**
     * A response containing the player name and server name.
     * @param playerName the name of the player
     * @param serverName the name of the server
     */
    record GetPlayerServer(@NotNull String playerName, @NotNull String serverName) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<GetPlayerServer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, GetPlayerServer::playerName,
                NetworkBuffer.STRING_IO_UTF8, GetPlayerServer::serverName,
                GetPlayerServer::new
        );

        public GetPlayerServer {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(serverName, "Server name cannot be null");
        }
    }

    /**
     * A response containing a UUID.
     * @param uuid the UUID of the player, must not be null
     */
    record UUID(@NotNull java.util.UUID uuid) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<UUID> SERIALIZER = NetworkBufferTemplate.template(
                BungeeProtocol.UUID_TYPE, UUID::uuid,
                UUID::new
        );

        public UUID {
            Check.notNull(uuid, "UUID cannot be null");
        }
    }

    /**
     * A response containing a player name and UUID.
     * @param playerName
     * @param uuid
     */
    record UUIDOther(@NotNull String playerName, @NotNull java.util.UUID uuid) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<UUIDOther> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, UUIDOther::playerName,
                BungeeProtocol.UUID_TYPE, UUIDOther::uuid,
                UUIDOther::new
        );

        public UUIDOther {
            Check.notNull(playerName, "Player name cannot be null");
            Check.notNull(uuid, "UUID cannot be null");
        }
    }

    /**
     * A response containing the server name, IP address, and port.
     * @param serverName the name of the server
     * @param ip the IP address of the server
     * @param port the port of the server, must be greater than 0 and less than or equal to 65535
     */
    record ServerIP(@NotNull String serverName, @NotNull String ip, int port) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<ServerIP> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ServerIP::serverName,
                NetworkBuffer.STRING_IO_UTF8, ServerIP::ip,
                NetworkBuffer.UNSIGNED_SHORT, ServerIP::port,
                ServerIP::new
        );

        public ServerIP {
            Check.notNull(serverName, "Server name cannot be null");
            Check.notNull(ip, "IP cannot be null");
            // Port is an unsigned short (so we use an int).
            Check.argCondition(port <= 0 || port > 65535, "Port must be greater than 0 and less than or equal to 65535");
        }
    }

    /**
     * Unused by default; potential response to a {@link BungeeRequest.KickPlayer} request.
     */
    record KickPlayer() implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<KickPlayer> SERIALIZER = NetworkBufferTemplate.template(KickPlayer::new);
    }

    /**
     * Unused by default; potential response to a {@link BungeeRequest.KickPlayerRaw} request.
     */
    record KickPlayerRaw() implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<KickPlayerRaw> SERIALIZER = NetworkBufferTemplate.template(KickPlayerRaw::new);
    }

    /**
     * A response to forward a message to a specific channel.
     * @param channel the channel forwarded
     * @param data the data forwarded
     */
    record Forward(@NotNull String channel, byte @NotNull [] data) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<Forward> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Forward::channel,
                BungeeProtocol.SHORT_BYTE_ARRAY_TYPE, Forward::data,
                Forward::new
        );

        public Forward {
            Check.notNull(channel, "Channel cannot be null");
            Check.notNull(data, "Data cannot be null");
            data = data.clone();
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Forward(String channel1, byte[] data1))) return false;
            return channel().equals(channel1) && Arrays.equals(data(), data1);
        }

        @Override
        public int hashCode() {
            int result = channel().hashCode();
            result = 31 * result + Arrays.hashCode(data());
            return result;
        }
    }

    /**
     * A response to forward a message to a specific player.
     * @param channel the channel forwarded
     * @param data the data forwarded
     */
    record ForwardToPlayer(@NotNull String channel, byte @NotNull [] data) implements BungeeResponse {
        @ApiStatus.Experimental
        public static final NetworkBuffer.Type<ForwardToPlayer> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, ForwardToPlayer::channel,
                BungeeProtocol.SHORT_BYTE_ARRAY_TYPE, ForwardToPlayer::data,
                ForwardToPlayer::new
        );

        public ForwardToPlayer {
            Check.notNull(channel, "Channel cannot be null");
            Check.notNull(data, "Data cannot be null");
            data = data.clone();
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof ForwardToPlayer(String channel1, byte[] data1))) return false;
            return channel().equals(channel1) && Arrays.equals(data(), data1);
        }

        @Override
        public int hashCode() {
            int result = channel().hashCode();
            result = 31 * result + Arrays.hashCode(data());
            return result;
        }
    }
}
