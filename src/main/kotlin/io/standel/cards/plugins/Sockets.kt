package io.standel.cards.plugins

import com.google.gson.Gson
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.standel.cards.models.CardHolder
import io.standel.cards.models.SocketSession
import io.standel.cards.models.response.SocketMessage
import io.standel.cards.repositories.GameRepository
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
        val gameRepo by inject<GameRepository>()
        val gson by inject<Gson>()
        val log by inject<Logger>()

        webSocket("/game/{gameId}/events/{username}") {
            val gameId = call.parameters["gameId"]
            val username = call.parameters["username"]
            if (gameId == null || username == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, gson.toJson(SocketMessage("MISSING_PARAMS"))))
            } else {
                val socketSession = SocketSession(this, gameId, username)
                log.info("Opening connection for $username at game $gameId")
                socketSessions += socketSession
                // Add player to game
                try {
                    val game = gameRepo.fetchGame(gameId)
                    game.players[username] = CardHolder(username)

                    try {
                        // Waiting for close
                        for (frame in incoming) {
                            log.info("Incoming frame from $username ignored")
                        }
                    } finally {
                        log.info("Closing connection for $username at game $gameId")
                        socketSessions -= socketSession
                        // Remove player from game
                        game.players.remove(username)
                        // Cleanup the game if it's empty
                        gameRepo.cleanupGameSession(game)
                    }
                } catch (exception: NotFoundException) {
                    log.info("Closing connection for $username due to no game $gameId")
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, gson.toJson(SocketMessage("GAME_NOT_FOUND"))))
                }
            }
        }
    }
}
