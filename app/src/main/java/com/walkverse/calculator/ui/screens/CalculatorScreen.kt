package com.walkverse.calculator.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walkverse.calculator.ui.components.*
import com.walkverse.calculator.ui.theme.GlassTheme
import com.walkverse.calculator.ui.theme.GlassTypography
import com.walkverse.calculator.ui.theme.SuperellipseShape
import com.walkverse.calculator.ui.viewmodel.CalculatorViewModel

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expressionDisplay by viewModel.expressionDisplay.collectAsState()
    val largeDisplay by viewModel.largeDisplay.collectAsState()
    val history by viewModel.history.collectAsState()
    val activeOperator by viewModel.activeOperator.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    var showHistory by remember { mutableStateOf(false) }
    
    val cardShape = SuperellipseShape(exponent = 4.8f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // 1. Specular Ambient Background Reflections
        MeshGradientBackground(themeName = currentTheme)

        // 2. Centered Floating Calculator Card Container
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .align(Alignment.Center)
                .shadow(
                    elevation = 20.dp,
                    shape = cardShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.6f),
                    spotColor = Color.Black.copy(alpha = 0.8f)
                )
                .clip(cardShape)
                .blur(4.dp) // Subtle blur (nearly invisible, only 4dp)
                .background(GlassTheme.CardGlassBase) // 10% alpha transparent white
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = cardShape
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Floating Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Theme Switcher (Sun/Moon icon)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(elevation = 4.dp, shape = CircleShape, clip = false)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    val nextTheme = when (currentTheme) {
                                        "mesh_nebula" -> "mesh_aurora"
                                        "mesh_aurora" -> "mesh_sunset"
                                        else -> "mesh_nebula"
                                    }
                                    viewModel.changeTheme(nextTheme)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentTheme == "mesh_sunset") Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Ambient Theme",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Right History Button (List menu icon)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(elevation = 4.dp, shape = CircleShape, clip = false)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showHistory = !showHistory }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Show Calculation History",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Display area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (kotlin.math.abs(dragAmount) > 24f) {
                                    viewModel.onDelete()
                                }
                            }
                        },
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        // Formula Display (Right-aligned)
                        AnimatedContent(
                            targetState = expressionDisplay,
                            transitionSpec = {
                                fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                            },
                            label = "ExpressionAnimation"
                        ) { targetExpr ->
                            Text(
                                text = targetExpr,
                                color = GlassTheme.TextSecondary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Large Value Display (E.g. =12,454)
                        AnimatedContent(
                            targetState = largeDisplay,
                            transitionSpec = {
                                fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                            },
                            label = "LargeDisplayAnimation"
                        ) { targetDisplay ->
                            // Format evaluated results with leading '=' matching reference image
                            val isResult = targetDisplay != "0" && expressionDisplay.isEmpty() && !targetDisplay.startsWith("-") && targetDisplay != "Error"
                            val displayText = if (isResult) "=$targetDisplay" else targetDisplay
                            
                            Text(
                                text = displayText,
                                style = GlassTypography.displayLarge.copy(
                                    fontSize = if (displayText.length > 9) 42.sp else 62.sp
                                ),
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Keyboard grid (Uniform squircles, 4x5 layout)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val rows = listOf(
                        // Row 1
                        listOf("AC" to GlassButtonType.FUNCTION, "±" to GlassButtonType.FUNCTION, "%" to GlassButtonType.FUNCTION, "÷" to GlassButtonType.OPERATOR),
                        // Row 2
                        listOf("7" to GlassButtonType.NUMBER, "8" to GlassButtonType.NUMBER, "9" to GlassButtonType.NUMBER, "×" to GlassButtonType.OPERATOR),
                        // Row 3
                        listOf("4" to GlassButtonType.NUMBER, "5" to GlassButtonType.NUMBER, "6" to GlassButtonType.NUMBER, "−" to GlassButtonType.OPERATOR),
                        // Row 4
                        listOf("1" to GlassButtonType.NUMBER, "2" to GlassButtonType.NUMBER, "3" to GlassButtonType.NUMBER, "+" to GlassButtonType.OPERATOR),
                        // Row 5
                        listOf("sc" to GlassButtonType.FUNCTION, "0" to GlassButtonType.NUMBER, "." to GlassButtonType.NUMBER, "=" to GlassButtonType.EQUALS)
                    )

                    rows.forEach { rowKeys ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowKeys.forEach { (key, buttonType) ->
                                val isClear = key == "AC" || key == "C"
                                val label = if (isClear) {
                                    if (largeDisplay != "0" && largeDisplay.isNotEmpty()) "C" else "AC"
                                } else {
                                    key
                                }

                                GlassButton(
                                    text = label,
                                    onClick = {
                                        when {
                                            label == "AC" || label == "C" -> viewModel.onClear()
                                            label == "±" -> viewModel.onToggleSign()
                                            label == "%" -> viewModel.onPercent()
                                            label == "=" -> viewModel.onEqual()
                                            label == "." -> viewModel.onDecimal()
                                            label == "sc" -> { /* Open scientific/history drawer */ showHistory = !showHistory }
                                            buttonType == GlassButtonType.OPERATOR -> viewModel.onOperator(label)
                                            else -> viewModel.onDigit(label)
                                        }
                                    },
                                    type = buttonType,
                                    isActiveOperator = activeOperator == label,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Floating Glass History Drawer Overlay
        AnimatedVisibility(
            visible = showHistory,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                                            viewModel.onDigit(res.replace(",", "").replace("=", ""))
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
