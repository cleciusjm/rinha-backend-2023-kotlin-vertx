package br.com.thecj

import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.http.HttpMethod.POST
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import org.bson.types.ObjectId
import java.lang.IllegalArgumentException
import java.lang.RuntimeException

val dateRegex = Regex("""([12]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01]))""")

private const val COLL_PESSOAS = "pessoas"

private const val FIELD_M_ID = "_id"

private const val FIELD_ID = "id"

private const val FIELD_APELIDO = "apelido"

private const val FIELD_NOME = "nome"

private const val FIELD_NASCIMENTO = "nascimento"

private const val FIELD_STACK = "stack"

private const val FIELD_SEARCH = "search_field"

fun Router.registerPersonRoutes(db: MongoClient) {

    route(POST, "/pessoas").consumes("application/json").coHandler { ctx ->
        val body = ctx.body().asJsonObject()
        if (body.isValid) {
            val others = db.count(COLL_PESSOAS, json { obj(FIELD_APELIDO to body.apelido) }).await()
            if (others > 0) throw IllegalArgumentException()

            body.id = ObjectId().toString()
            body.preInsert()
            db.save(COLL_PESSOAS, body)
            ctx.response().apply {
                statusCode = 201
                putHeader("Location", "/pessoas/${body.id}")
                end()
            }
        } else ctx.endWithJson(422)
    }

    route(GET, "/pessoas/:id").coHandler { ctx ->
        val id = ctx.pathParams()[FIELD_ID]
        val result = db.findOne(COLL_PESSOAS, json { obj(FIELD_M_ID to id) }, null).await()?.apply { cleanup() }
        if (result == null) ctx.endWithJson(404)
        else ctx.endWithJson(200, result.toString())
    }

    route(GET, "/pessoas").coHandler { ctx ->
        val search = ctx.queryParam("t")?.firstOrNull() ?: throw RuntimeException()
        val result = db.find(COLL_PESSOAS, json {
            obj(FIELD_SEARCH to search)
        }).await().onEach { it.cleanup() }
        ctx.endWithJson(200, JsonArray(result).toString())
    }

    route(GET, "/contagem-pessoas").coHandler { ctx ->
        val count = db.count(COLL_PESSOAS, json { obj() }).await()
        ctx.endWithJson(200, "$count")
    }
}

var JsonObject.id: String
    get() = getString(FIELD_ID)
    set(value) {
        put(FIELD_ID, value)
    }

val JsonObject.apelido: String?
    get() = getString(FIELD_APELIDO)

val JsonObject.nome: String?
    get() = getString(FIELD_NOME)

val JsonObject.nascimento: String?
    get() = getString(FIELD_NASCIMENTO)

val JsonObject.stack: List<String>
    get() = getJsonArray(FIELD_STACK)?.map { it as String } ?: emptyList()

val JsonObject.isValid: Boolean
    get() = apelido?.let { it.length <= 32 } == true
            && nome?.let { it.length <= 100 } == true
            && nascimento?.matches(dateRegex) == true
            && (stack.isEmpty() || stack.all { it.length <= 32 })

fun JsonObject.preInsert() {
    put(FIELD_M_ID, id)
    put(
        FIELD_SEARCH, JsonArray(
        listOfNotNull(
            apelido,
            nome,
            *stack.toTypedArray()
        ).map { it.lowercase() }
    ))
}

fun JsonObject.cleanup() {
    remove(FIELD_M_ID)
    remove(FIELD_SEARCH)
}