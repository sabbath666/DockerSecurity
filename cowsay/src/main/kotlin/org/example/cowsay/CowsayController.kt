package org.example.cowsay

import com.github.ricksbrown.cowsay.plugin.CowExecutor
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicReference

@RestController
class CowsayController {

    private val greetings = listOf("Ну привет", "Чокак?", "Хэй", "Пуньк-пунь")
    private val monster = AtomicReference("default")
    private val cowSayExecutor = CowExecutor().apply {
        setHtml(true)
    }

    @GetMapping("/hello")
    fun hello(): String {
        val greeting = greetings.random()
        return cowsay(greeting, monster.get())
    }

    @GetMapping("/sysinfo")
    fun sysinfo(): String {
        val ip = getIpAddress()
        val inContainer = isRunningInContainer()
        val runtime = Runtime.getRuntime()
        val cpuCores = runtime.availableProcessors()
        val maxMemoryMb = runtime.maxMemory() / (1024 * 1024)
        val allocatedMemoryMb = runtime.totalMemory() / (1024 * 1024)
        val freeMemoryMb = runtime.freeMemory() / (1024 * 1024)
        val info = """
            <pre>
            Hostname     : ${InetAddress.getLocalHost().hostName}
            IP           : ${ip}
            OS Name      : ${System.getProperty("os.name")}
            OS Version   : ${System.getProperty("os.version")}
            Kernel       : ${System.getProperty("os.arch")}
            User         : ${System.getProperty("user.name")}
            Processes    : ${getProcessCount()}
            CPU Cores    : $cpuCores
            Max Memory   : ${maxMemoryMb} MB
            --------------------------------------------------
                                          \
                                           \   ^__^
                                            \  (oo)\_______
                                               (__)\       )\/\
                                                    ||----w |
                                                    ||     ||
           </pre>                                         
        """.trimIndent()

        return info
    }

    @PostMapping("/setmonster")
    fun setMonster(@RequestBody request: MonsterRequest): String {
        monster.set(request.name)
        return cowsay("Монстр установлен на '${request.name}'", monster.get())
    }

    private fun getProcessCount(): Int {
        return try {
            ProcessHandle.allProcesses().count().toInt()
        } catch (e: Exception) {
            -1
        }
    }

    private fun cowsay(message: String, cowfile: String): String {
        cowSayExecutor.setMessage(message)
        cowSayExecutor.setCowfile(cowfile)
        return cowSayExecutor.execute()
    }
    private fun getIpAddress(): String {
        return try {
            InetAddress.getLocalHost().hostAddress
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun isRunningInContainer(): String {
        return try {
            val cgroup = java.io.File("/proc/1/cgroup")
            if (cgroup.exists() && cgroup.readText().contains("docker")) "Yes" else "No"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

data class MonsterRequest(val name: String)