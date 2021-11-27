package io.standel.cards.models

val suits: List<String> = listOf("hearts", "diamonds", "clubs", "spades")
val values: List<String> = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")

// Properties can be rewritten to be censored
data class Card(var suit: String, var value: String)
