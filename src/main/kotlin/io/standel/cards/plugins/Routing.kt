package io.standel.cards.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.standel.cards.repositories.GameRepository
import io.standel.cards.services.GameManager
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val gameManager by inject<GameManager>()
    val gameRepo by inject<GameRepository>()

    routing {
        get("/game") {
            val gameId = gameManager.createGame()
            call.respond(gameRepo.fetchGame(gameId))
        }

        route("/game/{gameId}") {
            get {
                val gameId = call.parameters["gameId"] ?: throw NotFoundException("Missing gameId parameter")
                val game = gameRepo.fetchCensoredGame(gameId)
                call.respond(game)
            }
        }
    }
}
