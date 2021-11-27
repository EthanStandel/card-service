package io.standel.cards.plugins

import com.google.gson.Gson
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.standel.cards.models.SocketSession
import io.standel.cards.models.request.IncomingSocketMessage
import io.standel.cards.models.response.OutgoingMessageType
import io.standel.cards.models.response.OutgoingSocketMessage
import io.standel.cards.services.GameManager
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
        val gameManager by inject<GameManager>()
        val gson by inject<Gson>()
        val log by inject<Logger>()

        webSocket("/game/{gameId}/events/{username}") {
            val gameId = call.parameters["gameId"]
            val username = call.parameters["username"]
            if (gameId == null || username == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, gson.toJson(OutgoingSocketMessage(OutgoingMessageType.MISSING_PARAMS))))
            } else {
                val socketSession = SocketSession(this, gameId, username)
                log.info("Opening connection for $username at game $gameId")
                socketSessions += socketSession
                try {
                    // Add player to game
                    gameManager.connectToGame(gameId, username)

                    try {
                        // Waiting for close
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                try {
                                    val message = gson.fromJson(frame.readText(), IncomingSocketMessage::class.java)
                                    gameManager.handleEvent(message, username, gameId)
                                } catch (e: Exception) {
                                    socketSession.session.send(gson.toJson(OutgoingSocketMessage(OutgoingMessageType.BAD_REQUEST, mapOf("message" to (e.message ?: "")))))
                                }
                            } else {
                                log.info("Incoming non-text frame from $username ignored")
                            }
                        }
                    } finally {
                        log.info("Closing connection for $username at game $gameId")
                        socketSessions -= socketSession
                        // Remove player from game
                        gameManager.disconnectFromGame(gameId, username)
                    }
                } catch (exception: NotFoundException) {
                    log.info("Closing connection for $username due to no game $gameId")
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, gson.toJson(OutgoingSocketMessage(OutgoingMessageType.GAME_NOT_FOUND))))
                }
            }
        }
    }
}
