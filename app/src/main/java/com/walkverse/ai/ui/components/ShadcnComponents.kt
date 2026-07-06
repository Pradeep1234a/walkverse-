package com.walkverse.ai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walkverse.ai.ui.theme.LocalShadcnColors

@Composable
fun ShadcnCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    gradient: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalShadcnColors.current
    val backgroundModifier = if (gradient) {
        Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(colors.cardGradStart, colors.cardGradEnd)
            )
        )
    } else {
        Modifier.background(colors.surface)
    }

    val baseModifier = modifier
        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
        .then(backgroundModifier)
        .padding(16.dp)

    if (onClick != null) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .then(baseModifier),
            content = content
        )
    } else {
        Column(
            modifier = baseModifier,
            content = content
        )
    }
}

@Composable
fun ShadcnButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outline: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val colors = LocalShadcnColors.current

    if (outline) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.textPrimary
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled).copy(
                brush = Brush.linearGradient(listOf(colors.border, colors.border))
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = modifier
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text, fontWeight = FontWeight.Medium)
            }
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = if (colors.primary == Color.White) Color.Black else Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = modifier
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ShadcnProgressRing(
    progress: Float,
    steps: Int,
    goal: Int,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 16.dp
) {
    val colors = LocalShadcnColors.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ringRadius = size.minDimension / 2 - strokeWidth.toPx() / 2
            
            // Draw Background Ring
            drawCircle(
                color = colors.border,
                radius = ringRadius,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Draw Active Progress Arc
            drawArc(
                color = colors.primary,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = String.format("%,d", steps),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Goal: ${String.format("%,d", goal)}",
                fontSize = 14.sp,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}% Done",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary
            )
        }
    }
}

@Composable
fun ShadcnProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    val colors = LocalShadcnColors.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(colors.border, RoundedCornerShape(height / 2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(colors.primary, RoundedCornerShape(height / 2))
        )
    }
}

@Composable
fun ShadcnStatCard(
    title: String,
    value: String,
    subText: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    ShadcnCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 14.sp, color = LocalShadcnColors.current.textMuted)
            if (icon != null) {
                icon()
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = LocalShadcnColors.current.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subText,
            fontSize = 11.sp,
            color = LocalShadcnColors.current.textMuted
        )
    }
}

@Composable
fun ShadcnBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    textColor: Color? = null
) {
    val colors = LocalShadcnColors.current
    val bg = containerColor ?: colors.border
    val tx = textColor ?: colors.textPrimary

    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = tx
        )
    }
}

@Composable
fun ShadcnBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val colors = LocalShadcnColors.current
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data available", color = colors.textMuted)
        }
        return;
    }

    val maxVal = (data.maxOfOrNull { it.second } ?: 1f).coerceAtLeast(1f)

    Canvas(modifier = modifier.padding(vertical = 12.dp)) {
        val width = size.width
        val height = size.height
        
        val bottomMargin = 40f
        val leftMargin = 20f
        
        val chartHeight = height - bottomMargin
        val chartWidth = width - leftMargin
        val barCount = data.size
        
        val spacing = 24f
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (chartWidth - totalSpacing) / barCount

        data.forEachIndexed { index, pair ->
            val value = pair.second
            val label = pair.first
            val fraction = value / maxVal
            val barHeight = chartHeight * fraction
            
            val x = leftMargin + index * (barWidth + spacing)
            val y = chartHeight - barHeight

            // Draw Bar
            drawRoundRect(
                color = colors.primary,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Draw values above bars
            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.0f", value),
                x + barWidth / 2,
                (y - 12f).coerceAtLeast(12f),
                android.graphics.Paint().apply {
                    color = colors.textPrimary.hashCode()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
            )

            // Draw labels
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x + barWidth / 2,
                height - 10f,
                android.graphics.Paint().apply {
                    color = colors.textMuted.hashCode()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun ShadcnLineChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val colors = LocalShadcnColors.current
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data available", color = colors.textMuted)
        }
        return
    }

    val maxVal = (data.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Canvas(modifier = modifier.padding(vertical = 12.dp)) {
        val width = size.width
        val height = size.height
        
        val bottomMargin = 40f
        val leftMargin = 30f
        
        val chartHeight = height - bottomMargin
        val chartWidth = width - leftMargin
        val pointCount = data.size
        
        val stepX = if (pointCount > 1) chartWidth / (pointCount - 1) else chartWidth

        val path = Path()
        val points = mutableListOf<Offset>()

        data.forEachIndexed { index, value ->
            val fraction = value / maxVal
            val x = leftMargin + index * stepX
            val y = chartHeight - (chartHeight * fraction)
            
            val point = Offset(x, y)
            points.add(point)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw line
        drawPath(
            path = path,
            color = colors.primary,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // Draw points and labels
        points.forEachIndexed { index, offset ->
            drawCircle(
                color = colors.primary,
                radius = 8f,
                center = offset
            )
            
            drawCircle(
                color = colors.background,
                radius = 4f,
                center = offset
            )

            // Draw Value
            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.0f", data[index]),
                offset.x,
                offset.y - 15f,
                android.graphics.Paint().apply {
                    color = colors.textPrimary.hashCode()
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )

            // Draw X-axis label
            if (index < labels.size) {
                drawContext.canvas.nativeCanvas.drawText(
                    labels[index],
                    offset.x,
                    height - 10f,
                    android.graphics.Paint().apply {
                        color = colors.textMuted.hashCode()
                        textSize = 22f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
