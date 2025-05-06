package ru.cib.log4shellrce

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Log4ShellApplication

fun main(args: Array<String>) {
    runApplication<Log4ShellApplication>(*args)
}
