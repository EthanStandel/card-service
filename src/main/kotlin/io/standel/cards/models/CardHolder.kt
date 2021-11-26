package io.standel.cards.models

// CardHolders are the players or the table for the game
// The CardHolder's id should be the username or the table's gameId
class CardHolder(val id: String) {
    val cards = CardCollection(emptyList(), emptyList())
}

data class CardCollection(val private: List<Card>, val public: List<Card>)