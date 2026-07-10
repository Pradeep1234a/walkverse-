package com.walkverse.calculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.walkverse.calculator.core.MathParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat

class CalculatorViewModel : ViewModel() {

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    private val _isScientific = MutableStateFlow(false)
    val isScientific: StateFlow<Boolean> = _isScientific.asStateFlow()

    private val _useRadians = MutableStateFlow(false)
    val useRadians: StateFlow<Boolean> = _useRadians.asStateFlow()

    // Themes: "mesh_nebula" (blue/purple), "mesh_aurora" (cyan/teal/blue), "mesh_sunset" (purple/pink/orange)
    private val _currentTheme = MutableStateFlow("mesh_nebula")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()

    private val decimalFormat = DecimalFormat("#.##########")

    fun onDigit(digit: String) {
        if (_expression.value == "Error") {
            _expression.value = ""
        }
        _expression.value += digit
        autoEvaluate()
    }

    fun onDecimal() {
        val expr = _expression.value
        if (expr == "Error") {
            _expression.value = "0."
            return
        }
        if (expr.isEmpty()) {
            _expression.value = "0."
            return
        }

        // Find last segment to ensure we don't double decimals in a single number
        val lastNumber = expr.split("+", "-", "×", "÷", "(", ")", "^").lastOrNull() ?: ""
        if (!lastNumber.contains(".")) {
            _expression.value += "."
        }
    }

    fun onOperator(op: String) {
        val expr = _expression.value
        if (expr == "Error") return

        if (expr.isNotEmpty()) {
            val lastChar = expr.last()
            if (lastChar == '+' || lastChar == '-' || lastChar == '×' || lastChar == '÷' || lastChar == '^') {
                // Replace last operator
                _expression.value = expr.substring(0, expr.length - 1) + op
            } else {
                _expression.value += op
            }
        } else if (op == "-") {
            // Negative start
            _expression.value = "-"
        }
    }

    fun onFunction(func: String) {
        if (_expression.value == "Error") {
            _expression.value = ""
        }
        _expression.value += "$func("
    }

    fun onConstant(constant: String) {
        if (_expression.value == "Error") {
            _expression.value = ""
        }
        _expression.value += constant
        autoEvaluate()
    }

    fun onParenthesis(paren: String) {
        if (_expression.value == "Error") {
            _expression.value = ""
        }
        _expression.value += paren
        autoEvaluate()
    }

    fun onClear() {
        _expression.value = ""
        _result.value = ""
    }

    fun onDelete() {
        val expr = _expression.value
        if (expr == "Error" || expr.isEmpty()) {
            _expression.value = ""
            _result.value = ""
            return
        }

        // Handle deleting function names like "sin(", "cos(", "log(", "sqrt("
        val knownFunctions = listOf("sin(", "cos(", "tan(", "log(", "sqrt(", "asin(", "acos(", "atan(")
        var deletedFunc = false
        for (func in knownFunctions) {
            if (expr.endsWith(func)) {
                _expression.value = expr.substring(0, expr.length - func.length)
                deletedFunc = true
                break
            }
        }
        if (expr.endsWith("ln(")) {
            _expression.value = expr.substring(0, expr.length - 3)
            deletedFunc = true
        }

        if (!deletedFunc) {
            _expression.value = expr.substring(0, expr.length - 1)
        }

        autoEvaluate()
    }

    fun onEqual() {
        val expr = _expression.value
        if (expr.isEmpty()) return

        try {
            val parser = MathParser(_useRadians.value)
            val evalResult = parser.evaluate(expr)
            
            if (evalResult.isNaN() || evalResult.isInfinite()) {
                _result.value = "Error"
                return
            }

            val formattedResult = formatValue(evalResult)
            
            // Add to history
            val entry = "$expr = $formattedResult"
            _history.value = listOf(entry) + _history.value

            _expression.value = formattedResult
            _result.value = ""
        } catch (e: Exception) {
            _result.value = "Error"
        }
    }

    private fun autoEvaluate() {
        val expr = _expression.value
        if (expr.isEmpty()) {
            _result.value = ""
            return
        }

        // Try evaluating as a preview, but swallow errors silently
        try {
            // Count matching parentheses. If unbalanced, temporarily balance for preview
            var balancedExpr = expr
            val openCount = expr.count { it == '(' }
            val closeCount = expr.count { it == ')' }
            if (openCount > closeCount) {
                balancedExpr += ")".repeat(openCount - closeCount)
            }

            val parser = MathParser(_useRadians.value)
            val evalResult = parser.evaluate(balancedExpr)
            
            if (!evalResult.isNaN() && !evalResult.isInfinite()) {
                _result.value = formatValue(evalResult)
            } else {
                _result.value = ""
            }
        } catch (e: Exception) {
            _result.value = ""
        }
    }

    private fun formatValue(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            decimalFormat.format(value)
        }
    }

    fun toggleScientific() {
        _isScientific.value = !_isScientific.value
    }

    fun toggleAngleUnit() {
        _useRadians.value = !_useRadians.value
        autoEvaluate()
    }

    fun changeTheme(theme: String) {
        _currentTheme.value = theme
    }

    fun clearHistory() {
        _history.value = emptyList()
    }
}
