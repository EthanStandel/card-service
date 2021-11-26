package io.standel.cards.models

import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.math.floor

val createDeck = {
    IntStream.range(0, suits.size * values.size)
        .mapToObj {
            Card(
                suits[floor(it.toDouble() / values.size).toInt()],
                values[it % values.size]
            )
        }.collect(Collectors.toList())
}