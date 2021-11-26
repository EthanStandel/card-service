package io.standel.cards.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.request.*
import io.standel.cards.models.request.ShuffleDeckRequest
import io.standel.cards.models.response.GameCreationResponse
import io.standel.cards.models.response.GenericStatus
import io.standel.cards.repositories.GameRepository
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val gameRepo by inject<GameRepository>()

    routing {
        get("/game") {
            val game = gameRepo.createGame()
            call.respond(GameCreationResponse(game.id))
        }

        route("/game/{gameId}") {
            get {
                val gameId = call.parameters["gameId"] ?: throw NotFoundException("Missing gameId parameter")
                val game = gameRepo.fetchGame(gameId)
                call.respond(game)
            }

            post("/shuffle") {
                val gameId = call.parameters["gameId"] ?: throw NotFoundException("Missing gameId parameter")
                val deckIndex = call.receive<ShuffleDeckRequest>().deckIndex
                gameRepo.shuffleDeck(gameId, deckIndex)
                call.respond(GenericStatus())
            }
        }
    }
}
