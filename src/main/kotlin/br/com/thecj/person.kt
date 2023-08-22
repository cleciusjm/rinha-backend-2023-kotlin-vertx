package br.com.thecj

import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.POST
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import java.lang.RuntimeException
import java.util.*

val dateRegex = Regex("""([12]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01]))""")

fun Router.registerPersonRoutes() {
    val db = mutableSetOf<JsonObject>()
    route(POST, "/pessoas").consumes("application/json").coHandler { ctx ->
        val body = ctx.body().asJsonObject()
        if (body.isValid) {
            body.id = UUID.randomUUID().toString()
            db.add(body)
            ctx.response().apply {
                statusCode=201
                putHeader("Location", "/pessoas/${body.id}")
                end()
            }
        } else ctx.endWithJson(422)
    }

    route(GET, "/pessoas/:id").coHandler { ctx ->
        val id = ctx.pathParams()["id"]
        val result = db.find { it.id == id }
        if (result == null) ctx.endWithJson(404)
        else ctx.endWithJson(200, result.toString())
    }

    route(GET, "/pessoas").coHandler { ctx ->
        val search = ctx.queryParam("t")?.first ?: throw RuntimeException()

        ctx.endWithJson(200, JsonArray(db.filter { it.apelido!! == search }.toList()).toString())
    }

    route(GET, "/contagem-pessoas").coHandler { ctx ->
        ctx.endWithJson(200, "${db.size}")
    }
}

var JsonObject.id: String
    get() = getString("id")
    set(value) {
        put("id", value)
    }

val JsonObject.apelido: String?
    get() = getString("apelido")

val JsonObject.nome: String?
    get() = getString("nome")

val JsonObject.nascimento: String?
    get() = getString("nascimento")

val JsonObject.stack: List<String>
    get() = getJsonArray("stack")?.map { it as String } ?: emptyList()

val JsonObject.isValid: Boolean
    get() = apelido?.let { it.length <= 32 } == true
            && nome?.let { it.length <= 100 } == true
            && nascimento?.matches(dateRegex) == true
            && (stack.isEmpty() || stack.all { it.length <= 32 })