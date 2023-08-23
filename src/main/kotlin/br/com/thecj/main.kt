package br.com.thecj

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.http.httpServerOptionsOf
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.await
import java.lang.System.getenv

suspend fun main() {
    val vertx = Vertx.vertx(vertxOptionsOf( preferNativeTransport = true ))
    val db = MongoClient.create(vertx, json {
        obj("connection_string" to getenv("CONNECTION_STRING"))
    })

    val router = Router.router(vertx).apply {
        route()
            .handler(BodyHandler.create())
            .failureHandler { it.endWithJson(400) }
        registerHelloRoute()
        registerPersonRoutes(db)
    }

    vertx.createHttpServer(httpServerOptionsOf(
        reusePort = true,
        tcpQuickAck = true,
        tcpFastOpen = true,
        tcpCork = true
    )).requestHandler(router).listen(8080).await()
}

private fun Router.registerHelloRoute() =
    route(HttpMethod.GET, "/hello").coHandler {
        it.response().end("Test ${it.normalizedPath()}")
    }






