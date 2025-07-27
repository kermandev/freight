# Freight

Freight is a library for working with BungeeCord plugin messaging in Minestom. It provides a type-safe, modern, and extensible API for sending and receiving BungeeCord plugin messages, including requests and responses for common BungeeCord actions.

## Features
- Type-safe request and response objects for BungeeCord plugin messaging
- Utilities for sending messages through adventures audiences
- Easy integration with Minestom event system

## Usage

### Sending a Request
```java
Player player = ...;
BungeeRequest request = new BungeeRequest.UUID();
request.send(player);
```

### Receiving a Response
Using the shorthand version of BungeeMessage#readResponse
```java
var eventNode = ...
eventNode.addListener(PlayerPluginMessageEvent.class, event -> {
    BungeeResponse response = BungeeMessage.readResponse(event);
    if (response == null) return;
    // Handle the response
});
```

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
The latest version of Freight is published to Maven Central. To use it in your project

add the following dependency:

```kts
repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.kerman:freight:$VERSION")
}
```
Where `$VERSION` can be found on [Maven Central](https://central.sonatype.com/artifact/dev.kerman/freight).
For example, the latest version as of now is `1.2.0`
## Documentation
The hosted Javadocs are hosted [here](https://javadoc.io/doc/dev.kerman/freight)

## Building
This project uses Gradle. To build:

```sh
gradlew build
```

## License
See [LICENSE](../LICENSE).

