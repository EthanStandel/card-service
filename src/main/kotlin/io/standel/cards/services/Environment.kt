package io.standel.cards.services

import io.ktor.application.*

class Environment(private val environment: ApplicationEnvironment) {
    fun getProperty(key: String) = environment.config.property(key).getString()
}