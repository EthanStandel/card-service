package io.standel.cards.models

import java.util.concurrent.ConcurrentHashMap

class SocketSessionsContainer {
    private val socketSessions: MutableMap<String, SocketSession> = ConcurrentHashMap()

    val list get() = socketSessions.values

    fun addSession(socketSession: SocketSession) {
        socketSessions[socketSession.username + socketSession.gameId] = socketSession
    }

    fun removeSession(socketSession: SocketSession) {
        socketSessions.remove(socketSession.username + socketSession.gameId)
    }
}
