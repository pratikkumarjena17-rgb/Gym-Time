package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CelebrationEvent
import com.example.data.model.CelebrationType
import com.example.ui.theme.GymCyanSecondary
import com.example.ui.theme.GymGreenTertiary
import com.example.ui.theme.GymOrangePrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class ParticleShape { RECTANGLE, CIRCLE, STAR, DIAMOND }

private class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Color,
    val shape: ParticleShape,
    var rotation: Float = 0f,
    val rotationSpeed: Float = (Random.nextFloat() - 0.5f) * 12f,
    val wobbleSpeed: Float = Random.nextFloat() * 4f + 2f,
    var alpha: Float = 1f
) {
    fun update(gravity: Float, drag: Float, deltaMs: Float) {
        vy += gravity * (deltaMs / 16.6f)
        vx *= drag
        x += vx * (deltaMs / 16.6f)
        y += vy * (deltaMs / 16.6f)
        rotation += rotationSpeed
        if (y > 400f) {
            alpha = (alpha - 0.008f * (deltaMs / 16.6f)).coerceAtLeast(0f)
        }
    }
}

@Composable
fun CelebrationOverlay(
    event: CelebrationEvent,
    onDismiss: () -> Unit,
    onShareToFeed: (CelebrationEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Trigger haptic on appearance
    LaunchedEffect(event) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Spring entrance scale for badge
    val badgeScale = remember { Animatable(0.2f) }
    LaunchedEffect(event) {
        badgeScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.58f,
                stiffness = 180f
            )
        )
    }

    // Card entrance
    var showCardContent by remember { mutableStateOf(false) }
    LaunchedEffect(event) {
        kotlinx.coroutines.delay(120)
        showCardContent = true
    }

    // Infinite rotation for radiant rays behind trophy
    val infiniteTransition = rememberInfiniteTransition(label = "aura_transition")
    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val shockwaveScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shockwave_scale"
    )

    val shockwaveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shockwave_alpha"
    )

    // Particle Cannon Simulation
    val particles = remember(event) {
        val colors = listOf(
            GymOrangePrimary,
            GymCyanSecondary,
            GymGreenTertiary,
            Color(0xFFFFD700), // Gold
            Color(0xFFFF2E93), // Vibrant Pink
            Color(0xFFFFFFFF)  // Sparkle White
        )
        val shapes = ParticleShape.values()
        List(90) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 22f + 4f
            ConfettiParticle(
                x = 0f,
                y = 0f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 10f, // Upward bias
                size = Random.nextFloat() * 14f + 8f,
                color = colors[Random.nextInt(colors.size)],
                shape = shapes[Random.nextInt(shapes.size)]
            )
        }
    }

    var frameTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(event) {
        var lastTime = System.currentTimeMillis()
        while (true) {
            withFrameNanos {
                val now = System.currentTimeMillis()
                val delta = (now - lastTime).coerceIn(8L, 33L).toFloat()
                lastTime = now
                particles.forEach { p ->
                    p.update(gravity = 0.45f, drag = 0.985f, deltaMs = delta)
                }
                frameTick = now
            }
        }
    }

    // Modal Scrim + Content
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE60A0D12))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .testTag("celebration_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Confetti Canvas Layer across entire screen
        Canvas(modifier = Modifier.fillMaxSize()) {
            val originX = size.width / 2f
            val originY = size.height * 0.36f

            // Shockwave Expanding Rings
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GymOrangePrimary.copy(alpha = shockwaveAlpha * 0.4f),
                        GymCyanSecondary.copy(alpha = shockwaveAlpha * 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(originX, originY),
                    radius = 160.dp.toPx() * shockwaveScale
                ),
                center = Offset(originX, originY),
                radius = 160.dp.toPx() * shockwaveScale
            )

            drawCircle(
                color = GymOrangePrimary.copy(alpha = shockwaveAlpha * 0.5f),
                radius = 120.dp.toPx() * shockwaveScale,
                center = Offset(originX, originY),
                style = Stroke(width = 3.dp.toPx())
            )

            // Confetti particles
            particles.forEach { p ->
                if (p.alpha > 0.01f) {
                    val px = originX + p.x
                    val py = originY + p.y

                    when (p.shape) {
                        ParticleShape.RECTANGLE -> {
                            val w = p.size
                            val h = p.size * 0.5f
                            val rad = p.rotation * (PI.toFloat() / 180f)
                            val cosR = cos(rad)
                            val sinR = sin(rad)
                            // Draw rotated ribbon
                            drawRect(
                                color = p.color.copy(alpha = p.alpha),
                                topLeft = Offset(px - w / 2, py - h / 2),
                                size = Size(w * cosR.coerceAtLeast(0.15f), h)
                            )
                        }
                        ParticleShape.CIRCLE -> {
                            drawCircle(
                                color = p.color.copy(alpha = p.alpha),
                                radius = p.size / 2f,
                                center = Offset(px, py)
                            )
                        }
                        ParticleShape.STAR -> {
                            drawStar(
                                center = Offset(px, py),
                                size = p.size * 1.2f,
                                color = p.color.copy(alpha = p.alpha),
                                rotation = p.rotation
                            )
                        }
                        ParticleShape.DIAMOND -> {
                            drawDiamond(
                                center = Offset(px, py),
                                size = p.size,
                                color = p.color.copy(alpha = p.alpha),
                                rotation = p.rotation
                            )
                        }
                    }
                }
            }
        }

        // Celebration Dialog Container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* prevent dismiss when tapping inside card */ }
                )
        ) {
            // Trophy / Badge with Radiant Aura Behind it
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(190.dp)
                    .scale(badgeScale.value)
            ) {
                // Spinning Starburst Rays Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(auraRotation)
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f
                    val rayCount = 12
                    for (i in 0 until rayCount) {
                        val angle1 = (i * (360f / rayCount)) * (PI.toFloat() / 180f)
                        val angle2 = ((i * (360f / rayCount)) + 12f) * (PI.toFloat() / 180f)

                        val path = Path().apply {
                            moveTo(center.x, center.y)
                            lineTo(center.x + radius * cos(angle1), center.y + radius * sin(angle1))
                            lineTo(center.x + radius * cos(angle2), center.y + radius * sin(angle2))
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.45f),
                                    GymOrangePrimary.copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = radius
                            )
                        )
                    }
                }

                // Glowing circular backdrop
                Box(
                    modifier = Modifier
                        .size(114.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700),
                                    GymOrangePrimary,
                                    Color(0xFFB34700)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner ring accent
                    Box(
                        modifier = Modifier
                            .size(102.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF141720)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Big Badge Emoji / Icon
                        Text(
                            text = event.badgeIcon.ifBlank { "🏆" },
                            fontSize = 48.sp
                        )
                    }
                }

                // Sparkle Badge Corner Accent
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset((-18).dp, 16.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(GymGreenTertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Celebration Card with Shimmering Gradient Border
            AnimatedVisibility(
                visible = showCardContent,
                enter = fadeIn(tween(300)) + slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 220f)
                ),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                    border = BorderStroke(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GymOrangePrimary,
                                Color(0xFFFFD700),
                                GymCyanSecondary,
                                GymGreenTertiary
                            )
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Category Tag Chip
                        val tagTitle = when (event.type) {
                            CelebrationType.MONTHLY_CHALLENGE -> "MONTHLY CHALLENGE CONQUERED"
                            CelebrationType.FITNESS_MILESTONE -> "NEW FITNESS MILESTONE UNLOCKED"
                            CelebrationType.PERSONAL_RECORD -> "NEW PERSONAL RECORD SMASHED"
                        }
                        val tagColor = when (event.type) {
                            CelebrationType.MONTHLY_CHALLENGE -> Color(0xFFFFD700)
                            CelebrationType.FITNESS_MILESTONE -> GymCyanSecondary
                            CelebrationType.PERSONAL_RECORD -> GymOrangePrimary
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = tagColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, tagColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(tagColor)
                                )
                                Text(
                                    text = tagTitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = tagColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Title
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black
                            ),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )

                        if (event.subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.subtitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                textAlign = TextAlign.Center,
                                color = GymOrangePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stat Highlight Banner
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF222634),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = GymOrangePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = event.statHighlight,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GymGreenTertiary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "+${event.xpEarned} XP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = GymGreenTertiary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Detailed celebration description
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFB0B7C3),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons: Share to community & Continue
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onShareToFeed(event)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GymOrangePrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GymOrangePrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_share_celebration")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GymOrangePrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("btn_claim_reward")
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Claim & Continue", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom canvas helper for 4-point star
private fun DrawScope.drawStar(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float
) {
    val r = size / 2f
    val innerR = r * 0.35f
    val rad = rotation * (PI.toFloat() / 180f)
    val path = Path()

    for (i in 0 until 8) {
        val currentR = if (i % 2 == 0) r else innerR
        val angle = rad + (i * PI.toFloat() / 4f)
        val x = center.x + currentR * cos(angle)
        val y = center.y + currentR * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

// Custom canvas helper for diamond
private fun DrawScope.drawDiamond(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float
) {
    val r = size / 2f
    val rad = rotation * (PI.toFloat() / 180f)
    val path = Path()
    for (i in 0 until 4) {
        val angle = rad + (i * PI.toFloat() / 2f)
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}
