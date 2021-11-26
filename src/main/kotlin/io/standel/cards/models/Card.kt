package io.standel.cards.models

val suits: List<String> = listOf("hearts", "diamonds", "clubs", "spades")
val values: List<String> = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")

data class Card(val suit: String, val value: String)