package dev.kerman.freight;

import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
            ip = Objects.requireNonNull(ip, "IP cannot be null");
            if (port < 0 || port > 65535)
                throw new IllegalArgumentException("Port must be non negative and less than or equal to 65535");
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
            playerName = Objects.requireNonNull(playerName, "Player name cannot be null");
            ip = Objects.requireNonNull(ip, "IP cannot be null");
            if (port < 0 || port > 65535)
                throw new IllegalArgumentException("Port must be non negative and less than or equal to 65535");
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
            serverName = Objects.requireNonNull(serverName, "Server name cannot be null");
            if (playerCount <= 0)
                throw new IllegalArgumentException("Player count must not be negative 0");
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
            serverName = Objects.requireNonNull(serverName, "Server name cannot be null");
            playerNameList = List.copyOf(Objects.requireNonNull(playerNameList, "Player name list cannot be null"));
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
            serverNames = List.copyOf(Objects.requireNonNull(serverNames, "Server names cannot be null"));
        }
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
            serverName = Objects.requireNonNull(serverName, "Server name cannot be null");
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
            playerName = Objects.requireNonNull(playerName, "Player name cannot be null");
            serverName = Objects.requireNonNull(serverName, "Server name cannot be null");
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
            uuid = Objects.requireNonNull(uuid, "UUID cannot be null");
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
            playerName = Objects.requireNonNull(playerName, "Player name cannot be null");
            uuid = Objects.requireNonNull(uuid, "UUID cannot be null");
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
            serverName = Objects.requireNonNull(serverName, "Server name cannot be null");
            ip = Objects.requireNonNull(ip, "IP cannot be null");
            if (port <= 0 || port > 65535)
                throw new IllegalArgumentException("Port must be greater than 0 and less than or equal to 65535");
        }
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
            channel = Objects.requireNonNull(channel, "Channel cannot be null");
            data = Objects.requireNonNull(data, "Data cannot be null").clone();
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
}
