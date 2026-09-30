package com.marketscanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * The practical difference vs the sync version is that this function is suspendable: the caller does not block a
 * thread while waiting for the network I/O to finish, so the thread can continue doing other work.
 */
suspend fun fetchPriceCoroutine(symbol: String = "BTCUSDT"): PriceTick = withContext(Dispatchers.IO) {
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.binance.com/api/v3/ticker/price?symbol=$symbol"))
        .header("Accept", "application/json")
        .GET()
        .build()

    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() !in 200..299) {
        throw IllegalStateException("Binance request failed: ${response.statusCode()} - ${response.body()}")
    }

    PriceTick.fromBinancePayload(response.body())
}

fun runCoroutineExample() = runBlocking {
    val task = async { fetchPriceCoroutine() }
    launch {
        println("A concurrent coroutine is running while the HTTP work is suspended.")
    }

    val priceTick = task.await()
    println("Coroutine price tick: $priceTick")
}
