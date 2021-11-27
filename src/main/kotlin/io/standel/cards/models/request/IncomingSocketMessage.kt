package io.standel.cards.models.request

data class IncomingSocketMessage(val type: IncomingMessageType, val payload: Map<String, String> = emptyMap())

enum class IncomingMessageType {
    SHUFFLE_DECK,
    DRAW_PRIVATE_CARD,
    DRAW_PUBLIC_CARD,
    FETCH_PRIVATE_CARDS
}