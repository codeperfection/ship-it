package com.codeperfection.shipit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShipItApplication

fun main(args: Array<String>) {
	runApplication<ShipItApplication>(*args)
}
