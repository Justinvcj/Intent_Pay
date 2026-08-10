package com.intentpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.intentpay.ui.theme.GlassBorder
import com.intentpay.ui.theme.GlassSurface
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = GlassSurface,
    borderColor: Color = GlassBorder,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.97f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 1f,
            stiffness = 400f
        ),
        label = "card_scale"
    )

    var finalModifier = modifier
        .scale(scale)
        .clip(shape)
        .background(backgroundColor)
        .border(0.5.dp, borderColor, shape)
        
    if (onClick != null) {
        finalModifier = finalModifier.clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material.ripple.rememberRipple(color = Color.White.copy(alpha = 0.2f)),
            onClick = onClick
        )
    }

    Box(
        modifier = finalModifier
    ) {
        content()
    }
}
