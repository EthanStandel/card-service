package io.standel.cards.plugins

import com.google.gson.Gson
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.application.*
import io.ktor.routing.*
import io.standel.cards.models.SocketSession
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import org.slf4j.Logger

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        val socketSessions by inject<MutableSet<SocketSession>>(named("socketSessions"))
        val log by inject<Logger>()

        webSocket("/game/{gameId}/events/{userName}") {
            val gameId = call.parameters["gameId"]
            val userName = call.parameters["userName"]
            if (gameId == null || userName == null) {
                close()
            } else {
                val socketSession = SocketSession(this, gameId, userName)
                socketSessions += socketSession
                log.info("Opening connection for $userName")

                try {
                    // Waiting for close
                    for (frame in incoming) {
                        log.info("Incoming frame")
                    }
                } finally {
                    log.info("Closing connection for $userName")
                    socketSessions -= socketSession
                }
            }

        }
    }
}
