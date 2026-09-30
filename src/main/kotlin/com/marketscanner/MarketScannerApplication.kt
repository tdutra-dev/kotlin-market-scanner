package com.marketscanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MarketScannerApplication

fun main(args: Array<String>) {
    runApplication<MarketScannerApplication>(*args)
}
