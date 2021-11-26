# card-service

## Purpose

A multiplayer service which will connect users for generic card games. Uses restful principals
to accept incoming data and uses a websocket layer to manage keeping player clients up to date.

## Tooling overview

- **Build**: [Gradle](https://gradle.org/)
- **Language**: [Kotlin](https://kotlinlang.org/)
- **Framework**: [Ktor](https://nextjs.org/)
- **Deploy service**: [Digital Ocean App Platform](https://www.digitalocean.com/products/app-platform/)
- **Deploy platform**: [Docker](https://www.docker.com/)

## Running locally

### Any platform

```bash
./gradlew
./gradlew shadowJar
java -jar ./build/libs/io.standel.cards.card-service-0.0.1-all.jar
```

### For best local experience

- Install IntelliJ (Community or IDEA)
- Run the `main` function in `io.standel.cards.ApplicationKt`

## Package overview

- `io.standel.cards.models` - data shape representations
- `io.standel.cards.plugins` - important plugins for Ktor (routing, websockets, and serialization)
- `io.standel.cards.repositories` - data-transfer layers
- `io.standel.cards.utils` - code-level synchronous utilities