package org.example.cowsay

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CowsayApplication

fun main(args: Array<String>) {
	runApplication<CowsayApplication>(*args)
}

