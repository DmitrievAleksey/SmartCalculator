fun main() {
    println("The program \"Simple Calculator\" supports operations of multiplication *, \n" +
            "integer division /, addition +, subtraction -, the power operator ^ and " +
            "parentheses (...).\nThe program also supports entering internal commands starting with " +
            "the \" / \" character, for example /help.")
    print("> ")
    Calculator(readLine()!!)
}