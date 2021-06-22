package io.github.flecomte

import io.github.flecomte.plugins.configureHTTP
import io.github.flecomte.plugins.configureRouting
import io.github.flecomte.plugins.configureSecurity
import io.github.flecomte.plugins.configureSerialization
import io.github.flecomte.plugins.configureSockets
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty

fun main() {
    embeddedServer(Jetty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSecurity()
        configureHTTP()
        configureSerialization()
        configureSockets()
    }.start(wait = true)
}
