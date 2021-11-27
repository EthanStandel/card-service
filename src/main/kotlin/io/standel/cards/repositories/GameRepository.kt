package io.standel.cards.repositories

import com.google.gson.Gson
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.standel.cards.models.Game
import io.standel.cards.models.response.SocketMessage
import io.standel.cards.models.SocketSession
import kotlinx.coroutines.*
import org.slf4j.Logger
import java.util.Collections
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

class GameRepository(
    private val log: Logger,
    private val socketSessions: MutableSet<SocketSession>,
    private val gson: Gson
) {
    // A very fancy in-memory database for POC
    private val games: MutableMap<String, Game> = Collections.synchronizedMap(mutableMapOf())
    private val gameCleanupJobs: MutableMap<String, Job> = Collections.synchronizedMap(mutableMapOf())

    private suspend fun updateSessions(socketMessage: SocketMessage, gameId: String) {
        socketSessions.filter { it.gameId == gameId }.forEach {
            it.session.send(Frame.Text(gson.toJson(socketMessage)))
        }
    }

    suspend fun createGame(): Game {
        val newGame = Game()
        this.games[newGame.id] = newGame
        log.info("Created game ${newGame.id}")
        // Remove the game if it's never used
        startGameCleanupListener(newGame)
        return newGame
    }

    fun fetchGame(gameId: String): Game {
        return this.games[gameId] ?: throw NotFoundException("No game found for $gameId")
    }

    private fun removeGame(gameId: String) {
        this.games[gameId] ?: throw NotFoundException("No game found for $gameId")
        log.info("Removing game $gameId")
        this.games.remove(gameId)
    }

    fun cancelCleanupListener(game: Game) {
        val oldJob = gameCleanupJobs[game.id]
        if (oldJob != null) {
            log.info("Closing out old cleanup listener for game ${game.id}")
            oldJob.cancel()
            gameCleanupJobs.remove(game.id)
        }
    }

    // remove empty games after events which could cause them
    suspend fun startGameCleanupListener(game: Game) {
        // Ensure a game doesn't have multiple listeners on it
        cancelCleanupListener(game)
        gameCleanupJobs[game.id] = GlobalScope.launch {
            log.info("Creating cleanup listener for ${game.id}")
            // If this game has no players after delay, delete it
            delay(timeMillis = 1.minutes.toLong(DurationUnit.MILLISECONDS))
            if (game.players.isEmpty()) {
                log.info("Clearing out unused game ${game.id}")
                removeGame(game.id)
            }
        }
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
