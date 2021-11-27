package io.standel.cards.services

import com.google.gson.Gson
import io.ktor.http.cio.websocket.*
import io.standel.cards.models.Game
import io.standel.cards.models.SocketSession
import io.standel.cards.models.request.IncomingSocketMessage
import io.standel.cards.models.response.OutgoingMessageType
import io.standel.cards.models.response.OutgoingSocketMessage
import io.standel.cards.repositories.GameRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.Logger
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

class GameManager(
    private val gameEvents: GameEventsManager,
    private val gameRepo: GameRepository,
    private val gson: Gson,
    private val log: Logger,
    private val socketSessions: MutableSet<SocketSession>,
) {
    private val gameCleanupJobs: MutableMap<String, Job> = Collections.synchronizedMap(mutableMapOf())

    suspend fun createGame(): String {
        val game = gameRepo.createGame()
        // Just in case no one joins
        startGameCleanupListener(game)
        return game.id
    }

    suspend fun handleEvent(request: IncomingSocketMessage, username: String, gameId: String) {
        val response = gameEvents.handleIncomingEvent(request, username, gameId)
        if (response.publicMessage != null) {
            updateGameSessions(response.publicMessage, gameId,
                // If there's a private message, don't send the public message to the requesting user
                if (response.privateMessage == null) listOf() else listOf(username)
            )
        }

        if (response.privateMessage != null) {
            updateUserSession(response.privateMessage, gameId, username)
        }
    }

    suspend fun connectToGame(gameId: String, username: String) {
        gameRepo.addPlayer(gameId, username)
        log.info("Game $gameId has been populated by $username")
        cancelCleanupListener(gameId)
        updateGameSessions(OutgoingSocketMessage(OutgoingMessageType.NEW_PLAYER, mapOf("user" to username)), gameId)
    }

    suspend fun disconnectFromGame(gameId: String, username: String) {
        gameRepo.removePlayer(gameId, username)
        val game = gameRepo.fetchGame(gameId)
        // Cleanup the game if it's empty
        if (game.players.isEmpty()) {
            startGameCleanupListener(game)
        }
    }

    private suspend fun updateUserSession(message: OutgoingSocketMessage, gameId: String, username: String) {
        socketSessions.filter { it.gameId == gameId && it.username == username }
            .forEach { it.session.send(Frame.Text(gson.toJson(message))) }
    }

    private suspend fun updateGameSessions(message: OutgoingSocketMessage, gameId: String, excludedUsers: List<String> = emptyList()) {
        socketSessions.filter { it.gameId == gameId && !excludedUsers.contains(it.username) }.forEach {
            it.session.send(Frame.Text(gson.toJson(message)))
        }
    }

    // remove empty games after events which could cause them
    private suspend fun startGameCleanupListener(game: Game) {
        // Ensure a game doesn't have a listener already on it
        cancelCleanupListener(game)
        gameCleanupJobs[game.id] = GlobalScope.launch {
            log.info("Creating cleanup listener for ${game.id}")
            // If this game has no players after delay, delete it
            delay(timeMillis = 1.minutes.toLong(DurationUnit.MILLISECONDS))
            if (game.players.isEmpty()) {
                log.info("Clearing out unused game ${game.id}")
                gameRepo.removeGame(game.id)
            }
            gameCleanupJobs.remove(game.id)
        }
    }

    private fun cancelCleanupListener(game: Game) {
        val oldJob = gameCleanupJobs[game.id]
        if (oldJob != null) {
            log.info("Closing out cleanup listener for game ${game.id}")
            oldJob.cancel()
            gameCleanupJobs.remove(game.id)
        }
    }

    private fun cancelCleanupListener(gameId: String) {
        cancelCleanupListener(gameRepo.fetchGame(gameId))
    }
}