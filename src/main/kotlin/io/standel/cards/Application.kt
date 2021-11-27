package io.standel.cards

import com.google.gson.Gson
import io.ktor.application.*
import io.standel.cards.models.SocketSession
import io.standel.cards.plugins.*
import io.standel.cards.repositories.GameRepository
import io.standel.cards.services.GameEventsManager
import io.standel.cards.services.GameManager
import org.koin.core.qualifier.named
import org.koin.ktor.ext.Koin
import java.util.*
import kotlin.collections.LinkedHashSet

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    install(Koin) {
        modules(org.koin.dsl.module {
            single { log }
            single { Gson() }
            single(named("socketSessions")) {
                Collections.synchronizedSet<SocketSession>(LinkedHashSet())
            }
            single { GameRepository(get(), get()) }
            single { GameEventsManager(get(), get()) }
            single { GameManager(get(), get(), get(), get(), get(named("socketSessions"))) }
        })
    }
    configureRouting()
    configureSockets()
    configureSerialization()
}
