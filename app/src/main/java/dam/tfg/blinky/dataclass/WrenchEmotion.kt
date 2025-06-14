package dam.tfg.blinky.dataclass

/**
 * Represents a minimalist emotion visualization inspired by Wrench from Watch Dogs 2.
 * Each emotion has two characters for the eyes and a descriptive name.
 */
enum class WrenchEmotion(val leftEye: String, val rightEye: String, val description: String) {
    NEUTRAL("o", "o", "Neutral"),  // straight eyes: indifferent
    HAPPY("^", "^", "Happy"),      // smiling closed eyes
    SAD("-", "-", "Sad"),       // droopy or teary eyes
    ANGRY(">", "<", "Angry"),     // intense gaze
    ERROR("X", "X", "Error"),       // failure or system collapse
    MEH("<", "<", "Meh"),           // Meh
    CONFUSED("o", "O", "Confused"),  // uneven eyes = confusion
    QUESTION("?", "?", "Question"); // User question

    companion object {
        val DEFAULT = NEUTRAL
    }
}
