package ru.cib.log4shellrce

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Log4shellRceApplication

fun main(args: Array<String>) {
    runApplication<Log4shellRceApplication>(*args)
}
