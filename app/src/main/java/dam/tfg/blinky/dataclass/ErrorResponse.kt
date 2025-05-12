package dam.tfg.blinky.dataclass

import com.fasterxml.jackson.annotation.JsonFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.ArrayList
import retrofit2.Response

data class ErrorResponse(
    var status: String? = null,
    var statusCode: Int = 0,
    var message: String? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    var timestamp: String = getCurrentFormattedDate(), // Almacenamos la fecha como String

    var errors: MutableList<String> = ArrayList()
) {
    constructor() : this(timestamp = getCurrentFormattedDate()) // Llama al método para manejar la fecha

    constructor(statusCode: Int, message: String) : this() {
        this.statusCode = statusCode
        this.status = getStatusMessageFromCode(statusCode)
        this.message = message
    }

    constructor(statusCode: Int, message: String, errors: MutableList<String>) : this(statusCode, message) {
        this.errors = errors
    }

    // Constructor que acepta un objeto Response de Retrofit
    constructor(response: Response<*>, message: String) : this() {
        this.statusCode = response.code()
        this.status = getStatusMessageFromCode(response.code())
        this.message = message
    }

    // Constructor que acepta un objeto Response de Retrofit y una lista de errores
    constructor(response: Response<*>, message: String, errors: MutableList<String>) : this(response, message) {
        this.errors = errors
    }

    private fun getStatusMessageFromCode(code: Int): String {
        return when (code) {
            200 -> "OK"
            201 -> "Created"
            204 -> "No Content"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            500 -> "Internal Server Error"
            else -> "Unknown Status"
        }
    }

    fun addError(error: String) {
        this.errors.add(error)
    }

    companion object {
        // Método para obtener la fecha formateada
        private fun getCurrentFormattedDate(): String {
            val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault())
            return formatter.format(Date()) // Formatea la fecha del sistema actual
        }
    }
}
