package io.standel.cards.models.response

data class SocketEventResponse(
    val triggeringUser: String,
    val gameId: String,
    val privateMessage: OutgoingSocketMessage? = null,
    val publicMessage: OutgoingSocketMessage? = null
)