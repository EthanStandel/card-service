package io.standel.cards.utils

import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.random.Random

val numeric = IntStream.range(0, 10).mapToObj{ it.toString() }.collect(Collectors.toList())
val alpha = IntStream.range(0, 26)
    .mapToObj{ (it + 65).toChar().toString() }
    .collect(Collectors.toList())
val alphaNumeric = emptyList<String>().plus(alpha).plus(numeric)

val generateGameId = {
    IntStream.range(0, 4)
        .mapToObj { alphaNumeric[Random.nextInt(0, 36)] }
        .collect(Collectors.toList())
        .joinToString("")
}