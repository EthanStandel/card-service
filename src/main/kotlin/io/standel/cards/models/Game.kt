package io.standel.cards.models

import io.standel.cards.utils.generateGameId

data class Game(
    val id: String = generateGameId(),
    val players: MutableMap<String, CardHolder> = mutableMapOf(),
    val table: CardHolder = CardHolder(id),
    val decks: MutableList<MutableList<Card>> = mutableListOf(createDeck())
)
