package io.standel.cards.repositories

import com.google.gson.Gson
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.standel.cards.models.Game
import io.standel.cards.models.response.SocketMessage
import io.standel.cards.models.SocketSession
import java.util.*

class GameRepository(
    private val socketSessions: MutableSet<SocketSession>,
    private val gson: Gson
) {
    // A very fancy in-memory database for POC
    private val games: MutableMap<String, Game> = Collections.synchronizedMap(mutableMapOf())

    private suspend fun updateSessions(socketMessage: SocketMessage, gameId: String) {
        socketSessions.filter { it.gameId == gameId }.forEach {
            it.session.send(Frame.Text(gson.toJson(socketMessage)))
        }
    }

    fun createGame(): Game {
        val newGame = Game()
        this.games[newGame.id] = newGame
        return newGame
    }

    fun fetchGame(gameId: String): Game {
        val game = this.games[gameId] ?: throw NotFoundException("No game found for $gameId")
        return game
    }

    fun removeGame(gameId: String) {
        this.games[gameId] ?: throw NotFoundException("No game found for $gameId")
        this.games.remove(gameId)
    }

    suspend fun shuffleDeck(gameId: String, deckIndex: Int) {
        val game = this.games[gameId] ?: throw NotFoundException("No game found for $gameId")
        val deck = game.decks[deckIndex] ?: throw NotFoundException("No deck found for $deckIndex in $gameId")
        deck.shuffle()
        // Inform relevant socketSessions
        this.updateSessions(
            SocketMessage(
                "DECK_SHUFFLE",
                payload = mapOf("deckIndex" to deckIndex.toString())
            ), gameId
        )
    }
}
