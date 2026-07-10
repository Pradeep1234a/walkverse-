package com.walkverse.calculator.core

import kotlin.math.*

class MathParser(private val useRadians: Boolean = false) {

    fun evaluate(expression: String): Double {
        val cleanExpr = expression.replace(" ", "")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "3.141592653589793")
            .replace("e", "2.718281828459045")
        
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < cleanExpr.length) cleanExpr[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < cleanExpr.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) x /= parseFactor() // division
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return +parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    if (!eat(')'.code)) throw RuntimeException("Missing closing parenthesis")
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = cleanExpr.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = cleanExpr.substring(startPos, pos)
                    if (eat('('.code)) {
                        val arg = parseExpression()
                        if (!eat(')'.code)) throw RuntimeException("Missing closing parenthesis for $func")
                        x = when (func) {
                            "sin" -> if (useRadians) sin(arg) else sin(Math.toRadians(arg))
                            "cos" -> if (useRadians) cos(arg) else cos(Math.toRadians(arg))
                            "tan" -> if (useRadians) tan(arg) else tan(Math.toRadians(arg))
                            "log" -> log10(arg)
                            "ln" -> ln(arg)
                            "sqrt" -> sqrt(arg)
                            else -> throw RuntimeException("Unknown function: $func")
                        }
                    } else {
                        throw RuntimeException("Missing parentheses for function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected character: " + ch.toChar())
                }

                if (eat('^'.code)) x = x.pow(parseFactor()) // exponentiation

                return x
            }
        }.parse()
    }
}
