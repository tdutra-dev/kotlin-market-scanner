package com.marketscanner

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private val httpClient = HttpClient.newHttpClient()

/**
 * Blocking HTTP request to a public endpoint. This is intentionally simple and mirrors the common "just call the API"
 * approach; it blocks the caller thread until the network call completes.
 */
fun fetchPriceSync(symbol: String = "BTCUSDT"): PriceTick {
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.binance.com/api/v3/ticker/price?symbol=$symbol"))
        .header("Accept", "application/json")
        .GET()
        .build()

    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() !in 200..299) {
        throw IllegalStateException("Binance request failed: ${response.statusCode()} - ${response.body()}")
    }

    return PriceTick.fromBinancePayload(response.body())
}

fun main() {
    val priceTick = fetchPriceSync()
    println("Sync price tick: $priceTick")
}
