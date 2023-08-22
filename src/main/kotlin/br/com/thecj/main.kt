package br.com.thecj

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.await

suspend fun main() {
    val vertx = Vertx.vertx(VertxOptions().apply { preferNativeTransport = true })

    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create());
    router.registerHelloRoute()
    router.registerPersonRoutes()

    vertx.createHttpServer().requestHandler(router).listen(8080).await()
}

private fun Router.registerHelloRoute() =
        route(HttpMethod.GET, "/hello").coHandler {
            "Test ${it.normalizedPath()}"
        }






