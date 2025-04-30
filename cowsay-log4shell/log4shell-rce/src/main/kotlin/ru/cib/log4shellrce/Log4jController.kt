package ru.cib.log4shellrce

import org.apache.logging.log4j.LogManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class Log4jController {

    companion object {
        private val logger = LogManager.getLogger(Log4jController::class.java)
    }

    @GetMapping("/log")
    fun logInput(@RequestHeader("X-Api-Version") apiVersion: String): String {
        logger.info(apiVersion)
        return "Logged: $apiVersion"
    }
}