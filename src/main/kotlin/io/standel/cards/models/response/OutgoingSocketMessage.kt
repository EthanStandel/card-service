package io.standel.cards.models.response

data class OutgoingSocketMessage(val type: OutgoingMessageType, val payload: Map<String, String> = emptyMap())

enum class OutgoingMessageType {
    DECK_SHUFFLED,
    PRIVATE_CARD_DRAWN,
    PUBLIC_CARD_DRAWN,
    PRIVATE_CARD_FETCH,
    NEW_PLAYER,
    // Errors
    MISSING_PARAMS,
    BAD_REQUEST,
    GAME_NOT_FOUND
}