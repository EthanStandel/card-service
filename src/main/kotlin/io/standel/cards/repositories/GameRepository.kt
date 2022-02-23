package io.standel.cards.repositories

import com.google.gson.Gson
import io.ktor.features.*
import io.standel.cards.models.Card
import io.standel.cards.models.CardHolder
import io.standel.cards.models.Game
import org.slf4j.Logger
import java.util.Collections

class GameRepository(
    private val log: Logger,
    private val gson: Gson
) {
    // A very fancy in-memory database
    private val games: MutableMap<String, Game> = Collections.synchronizedMap(mutableMapOf())

    fun createGame(): Game {
        val newGame = Game()
        games[newGame.id] = newGame
        log.info("Created game ${newGame.id}")
        return newGame
    }

    fun fetchGame(gameId: String): Game {
        return this.games[gameId] ?: throw NotFoundException("No game found for $gameId")
    }

    fun fetchCensoredGame(gameId: String): Game {
        val game = fetchGame(gameId)
        val gameJson = gson.toJson(game)
        val gameCopy = gson.fromJson(gameJson, Game::class.java)
        gameCopy.players.values.forEach {
            it.cards.private.forEach {
                it.suit = "UNKNOWN"
                it.value = "UNKNOWN"
            }
        }
        gameCopy.decks.forEach {
            it.map {
                it.suit = "UNKNOWN"
                it.value = "UNKNOWN"
            }
        }
        gameCopy.table.cards.private.forEach {
            it.suit = "UNKNOWN"
            it.value = "UNKNOWN"
        }
        return gameCopy
    }

    fun fetchPlayer(game: Game, username: String): CardHolder {
        return game.players[username] ?: throw NotFoundException("No player $username found for game ${game.id}")
    }

    private fun fetchDeck(game: Game, deckIndex: Int): MutableList<Card> {
        return if (deckIndex in game.decks.indices) {
            game.decks[deckIndex]
        } else {
            throw NotFoundException("No deckIndex $deckIndex found for game ${game.id}")
        }
    }

    fun removeGame(gameId: String) {
        fetchGame(gameId)
        log.info("Removing game $gameId")
        games.remove(gameId)
    }

    fun addPlayer(gameId: String, username: String) {
        val game = fetchGame(gameId)
        if (game.players[username] != null) {
            throw BadRequestException("Username $username already exists in game $gameId")
        }
        game.players[username] = CardHolder(username)
    }

    fun shuffleDeck(gameId: String, deckIndex: Int) {
        val game = fetchGame(gameId)
        if (deckIndex !in game.decks.indices) {
            throw NotFoundException("No deck found for $deckIndex in $gameId")
        }
        val deck = game.decks[deckIndex]
        deck.shuffle()
    }

    fun drawPrivateCard(gameId: String, username: String, deckIndex: Int): Card {
        val game = fetchGame(gameId)
        val player = fetchPlayer(game, username)
        val deck = fetchDeck(game, deckIndex)
        val card = deck.removeFirst()
        player.cards.private.add(card)
        return card
    }

    fun drawPublicCard(gameId: String, username: String, deckIndex: Int): Card {
        val game = fetchGame(gameId)
        val player = fetchPlayer(game, username)
        val deck = fetchDeck(game, deckIndex)
        val card = deck.removeFirst()
        player.cards.public.add(card)
        return card
    }
}
