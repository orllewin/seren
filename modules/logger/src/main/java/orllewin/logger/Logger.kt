package orllewin.logger

import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.*

object Logger {

    private const val MAX_LOGS = 500
    private val logStream = MutableStateFlow("")
    val logs = mutableListOf<String>()

    fun log(message: String){
        println("Ariane logger: $message")
        logs.add(message)
        logStream.value = message
        when {
            logs.size > MAX_LOGS -> logs.removeAt(0)
        }
    }

    fun logWithTime(message: String){
        log("${time()} - $message")
    }

    private fun time(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    fun get(onLog: (logs: List<String>) -> Unit){
        onLog(logs)
    }

    fun initialise() {
        logs.clear()
        logStream.value = ""
    }
}