package com.liquidglass.calculator.core

import kotlin.math.*

class MathParser(private val useRadians: Boolean = false) {

    fun evaluate(expression: String): Double {
        // Preprocess string to normalize characters
        val cleanExpr = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "pi")
            .replace(" ", "")

        if (cleanExpr.isEmpty()) return 0.0

        val parser = Evaluator(cleanExpr, useRadians)
        return parser.parse()
    }

    private class Evaluator(private val str: String, private val useRadians: Boolean) {
        private var pos = -1
        private var ch = ' '

        private fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw IllegalArgumentException("Unexpected character: $ch")
            return x
        }

        // expression = term | expression `+` term | expression `-` term
        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+')) x += parseTerm() // addition
                else if (eat('-')) x -= parseTerm() // subtraction
                else return x
            }
        }

        // term = factor | term `*` factor | term `/` factor
        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*')) x *= parseFactor() // multiplication
                else if (eat('/')) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor // division
                } else return x
            }
        }

        // factor = unary | unary `^` factor
        private fun parseFactor(): Double {
            var x = parseUnary()
            if (eat('^')) {
                x = x.pow(parseFactor()) // exponentiation
            }
            return x
        }

        // unary = `+` unary | `-` unary | primary
        private fun parseUnary(): Double {
            if (eat('+')) return parseUnary() // unary plus
            if (eat('-')) return -parseUnary() // unary minus

            var x: Double
            val startPos = this.pos
            if (eat('(')) { // parentheses
                x = parseExpression()
                if (!eat(')')) throw IllegalArgumentException("Missing closing parenthesis")
            } else if (ch in '0'..'9' || ch == '.') { // numbers
                while (ch in '0'..'9' || ch == '.') nextChar()
                x = str.substring(startPos, this.pos).toDoubleOrNull()
                    ?: throw IllegalArgumentException("Invalid number format")
            } else if (ch in 'a'..'z' || ch in 'A'..'Z') { // functions or constants
                while (ch in 'a'..'z' || ch in 'A'..'Z') nextChar()
                val func = str.substring(startPos, this.pos)
                if (func == "pi") {
                    x = PI
                } else if (func == "e") {
                    x = E
                } else {
                    if (!eat('(')) throw IllegalArgumentException("Expected '(' after function: $func")
                    val arg = parseExpression()
                    if (!eat(')')) throw IllegalArgumentException("Missing closing parenthesis for: $func")
                    x = when (func) {
                        "sqrt" -> {
                            if (arg < 0.0) throw ArithmeticException("Square root of negative number")
                            sqrt(arg)
                        }
                        "sin" -> sin(if (useRadians) arg else Math.toRadians(arg))
                        "cos" -> cos(if (useRadians) arg else Math.toRadians(arg))
                        "tan" -> tan(if (useRadians) arg else Math.toRadians(arg))
                        "asin" -> {
                            val res = asin(arg)
                            if (useRadians) res else Math.toDegrees(res)
                        }
                        "acos" -> {
                            val res = acos(arg)
                            if (useRadians) res else Math.toDegrees(res)
                        }
                        "atan" -> {
                            val res = atan(arg)
                            if (useRadians) res else Math.toDegrees(res)
                        }
                        "log" -> log10(arg)
                        "ln" -> ln(arg)
                        "abs" -> abs(arg)
                        else -> throw IllegalArgumentException("Unknown function: $func")
                    }
                }
            } else {
                throw IllegalArgumentException("Unexpected character: $ch")
            }

            return x
        }
    }
}
