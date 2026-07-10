package com.walkverse.calculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.walkverse.calculator.core.MathParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat

class CalculatorViewModel : ViewModel() {

    private val _currentTheme = MutableStateFlow("mesh_nebula")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()

    private val _expressionDisplay = MutableStateFlow("")
    val expressionDisplay: StateFlow<String> = _expressionDisplay.asStateFlow()

    private val _largeDisplay = MutableStateFlow("0")
    val largeDisplay: StateFlow<String> = _largeDisplay.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    private val _activeOperator = MutableStateFlow<String?>(null)
    val activeOperator: StateFlow<String?> = _activeOperator.asStateFlow()

    private var rawExpression = ""
    private var isEnteringNumber = false
    private var currentInput = ""

    private val decimalFormat = DecimalFormat("#.##########")

    fun onDigit(digit: String) {
        if (currentInput == "Error") {
            currentInput = ""
        }
        
        if (currentInput == "0" && digit == "0") return
        if (currentInput == "0") {
            currentInput = digit
        } else {
            currentInput += digit
        }

        isEnteringNumber = true
        _largeDisplay.value = formatNumberWithCommas(currentInput)
        _activeOperator.value = null
        updateExpressionDisplay()
    }

    fun onDecimal() {
        if (currentInput == "Error") {
            currentInput = "0."
        } else if (currentInput.isEmpty()) {
            currentInput = "0."
        } else if (!currentInput.contains(".")) {
            currentInput += "."
        }
        
        isEnteringNumber = true
        _largeDisplay.value = formatNumberWithCommas(currentInput)
        _activeOperator.value = null
        updateExpressionDisplay()
    }

    fun onOperator(op: String) {
        if (currentInput == "Error") return

        if (isEnteringNumber && currentInput.isNotEmpty()) {
            rawExpression += currentInput
            currentInput = ""
            isEnteringNumber = false
        }

        if (rawExpression.isNotEmpty()) {
            val lastChar = rawExpression.last()
            if (lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/' || lastChar == '^') {
                rawExpression = rawExpression.substring(0, rawExpression.length - 1) + convertOp(op)
            } else {
                rawExpression += convertOp(op)
            }
            _activeOperator.value = op
        }
        updateExpressionDisplay()
    }

    fun onPercent() {
        if (currentInput.isNotEmpty() && currentInput != "Error") {
            try {
                val value = currentInput.toDouble() / 100.0
                currentInput = value.toString()
                _largeDisplay.value = formatNumberWithCommas(currentInput)
                updateExpressionDisplay()
            } catch (e: Exception) {
                currentInput = "Error"
                _largeDisplay.value = "Error"
            }
        }
    }

    fun onToggleSign() {
        if (currentInput.isNotEmpty() && currentInput != "Error") {
            currentInput = if (currentInput.startsWith("-")) {
                currentInput.substring(1)
            } else {
                "-$currentInput"
            }
            _largeDisplay.value = formatNumberWithCommas(currentInput)
            updateExpressionDisplay()
        }
    }

    fun onClear() {
        if (currentInput.isNotEmpty() && currentInput != "0") {
            currentInput = "0"
            _largeDisplay.value = "0"
        } else {
            rawExpression = ""
            currentInput = ""
            _largeDisplay.value = "0"
            _expressionDisplay.value = ""
            _activeOperator.value = null
        }
        updateExpressionDisplay()
    }

    fun onDelete() {
        if (currentInput.isEmpty() || currentInput == "Error" || currentInput == "0") return
        
        currentInput = if (currentInput.length > 1) {
            currentInput.substring(0, currentInput.length - 1)
        } else {
            "0"
        }
        
        _largeDisplay.value = formatNumberWithCommas(currentInput)
        updateExpressionDisplay()
    }

    fun onEqual() {
        if (currentInput.isNotEmpty()) {
            rawExpression += currentInput
        }

        if (rawExpression.isEmpty()) return

        try {
            val parser = MathParser(useRadians = false)
            val evalResult = parser.evaluate(rawExpression)

            if (evalResult.isNaN() || evalResult.isInfinite()) {
                _largeDisplay.value = "Error"
                currentInput = "Error"
                return
            }

            val formattedResult = formatValue(evalResult)
            val entry = "${formatExpressionString(rawExpression)} = ${formatNumberWithCommas(formattedResult)}"
            _history.value = listOf(entry) + _history.value

            _largeDisplay.value = formatNumberWithCommas(formattedResult)
            currentInput = formattedResult
            rawExpression = ""
            isEnteringNumber = false
            _activeOperator.value = null
        } catch (e: Exception) {
            _largeDisplay.value = "Error"
            currentInput = "Error"
        }
    }

    fun changeTheme(theme: String) {
        _currentTheme.value = theme
    }

    private fun updateExpressionDisplay() {
        _expressionDisplay.value = formatExpressionString(rawExpression + currentInput)
    }

    private fun convertOp(op: String): String {
        return when (op) {
            "÷" -> "/"
            "×" -> "*"
            "−" -> "-"
            "+" -> "+"
            else -> op
        }
    }

    private fun formatExpressionString(expr: String): String {
        val pattern = "([\\d\\.]+)|([\\+\\-\\*/\\^÷×−\\(\\)])".toRegex()
        val matches = pattern.findAll(expr)
        
        val sb = StringBuilder()
        for (match in matches) {
            val num = match.groups[1]?.value
            val op = match.groups[2]?.value
            
            if (num != null) {
                sb.append(formatNumberWithCommas(num))
            } else if (op != null) {
                val displayOp = when (op) {
                    "/" -> "÷"
                    "*" -> "×"
                    "-" -> "−"
                    else -> op
                }
                sb.append(displayOp)
            }
        }
        return sb.toString()
    }

    private fun formatNumberWithCommas(numberStr: String): String {
        if (numberStr.isEmpty() || numberStr == "Error" || numberStr == "Infinity" || numberStr == "NaN") return numberStr
        
        val parts = numberStr.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) "." + parts[1] else ""
        
        val isNegative = integerPart.startsWith("-")
        val cleanInt = if (isNegative) integerPart.substring(1) else integerPart
        
        if (cleanInt.isEmpty() || !cleanInt.all { it.isDigit() }) return numberStr
        
        val sb = StringBuilder()
        var count = 0
        for (i in cleanInt.length - 1 downTo 0) {
            if (count > 0 && count % 3 == 0) {
                sb.append(",")
            }
            sb.append(cleanInt[i])
            count++
        }
        val formattedInt = sb.reverse().toString()
        return (if (isNegative) "-" else "") + formattedInt + decimalPart
    }

    private fun formatValue(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            decimalFormat.format(value)
        }
    }

    fun clearHistory() {
        _history.value = emptyList()
    }
}
