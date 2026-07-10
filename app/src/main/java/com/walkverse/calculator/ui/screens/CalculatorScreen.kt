package com.walkverse.calculator.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walkverse.calculator.ui.components.*
import com.walkverse.calculator.ui.theme.GlassTheme
import com.walkverse.calculator.ui.theme.GlassTypography
import com.walkverse.calculator.ui.viewmodel.CalculatorViewModel

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsState()
    val result by viewModel.result.collectAsState()
    val history by viewModel.history.collectAsState()
    val isScientific by viewModel.isScientific.collectAsState()
    val useRadians by viewModel.useRadians.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    var showHistory by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // 1. Moving Mesh Gradient Background
        MeshGradientBackground(themeName = currentTheme)

        // Main Layout containing Display and Buttons
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2. Floating Top Glass Toolbar
            GlassPanel(
                cornerRadius = 24.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // History Button
                    IconButton(onClick = { showHistory = !showHistory }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Show History",
                            tint = Color.White
                        )
                    }

                    // Mode indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.toggleScientific() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isScientific) Icons.Default.Science else Icons.Default.Calculate,
                            contentDescription = "Toggle Calculator Mode",
                            tint = GlassTheme.OperatorGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isScientific) "Scientific" else "Standard",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Theme and Angle unit buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Radians/Degrees Toggle
                        Text(
                            text = if (useRadians) "RAD" else "DEG",
                            color = GlassTheme.OperatorGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleAngleUnit() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        // Cycle Themes
                        IconButton(onClick = {
                            val nextTheme = when (currentTheme) {
                                "mesh_nebula" -> "mesh_aurora"
                                "mesh_aurora" -> "mesh_sunset"
                                else -> "mesh_nebula"
                            }
                            viewModel.changeTheme(nextTheme)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Change Theme",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // 3. Display Panel
            GlassPanel(
                cornerRadius = 28.dp,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Expands to fill available vertical space
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    // Current formula expression
                    AnimatedContent(
                        targetState = expression,
                        transitionSpec = {
                            fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                        },
                        label = "ExpressionAnimation"
                    ) { targetExpression ->
                        Text(
                            text = targetExpression.ifEmpty { "0" },
                            style = GlassTypography.displayLarge.copy(
                                fontSize = if (targetExpression.length > 10) 36.sp else 54.sp
                            ),
                            textAlign = TextAlign.End,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preview Evaluated Result
                    AnimatedVisibility(
                        visible = result.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = result,
                            style = GlassTypography.headlineMedium.copy(
                                color = GlassTheme.TextSecondary,
                                fontSize = 28.sp
                            ),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 4. Keyboard Layout (Dynamic columns with morphing)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Panel: Scientific Keys (visible only in scientific mode)
                AnimatedVisibility(
                    visible = isScientific,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                    modifier = Modifier.weight(2f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val row1 = listOf("sin", "cos")
                        val row2 = listOf("tan", "ln")
                        val row3 = listOf("log", "sqrt")
                        val row4 = listOf("π", "e")
                        val row5 = listOf("(", ")")
                        
                        val sciGrid = listOf(row1, row2, row3, row4, row5)
                        
                        sciGrid.forEach { rowKeys ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowKeys.forEach { key ->
                                    val isConstant = key == "π" || key == "e"
                                    val isParenthesis = key == "(" || key == ")"
                                    
                                    GlassButton(
                                        text = key,
                                        onClick = {
                                            when {
                                                isConstant -> viewModel.onConstant(key)
                                                isParenthesis -> viewModel.onParenthesis(key)
                                                key == "sqrt" -> viewModel.onFunction("sqrt")
                                                else -> viewModel.onFunction(key)
                                            }
                                        },
                                        type = GlassButtonType.FUNCTION,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Panel: Standard Keys
                Column(
                    modifier = Modifier.weight(4f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val keys = listOf(
                        listOf("C", "Del", "^", "÷"),
                        listOf("7", "8", "9", "×"),
                        listOf("4", "5", "6", "-"),
                        listOf("1", "2", "3", "+"),
                        listOf("0", ".", "=")
                    )

                    keys.forEach { rowKeys ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowKeys.forEach { key ->
                                val isOperator = key == "÷" || key == "×" || key == "-" || key == "+" || key == "^"
                                val isEquals = key == "="
                                val isClearOrDel = key == "C" || key == "Del"
                                
                                val buttonType = when {
                                    isEquals -> GlassButtonType.EQUALS
                                    isOperator -> GlassButtonType.OPERATOR
                                    isClearOrDel -> GlassButtonType.FUNCTION
                                    else -> GlassButtonType.NUMBER
                                }

                                GlassButton(
                                    text = key,
                                    onClick = {
                                        when {
                                            key == "C" -> viewModel.onClear()
                                            key == "Del" -> viewModel.onDelete()
                                            key == "=" -> viewModel.onEqual()
                                            key == "." -> viewModel.onDecimal()
                                            isOperator -> viewModel.onOperator(key)
                                            else -> viewModel.onDigit(key)
                                        }
                                    },
                                    type = buttonType,
                                    // Expand '0' button to take double width in 3-column row
                                    modifier = Modifier.weight(if (key == "0" && rowKeys.size == 3) 2f else 1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Floating Glass History Drawer Overlay
        AnimatedVisibility(
            visible = showHistory,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            GlassPanel(
                cornerRadius = 32.dp,
                shadowElevation = 24.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Calculation History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Row {
                            IconButton(onClick = { viewModel.clearHistory() }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear History",
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            IconButton(onClick = { showHistory = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close History",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.15f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No calculations yet",
                                color = GlassTheme.TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(history) { item ->
                                val parts = item.split(" = ")
                                val expr = parts.getOrNull(0) ?: ""
                                val res = parts.getOrNull(1) ?: ""

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onClear()
                                            viewModel.onDigit(expr)
                                            showHistory = false
                                        }
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = expr,
                                        fontSize = 14.sp,
                                        color = GlassTheme.TextSecondary,
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = "= $res",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
