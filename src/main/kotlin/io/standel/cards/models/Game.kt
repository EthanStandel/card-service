package io.standel.cards.models

import io.standel.cards.utils.generateGameId

class Game {
    val id = generateGameId()
    val players = mutableMapOf<String, CardHolder>()
    val table = CardHolder(this.id)
    val decks = mutableListOf(createDeck())
}
