package com.vayu.agenticbrowser.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vayu.agenticbrowser.R
import com.vayu.agenticbrowser.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // Animation states
    var logoAlpha by remember { mutableFloatStateOf(0f) }
    var titleAlpha by remember { mutableFloatStateOf(0f) }
    var subtitleAlpha by remember { mutableFloatStateOf(0f) }
    var taglineAlpha by remember { mutableFloatStateOf(0f) }

    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "splashGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Staggered entrance animations
    LaunchedEffect(Unit) {
        delay(200)
        logoAlpha = 1f
        delay(400)
        titleAlpha = 1f
        delay(300)
        subtitleAlpha = 1f
        delay(300)
        taglineAlpha = 1f
        delay(1500)
        onSplashComplete()
    }

    // Animated values
    val logoAnim by animateFloatAsState(
        targetValue = logoAlpha,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "logoAnim"
    )
    val titleAnim by animateFloatAsState(
        targetValue = titleAlpha,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "titleAnim"
    )
    val subtitleAnim by animateFloatAsState(
        targetValue = subtitleAlpha,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "subtitleAnim"
    )
    val taglineAnim by animateFloatAsState(
        targetValue = taglineAlpha,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "taglineAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        VayuNavy,
                        VayuNavyMedium,
                        VayuNavyLight,
                        VayuNavy
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glow effect behind icon
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(glowScale)
                        .alpha(glowAlpha)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    VayuCyan.copy(alpha = 0.3f),
                                    VayuCyan.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )

                // App icon
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "VAYU",
                    modifier = Modifier
                        .size(120.dp)
                        .alpha(logoAnim)
                        .scale(0.85f + logoAnim * 0.15f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // VAYU title
            Text(
                text = "VAYU",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 56.sp,
                    letterSpacing = 8.sp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(VayuCyan, VayuCyanLight, VayuTeal)
                    )
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAnim)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "AGENTIC BROWSER",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 6.sp,
                    fontSize = 14.sp
                ),
                color = VayuOnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAnim)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Tagline
            Text(
                text = "Let the wind carry your intent",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                color = VayuCyan.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAnim)
            )
        }

        // Bottom version info
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Loading indicator dots
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotAlpha"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val delayedAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(delayedAlpha)
                            .background(
                                VayuCyan,
                                androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "v1.0.0-beta",
                style = MaterialTheme.typography.labelSmall,
                color = VayuOnSurfaceDim
            )
        }
    }
}
