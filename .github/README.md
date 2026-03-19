# Freight

Freight is a library for working with BungeeCord plugin messages in Minestom. It provides bindings for sending and receiving BungeeCord plugin messages, by including types for requests and responses for [BungeeCord plugin messaging](https://docs.papermc.io/paper/dev/plugin-messaging/#plugin-message-types).

## Features
- Type-safe request and response objects for BungeeCord plugin messaging
- Utilities for sending messages through adventures audiences
- Easy integration with Minestom event system

## Supported Requests & Responses
- Connect, ConnectOther
- IP, IPOther
- PlayerCount, PlayerList
- GetServers, GetServer, GetPlayerServer
- UUID, UUIDOther
- ServerIP
- KickPlayer, KickPlayerRaw
- Forward, ForwardToPlayer
- Message, MessageRaw

## Installation
The latest version of Freight is published to Maven Central. To use it in your project add the following dependency:

```kts
repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.kerman:freight:$VERSION")
}
```
Where `$VERSION` can be found on [Maven Central](https://central.sonatype.com/artifact/dev.kerman/freight) or by using the latest release dropping the `v`.
For example, the latest version as of writing is `1.6.2`.

## Usage
### Importing
```java
import module dev.kerman.freight;
// or
import dev.kerman.freight.BungeeMessage;
import dev.kerman.freight.BungeeRequest;
import dev.kerman.freight.BungeeResponse;
```

### Sending a Request
```java
Player player = ...;
BungeeRequest request = new BungeeRequest.UUID();
request.send(player);
```

### Receiving a Response
Using the shorthand version of `BungeeMessage#readResponse(PlayerPluginMessageEvent)`
```java
var eventNode = ...
eventNode.addListener(PlayerPluginMessageEvent.class, event -> {
    BungeeResponse response = BungeeMessage.readResponse(event);
    if (response == null) return;
    // Handle the response here
    switch (response) {
        case BungeeResponse.UUID(UUID uuid) -> {
            // Handle the UUID response
        }
        ...
    }
});
```
## Documentation
Javadocs are hosted [here](https://javadoc.io/doc/dev.kerman/freight)

## Building
This project uses Gradle. To build:

```sh
gradlew build
```

## License
See [LICENSE](../LICENSE).

