package io.standel.cards.models.response

data class SocketMessage(val type: String, val payload: Map<String, String> = emptyMap())
