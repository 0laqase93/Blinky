package dam.tfg.blinky.dataclass

/**
 * Represents a minimalist emotion visualization inspired by Wrench from Watch Dogs 2.
 * Each emotion has two characters for the eyes and a descriptive name.
 */
enum class WrenchEmotion(val leftEye: String, val rightEye: String, val description: String) {
    NEUTRAL("o", "o", "Neutral"),         // ojos rectos: indiferente
    HAPPY("^", "^", "Feliz"),             // ojos cerrados sonrientes
    SAD("-", "-", "Triste"),              // ojos caídos o llorosos
    ANGRY(">", "<", "Enojado"),           // mirada intensa
    ERROR("X", "X", "Error"),             // fallo o sistema colapsado
    MEH("<", "<", "Meh"),
    CONFUSED("o", "O", "Confuso");        // ojos desiguales = desconcierto

    companion object {
        val DEFAULT = NEUTRAL
    }
}
