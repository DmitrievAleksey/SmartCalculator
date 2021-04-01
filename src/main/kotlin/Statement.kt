enum class Statement(val message: String) {
    UnknownVar("Unknown variable"),
    InvalidVar("Invalid identifier"),
    InvalidAss("Invalid assignment"),
    InvalidEx("Invalid expression"),
    RepeatIn("Repeat the input or ask for help on the command \"/help\""),
    Bye("Bye!")
}