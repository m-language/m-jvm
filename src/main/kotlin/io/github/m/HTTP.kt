package io.github.m

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Method

/**
 * M wrapper class for http.
 */
object HTTP {
    @Suppress("unused")
    object Definitions {
        private val methodMap =
                mapOf(
                        "GET" to Method.GET,
                        "HEAD" to Method.HEAD,
                        "POST" to Method.POST,
                        "PUT" to Method.PUT,
                        "PATCH" to Method.PATCH,
                        "DELETE" to Method.DELETE
                )

        @MField(name = "http/send")
        @JvmField
        val sendRequest: Value = Value.Impl4("http/send") { method, url, headers, body ->
            val reqHeaders = List.from(headers).map {
                val list = List.from(it)
                val headerName = Symbol.from(list.first()).value
                val values = (list as List.Cons).cdr
                Pair(headerName, values)
            }.toMap()

            val httpMethod = Symbol.from(method)
            val methodUsed = methodMap[httpMethod.value]
                    ?: return@Impl4 Error("$httpMethod is not a valid HTTP method")

            val request = Fuel.request(methodUsed, Symbol.from(url).value)
                    .header(reqHeaders)

            Process {
                val fullRequest = when (body) {
                    is File -> request.body(body.value)
                    is Symbol -> request.body(body.value)
                    else -> return@Process Error("$body is not a valid request body")
                }
                Symbol.valueOf(fullRequest.responseString().second.body().asString(null))
            }
        }
    }
}
