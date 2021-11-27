package io.standel.cards.models

import io.ktor.http.cio.websocket.*

data class SocketSession(
    val session: DefaultWebSocketSession,
    val gameId: String,
    val username: String
)
