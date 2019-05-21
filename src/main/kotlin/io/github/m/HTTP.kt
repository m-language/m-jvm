package io.github.m

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Method
import java.lang.IllegalStateException

object HTTP {
    @Suppress("unused")
    object Definitions {
        val methodMap: Map<String, Method> =
                mapOf(
                        "GET" to Method.GET,
                        "HEAD" to Method.HEAD,
                        "POST" to Method.POST,
                        "PUT" to Method.PUT,
                        "PATCH" to Method.PATCH,
                        "DELETE" to Method.DELETE
                )

        @MField(name="http/send")
        @JvmField
        val sendRequest: Value = Value.Impl4("http/send") { method, url, headers, body ->
            val reqHeaders = List.from(headers).map { List.from(it) }.map {
                val headerName = Symbol.from(it.first()).value
                val values = it.drop(1).toList()
                Pair(headerName, values)
            }.toMap()

            val httpMethod = method as Symbol
            val methodUsed = methodMap[httpMethod.value]
                    ?: throw IllegalStateException("$httpMethod is not a valid HTTP method")

            val request = Fuel.request(methodUsed, Symbol.from(url).value)
                    .header(reqHeaders)

            Process {
                val fullRequest = when(body) {
                    is File -> request.body(body.value)
                    is Symbol -> request.body(body.value)
                    else -> throw IllegalStateException("$body is not a valid request body")
                }
                Symbol.valueOf(fullRequest.responseString().second.body().asString(null))
            }
        }
    }
}
