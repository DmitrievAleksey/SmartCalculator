import kotlin.system.exitProcess

enum class Commands(val term: String) {
    EXIT("/exit") {
        override fun run() {
            println("Bye!")
            exitProcess(1)
        }
                  },
    HELP("/help") {
        override fun run() {
            println("The program \"Simple Calculator\" supports operations:\n " +
                    "^ - the power operator,\n" +
                    "* - multiplication,\n" +
                    "/ - integer division,\n" +
                    "+ - addition,\n" +
                    "- - subtraction,\n" +
                    "(..) - parentheses.\n" +
                    "The power operator ^ that has higher priority than * and /.\n" +
                    "The + and - operator has the lowest priority. A sequence of + (like +++ or +++++) \n" +
                    "is an admissible operator that should be interpreted as a single plus. A sequence \n" +
                    "of - (like -- or ---) is also an admissible operator and its meaning depends on the length.")
        }
    };

    companion object {
        fun getResponse(action: String) {
            if (action.toLowerCase() in values().map { it.term }) {
                for (command in values()) {
                    if (action.equals(command.term, ignoreCase = true)) command.run()
                }
            } else {
                println("Unknown command")
            }

        }
    }

    abstract fun run()
}