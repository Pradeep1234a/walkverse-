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
import androidx.compose.ui.draw.shadow
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // 1. Live Ambient Glow Background
        MeshGradientBackground(themeName = currentTheme)

        // Main Layout Panel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 2. Top Bar: Toggler for history drawer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Floating Circular Glass Menu Button (Matching image reference)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showHistory = !showHistory }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open History Drawer",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 3. Right-Aligned Display panel with swipe gestures to erase
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            // Erase one digit if horizontal swipe exceeds threshold
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
                    // Small Expression display (e.g. 1,234+5,678)
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
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Large Input display (e.g. 6,912)
                    AnimatedContent(
                        targetState = largeDisplay,
                        transitionSpec = {
                            fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                        },
                        label = "InputDisplayAnimation"
                    ) { targetDisplay ->
                        Text(
                            text = targetDisplay,
                            style = GlassTypography.displayLarge.copy(
                                fontSize = if (targetDisplay.length > 9) 44.sp else 68.sp
                            ),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 4. Apple iPhone Grid Keyboard layout with liquid glass styles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: AC, +/−, %, ÷
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // C / AC label toggler
                    val showClearOnly = largeDisplay != "0" && largeDisplay.isNotEmpty()
                    val clearLabel = if (showClearOnly) "C" else "AC"
                    
                    GlassButton(
                        text = clearLabel,
                        onClick = { viewModel.onClear() },
                        type = GlassButtonType.FUNCTION,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "±",
                        onClick = { viewModel.onToggleSign() },
                        type = GlassButtonType.FUNCTION,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "%",
                        onClick = { viewModel.onPercent() },
                        type = GlassButtonType.FUNCTION,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "÷",
                        onClick = { viewModel.onOperator("÷") },
                        type = GlassButtonType.OPERATOR,
                        isActiveOperator = activeOperator == "÷",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassButton(text = "7", onClick = { viewModel.onDigit("7") }, modifier = Modifier.weight(1f))
                    GlassButton(text = "8", onClick = { viewModel.onDigit("8") }, modifier = Modifier.weight(1f))
                    GlassButton(text = "9", onClick = { viewModel.onDigit("9") }, modifier = Modifier.weight(1f))
                    GlassButton(
                        text = "×",
                        onClick = { viewModel.onOperator("×") },
                        type = GlassButtonType.OPERATOR,
                        isActiveOperator = activeOperator == "×",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: 4, 5, 6, −
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassButton(text = "4", onClick = { viewModel.onDigit("4") }, modifier = Modifier.weight(1f))
                    GlassButton(text = "5", onClick = { viewModel.onDigit("5") }, modifier = Modifier.weight(1f))
                    GlassButton(text = "6", onClick = { viewModel.onDigit("6") }, modifier = Modifier.weight(1f))
                    GlassButton(
                        text = "−",
                        onClick = { viewModel.onOperator("−") },
                        type = GlassButtonType.OPERATOR,
                        isActiveOperator = activeOperator == "−",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 4: 1, 2, 3, +
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassButton(text = "1", onClick = { viewModel.onDigit("1") }, modifier = Modifier.weight(1f))
                    GlassButton(text = "2", onClick = { viewModel.onDigit("2") }, modifier = Modifier.weight(1f))
                    GlassButton(text = "3", onClick = { viewModel.onDigit("3") }, modifier = Modifier.weight(1f))
                    GlassButton(
                        text = "+",
                        onClick = { viewModel.onOperator("+") },
                        type = GlassButtonType.OPERATOR,
                        isActiveOperator = activeOperator == "+",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 5: 0 (double span), ., =
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassButton(
                        text = "0",
                        onClick = { viewModel.onDigit("0") },
                        isWide = true,
                        modifier = Modifier.weight(2f)
                    )
                    GlassButton(
                        text = ".",
                        onClick = { viewModel.onDecimal() },
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "=",
                        onClick = { viewModel.onEqual() },
                        type = GlassButtonType.EQUALS,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. Floating Glass History Drawer Overlay
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
                                            // Split expression back to enter it
                                            viewModel.onDigit(res.replace(",", ""))
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
