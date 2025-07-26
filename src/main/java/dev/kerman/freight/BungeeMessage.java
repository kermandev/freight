package dev.kerman.freight;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.network.packet.server.common.PluginMessagePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * BungeeCord Messaging interface.
 * <p>
 * This interface holds all the methods to read and write BungeeCord messages.
 * It also provides methods to send messages to audiences and player connections.
 * <p>
 * You can construct requests using {@link BungeeRequest} and responses using {@link BungeeResponse}.
 * For example, to send a request to a player connection, you can do:
 * <pre>
 * {@code
 *     Player player = ...;
 *     BungeeRequest request = new BungeeRequest.UUID();
 *     request.send(connection);
 * }
 * </pre>
 * <p>
 * To receive the response, you can listen to the {@link PlayerPluginMessageEvent} event.
 * For example, to receive a response from a player connection, you can do:
 * <pre>
 * {@code
 * eventNode.addListener(PlayerPluginMessageEvent.class, event -> {
 *     if (!BungeeMessage.isIdentifier(event.channel())) return;
 *     BungeeResponse response = BungeeMessage.readResponse(event.getMessage());
 *     // Handle the response here
 *     switch (response) {
 *         case BungeeResponse.UUID(UUID uuid) -> {
 *             // Handle the UUID response
 *         }
 *         ...
 *     }
 * });
 * }
 * </pre>
 * Or you can use the shorthand version with {@link BungeeMessage#readResponse(PlayerPluginMessageEvent)}
 * Using the example below:
 * <pre>
 * {@code
 * eventNode.addListener(PlayerPluginMessageEvent.class, event -> {
 *      BungeeResponse response = BungeeMessage.readResponse(event);
 *      if (response == null) return;
 *      // Handle the response here
 *      switch (response) {
 *          ...
 *      }
 * });
 * }
 * </pre>
 * Some notes about this class include attempting to hide all the serialization implementation behind the protocol,
 * We still expose all the serializers for the requests and responses in case you want to use them, but don't expect them
 * to be stable, but they will probably be stable as the messaging system is likely to not change.
 */
public sealed interface BungeeMessage permits BungeeRequest, BungeeResponse {
    String ALL = "ALL";
    String ONLINE = "ONLINE";

    /**
     * Checks if the given channel is a valid BungeeCord identifier.
     * <p>Valid identifiers are:
     * <ul>
     *     <li>{@code BungeeCord} (the default channel)</li>
     *     <li>{@code bungeecord:main} (since 1.13)</li>
     * </ul>
     * </p>
     *
     * @param channel the channel to check, can be null
     * @return true if the channel is a valid BungeeCord identifier, false otherwise
     */
    static boolean isIdentifier(@Nullable String channel) {
        return BungeeProtocol.isIdentifier(channel);
    }

    /**
     * Writes the {@link BungeeMessage} to a byte array.
     * <p>Defers the implementation to the correct interface</p>
     *
     * @param message the message to write
     * @return the byte array containing the serialized message
     */
    static byte @NotNull [] write(@NotNull BungeeMessage message) {
        Check.notNull(message, "Message cannot be null");
        return switch (message) {
            case BungeeRequest request -> writeRequest(request);
            case BungeeResponse response -> writeResponse(response);
        };
    }

    // Requests

    /**
     * Writes the {@link BungeeRequest} to a byte array.
     * <p>You should use the {@link BungeeMessage#send(Audience)} when available.</p>
     *
     * @param request the request to write
     * @return the byte array containing the serialized request
     */
    static byte @NotNull [] writeRequest(@NotNull BungeeRequest request) {
        Check.notNull(request, "Request cannot be null");
        return NetworkBuffer.makeArray(BungeeRequest.SERIALIZER, request);
    }

    /**
     * Reads the {@link BungeeRequest} from the byte array.
     * Shares any side effects with {@link #readRequest(NetworkBuffer)}.
     *
     * @param bytes the byte array to read the request from
     * @return the request, never null
     * @throws IllegalStateException if there are leftover bytes in the buffer after reading the request
     */
    static @NotNull BungeeRequest readRequest(byte @NotNull [] bytes) throws IllegalStateException {
        Check.notNull(bytes, "Bytes cannot be null!");
        return readRequest(NetworkBuffer.wrap(bytes, 0, bytes.length));
    }

    /**
     * Reads the {@link BungeeRequest} from the {@link NetworkBuffer}.
     * <p> This method will warn if there are any leftover bytes in the buffer after reading the response.</p>
     *
     * @param buffer the buffer to read the response from
     * @return the response, never null
     * @throws IllegalStateException if there are leftover bytes in the buffer after reading the request
     */
    static @NotNull BungeeRequest readRequest(@NotNull NetworkBuffer buffer) throws IllegalStateException {
        Check.notNull(buffer, "Buffer cannot be null!");
        return BungeeProtocol.read(buffer, BungeeRequest.SERIALIZER);
    }

    // Responses

    /**
     * Writes the {@link BungeeResponse} to a byte array.
     *
     * @param response the response to write
     * @return the byte array containing the serialized response
     */
    static byte @NotNull [] writeResponse(@NotNull BungeeResponse response) {
        Check.notNull(response, "Response cannot be null");
        return NetworkBuffer.makeArray(BungeeResponse.SERIALIZER, response);
    }

    /**
     * Reads the {@link BungeeResponse} from the byte array.
     *
     * @param bytes the byte array to read the response from
     * @return the response, never null
     * @throws IllegalStateException if there are leftover bytes in the buffer after reading the request
     */
    static @NotNull BungeeResponse readResponse(byte @NotNull [] bytes) throws IllegalStateException {
        Check.notNull(bytes, "Bytes cannot be null!");
        return readResponse(NetworkBuffer.wrap(bytes, 0, bytes.length));
    }

    /**
     * Reads the {@link BungeeResponse} from the {@link NetworkBuffer}.
     * <p> This method will warn if there are any leftover bytes in the buffer after reading the response.</p>
     *
     * @param buffer the buffer to read the response from
     * @return the response, never null
     * @throws IllegalStateException if there are leftover bytes in the buffer after reading the request
     */
    static @NotNull BungeeResponse readResponse(@NotNull NetworkBuffer buffer) throws IllegalStateException {
        Check.notNull(buffer, "Buffer cannot be null!");
        return BungeeProtocol.read(buffer, BungeeResponse.SERIALIZER);
    }

    /**
     * Reads the response from the {@link PlayerPluginMessageEvent} event.
     * <p>
     * A shorthand method to read the response from a player plugin message event, safe to use with incorrect identifier.
     * </p>
     *
     * @param event the event to read the response from
     * @return the response, or null if the event is not a BungeeCord message
     * @throws IllegalStateException if there are leftover bytes in the buffer after reading the request
     */
    static @Nullable BungeeResponse readResponse(@NotNull PlayerPluginMessageEvent event) throws IllegalStateException {
        Check.notNull(event, "Event cannot be null");
        if (!isIdentifier(event.getIdentifier())) return null;
        return readResponse(event.getMessage());
    }

    /**
     * Sends the message to the specified player connection.
     * <p>
     * This method is used to send messages directly through a player's connection.
     * In most cases, you will want to use the {@link #send(Audience)} method instead.
     * </p>
     *
     * @param connection the player connection to send the message to
     * @param message    the message to send
     */
    static void send(@NotNull PlayerConnection connection, @NotNull BungeeMessage message) {
        Check.notNull(connection, "Connection cannot be null");
        Check.notNull(message, "Message cannot be null");
        connection.sendPacket(message.toPacket());
    }

    /**
     * Sends the message to the specified audience.
     * <p>
     * You can use this method to send to a single player or a group of players.
     * </p>
     *
     * @param audience the audience to send the message
     * @param message  the message to send
     */
    static void send(@NotNull Audience audience, @NotNull BungeeMessage message) {
        Check.notNull(audience, "Audience cannot be null");
        Check.notNull(message, "Message cannot be null");
        PacketSendingUtils.sendPacket(audience, message.toPacket());
    }

    /**
     * Sends the message to a single audience from a collection of audiences.
     * <p>Shuffles the collection and sends the message to a random audience.</p>
     *
     * @param audiences the collection of audiences to send the message to
     * @param message   the message to send
     * @throws IllegalArgumentException if the collection is empty
     */
    static void sendSingle(@NotNull Collection<? extends Audience> audiences, @NotNull BungeeMessage message) {
        Check.notNull(audiences, "Audiences cannot be null");
        Check.notNull(message, "Message cannot be null");
        Check.argCondition(audiences.isEmpty(), "Audiences cannot be empty");
        List<Audience> collection = new ArrayList<>(audiences);
        Collections.shuffle(collection);
        BungeeMessage.send(collection.getFirst(), message);
    }

    /**
     * Converts the message to a {@link ClientPluginMessagePacket}.
     * <p>
     * This method is used to create messages from a client/proxy.
     * </p>
     *
     * @return the client plugin message packet containing the serialized message
     */
    default @NotNull ClientPluginMessagePacket toClientPacket() {
        return new ClientPluginMessagePacket(BungeeProtocol.CHANNEL, write(this));
    }

    /**
     * Converts the message to a {@link PluginMessagePacket}.
     * <p>
     * This method is used to create messages to the client/proxy.
     * </p>
     *
     * @return the plugin message packet containing the serialized message
     */
    default @NotNull PluginMessagePacket toPacket() {
        return new PluginMessagePacket(BungeeProtocol.CHANNEL, write(this));
    }

    /**
     * Sends the message to the specified player connection.
     * <p>
     * This method is used to send messages directly through a player's connection.
     * In most cases, you will want to use the {@link #send(Audience)} method instead.
     * </p>
     *
     * @param connection the player connection to send the message to
     */
    default void send(@NotNull PlayerConnection connection) {
        Check.notNull(connection, "Connection cannot be null");
        BungeeMessage.send(connection, this);
    }

    /**
     * Sends the message to the specified audience.
     * <p>
     * You can use this method to send to a single player or a group of players.
     * </p>
     *
     * @param audience the audience to send the message
     */
    default void send(@NotNull Audience audience) {
        Check.notNull(audience, "Audience cannot be null");
        BungeeMessage.send(audience, this);
    }

    /**
     * Sends the message to a single audience from a collection of audiences.
     * <p>
     * Shuffles the collection and sends the message to a random audience.
     * </p>
     *
     * @param audiences the collection of audiences to send the message to
     * @throws IllegalArgumentException if the collection is empty
     */
    default void sendSingle(@NotNull Collection<? extends Audience> audiences) {
        Check.notNull(audiences, "Audience cannot be null");
        Check.argCondition(audiences.isEmpty(), "Audiences cannot be empty");
        BungeeMessage.sendSingle(audiences, this);
    }
}
