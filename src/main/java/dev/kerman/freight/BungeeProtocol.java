package dev.kerman.freight;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * BungeeCord protocol interface.
 * <p>
 * This interface contains the protocol information for the BungeeCord messaging system.
 * It includes the channel name, data types, and message types.
 * <p>
 * The protocol is used to communicate between the server and BungeeCord proxy.
 */
final class BungeeProtocol {
    static final String CHANNEL = "BungeeCord"; // aka bungeecord:main
    static final NetworkBuffer.Type<List<String>> CSV_TYPE = NetworkBuffer.STRING_IO_UTF8.transform(
            string -> List.of(string.split(",")),
            stringList -> String.join(",", stringList)
    );
    // This type is awful, just to "save" 4 bytes.
    static final NetworkBuffer.Type<UUID> UUID_TYPE = NetworkBuffer.STRING_IO_UTF8
            .transform((string) -> new UUID(Long.parseUnsignedLong(string.substring(0, 16), 16),
                            Long.parseUnsignedLong(string.substring(16, 32), 16)
                    ),
                    uuid -> uuid.toString().replace("-", "")); // TODO see if we can skip replace
    static final NetworkBuffer.Type<byte[]> SHORT_BYTE_ARRAY_TYPE = new NetworkBuffer.Type<>() {
        // Reminder that they use big endian for IO, so we should be good as the protocol uses it too
        @Override
        public void write(@NotNull NetworkBuffer buffer, byte @NotNull [] value) {
            final int length = value.length;
            if (length > 65535) throw new IllegalStateException("Value too long");
            buffer.write(NetworkBuffer.UNSIGNED_SHORT, length);
            buffer.write(NetworkBuffer.FixedRawBytes(length), value);
        }

        @Override
        public byte[] read(@NotNull NetworkBuffer buffer) {
            final int length = buffer.read(NetworkBuffer.UNSIGNED_SHORT);
            if (length > 65535) throw new IllegalStateException("Value too long");
            if (buffer.readableBytes() > length) throw new IllegalStateException("Value too long to read");
            return buffer.read(NetworkBuffer.FixedRawBytes(length));
        }
    };

    static boolean isIdentifier(@Nullable String channel) {
        return CHANNEL.equals(channel) || "bungeecord:main".equals(channel);
    }

    // Reads the message from the buffer and checks if there are any leftover bytes
    static <T extends BungeeMessage> T read(@NotNull NetworkBuffer buffer, NetworkBuffer.@NotNull Type<T> type) throws IllegalStateException {
        final T read = buffer.read(type);
        final long readableBytes = buffer.readableBytes();
        if (readableBytes > 0) throw new IllegalStateException("%s message not fully read! %d bytes left over.".formatted(read.getClass().getName(), readableBytes));
        return read;
    }

    // This is awful, but I don't want a map lookup.
    enum Type {
        Connect(BungeeRequest.Connect.SERIALIZER, null),
        ConnectOther(BungeeRequest.ConnectOther.SERIALIZER, null),
        IP(BungeeRequest.IP.SERIALIZER, BungeeResponse.IP.SERIALIZER),
        IPOther(BungeeRequest.IPOther.SERIALIZER, BungeeResponse.IPOther.SERIALIZER),
        PlayerCount(BungeeRequest.PlayerCount.SERIALIZER, BungeeResponse.PlayerCount.SERIALIZER),
        PlayerList(BungeeRequest.PlayerList.SERIALIZER, BungeeResponse.PlayerList.SERIALIZER),
        GetServers(BungeeRequest.GetServers.SERIALIZER, BungeeResponse.GetServers.SERIALIZER),
        Message(BungeeRequest.Message.SERIALIZER, null),
        MessageRaw(BungeeRequest.MessageRaw.SERIALIZER, null),
        GetServer(BungeeRequest.GetServer.SERIALIZER, BungeeResponse.GetServer.SERIALIZER),
        GetPlayerServer(BungeeRequest.GetPlayerServer.SERIALIZER, BungeeResponse.GetPlayerServer.SERIALIZER),
        UUID(BungeeRequest.UUID.SERIALIZER, BungeeResponse.UUID.SERIALIZER),
        UUIDOther(BungeeRequest.UUIDOther.SERIALIZER, BungeeResponse.UUIDOther.SERIALIZER),
        ServerIp(BungeeRequest.ServerIP.SERIALIZER, BungeeResponse.ServerIP.SERIALIZER),
        KickPlayer(BungeeRequest.KickPlayer.SERIALIZER, null),
        KickPlayerRaw(BungeeRequest.KickPlayerRaw.SERIALIZER, null),
        Forward(BungeeRequest.Forward.SERIALIZER, BungeeResponse.Forward.SERIALIZER),
        ForwardToPlayer(BungeeRequest.ForwardToPlayer.SERIALIZER, BungeeResponse.Forward.SERIALIZER);

        // Always prefixed
        public static final NetworkBuffer.Type<Type> REQUEST_SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING_IO_UTF8, Type::name,
                Type::valueOf
        );

        // Sometimes unprefixed, so we need to handle that.
        public static final NetworkBuffer.Type<Type> RESPONSE_SERIALIZER = new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Type value) {
                if (value == Forward || value == ForwardToPlayer) return; // These are unprefixed, so we don't write the name.
                buffer.write(NetworkBuffer.STRING_IO_UTF8, value.name());
            }

            @Override
            public Type read(@NotNull NetworkBuffer buffer) {
                final long readIndex = buffer.readIndex();
                try { // Try to determine if the type is prefixed or not.
                    final String name = buffer.read(NetworkBuffer.STRING_IO_UTF8);
                    return Type.valueOf(name);
                } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {
                    buffer.readIndex(readIndex);
                }
                return Type.Forward; // They are unprefixed YAY!
            }
        };

        private final NetworkBuffer.Type<? extends BungeeRequest> requestSerializer;
        private final NetworkBuffer.Type<? extends BungeeResponse> responseSerializer;

        Type(NetworkBuffer.Type<? extends BungeeRequest> requestSerializer, NetworkBuffer.Type<? extends BungeeResponse> responseSerializer) {
            this.requestSerializer = requestSerializer;
            this.responseSerializer = responseSerializer;
        }

        // Could probably use polymorphism here, but it makes the classes have less information about the serialization
        // See the entries in the regular ClientPacket and ServerPacket not caring about the id near the data structure.
        static @NotNull Type toType(@NotNull BungeeMessage message) {
            return switch (message) {
                // Requests
                case BungeeRequest.Connect ignored -> Connect;
                case BungeeRequest.ConnectOther ignored -> ConnectOther;
                case BungeeRequest.IP ignored -> IP;
                case BungeeRequest.IPOther ignored -> IPOther;
                case BungeeRequest.PlayerCount ignored -> PlayerCount;
                case BungeeRequest.PlayerList ignored -> PlayerList;
                case BungeeRequest.GetServers ignored -> GetServers;
                case BungeeRequest.Message ignored -> Message;
                case BungeeRequest.MessageRaw ignored -> MessageRaw;
                case BungeeRequest.GetServer ignored -> GetServer;
                case BungeeRequest.GetPlayerServer ignored -> GetPlayerServer;
                case BungeeRequest.UUID ignored -> UUID;
                case BungeeRequest.UUIDOther ignored -> UUIDOther;
                case BungeeRequest.ServerIP ignored -> ServerIp;
                case BungeeRequest.KickPlayer ignored -> KickPlayer;
                case BungeeRequest.KickPlayerRaw ignored -> KickPlayerRaw;
                case BungeeRequest.Forward ignored -> Forward;
                case BungeeRequest.ForwardToPlayer ignored -> ForwardToPlayer;
                // Responses
                case BungeeResponse.IP ignored -> IP;
                case BungeeResponse.IPOther ignored -> IPOther;
                case BungeeResponse.PlayerCount ignored -> PlayerCount;
                case BungeeResponse.PlayerList ignored -> PlayerList;
                case BungeeResponse.GetServers ignored -> GetServers;
                case BungeeResponse.GetServer ignored -> GetServer;
                case BungeeResponse.GetPlayerServer ignored -> GetPlayerServer;
                case BungeeResponse.UUID ignored -> UUID;
                case BungeeResponse.UUIDOther ignored -> UUIDOther;
                case BungeeResponse.ServerIP ignored -> ServerIp;
                case BungeeResponse.Forward ignored -> Forward;
            };
        }

        public NetworkBuffer.Type<? extends BungeeResponse> responseSerializer() {
            return responseSerializer;
        }

        public NetworkBuffer.Type<? extends BungeeRequest> requestSerializer() {
            return requestSerializer;
        }
    }

    private BungeeProtocol() {}
}
