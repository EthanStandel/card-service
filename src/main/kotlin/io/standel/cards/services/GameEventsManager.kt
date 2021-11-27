package io.standel.cards.services

import com.google.gson.Gson
import io.ktor.features.*
import io.standel.cards.models.request.IncomingMessageType
import io.standel.cards.models.request.IncomingSocketMessage
import io.standel.cards.models.response.OutgoingMessageType
import io.standel.cards.models.response.OutgoingSocketMessage
import io.standel.cards.models.response.SocketEventResponse
import io.standel.cards.repositories.GameRepository

class GameEventsManager(
    private val gameRepo: GameRepository,
    private val gson: Gson
) {
    fun handleIncomingEvent(request: IncomingSocketMessage, username: String, gameId: String): SocketEventResponse {
        when (request.type) {
            IncomingMessageType.SHUFFLE_DECK -> {
                val deckIndex = request.payload["deckIndex"]?.toInt() ?: throw BadRequestException("Malformed payload, deckIndex required")
                gameRepo.shuffleDeck(gameId, deckIndex)
                return SocketEventResponse(
                    username, gameId,
                    publicMessage = OutgoingSocketMessage(OutgoingMessageType.DECK_SHUFFLED, payload = mapOf("user" to username, "deckIndex" to deckIndex.toString()))
                )
            } IncomingMessageType.DRAW_PRIVATE_CARD -> {
                val deckIndex = request.payload["deckIndex"]?.toInt() ?: throw BadRequestException("Malformed payload, deckIndex required")
                val card = gameRepo.drawPrivateCard(gameId, username, deckIndex)
                return SocketEventResponse(
                    username, gameId,
                    publicMessage = OutgoingSocketMessage(OutgoingMessageType.PRIVATE_CARD_DRAWN, payload = mapOf("user" to username)),
                    privateMessage = OutgoingSocketMessage(OutgoingMessageType.PRIVATE_CARD_DRAWN, payload = mapOf("user" to username, "card" to gson.toJson(card)))
                )
            } IncomingMessageType.DRAW_PUBLIC_CARD -> {
                val deckIndex = request.payload["deckIndex"]?.toInt() ?: throw BadRequestException("Malformed payload, deckIndex required")
                val card = gameRepo.drawPublicCard(gameId, username, deckIndex)
                return SocketEventResponse(
                    username, gameId,
                    publicMessage = OutgoingSocketMessage(OutgoingMessageType.PUBLIC_CARD_DRAWN, payload = mapOf("user" to username, "card" to gson.toJson(card)))
                )
            } IncomingMessageType.FETCH_PRIVATE_CARDS -> {
                val game = gameRepo.fetchGame(gameId)
                val player = gameRepo.fetchPlayer(game, username)
                return SocketEventResponse(
                    username, gameId,
                    privateMessage = OutgoingSocketMessage(OutgoingMessageType.PRIVATE_CARD_FETCH, payload = mapOf("user" to username, "cards" to gson.toJson(player.cards.private)))
                )
            }
        }
    }
}