package io.standel.cards

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.*
import io.standel.cards.models.SocketSessionsContainer
import io.standel.cards.plugins.*
import io.standel.cards.repositories.GameRepository
import io.standel.cards.services.Environment
import io.standel.cards.services.GameEventsManager
import io.standel.cards.services.GameManager
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    install(Koin) {
        modules(org.koin.dsl.module {
            single { environment }
            single { Environment(get()) }
            single { log }
            single { Gson() }
            single { SocketSessionsContainer() }
            single { GameRepository(get(), get()) }
            single { GameEventsManager(get(), get()) }
            single { GameManager(get(), get(), get(), get(), get()) }
        })
    }
    install(CORS) {
        val environment by inject<Environment>()
        host(environment.getProperty("ktor.deployment.client"))
    }
    configureRouting()
    configureSockets()
    configureSerialization()
}
