import Statement.*
import java.math.BigInteger
import kotlin.Exception
import kotlin.math.pow

class Calculator(private var mathExpression: String) {

    private var mathVariable = mutableMapOf<String, String>()

    init {
        this.begin()
    }

    /* Initial analysis of the entered expression. If it starts with the " / " character,
    * it is parsed as a command. If it has the " = " character, then it is entering a variable
    * and storing its value in mathStackOperation. In all other cases, if the expression is not empty,
    * then its mathematical result is calculated. */
    private fun begin() {
        while (mathExpression != Commands.EXIT.term) {
            when {
                "=" in mathExpression -> this.setVariable()
                mathExpression.firstOrNull() == '/' -> Commands.getResponse(mathExpression)
                mathExpression.isNotEmpty() -> println(this.getCalculation())
            }
            mathExpression = readLine()!!
        }
        println(Bye.message)
    }

    /* Parsing a variable and storing its value in mathStackOperation */
    private fun setVariable() {
        val key = mathExpression.substringBefore('=').trim()
        if (key.all { it.isLetter()}) {
            val value = mathExpression.substringAfter('=').trim()
            when {
                value.toBigIntegerOrNull() != null -> mathVariable[key] = value
                value in mathVariable -> mathVariable[key] = mathVariable[value].orEmpty()
                value.all { it.isLetter()} -> println(UnknownVar.message)
                else -> println(InvalidAss.message)
            }
        } else {
            println(InvalidVar.message)
        }
    }

    /* If a mathematical expression has an internal construction in parentheses, then it is evaluated,
    * and this expression is transformed taking into account the resulting calculation. */
    private fun getCalculation(): String {
        while ('(' in mathExpression && ')' in mathExpression) {
            val indexFirst = mathExpression.lastIndexOf('(')
            val indexLast = mathExpression.indexOf(')',indexFirst)
            val partOf = mathExpression.substring(indexFirst, indexLast + 1)
            mathExpression = mathExpression.replace(partOf, this.calculation(partOf.trim('(',')')))
        }
        return this.calculation(mathExpression)
    }

    private fun calculation(mathExpression: String): String {
        val mathStackOperation: MutableList<String> = this.conversion(mathExpression)
        var result = BigInteger.ZERO

        if (mathStackOperation.isNotEmpty()) {
            /* The power operator ^ that has higher priority than * and /. */
            try {
                while ("^" in mathStackOperation) {
                    val i = mathStackOperation.indexOf("^")
                    mathStackOperation[i] =
                        "%.0f".format(mathStackOperation[i - 1].toDouble().pow(mathStackOperation[i + 1].toInt()))
                    mathStackOperation.removeAt(i - 1)
                    mathStackOperation.removeAt(i)
                }
            } catch (e: Exception) {
                return InvalidEx.message
            }

            /* The operator multiplication * and integer division / that has higher priority than + and -. */
            try {
                while ("*" in mathStackOperation || "/" in mathStackOperation) {
                    if ("*" in mathStackOperation) {
                        val i = mathStackOperation.indexOf("*")
                        mathStackOperation[i] = (mathStackOperation[i - 1].toBigInteger() *
                                mathStackOperation[i + 1].toBigInteger()).toString()
                        mathStackOperation.removeAt(i - 1)
                        mathStackOperation.removeAt(i)
                    }
                    if ("/" in mathStackOperation) {
                        val i = mathStackOperation.indexOf("/")
                        mathStackOperation[i] = (mathStackOperation[i - 1].toBigInteger() /
                                mathStackOperation[i + 1].toBigInteger()).toString()
                        mathStackOperation.removeAt(i - 1)
                        mathStackOperation.removeAt(i)
                    }
                }
            } catch (e: Exception) {
                return InvalidEx.message
            }

            /* The + and - operator has the lowest priority. A sequence of + (like +++ or +++++)
            * is an admissible operator that should be interpreted as a single plus. A sequence
            * of - (like -- or ---) is also an admissible operator and its meaning depends on the length. */
            var operator = "+"
            for (i in mathStackOperation) {
                if (i.toBigIntegerOrNull() != null) {
                    when (operator) {
                        "+" -> {
                            result += i.toBigInteger()
                            operator = ""
                        }
                        "-" -> {
                            result -= i.toBigInteger()
                            operator = ""
                        }
                        else -> return InvalidEx.message
                    }
                } else {
                    operator = if (i == "-") {
                        when (operator) {
                            "+" -> "-"
                            "-" -> "+"
                            else -> i
                        }
                    } else {
                        i
                    }
                }
            }
        }
        else {
            return RepeatIn.message
        }

        return result.toString()
    }

    /* Convert a mathematical expression to a sequence of numbers and operations */
    private fun conversion(mathExpression: String): MutableList<String> {
        val mathStackOperation = mutableListOf<String>()

        if ('(' in mathExpression || ')' in mathExpression) {
            println(InvalidEx.message)
            mathStackOperation.clear()
            return mathStackOperation
        } else {
            for (element in mathExpression.split(" ")) {
                when {
                    element == "+" -> mathStackOperation.add("+")
                    element == "-" -> mathStackOperation.add("-")
                    element == "/" -> mathStackOperation.add("/")
                    element == "*" -> mathStackOperation.add("*")
                    element == "^" -> mathStackOperation.add("*")
                    element.toBigIntegerOrNull() != null -> mathStackOperation.add(element)

                    /* A sequence of + is an admissible operator that should be interpreted as a single plus.
                    *  A sequence is also an admissible operator and its meaning depends on the length.*/
                    element.all {it == '+' || it == '-'} -> {
                        if (element.count { it == '-' } % 2 == 0) {
                            mathStackOperation.add("+")
                        } else {
                            mathStackOperation.add("-")
                        }
                    }
                    /* Converting a sequence of symbolic variables and mathematical operations (like a*b/c) */
                    element.all { it.isLetter() } -> {
                        if (element in mathVariable) {
                            mathStackOperation.add(mathVariable[element].toString())
                        } else {
                            var variable = ""
                            for (i in element) {
                                if (i in listOf('+', '-', '*', '/', '^')) {
                                    when {
                                        variable.isNotEmpty() && variable in mathVariable -> {
                                            mathStackOperation.add(mathVariable[variable].toString())
                                            mathStackOperation.add(i.toString())
                                            variable = ""
                                        }
                                        variable.isNotEmpty() && variable !in mathVariable -> {
                                            println(UnknownVar.message)
                                            mathStackOperation.clear()
                                            return mathStackOperation
                                        }
                                        else -> mathStackOperation.add(i.toString())
                                    }
                                } else {
                                    variable += i.toString()
                                }
                            }
                            when {
                                variable.isNotEmpty() && variable in mathVariable -> {
                                    mathStackOperation.add(mathVariable[variable].toString())
                                }
                                variable.isNotEmpty() && variable !in mathVariable -> {
                                    println(UnknownVar.message)
                                    mathStackOperation.clear()
                                    return mathStackOperation
                                }
                                else -> {
                                    println(InvalidEx.message)
                                    mathStackOperation.clear()
                                    return mathStackOperation
                                }
                            }
                        }
                    }
                    /* Convert a sequence of symbolic variables, numeric values, and mathematical operations
                    * (for example, a*2/c) */
                    else -> {
                        var digit = ""
                        var variable = ""
                        for (i in element) {
                            if (i in listOf('+', '-', '*', '/', '^')) {
                                when {
                                    digit.isNotEmpty() && variable.isNotEmpty() -> {
                                        println(InvalidEx.message)
                                        mathStackOperation.clear()
                                        return mathStackOperation
                                    }
                                    digit.isNotEmpty() -> {
                                        mathStackOperation.add(digit)
                                        mathStackOperation.add(i.toString())
                                        digit = ""
                                    }
                                    variable.isNotEmpty() && variable in mathVariable -> {
                                        mathStackOperation.add(mathVariable[variable].toString())
                                        mathStackOperation.add(i.toString())
                                        variable = ""
                                    }
                                    variable.isNotEmpty() && variable !in mathVariable -> {
                                        println(UnknownVar.message)
                                        mathStackOperation.clear()
                                        return mathStackOperation
                                    }
                                    else -> mathStackOperation.add(i.toString())
                                }
                            } else {
                                if (i.isDigit()) digit += i.toString()
                                if (i.isLetter()) variable += i.toString()
                            }
                        }
                        when {
                            digit.isNotEmpty() && variable.isNotEmpty() -> {
                                println(InvalidVar.message)
                            }
                            variable.isNotEmpty() && variable in mathVariable -> {
                                mathStackOperation.add(mathVariable[variable].toString())
                            }
                            variable.isNotEmpty() && variable !in mathVariable -> {
                                println(UnknownVar.message)
                                mathStackOperation.clear()
                                return mathStackOperation
                            }
                            digit.isNotEmpty() -> mathStackOperation.add(digit)
                            else -> {
                                println(InvalidEx.message)
                                mathStackOperation.clear()
                                return mathStackOperation
                            }
                        }
                    }
                }
            }
        }
        return mathStackOperation
    }

}