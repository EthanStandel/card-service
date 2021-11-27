package io.standel.cards.models

// CardHolders are the players or the table for the game
// The CardHolder's id should be the username or the table's gameId
data class CardHolder(val id: String, val cards: CardCollection = CardCollection())

data class CardCollection(val private: MutableList<Card> = mutableListOf(), val public: MutableList<Card> = mutableListOf())