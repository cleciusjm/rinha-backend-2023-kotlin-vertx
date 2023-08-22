package br.com.thecj

import io.vertx.core.Future
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.vertxFuture

fun Route.coHandler(action: suspend (RoutingContext) -> Unit): Route = handler { ctx ->
    vertxFuture { action(ctx) }
}

fun RoutingContext.endWithJson(status: Int): Future<Void> = response().let { r ->
    r.statusCode = status
    r.end()
}

fun RoutingContext.endWithJson(status: Int, buffer: String): Future<Void> = response().let { r ->
    r.statusCode = status
    r.putHeader("Content-Type", "application/json")
    r.end(buffer)
}